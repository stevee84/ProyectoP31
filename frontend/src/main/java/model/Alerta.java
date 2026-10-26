package model;

import dto.RecursoDTO;
import dto.ReservaDTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Una reserva hecha por otro usuario, tal como se muestra en el área de alertas.
 * Es solo informativa: descartarla no cambia la reserva en el Backend.
 */
public record Alerta(ReservaDTO reserva, LocalDateTime recibida) {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    /** Texto de la lista: "Juan Mora - Reunion clientes". */
    public String titulo() {
        return encargado() + " - " + actividad();
    }

    public String actividad() {
        return reserva.descripcionActividad();
    }

    public String encargado() {
        return reserva.autor() != null ? reserva.autor().nombre() : "";
    }

    public String fecha() {
        return reserva.inicio().toLocalDate().toString();
    }

    public String desde() {
        return HORA.format(reserva.inicio());
    }

    public String hasta() {
        return HORA.format(reserva.fin());
    }

    public List<String> recursos() {
        return reserva.recursos().stream().map(RecursoDTO::descripcion).toList();
    }
}
