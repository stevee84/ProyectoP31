package controller;

import model.EstadisticaSemana;
import model.EstadisticasModel;
import view.EstadisticasPanel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

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
        String desdeStr = view.getDesde();
        String hastaStr = view.getHasta();

        LocalDate desde;
        LocalDate hasta;
        try {
            desde = LocalDate.parse(desdeStr, FORMATO_FECHA);
        } catch (DateTimeParseException e) {
            view.mostrarError("Formato de fecha 'desde' invalido. Use dd/MM/aaaa.");
            return;
        }
        try {
            hasta = LocalDate.parse(hastaStr, FORMATO_FECHA);
        } catch (DateTimeParseException e) {
            view.mostrarError("Formato de fecha 'hasta' invalido. Use dd/MM/aaaa.");
            return;
        }

        try {
            List<EstadisticaSemana> semanas = modelo.contarPorSemana(desde, hasta);

            List<Object[]> filas = new ArrayList<>();
            List<String> etiquetas = new ArrayList<>();
            List<Integer> cantidades = new ArrayList<>();

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
            for (EstadisticaSemana s : semanas) {
                String etiqueta = fmt.format(s.inicioSemana()) + " - " + fmt.format(s.finSemana());
                filas.add(new Object[]{etiqueta, s.cantidad()});
                etiquetas.add(etiqueta);
                cantidades.add(s.cantidad());
            }

            view.cargarTabla(filas);
            view.actualizarGrafico(etiquetas, cantidades);
        } catch (Exception e) {
            view.mostrarError(e.getMessage());
        }
    }
}
