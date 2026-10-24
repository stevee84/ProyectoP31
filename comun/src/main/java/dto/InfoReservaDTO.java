package dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public record InfoReservaDTO(
        LocalDateTime inicio,
        LocalDateTime fin,
        String descripcionActividad,
        String nombreFuncionario,
        List<String> recursos
) implements Serializable {

    public InfoReservaDTO {
        recursos = List.copyOf(recursos);
    }
}
