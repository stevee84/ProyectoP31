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
    /** Primer ingreso: hasta cambiar la clave solo se permiten cambiarClave y cerrarSesion. */
    private volatile boolean cambioClavePendiente;

    public void iniciar(UsuarioDTO usuario) {
        iniciar(usuario, false);
    }

    public void iniciar(UsuarioDTO usuario, boolean requiereCambioClave) {
        this.usuario = usuario;
        this.cambioClavePendiente = requiereCambioClave;
    }

    public void cerrar() {
        usuario = null;
        cambioClavePendiente = false;
    }

    /** El servidor confirmó el cambio de clave: se levanta la restricción del primer ingreso. */
    public void claveCambiada() {
        cambioClavePendiente = false;
    }

    public boolean requiereCambioClave() {
        return usuario != null && cambioClavePendiente;
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
