package servicio;

import dto.UsuarioDTO;

/**
 * Identidad de una conexión. Cada cliente tiene la suya, así una sesión
 * nunca se mezcla con la de otro usuario conectado al mismo servidor.
 * Solo el Despachador la escribe (iniciar/cerrar); los servicios únicamente la leen
 * para saber quién hace la operación y con qué rol.
 */
public class ContextoSesion {

    // volatile: lo lee también el hilo de otro cliente al difundir eventos.
    private volatile UsuarioDTO usuario;

    public void iniciar(UsuarioDTO usuario) {
        this.usuario = usuario;
    }

    public void cerrar() {
        usuario = null;
    }

    /** Usuario de la conexión, o null si todavía no inició sesión. */
    public UsuarioDTO getUsuario() {
        return usuario;
    }

    public boolean estaAutenticado() {
        return usuario != null;
    }

    public boolean esAdministrador() {
        UsuarioDTO actual = usuario;
        return actual != null && actual.esAdministrador();
    }
}
