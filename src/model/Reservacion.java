package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Reservacion implements Comparable<Reservacion> {

    private final int id;
    private Empleado empleado;
    private List<Recurso> recursos;
    private String descripcionActividad;
    private LocalDateTime inicio;
    private LocalDateTime fin;
    private EstadoReservacion estado;

    public Reservacion(int id, Empleado empleado, List<Recurso> recursos, String descripcionActividad,
                       LocalDateTime inicio, LocalDateTime fin) {
        this.id = id;
        actualizarDatos(empleado, recursos, descripcionActividad, inicio, fin);
        this.estado = EstadoReservacion.ACTIVA;
    }

    public void actualizarDatos(Empleado empleado, List<Recurso> recursos, String descripcionActividad,
                                LocalDateTime inicio, LocalDateTime fin) {
        if (empleado == null) {
            throw new IllegalArgumentException("El empleado es obligatorio.");
        }
        if (recursos == null || recursos.isEmpty()) {
            throw new IllegalArgumentException("Debe asignarse al menos un recurso.");
        }
        if (recursos.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("La lista de recursos no puede contener elementos nulos.");
        }
        if (descripcionActividad == null || descripcionActividad.isBlank()) {
            throw new IllegalArgumentException("La descripción de la actividad es obligatoria.");
        }
        if (inicio == null || fin == null) {
            throw new IllegalArgumentException("La fecha de inicio y de terminación son obligatorias.");
        }
        if (!inicio.isBefore(fin)) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior a la de terminación.");
        }
        this.empleado = empleado;
        this.recursos = new ArrayList<>(recursos);
        this.descripcionActividad = descripcionActividad.trim();
        this.inicio = inicio;
        this.fin = fin;
    }

    public void cancelar() {
        if (estado == EstadoReservacion.CANCELADA) {
            throw new IllegalStateException("La reservación ya está cancelada.");
        }
        if (!inicio.isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Solo se pueden cancelar reservaciones futuras.");
        }
        this.estado = EstadoReservacion.CANCELADA;
    }

    public boolean esActiva() {
        return estado == EstadoReservacion.ACTIVA;
    }

    public boolean incluyeRecurso(Recurso recurso) {
        return recursos.contains(recurso);
    }

    public boolean seSolapa(Recurso recurso, LocalDateTime inicio, LocalDateTime fin) {
        return esActiva()
                && incluyeRecurso(recurso)
                && this.inicio.isBefore(fin)
                && inicio.isBefore(this.fin);
    }

    public int getId() {
        return id;
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public List<Recurso> getRecursos() {
        return Collections.unmodifiableList(recursos);
    }

    public String getDescripcionActividad() {
        return descripcionActividad;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public LocalDateTime getFin() {
        return fin;
    }

    public EstadoReservacion getEstado() {
        return estado;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservacion r)) return false;
        return id == r.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(Reservacion otra) {
        return this.inicio.compareTo(otra.inicio);
    }

    @Override
    public String toString() {
        String recursosTxt = recursos.stream()
                .map(Recurso::getDescripcion)
                .collect(Collectors.joining(", "));
        return String.format("Reservacion[id=%d, empleado=%s, recursos=[%s], actividad='%s', inicio=%s, fin=%s, estado=%s]",
                id, empleado.getName(), recursosTxt, descripcionActividad, inicio, fin, estado);
    }
}