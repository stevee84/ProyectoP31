package model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación real de ExtractorReservaIA usando la API de Gemini (Google AI Studio).
 * <p>
 * Requiere una clave de API válida (gratuita en https://aistudio.google.com/apikey).
 * La clave se recibe por constructor — NUNCA se escribe acá adentro ni se sube al
 * repositorio. Léela desde una variable de entorno (GEMINI_API_KEY) al construir esta
 * clase, no la escribas literal en ningún archivo versionado.
 */
public class ExtractorReservaIAGemini implements ExtractorReservaIA {

    // Gemini 2.5 Flash quedó bloqueado para claves de API nuevas (mensaje de error
    // de Google, sept. 2026): usar gemini-3.6-flash en su lugar. Si esto vuelve a
    // fallar más adelante, revisar https://ai.google.dev/gemini-api/docs/models
    // porque los nombres de modelo de Gemini cambian con frecuencia.
    private static final String MODELO = "gemini-3.1-flash-lite";
    private static final String URL_BASE = "https://generativelanguage.googleapis.com/v1beta/models/";

    private final String apiKey;
    private final HttpClient httpClient;

    public ExtractorReservaIAGemini(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public ResultadoExtraccionIA extraer(String frase, List<CategoriaRecurso> categoriasDisponibles) {
        String cuerpoSolicitud = construirCuerpoSolicitud(frase, categoriasDisponibles);
        HttpRequest solicitud = HttpRequest.newBuilder()
                .uri(URI.create(URL_BASE + MODELO + ":generateContent?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(cuerpoSolicitud))
                .build();

        // Hasta 3 intentos: el modelo puede estar temporalmente saturado (503),
        // y reintentar con una pequeña espera suele resolverlo solo.
        final int intentosMaximos = 3;
        for (int intento = 1; intento <= intentosMaximos; intento++) {
            try {
                HttpResponse<String> respuesta = httpClient.send(solicitud, HttpResponse.BodyHandlers.ofString());

                if (respuesta.statusCode() == 200) {
                    return parsearRespuesta(respuesta.body());
                }

                if (respuesta.statusCode() == 503 && intento < intentosMaximos) {
                    esperarAntesDeReintentar(intento);
                    continue;
                }

                return ResultadoExtraccionIA.fallo(
                        "El servicio de IA respondió con error " + respuesta.statusCode()
                                + ": " + respuesta.body());
            } catch (IOException | InterruptedException error) {
                return ResultadoExtraccionIA.fallo(
                        "No se pudo conectar con el servicio de IA: " + error.getMessage());
            }
        }

        return ResultadoExtraccionIA.fallo("El servicio de IA sigue saturado después de "
                + intentosMaximos + " intentos. Probá de nuevo en un rato.");
    }

    private void esperarAntesDeReintentar(int intento) throws InterruptedException {
        // Espera creciente: 1s, luego 2s, etc. (backoff simple).
        Thread.sleep(1000L * intento);
    }

    private String construirCuerpoSolicitud(String frase, List<CategoriaRecurso> categorias) {
        String listaCategorias = categorias.stream()
                .map(c -> "- id \"" + c.getId() + "\": " + c.getDescripcion())
                .collect(Collectors.joining("\n"));

        String prompt = """
                Extraé los datos de una reserva a partir de esta frase de un funcionario: "%s"
 
                Categorías de recursos disponibles en el sistema:
                %s
                
                Hoy es %s (formato aaaa-mm-dd). Usá esta fecha como referencia para
                resolver expresiones relativas como "mañana", "el próximo lunes", etc.
 
                Respondé ÚNICAMENTE con un objeto JSON (sin texto adicional, sin markdown),
                con exactamente esta forma:
                {
                  "descripcionActividad": string o null si no se identifica,
                  "fecha": "aaaa-mm-dd" o null,
                  "horaInicio": "HH:mm" o null,
                  "horaFin": "HH:mm" o null,
                  "idsCategorias": array con los ids exactos (de la lista de arriba) de las
                    categorías mencionadas en la frase
                }
                """.formatted(LocalDate.now(), frase, listaCategorias);

        JSONObject parte = new JSONObject().put("text", prompt);
        JSONObject contenido = new JSONObject().put("parts", new JSONArray().put(parte));

        // response_mime_type le pide a Gemini que devuelva SOLO JSON válido, sin
        // texto extra alrededor -> hace el parseo de abajo mucho más confiable.
        JSONObject generationConfig = new JSONObject().put("response_mime_type", "application/json");

        JSONObject cuerpo = new JSONObject();
        cuerpo.put("contents", new JSONArray().put(contenido));
        cuerpo.put("generationConfig", generationConfig);
        return cuerpo.toString();
    }

    private ResultadoExtraccionIA parsearRespuesta(String cuerpoRespuesta) {
        try {
            JSONObject respuesta = new JSONObject(cuerpoRespuesta);
            String textoJson = respuesta.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text");

            JSONObject datos = new JSONObject(textoJson);

            String descripcionActividad = datos.isNull("descripcionActividad")
                    ? null : datos.getString("descripcionActividad");
            LocalDate fecha = datos.isNull("fecha") ? null : LocalDate.parse(datos.getString("fecha"));
            LocalTime horaInicio = datos.isNull("horaInicio") ? null : LocalTime.parse(datos.getString("horaInicio"));
            LocalTime horaFin = datos.isNull("horaFin") ? null : LocalTime.parse(datos.getString("horaFin"));

            List<String> idsCategorias = new ArrayList<>();
            JSONArray arregloIds = datos.optJSONArray("idsCategorias");
            if (arregloIds != null) {
                for (int i = 0; i < arregloIds.length(); i++) {
                    idsCategorias.add(arregloIds.getString(i));
                }
            }

            return ResultadoExtraccionIA.exito(new DatosReservaExtraidos(
                    descripcionActividad, fecha, horaInicio, horaFin, idsCategorias));
        } catch (Exception error) {
            return ResultadoExtraccionIA.fallo("No se pudo interpretar la respuesta de la IA: " + error.getMessage());
        }
    }
}
