package model;

import contrato.IServicioAplicacion;
import dto.CategoriaDTO;
import dto.RecursoDTO;
import dto.ReservaDTO;
import dto.ResultadoExtraccionDTO;
import dto.ResultadoReservaDTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Fachada de reservas. Además de delegar, convierte los DTO en filas para la tabla
 * "Mis reservas"; las reglas (solapes, permisos, horario) las aplica el Backend.
 */
public class ReservaModel {

    public static final String[] COLUMNAS = {"Id", "Actividad", "Fecha", "Horario", "Recursos", "Estado"};
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final IServicioAplicacion servicio;

    public ReservaModel(IServicioAplicacion servicio) {
        this.servicio = servicio;
    }

    public ResultadoReservaDTO crear(List<String> idsCategorias, String actividad,
                                     LocalDateTime inicio, LocalDateTime fin) {
        return servicio.crearReservacion(idsCategorias, actividad, inicio, fin);
    }

    public void cancelar(int id) {
        servicio.cancelarReservacion(id);
    }

    public List<ReservaDTO> listarMias() {
        return servicio.listarReservacionesSesion();
    }

    public ResultadoExtraccionDTO extraerConIA(String frase) {
        return servicio.extraerDatosDesdeFrase(frase);
    }

    // ── Conversión para la vista ─────────────────────────────────────

    public static TablaDatos aTabla(List<ReservaDTO> reservas) {
        Object[][] filas = reservas.stream().map(r -> new Object[]{
                r.id(),
                r.descripcionActividad(),
                r.inicio().toLocalDate().toString(),
                horario(r),
                nombresRecursos(r.recursos()),
                r.estado().name()
        }).toArray(Object[][]::new);
        return new TablaDatos(COLUMNAS, filas);
    }

    public static String horario(ReservaDTO r) {
        return HORA.format(r.inicio()) + " - " + HORA.format(r.fin());
    }

    public static String nombresRecursos(List<RecursoDTO> recursos) {
        return recursos.stream().map(RecursoDTO::descripcion).collect(Collectors.joining(", "));
    }

    /** Mensaje para el caso normal de falta de disponibilidad (no es una excepción). */
    public static String mensajeSinDisponibilidad(ResultadoReservaDTO resultado) {
        String categorias = resultado.categoriasNoDisponibles().stream()
                .map(CategoriaDTO::descripcion)
                .collect(Collectors.joining(", "));
        return categorias.isEmpty()
                ? "No hay recursos disponibles en ese horario."
                : "No hay disponibilidad en: " + categorias;
    }
}
