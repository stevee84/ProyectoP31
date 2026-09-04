package model;

import java.util.List;

/**
 * Implementación falsa de {@link ExtractorReservaIA} para pruebas, sin depender de
 * Internet ni de un servicio real. Es un "stub": se configura de antemano con el
 * {@link ResultadoExtraccionIA} que se quiere simular (extracción completa,
 * incompleta, o un fallo), y siempre lo devuelve sin importar la frase recibida.
 */
public class ExtractorReservaIAFalso implements ExtractorReservaIA {

    private final ResultadoExtraccionIA resultadoFijo;

    public ExtractorReservaIAFalso(ResultadoExtraccionIA resultadoFijo) {
        this.resultadoFijo = resultadoFijo;
    }

    @Override
    public ResultadoExtraccionIA extraer(String frase, List<CategoriaRecurso> categoriasDisponibles) {
        // Ignora la frase real a propósito: es un doble de prueba, no un extractor de verdad.
        return resultadoFijo;
    }
}
