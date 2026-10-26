package controller;

import dto.EstadisticaCategoriaDTO;
import model.EstadisticasModel;
import util.Tarea;
import view.EstadisticasRecursosPanel;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/** Reservas por categoría en un rango; una sola consulta agregada al servidor. */
public class EstadisticasRecursosController {

    private final EstadisticasRecursosPanel view;
    private final EstadisticasModel modelo;

    public EstadisticasRecursosController(EstadisticasRecursosPanel view, EstadisticasModel modelo) {
        this.view = view;
        this.modelo = modelo;

        view.setOnCalcular(this::calcular);
    }

    private void calcular() {
        LocalDate desde;
        LocalDate hasta;
        try {
            desde = LocalDate.parse(view.getDesde());
        } catch (DateTimeParseException e) {
            view.mostrarError("Formato de fecha 'desde' invalido. Use aaaa-mm-dd.");
            return;
        }
        try {
            hasta = LocalDate.parse(view.getHasta());
        } catch (DateTimeParseException e) {
            view.mostrarError("Formato de fecha 'hasta' invalido. Use aaaa-mm-dd.");
            return;
        }

        Tarea.ejecutar(() -> modelo.contarPorCategoria(desde, hasta), this::mostrar,
                e -> view.mostrarError(e.getMessage()));
    }

    private void mostrar(List<EstadisticaCategoriaDTO> estadisticas) {
        List<String> nombres = new ArrayList<>();
        List<Integer> cantidades = new ArrayList<>();
        for (EstadisticaCategoriaDTO e : estadisticas) {
            nombres.add(e.categoria().descripcion());
            cantidades.add(e.cantidad());
        }
        view.cargarDatos(nombres, cantidades);
    }
}
