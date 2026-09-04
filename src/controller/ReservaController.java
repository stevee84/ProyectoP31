package controller;

import model.Reservacion;
import model.CategoriaRecurso;
import model.ResultadoReserva;

import java.time.LocalDateTime;
import java.util.List;

public class ReservaController {

    private final ControladorReservaciones controlador;

    public ReservaController(ControladorReservaciones controlador) {
        this.controlador = controlador;
    }

    public List<Reservacion> misReservas() {
        return controlador.listarReservacionesSesionActual();
    }

    public List<CategoriaRecurso> listarCategorias() {
        return controlador.listarCategorias();
    }

    public ResultadoReserva crearReservacion(List<String> idsCategorias, String descripcionActividad, LocalDateTime inicio, LocalDateTime fin) {
        return controlador.crearReservacion(idsCategorias, descripcionActividad, inicio, fin);
    }

    public void cancelarReservacion(int id) {
        controlador.cancelarReservacion(id);
    }
}
