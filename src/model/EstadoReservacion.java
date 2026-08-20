package model;

public enum EstadoReservacion {
    ACTIVA("Activa"),
    CANCELADA("Cancelada");

    private final String etiqueta;

    EstadoReservacion(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}