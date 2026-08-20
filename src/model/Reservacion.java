package model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Reservacion implements Comparable<Reservacion> {

    private final int id;
    private Empleado empleado;
    private Recurso recurso;
    private LocalDateTime inicio;
    private LocalDateTime fin;
    private EstadoReservacion estado;

    public Reservacion(int id, Empleado empleado, Recurso recurso,
                       LocalDateTime inicio, LocalDateTime fin) {
        this.id = id;
        actualizarDatos(empleado, recurso, inicio, fin);
        this.estado = EstadoReservacion.ACTIVA;
    }

    public void actualizarDatos(Empleado empleado, Recurso recurso,
                                LocalDateTime inicio, LocalDateTime fin) {
        if (empleado == null) {
            throw new IllegalArgumentException("El empleado es obligatorio.");
        }
        if (recurso == null) {
            throw new IllegalArgumentException("El recurso es obligatorio.");
        }
        if (inicio == null || fin == null) {
            throw new IllegalArgumentException("La fecha de inicio y de terminación son obligatorias.");
        }
        if (!inicio.isBefore(fin)) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior a la de terminación.");
        }
        this.empleado = empleado;
        this.recurso = recurso;
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

    public boolean seSolapa(LocalDateTime inicio, LocalDateTime fin) {
        return esActiva()
                && this.inicio.isBefore(fin)
                && inicio.isBefore(this.fin);
    }

    public int getId() {
        return id;
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public Recurso getRecurso() {
        return recurso;
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
        return String.format("Reservacion[id=%d, empleado=%s, recurso=%s, inicio=%s, fin=%s, estado=%s]",
                id, empleado.getName(), recurso.getDescripcion(), inicio, fin, estado);
    }
}