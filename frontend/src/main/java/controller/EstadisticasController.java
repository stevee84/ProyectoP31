package controller;

import dto.EstadisticaSemanaDTO;
import model.EstadisticasModel;
import util.Tarea;
import view.EstadisticasPanel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/** Actividades realizadas por semana en un rango de fechas. */
public class EstadisticasController {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final EstadisticasPanel view;
    private final EstadisticasModel modelo;

    public EstadisticasController(EstadisticasPanel view, EstadisticasModel modelo) {
        this.view = view;
        this.modelo = modelo;

        view.setOnGenerar(this::generar);
    }

    private void generar() {
        LocalDate desde;
        LocalDate hasta;
        try {
            desde = LocalDate.parse(view.getDesde(), FORMATO_FECHA);
        } catch (DateTimeParseException e) {
            view.mostrarError("Formato de fecha 'desde' invalido. Use dd/MM/aaaa.");
            return;
        }
        try {
            hasta = LocalDate.parse(view.getHasta(), FORMATO_FECHA);
        } catch (DateTimeParseException e) {
            view.mostrarError("Formato de fecha 'hasta' invalido. Use dd/MM/aaaa.");
            return;
        }

        Tarea.ejecutar(() -> modelo.contarPorSemana(desde, hasta), this::mostrar,
                e -> view.mostrarError(e.getMessage()));
    }

    private void mostrar(List<EstadisticaSemanaDTO> semanas) {
        List<Object[]> filas = new ArrayList<>();
        List<String> etiquetas = new ArrayList<>();
        List<Integer> cantidades = new ArrayList<>();
        for (EstadisticaSemanaDTO s : semanas) {
            String etiqueta = EstadisticasModel.etiquetaSemana(s);
            filas.add(new Object[]{etiqueta, s.cantidad()});
            etiquetas.add(etiqueta);
            cantidades.add(s.cantidad());
        }
        view.cargarTabla(filas);
        view.actualizarGrafico(etiquetas, cantidades);
    }
}
