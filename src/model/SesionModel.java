package model;

import service.SesionService;

/**
 * Fachada de la capa Model para sesiones. El Controller solo conoce esta
 * clase, nunca importa "service" directamente.
 */
public class SesionModel {

    private final SesionService servicio;

    public SesionModel(SesionService servicio) {
        this.servicio = servicio;
    }

    public ResultadoSesion iniciarSesion(String id, String pass) {
        return servicio.iniciarSesion(id, pass);
    }

    public void cerrarSesion() {
        servicio.cerrarSesion();
    }

    public void cambiarContrasena(String nueva) {
        servicio.cambiarContrasena(nueva);
    }

    public Empleado getSesionActual() {
        return servicio.getSesionActual();
    }
}
