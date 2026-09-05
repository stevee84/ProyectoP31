import model.CategoriaRecurso;
import model.ExtractorReservaIA;
import model.ExtractorReservaIAGemini;
import model.ResultadoExtraccionIA;

import java.util.List;

/**
 * Prueba aislada del extractor REAL de Gemini, por consola.
 * Requiere que la variable de entorno GEMINI_API_KEY esté configurada
 * (reiniciar IntelliJ después de correr "setx" en PowerShell).
 */
public class PruebaExtractorIAGemini {

    public static void main(String[] args) {
        String apiKey = System.getenv("GEMINI_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("ERROR: no se encontró la variable de entorno GEMINI_API_KEY.");
            System.out.println("Revisá que hayas corrido 'setx GEMINI_API_KEY \"...\"' y reiniciado IntelliJ.");
            return;
        }

        System.out.println("Clave encontrada (primeros 6 caracteres): "
                + apiKey.substring(0, Math.min(6, apiKey.length())) + "...");

        // Categorías de prueba, simulando lo que tendrías en el sistema real.
        List<CategoriaRecurso> categorias = List.of(
                new CategoriaRecurso("CAT-000001", "Sala para 10 personas"),
                new CategoriaRecurso("CAT-000002", "Laptop Windows"),
                new CategoriaRecurso("CAT-000003", "Sala de juntas")
        );

        ExtractorReservaIA extractor = new ExtractorReservaIAGemini(apiKey);

        String frase = "Necesito una reunion de equipo el 10 de septiembre de 2026 de 2pm a 3pm, "
                + "usando una laptop y la sala de juntas";

        System.out.println("Frase enviada: " + frase);
        System.out.println("Llamando a Gemini...");

        ResultadoExtraccionIA resultado = extractor.extraer(frase, categorias);

        if (resultado.esExito()) {
            System.out.println("EXITO. Datos extraidos: " + resultado.getDatos());
        } else {
            System.out.println("FALLO: " + resultado.getMensajeError());
        }
    }
}
