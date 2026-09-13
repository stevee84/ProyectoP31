package model;

import service.ReservaService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Fachada de la capa Model para reservas. El Controller solo conoce esta
 * clase, nunca importa "service" directamente.
 */
public class ReservaModel {

    private final ReservaService servicio;

    public ReservaModel(ReservaService servicio) {
        this.servicio = servicio;
    }

    public ResultadoReserva crearReservacion(List<String> idsCategorias, String descripcionActividad,
                                              LocalDateTime inicio, LocalDateTime fin) {
        return servicio.crearReservacion(idsCategorias, descripcionActividad, inicio, fin);
    }

    public void cancelarReservacion(int id) {
        servicio.cancelarReservacion(id);
    }

    public List<Reservacion> listarReservacionesSesionActual() {
        return servicio.listarReservacionesSesionActual();
    }

    public ResultadoExtraccionIA extraerDatosDesdeFrase(String frase) {
        return servicio.extraerDatosDesdeFrase(frase);
    }
}
