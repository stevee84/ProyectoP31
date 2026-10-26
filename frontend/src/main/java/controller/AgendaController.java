package controller;

import model.EstadisticasModel;
import model.TablaDatos;
import util.Tarea;
import view.AgendaSemanalPanel;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AgendaController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final AgendaSemanalPanel view;
    private final EstadisticasModel modelo;
    private LocalDate semanaActual;
    /** Identifica la última consulta; las respuestas de consultas anteriores se descartan. */
    private int ultimaSolicitud;

    public AgendaController(AgendaSemanalPanel view, EstadisticasModel modelo) {
        this.view = view;
        this.modelo = modelo;
        this.semanaActual = LocalDate.now();

        view.setOnSemanaAnterior(() -> {
            semanaActual = semanaActual.minusWeeks(1);
            cargarSemana();
        });
        view.setOnSemanaActual(() -> {
            semanaActual = LocalDate.now();
            cargarSemana();
        });
        view.setOnSemanaSiguiente(() -> {
            semanaActual = semanaActual.plusWeeks(1);
            cargarSemana();
        });

        cargarSemana();
    }

    private void cargarSemana() {
        LocalDate dia = semanaActual;
        LocalDate lunes = dia.with(DayOfWeek.MONDAY);
        String etiqueta = FMT.format(lunes) + " - " + FMT.format(lunes.plusDays(6));

        int solicitud = ++ultimaSolicitud;
        Tarea.ejecutar(() -> EstadisticasModel.agendaATabla(modelo.obtenerAgenda(dia)),
                (TablaDatos tabla) -> {
                    if (solicitud == ultimaSolicitud) {
                        view.cargarSemana(etiqueta, tabla.columnas(), tabla.filas());
                    }
                },
                e -> {
                    if (solicitud == ultimaSolicitud) {
                        view.mostrarError(e.getMessage());
                    }
                });
    }
}
