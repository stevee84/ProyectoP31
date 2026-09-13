package controller;

import model.CategoriaRecurso;
import model.DatosReservaExtraidos;
import model.Recurso;
import model.Reservacion;
import model.ResultadoExtraccionIA;
import model.ResultadoReserva;
import service.CategoriaService;
import service.ReservaService;
import view.ReservaDialog;
import view.ReservaPanel;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.Frame;
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

        view.setOnAbrirNueva(this::abrirDialogoNueva);
        view.setOnCancelar(this::cancelar);
        view.setOnVisible(this::cargarReservas);

        cargarReservas();
    }

    private Frame getFrame() {
        return (Frame) SwingUtilities.getWindowAncestor(view);
    }

    private void abrirDialogoNueva() {
        List<CategoriaRecurso> categorias;
        try {
            categorias = categoriaService.listar();
        } catch (Exception e) {
            view.mostrarError(e.getMessage());
            return;
        }

        ReservaDialog dialog = new ReservaDialog(getFrame(), categorias);
        dialog.setOnAplicar(() -> aplicar(dialog));
        dialog.setOnExtraerIA(() -> extraerConIA(dialog, categorias));
        dialog.setVisible(true);
    }

    private void aplicar(ReservaDialog dialog) {
        String actividad = dialog.getActividad();
        String fechaStr = dialog.getFecha();
        String horaInicioStr = dialog.getHoraInicio();
        String horaFinStr = dialog.getHoraFin();
        List<CategoriaRecurso> categorias = dialog.getCategoriasSeleccionadas();

        if (actividad.isBlank()) {
            dialog.mostrarError("La actividad es obligatoria.");
            return;
        }
        if (categorias.isEmpty()) {
            dialog.mostrarError("Seleccione al menos una categoria.");
            return;
        }

        LocalDate fecha;
        LocalTime horaInicio;
        LocalTime horaFin;
        try {
            fecha = LocalDate.parse(fechaStr);
        } catch (DateTimeParseException e) {
            dialog.mostrarError("Formato de fecha invalido. Use aaaa-mm-dd.");
            return;
        }
        try {
            horaInicio = LocalTime.parse(horaInicioStr);
        } catch (DateTimeParseException e) {
            dialog.mostrarError("Formato de hora inicio invalido. Use HH:mm.");
            return;
        }
        try {
            horaFin = LocalTime.parse(horaFinStr);
        } catch (DateTimeParseException e) {
            dialog.mostrarError("Formato de hora fin invalido. Use HH:mm.");
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
                dialog.cerrar();
                view.mostrarMensaje("Reservacion creada exitosamente (ID: " + resultado.getReservacion().getId() + ").");
                cargarReservas();
            } else {
                String catsFaltantes = resultado.getCategoriasNoDisponibles().stream()
                        .map(CategoriaRecurso::getDescripcion)
                        .collect(Collectors.joining(", "));
                dialog.mostrarError("No hay disponibilidad en: " + catsFaltantes);
            }
        } catch (Exception e) {
            dialog.mostrarError(e.getMessage());
        }
    }

    private void extraerConIA(ReservaDialog dialog, List<CategoriaRecurso> categorias) {
        String frase = dialog.getFrase();
        if (frase.isBlank()) {
            dialog.mostrarError("Escriba una frase para extraer datos.");
            return;
        }

        dialog.setEstadoBotonIA(false, "Procesando...");

        new SwingWorker<ResultadoExtraccionIA, Void>() {
            @Override
            protected ResultadoExtraccionIA doInBackground() {
                return service.extraerDatosDesdeFrase(frase);
            }

            @Override
            protected void done() {
                dialog.setEstadoBotonIA(true, "Extraer con IA");
                try {
                    ResultadoExtraccionIA resultado = get();
                    if (!resultado.esExito()) {
                        dialog.mostrarError(resultado.getMensajeError());
                        return;
                    }
                    DatosReservaExtraidos datos = resultado.getDatos();
                    dialog.llenarFormulario(
                            datos.descripcionActividad(),
                            datos.fecha() != null ? datos.fecha().toString() : null,
                            datos.horaInicio() != null ? datos.horaInicio().format(DateTimeFormatter.ofPattern("HH:mm")) : null,
                            datos.horaFin() != null ? datos.horaFin().format(DateTimeFormatter.ofPattern("HH:mm")) : null
                    );
                    if (!datos.idsCategorias().isEmpty()) {
                        dialog.seleccionarCategoriasPorId(datos.idsCategorias(), categorias);
                    }
                } catch (Exception e) {
                    dialog.mostrarError("Error al extraer datos: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void cancelar(int filaModelo) {
        int id = view.getReservaIdEnFila(filaModelo);
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
