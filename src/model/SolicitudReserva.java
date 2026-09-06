package model;

import exception.ValidacionException;

import java.time.LocalDateTime;
import java.util.List;

public record SolicitudReserva (
        Empleado empleado,
        List<String> idsCategorias,
        String descripcionActividad,
        LocalDateTime inicio,
        LocalDateTime fin
) {
    public SolicitudReserva {
        if (empleado == null) {
            throw new ValidacionException("El Empleado es obligatorio");
        }
        if (idsCategorias == null || idsCategorias.isEmpty()) {
            throw new ValidacionException("Debe seleccionar al menos una categoria");
        }
        if (descripcionActividad == null || descripcionActividad.isBlank()) {
            throw new ValidacionException("La descripcion de la actividad es obligatoria");
        }
        if (inicio == null || fin == null) {
            throw new ValidacionException("Las fechas de inicio y fin son obligatorias");
        }
        if (inicio.isAfter(fin)) {
            throw new ValidacionException("La hora de inicio no puede ser posterior a la hora finalizada");
        }
        idsCategorias = List.copyOf(idsCategorias);
    }
}
