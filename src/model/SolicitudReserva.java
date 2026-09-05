package model;

import java.time.LocalDateTime;
import java.util.List;
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
            throw new IllegalArgumentException("El Empleado es obligatorio");
        }
        if (idsCategorias == null || idsCategorias.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos una categoria");
        }
        if (descripcionActividad == null || descripcionActividad.isBlank()) {
            throw new IllegalArgumentException("La descripcion de la actividad es obligatoria");
        }
        if (inicio == null || fin == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias");
        }
        if (inicio.isAfter(fin)) {
            throw new IllegalArgumentException("La hora de inicio no puede ser posterior a la hora finalizada");
        }
        idsCategorias = List.copyOf(idsCategorias);
    }
}
