package dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * Calendarización completa de una categoría en una fecha, en una sola respuesta.
 * celdas.get(fila).get(columna): fila = hora - HORA_INICIO, columna = índice en recursos.
 */
public record MatrizCalendarioDTO(
        LocalDate fecha,
        List<RecursoDTO> recursos,
        List<List<String>> celdas
) implements Serializable {

    public MatrizCalendarioDTO {
        recursos = List.copyOf(recursos);
        celdas = celdas.stream().map(List::copyOf).toList();
    }
}
