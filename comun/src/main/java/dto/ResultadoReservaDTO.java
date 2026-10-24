package dto;

import java.io.Serializable;
import java.util.List;

/**
 * Éxito con la reserva confirmada, o fracaso con las categorías sin recurso disponible.
 */
public record ResultadoReservaDTO(
        boolean exito,
        ReservaDTO reserva,
        List<CategoriaDTO> categoriasNoDisponibles
) implements Serializable {

    public ResultadoReservaDTO {
        categoriasNoDisponibles = List.copyOf(categoriasNoDisponibles);
    }

    public static ResultadoReservaDTO exito(ReservaDTO reserva) {
        return new ResultadoReservaDTO(true, reserva, List.of());
    }

    public static ResultadoReservaDTO fracaso(List<CategoriaDTO> categoriasNoDisponibles) {
        return new ResultadoReservaDTO(false, null, categoriasNoDisponibles);
    }
}
