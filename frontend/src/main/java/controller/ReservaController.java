package controller;

import dto.CategoriaDTO;
import dto.ResultadoExtraccionDTO;
import model.CategoriaModel;
import model.ReservaModel;
import model.TablaDatos;
import protocolo.CodigoError;
import util.Tarea;
import view.ReservaDialog;
import view.ReservaPanel;

import javax.swing.SwingUtilities;
import java.awt.Frame;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Crear, listar y cancelar reservas propias. El controlador solo convierte el texto
 * del formulario a fechas; horario permitido, solapes y permisos los valida el Backend.
 */
public class ReservaController {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final ReservaPanel view;
    private final ReservaModel modelo;
    private final CategoriaModel categoriaModelo;

    public ReservaController(ReservaPanel view, ReservaModel modelo, CategoriaModel categoriaModelo) {
        this.view = view;
        this.modelo = modelo;
        this.categoriaModelo = categoriaModelo;

        view.setOnAbrirNueva(this::abrirDialogoNueva);
        view.setOnCancelar(this::cancelar);
        view.setOnVisible(this::cargarReservas);

        cargarReservas();
    }

    private Frame getFrame() {
        return (Frame) SwingUtilities.getWindowAncestor(view);
    }

    private void abrirDialogoNueva() {
        Tarea.ejecutar(categoriaModelo::listar, categorias -> {
            if (categorias.isEmpty()) {
                view.mostrarError("No hay categorias registradas.");
                return;
            }
            ReservaDialog dialog = new ReservaDialog(getFrame(), categorias);
            dialog.setOnAplicar(() -> aplicar(dialog));
            dialog.setOnExtraerIA(() -> extraerConIA(dialog, categorias));
            dialog.setVisible(true);
        }, e -> view.mostrarError(e.getMessage()));
    }

    private void aplicar(ReservaDialog dialog) {
        String actividad = dialog.getActividad();
        List<CategoriaDTO> categorias = dialog.getCategoriasSeleccionadas();

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
            fecha = LocalDate.parse(dialog.getFecha());
        } catch (DateTimeParseException e) {
            dialog.mostrarError("Formato de fecha invalido. Use aaaa-mm-dd.");
            return;
        }
        try {
            horaInicio = LocalTime.parse(dialog.getHoraInicio(), HORA);
        } catch (DateTimeParseException e) {
            dialog.mostrarError("Formato de hora inicio invalido. Use HH:mm.");
            return;
        }
        try {
            horaFin = LocalTime.parse(dialog.getHoraFin(), HORA);
        } catch (DateTimeParseException e) {
            dialog.mostrarError("Formato de hora fin invalido. Use HH:mm.");
            return;
        }

        LocalDateTime inicio = fecha.atTime(horaInicio);
        LocalDateTime fin = fecha.atTime(horaFin);
        List<String> idsCategorias = categorias.stream().map(CategoriaDTO::id).toList();

        dialog.setOcupado(true);
        Tarea.ejecutar(() -> modelo.crear(idsCategorias, actividad, inicio, fin),
                resultado -> {
                    dialog.setOcupado(false);
                    if (resultado.exito()) {
                        dialog.cerrar();
                        view.mostrarMensaje("Reservacion creada exitosamente (ID: " + resultado.reserva().id() + ").");
                        cargarReservas();
                    } else {
                        dialog.mostrarError(ReservaModel.mensajeSinDisponibilidad(resultado));
                    }
                },
                error -> {
                    dialog.setOcupado(false);
                    if (error.getCodigo() == CodigoError.COMUNICACION) {
                        // Sin reintento automático: la reserva pudo quedar registrada.
                        dialog.mostrarError(error.getMessage()
                                + "\nRevise la lista de sus reservas antes de intentarlo de nuevo.");
                        cargarReservas();
                    } else {
                        dialog.mostrarError(error.getMessage());
                    }
                });
    }

    private void extraerConIA(ReservaDialog dialog, List<CategoriaDTO> categorias) {
        String frase = dialog.getFrase();
        if (frase.isBlank()) {
            dialog.mostrarError("Escriba una frase para extraer datos.");
            return;
        }

        dialog.setEstadoBotonIA(false, "Procesando...");
        Tarea.ejecutar(() -> modelo.extraerConIA(frase),
                resultado -> {
                    dialog.setEstadoBotonIA(true, "Extraer con IA");
                    llenarDesdeIA(dialog, categorias, resultado);
                },
                error -> {
                    dialog.setEstadoBotonIA(true, "Extraer con IA");
                    dialog.mostrarError("Error al extraer datos: " + error.getMessage());
                });
    }

    /** La IA solo propone datos: el funcionario los revisa y completa antes de aplicar. */
    private static void llenarDesdeIA(ReservaDialog dialog, List<CategoriaDTO> categorias,
                                      ResultadoExtraccionDTO resultado) {
        if (!resultado.exito()) {
            dialog.mostrarError(resultado.mensajeError());
            return;
        }
        dialog.llenarFormulario(
                resultado.descripcionActividad(),
                resultado.fecha() != null ? resultado.fecha().toString() : null,
                resultado.horaInicio() != null ? HORA.format(resultado.horaInicio()) : null,
                resultado.horaFin() != null ? HORA.format(resultado.horaFin()) : null);
        if (!resultado.idsCategorias().isEmpty()) {
            dialog.seleccionarCategoriasPorId(resultado.idsCategorias(), categorias);
        }
    }

    private void cancelar(int filaModelo) {
        int id = view.getReservaIdEnFila(filaModelo);
        if (!view.confirmarAccion("Desea cancelar la reservacion " + id + "?")) {
            return;
        }
        Tarea.ejecutar(() -> { modelo.cancelar(id); return null; },
                r -> {
                    view.mostrarMensaje("Reservacion cancelada.");
                    cargarReservas();
                },
                e -> view.mostrarError(e.getMessage()));
    }

    private void cargarReservas() {
        Tarea.ejecutar(() -> ReservaModel.aTabla(modelo.listarMias()),
                (TablaDatos tabla) -> view.cargarReservas(tabla.filas()),
                e -> view.mostrarError(e.getMessage()));
    }
}
