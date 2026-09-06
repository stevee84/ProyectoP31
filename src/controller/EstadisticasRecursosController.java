package controller;

import exception.ValidacionException;
import model.CategoriaRecurso;
import model.Recurso;
import model.Reservacion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class EstadisticasRecursosController {

    private ControladorReservaciones controlador;

    public EstadisticasRecursosController(
            ControladorReservaciones controlador
    ) {
        this.controlador = controlador;
    }

    public List<CategoriaRecurso> listarCategorias() {
        return controlador.listarCategorias();
    }

    public int contarReservasCategoria(
            CategoriaRecurso categoria,
            LocalDate desde,
            LocalDate hasta
    ) {
        if (categoria == null) {
            throw new ValidacionException(
                    "La categoría es obligatoria."
            );
        }

        if (desde == null || hasta == null) {
            throw new ValidacionException(
                    "Las fechas son obligatorias."
            );
        }

        if (desde.isAfter(hasta)) {
            throw new ValidacionException(
                    "La fecha desde no puede ser posterior a la fecha hasta."
            );
        }

        LocalDateTime inicio =
                desde.atStartOfDay();

        LocalDateTime fin =
                hasta.plusDays(1).atStartOfDay();

        int cantidad = 0;

        for (Reservacion reservacion :
                controlador.listarReservacionesEnRango(
                        inicio,
                        fin
                )) {

            if (reservacion.esActiva()) {
                for (Recurso recurso :
                        reservacion.getRecursos()) {

                    if (recurso.getCategoria()
                            .equals(categoria)) {

                        cantidad++;
                    }
                }
            }
        }

        return cantidad;
    }
}
