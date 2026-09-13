package model;

import service.CalendarizacionService;

import java.time.LocalDate;
import java.util.List;

/**
 * Fachada de la capa Model para calendarizacion. El Controller solo conoce
 * esta clase, nunca importa "service" directamente.
 */
public class CalendarizacionModel {

    private final CalendarizacionService servicio;

    public CalendarizacionModel(CalendarizacionService servicio) {
        this.servicio = servicio;
    }

    public List<CategoriaRecurso> listarCategorias() {
        return servicio.listarCategorias();
    }

    public List<Recurso> listarRecursosPorCategoria(String idCategoria) {
        return servicio.listarRecursosPorCategoria(idCategoria);
    }

    public List<Reservacion> listarReservacionesEnFecha(LocalDate fecha) {
        return servicio.listarReservacionesEnFecha(fecha);
    }

    public String obtenerInformacionCelda(Recurso recurso, LocalDate fecha, int hora) {
        return servicio.obtenerInformacionCelda(recurso, fecha, hora);
    }
}
