package controller;

import model.CategoriaRecurso;
import model.Recurso;
import model.Reservacion;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class CalendarizacionRecursosController {

    private ControladorReservaciones controlador;

    public CalendarizacionRecursosController(
            ControladorReservaciones controlador
    ) {
        this.controlador = controlador;
    }

    public List<CategoriaRecurso> listarCategorias() {
        return controlador.listarCategorias();
    }

    public List<Recurso> listarRecursosPorCategoria(
            String idCategoria
    ) {
        return controlador.listarRecursosPorCategoria(
                idCategoria
        );
    }

    public List<Reservacion> listarReservacionesEnFecha(
            LocalDate fecha
    ) {
        return controlador.listarReservacionesEnFecha(
                fecha
        );
    }

    public String obtenerInformacionCelda(
            Recurso recurso,
            LocalDate fecha,
            int hora
    ) {
        LocalDateTime inicioHora =
                fecha.atTime(hora, 0);

        LocalDateTime finHora =
                inicioHora.plusHours(1);

        for (Reservacion reservacion :
                listarReservacionesEnFecha(fecha)) {

            boolean estaActiva =
                    reservacion.esActiva();

            boolean contieneRecurso =
                    reservacion.incluyeRecurso(recurso);

            boolean coincideHorario =
                    reservacion.getInicio().isBefore(finHora)
                            && inicioHora.isBefore(
                            reservacion.getFin()
                    );

            if (estaActiva
                    && contieneRecurso
                    && coincideHorario) {

                return reservacion.getDescripcionActividad()
                        + " - "
                        + reservacion.getEmpleado().getName();
            }
        }

        return "Disponible";
    }
}
