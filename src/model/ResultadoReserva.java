package model;

import java.util.List;
import java.util.Objects;

public final class ResultadoReserva {
    private final boolean exito;
    private final Reservacion reservacion;
    private final List<CategoriaRecurso> categoriasNoDisponibles;

    private ResultadoReserva(boolean exito, Reservacion reservacion, List<CategoriaRecurso> categorias) {
        this.exito = exito;
        this.reservacion = reservacion;
        this.categoriasNoDisponibles = categorias;
    }

    public static ResultadoReserva exito(Reservacion reservacion) {
        Objects.requireNonNull(reservacion, "La reservacion no puede ser nula en un resultado exitoso...");
        return new ResultadoReserva(true, reservacion, List.of());
    }

    public static ResultadoReserva fracaso(List<CategoriaRecurso> categoriasNoDisponibles) {
        if (categoriasNoDisponibles == null || categoriasNoDisponibles.isEmpty()) {
            throw new IllegalArgumentException("Un resultado no exitoso debe indicar al menos una categoria sin disponibilidad");
        }
        return new ResultadoReserva(false, null, List.copyOf(categoriasNoDisponibles));
    }

    public boolean esExito() {
        return exito;
    }

    public Reservacion getReservacion() {
        if (!exito) {
            throw new IllegalStateException("No hay reservacion: el intento de reserva fallo");
        }
        return reservacion;
    }

    public List<CategoriaRecurso> getCategoriasNoDisponibles() {
        return categoriasNoDisponibles;
    }

    @Override
    public String toString() {
        if (exito){
            return "ResultadoReserva[exito=true, reservacion=" + reservacion.getId() + "]";
        }
        return "ResultadoReserva[exito=false, categoriasNoDisponibles=" + categoriasNoDisponibles + "]";
    }
}
