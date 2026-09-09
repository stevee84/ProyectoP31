package controller;

import model.CategoriaRecurso;
import model.Recurso;
import service.CalendarizacionService;
import view.CalendarizacionRecursosPanel;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class CalendarizacionRecursosController {

    private static final int HORA_INICIO = 7;
    private static final int HORA_FIN = 18;

    private final CalendarizacionRecursosPanel view;
    private final CalendarizacionService service;

    public CalendarizacionRecursosController(CalendarizacionRecursosPanel view, CalendarizacionService service) {
        this.view = view;
        this.service = service;

        view.setOnMostrar(this::mostrar);
        view.setOnVisible(this::cargarCategorias);

        cargarCategorias();
    }

    private void mostrar() {
        String fechaStr = view.getFecha();
        CategoriaRecurso cat = view.getCategoriaSeleccionada();

        if (cat == null) {
            view.mostrarError("Seleccione una categoria.");
            return;
        }

        LocalDate fecha;
        try {
            fecha = LocalDate.parse(fechaStr);
        } catch (DateTimeParseException e) {
            view.mostrarError("Formato de fecha invalido. Use aaaa-mm-dd.");
            return;
        }

        try {
            List<Recurso> recursos = service.listarRecursosPorCategoria(cat.getId());
            if (recursos.isEmpty()) {
                view.mostrarError("No hay recursos en esta categoria.");
                return;
            }

            String[] columnas = new String[recursos.size() + 1];
            columnas[0] = "Hora";
            for (int i = 0; i < recursos.size(); i++) {
                columnas[i + 1] = recursos.get(i).getDescripcion();
            }

            int filas = HORA_FIN - HORA_INICIO;
            Object[][] datos = new Object[filas][recursos.size() + 1];
            for (int h = 0; h < filas; h++) {
                int hora = HORA_INICIO + h;
                datos[h][0] = String.format("%02d:00", hora);
                for (int r = 0; r < recursos.size(); r++) {
                    datos[h][r + 1] = service.obtenerInformacionCelda(recursos.get(r), fecha, hora);
                }
            }

            view.mostrarCalendarizacion(columnas, datos);
        } catch (Exception e) {
            view.mostrarError(e.getMessage());
        }
    }

    private void cargarCategorias() {
        try {
            view.cargarCategorias(service.listarCategorias());
        } catch (Exception e) {
            view.mostrarError(e.getMessage());
        }
    }
}
