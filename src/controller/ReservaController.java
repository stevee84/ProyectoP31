package controller;

import model.CategoriaRecurso;
import model.ExtractorReservaIA;
import model.Reservacion;
import model.ResultadoExtraccionIA;
import model.ResultadoReserva;

import java.time.LocalDateTime;
import java.util.List;

public class ReservaController {

    private final ControladorReservaciones controlador;
    private final ExtractorReservaIA extractorIA;

    public ReservaController(ControladorReservaciones controlador, ExtractorReservaIA extractorIA) {
        this.controlador = controlador;
        this.extractorIA = extractorIA;
    }

    public List<Reservacion> misReservas() {
        return controlador.listarReservacionesSesionActual();
    }

    public List<CategoriaRecurso> listarCategorias() {
        return controlador.listarCategorias();
    }

    public ResultadoReserva crearReservacion(List<String> idsCategorias, String descripcionActividad,
                                             LocalDateTime inicio, LocalDateTime fin) {
        return controlador.crearReservacion(idsCategorias, descripcionActividad, inicio, fin);
    }

    public void cancelarReservacion(int id) {
        controlador.cancelarReservacion(id);
    }

    public ResultadoExtraccionIA extraerDatosDesdeFrase(String frase) {
        return extractorIA.extraer(frase, listarCategorias());
    }
}
