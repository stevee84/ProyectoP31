package controller;

import model.CategoriaRecurso;
import model.EstadisticasModel;
import view.EstadisticasRecursosPanel;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class EstadisticasRecursosController {

    private final EstadisticasRecursosPanel view;
    private final EstadisticasModel modelo;

    public EstadisticasRecursosController(EstadisticasRecursosPanel view, EstadisticasModel modelo) {
        this.view = view;
        this.modelo = modelo;

        view.setOnCalcular(this::calcular);
    }

    private void calcular() {
        String desdeStr = view.getDesde();
        String hastaStr = view.getHasta();

        LocalDate desde;
        LocalDate hasta;
        try {
            desde = LocalDate.parse(desdeStr);
        } catch (DateTimeParseException e) {
            view.mostrarError("Formato de fecha 'desde' invalido. Use aaaa-mm-dd.");
            return;
        }
        try {
            hasta = LocalDate.parse(hastaStr);
        } catch (DateTimeParseException e) {
            view.mostrarError("Formato de fecha 'hasta' invalido. Use aaaa-mm-dd.");
            return;
        }

        try {
            List<CategoriaRecurso> categorias = modelo.listarCategorias();
            List<String> nombres = new ArrayList<>();
            List<Integer> cantidades = new ArrayList<>();

            for (CategoriaRecurso cat : categorias) {
                int cantidad = modelo.contarReservasCategoria(cat, desde, hasta);
                nombres.add(cat.getDescripcion());
                cantidades.add(cantidad);
            }

            view.cargarDatos(nombres, cantidades);
        } catch (Exception e) {
            view.mostrarError(e.getMessage());
        }
    }
}
