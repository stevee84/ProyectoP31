package model;

import exception.DisponibilidadException;
import exception.ValidacionException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Reservacion implements Comparable<Reservacion> {

    private final int id;
    private Empleado empleado;
    private Coleccion<Recurso> recursos;
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
            throw new ValidacionException("El empleado es obligatorio.");
        }
        if (recursos == null || recursos.isEmpty()) {
            throw new ValidacionException("Debe asignarse al menos un recurso.");
        }
        if (recursos.stream().anyMatch(Objects::isNull)) {
            throw new ValidacionException("La lista de recursos no puede contener elementos nulos.");
        }
        if (descripcionActividad == null || descripcionActividad.isBlank()) {
            throw new ValidacionException("La descripción de la actividad es obligatoria.");
        }
        if (inicio == null || fin == null) {
            throw new ValidacionException("La fecha de inicio y de terminación son obligatorias.");
        }
        if (!inicio.isBefore(fin)) {
            throw new ValidacionException("La hora de inicio debe ser anterior a la de terminación.");
        }
        this.empleado = empleado;
        this.recursos = new Coleccion<>(recursos);
        this.descripcionActividad = descripcionActividad.trim();
        this.inicio = inicio;
        this.fin = fin;
    }

    public void cancelar() {
        if (estado == EstadoReservacion.CANCELADA) {
            throw new DisponibilidadException("La reservación ya está cancelada.");
        }
        if (!inicio.isAfter(LocalDateTime.now())) {
            throw new DisponibilidadException("Solo se pueden cancelar reservaciones futuras.");
        }
        this.estado = EstadoReservacion.CANCELADA;
    }

    public boolean esActiva() {
        return estado == EstadoReservacion.ACTIVA;
    }

    public boolean incluyeRecurso(Recurso recurso) {
        return recursos.contiene(recurso);
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
        return recursos.listar();
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

    public void setEstado(EstadoReservacion estado) {
        this.estado = estado;
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