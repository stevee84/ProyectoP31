package dto;

import java.io.Serializable;

/**
 * Usuario autenticado tal como viaja por la red: nunca incluye la clave.
 */
public record UsuarioDTO(String id, String nombre, Rol rol) implements Serializable {

    public boolean esAdministrador() {
        return rol == Rol.ADMINISTRADOR;
    }
}
