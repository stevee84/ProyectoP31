package dto;

import java.io.Serializable;

public record ResultadoSesionDTO(UsuarioDTO usuario, boolean requiereCambioClave) implements Serializable {
}
