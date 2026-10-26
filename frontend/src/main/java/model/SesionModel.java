package model;

import contrato.IServicioAplicacion;
import dto.ResultadoSesionDTO;
import dto.UsuarioDTO;

/**
 * Fachada de sesión del Frontend. Recuerda el usuario autenticado solo para la
 * presentación (título, pestañas visibles); los permisos reales los valida el Backend.
 */
public class SesionModel {

    private final IServicioAplicacion servicio;
    private final Conexion conexion;
    private volatile UsuarioDTO usuarioActual;

    /** Permite reabrir el socket antes de iniciar sesión si el servidor se reinició. */
    @FunctionalInterface
    public interface Conexion {
        void asegurar();
    }

    public SesionModel(IServicioAplicacion servicio, Conexion conexion) {
        this.servicio = servicio;
        this.conexion = conexion != null ? conexion : () -> { };
    }

    public ResultadoSesionDTO iniciarSesion(String id, String clave) {
        conexion.asegurar();
        ResultadoSesionDTO resultado = servicio.iniciarSesion(id, clave);
        usuarioActual = resultado.usuario();
        return resultado;
    }

    public void cerrarSesion() {
        try {
            servicio.cerrarSesion();
        } finally {
            usuarioActual = null;
        }
    }

    public void cambiarClave(String nueva) {
        servicio.cambiarClave(nueva);
    }

    public UsuarioDTO getUsuarioActual() {
        return usuarioActual;
    }
}
