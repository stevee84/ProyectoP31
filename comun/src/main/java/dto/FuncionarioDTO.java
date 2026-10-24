package dto;

import java.io.Serializable;

public record FuncionarioDTO(String id, String nombre, String telefono) implements Serializable {
}
