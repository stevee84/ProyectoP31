package dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public record ReservaDTO(
        int id,
        UsuarioDTO autor,
        List<RecursoDTO> recursos,
        String descripcionActividad,
        LocalDateTime inicio,
        LocalDateTime fin,
        EstadoReserva estado
) implements Serializable {

    public ReservaDTO {
        recursos = List.copyOf(recursos);
    }
}
