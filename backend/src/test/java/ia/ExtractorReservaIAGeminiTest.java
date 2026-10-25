package ia;

import dto.CategoriaDTO;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.time.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ExtractorReservaIAGeminiTest {
    private final List<CategoriaDTO> categorias = List.of(new CategoriaDTO("CAT-000001", "Sala"));
    private final Clock reloj = Clock.fixed(Instant.parse("2030-03-11T12:00:00Z"), ZoneOffset.UTC);

    private ExtractorReservaIAGemini extractor(ExtractorReservaIAGemini.Transporte transporte) {
        return new ExtractorReservaIAGemini("clave-temporal-prueba", "modelo-prueba", Duration.ofSeconds(8), reloj, transporte);
    }

    private static String respuesta(String json) {
        return new JSONObject().put("candidates", new JSONArray().put(new JSONObject().put("finishReason", "STOP")
                .put("content", new JSONObject().put("parts", new JSONArray().put(new JSONObject().put("text", json)))))).toString();
    }

    @Test
    void sinClaveNoHaceNingunaLlamada() {
        var extractor = new ExtractorReservaIAGemini(null, "modelo", Duration.ofSeconds(8), reloj,
                r -> { fail("No debe consumir la API"); return null; });
        assertFalse(extractor.extraer("sala mañana", categorias).exito());
    }

    @Test
    void claveVaEnCabeceraNoEnUrlYDatosPuedenEstarIncompletos() {
        var resultado = extractor(r -> {
            assertFalse(r.uri().toString().contains("clave-temporal-prueba"));
            assertEquals("clave-temporal-prueba", r.headers().firstValue("x-goog-api-key").orElseThrow());
            assertEquals(Duration.ofSeconds(8), r.timeout().orElseThrow());
            assertEquals("POST", r.method());
            return new ExtractorReservaIAGemini.Respuesta(200, respuesta("""
                    {"descripcionActividad":null,"fecha":"2030-03-12","horaInicio":null,"horaFin":null,
                     "idsCategorias":["CAT-000001","CAT-000001"]}
                    """));
        }).extraer("dos salas mañana", categorias);
        assertTrue(resultado.exito());
        assertNull(resultado.descripcionActividad());
        assertNull(resultado.horaInicio());
        assertEquals(LocalDate.of(2030, 3, 12), resultado.fecha());
        assertEquals(List.of("CAT-000001", "CAT-000001"), resultado.idsCategorias());
    }

    @Test
    void fallaExternaNoExponeCuerpoNiExcepcion() {
        var externo = extractor(r -> new ExtractorReservaIAGemini.Respuesta(403, "clave-temporal-prueba SQL secreto"))
                .extraer("una sala", categorias);
        assertFalse(externo.exito());
        assertFalse(externo.mensajeError().contains("secreto"));
        assertFalse(externo.mensajeError().contains("clave-temporal-prueba"));
        var red = extractor(r -> { throw new IOException("secreto privado"); }).extraer("una sala", categorias);
        assertFalse(red.exito());
        assertFalse(red.mensajeError().contains("secreto"));
    }

    @Test
    void respuestaInvalidaOConCategoriaInventadaEsFalloControlado() {
        for (String cuerpo : List.of("no es json", respuesta("{\"idsCategorias\":[\"INVENTADA\"]}"),
                respuesta("{\"fecha\":\"2030-99-99\",\"idsCategorias\":[]}"), "{\"candidates\":[]}")) {
            assertFalse(extractor(r -> new ExtractorReservaIAGemini.Respuesta(200, cuerpo)).extraer("una sala", categorias).exito());
        }
    }

    @Test
    void interrupcionConservaBanderaDelHilo() {
        try {
            assertFalse(extractor(r -> { throw new InterruptedException(); }).extraer("una sala", categorias).exito());
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }
}
