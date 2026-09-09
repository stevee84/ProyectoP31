package controller;

import model.CategoriaRecurso;
import model.DatosReservaExtraidos;
import model.Recurso;
import model.Reservacion;
import model.ResultadoExtraccionIA;
import model.ResultadoReserva;
import service.CategoriaService;
import service.ReservaService;
import view.ReservaPanel;

import javax.swing.SwingWorker;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

public class ReservaController {

    private final ReservaPanel view;
    private final ReservaService service;
    private final CategoriaService categoriaService;

    public ReservaController(ReservaPanel view, ReservaService service, CategoriaService categoriaService) {
        this.view = view;
        this.service = service;
        this.categoriaService = categoriaService;

        view.setOnAplicar(this::aplicar);
        view.setOnCancelar(this::cancelar);
        view.setOnLimpiar(this::limpiar);
        view.setOnExtraerIA(this::extraerConIA);
        view.setOnVisible(this::cargarDatos);

        cargarDatos();
    }

    private void aplicar() {
        String actividad = view.getActividad();
        String fechaStr = view.getFecha();
        String horaInicioStr = view.getHoraInicio();
        String horaFinStr = view.getHoraFin();
        List<CategoriaRecurso> categorias = view.getCategoriasSeleccionadas();

        if (actividad.isBlank()) {
            view.mostrarError("La actividad es obligatoria.");
            return;
        }
        if (categorias.isEmpty()) {
            view.mostrarError("Seleccione al menos una categoria.");
            return;
        }

        LocalDate fecha;
        LocalTime horaInicio;
        LocalTime horaFin;
        try {
            fecha = LocalDate.parse(fechaStr);
        } catch (DateTimeParseException e) {
            view.mostrarError("Formato de fecha invalido. Use aaaa-mm-dd.");
            return;
        }
        try {
            horaInicio = LocalTime.parse(horaInicioStr);
        } catch (DateTimeParseException e) {
            view.mostrarError("Formato de hora inicio invalido. Use HH:mm.");
            return;
        }
        try {
            horaFin = LocalTime.parse(horaFinStr);
        } catch (DateTimeParseException e) {
            view.mostrarError("Formato de hora fin invalido. Use HH:mm.");
            return;
        }

        LocalDateTime inicio = fecha.atTime(horaInicio);
        LocalDateTime fin = fecha.atTime(horaFin);

        List<String> idsCategorias = categorias.stream()
                .map(CategoriaRecurso::getId)
                .collect(Collectors.toList());

        try {
            ResultadoReserva resultado = service.crearReservacion(idsCategorias, actividad, inicio, fin);
            if (resultado.esExito()) {
                view.mostrarMensaje("Reservacion creada exitosamente (ID: " + resultado.getReservacion().getId() + ").");
                limpiar();
            } else {
                String catsFaltantes = resultado.getCategoriasNoDisponibles().stream()
                        .map(CategoriaRecurso::getDescripcion)
                        .collect(Collectors.joining(", "));
                view.mostrarError("No hay disponibilidad en: " + catsFaltantes);
            }
        } catch (Exception e) {
            view.mostrarError(e.getMessage());
        }
    }

    private void cancelar() {
        int id = view.getReservaSeleccionadaId();
        if (id == -1) {
            view.mostrarError("Seleccione una reservacion para cancelar.");
            return;
        }
        if (!view.confirmarAccion("Desea cancelar la reservacion " + id + "?")) {
            return;
        }
        try {
            service.cancelarReservacion(id);
            view.mostrarMensaje("Reservacion cancelada.");
            cargarReservas();
        } catch (Exception e) {
            view.mostrarError(e.getMessage());
        }
    }

    private void extraerConIA() {
        String frase = view.getFrase();
        if (frase.isBlank()) {
            view.mostrarError("Escriba una frase para extraer datos.");
            return;
        }

        view.setEstadoBotonIA(false, "Procesando...");

        new SwingWorker<ResultadoExtraccionIA, Void>() {
            @Override
            protected ResultadoExtraccionIA doInBackground() {
                return service.extraerDatosDesdeFrase(frase);
            }

            @Override
            protected void done() {
                view.setEstadoBotonIA(true, "Extraer con IA");
                try {
                    ResultadoExtraccionIA resultado = get();
                    if (!resultado.esExito()) {
                        view.mostrarError(resultado.getMensajeError());
                        return;
                    }
                    DatosReservaExtraidos datos = resultado.getDatos();
                    view.llenarFormulario(
                            datos.descripcionActividad(),
                            datos.fecha() != null ? datos.fecha().toString() : null,
                            datos.horaInicio() != null ? datos.horaInicio().format(DateTimeFormatter.ofPattern("HH:mm")) : null,
                            datos.horaFin() != null ? datos.horaFin().format(DateTimeFormatter.ofPattern("HH:mm")) : null
                    );
                    if (!datos.idsCategorias().isEmpty()) {
                        List<CategoriaRecurso> todas = categoriaService.listar();
                        view.seleccionarCategoriasPorId(datos.idsCategorias(), todas);
                    }
                } catch (Exception e) {
                    view.mostrarError("Error al extraer datos: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void limpiar() {
        view.limpiar();
        cargarReservas();
    }

    private void cargarDatos() {
        try {
            view.cargarCategorias(categoriaService.listar());
        } catch (Exception e) {
            view.mostrarError(e.getMessage());
        }
        cargarReservas();
    }

    private void cargarReservas() {
        try {
            List<Reservacion> reservas = service.listarReservacionesSesionActual();
            Object[][] datos = reservas.stream().map(r -> new Object[]{
                    r.getId(),
                    r.getDescripcionActividad(),
                    r.getInicio().toLocalDate().toString(),
                    r.getInicio().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                            + " - " + r.getFin().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                    r.getRecursos().stream().map(Recurso::getDescripcion).collect(Collectors.joining(", ")),
                    r.getEstado().name()
            }).toArray(Object[][]::new);
            view.cargarReservas(datos);
        } catch (Exception e) {
            view.mostrarError(e.getMessage());
        }
    }
}
