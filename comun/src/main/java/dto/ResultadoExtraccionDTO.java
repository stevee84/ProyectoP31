package dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Datos que la IA extrajo de una frase. Los campos que no reconoció quedan en null
 * para que el funcionario los complete en el formulario.
 */
public record ResultadoExtraccionDTO(
        boolean exito,
        String descripcionActividad,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        List<String> idsCategorias,
        String mensajeError
) implements Serializable {

    public ResultadoExtraccionDTO {
        idsCategorias = idsCategorias == null ? List.of() : List.copyOf(idsCategorias);
    }

    public static ResultadoExtraccionDTO fallo(String mensajeError) {
        return new ResultadoExtraccionDTO(false, null, null, null, null, List.of(), mensajeError);
    }
}
