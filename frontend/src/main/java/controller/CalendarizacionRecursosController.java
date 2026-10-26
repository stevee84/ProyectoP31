package controller;

import dto.CategoriaDTO;
import model.CalendarizacionModel;
import model.TablaDatos;
import util.Tarea;
import view.CalendarizacionRecursosPanel;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class CalendarizacionRecursosController {

    private final CalendarizacionRecursosPanel view;
    private final CalendarizacionModel modelo;

    public CalendarizacionRecursosController(CalendarizacionRecursosPanel view, CalendarizacionModel modelo) {
        this.view = view;
        this.modelo = modelo;

        view.setOnMostrar(this::mostrar);
        view.setOnVisible(this::cargarCategorias);

        cargarCategorias();
    }

    private void mostrar() {
        CategoriaDTO categoria = view.getCategoriaSeleccionada();
        if (categoria == null) {
            view.mostrarError("Seleccione una categoria.");
            return;
        }

        LocalDate fecha;
        try {
            fecha = LocalDate.parse(view.getFecha());
        } catch (DateTimeParseException e) {
            view.mostrarError("Formato de fecha invalido. Use aaaa-mm-dd.");
            return;
        }

        // Una sola solicitud trae la matriz completa (todas las horas y recursos).
        Tarea.ejecutar(() -> modelo.obtener(fecha, categoria.id()),
                matriz -> {
                    if (matriz.recursos().isEmpty()) {
                        view.mostrarError("No hay recursos en esta categoria.");
                        return;
                    }
                    TablaDatos tabla = CalendarizacionModel.aTabla(matriz);
                    view.mostrarCalendarizacion(tabla.columnas(), tabla.filas());
                },
                e -> view.mostrarError(e.getMessage()));
    }

    private void cargarCategorias() {
        Tarea.ejecutar(modelo::listarCategorias, view::cargarCategorias, e -> view.mostrarError(e.getMessage()));
    }
}
