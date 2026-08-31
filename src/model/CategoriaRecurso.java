package model;

import java.util.Objects;

public class CategoriaRecurso implements Comparable<CategoriaRecurso> {

    private String id;
    private String descripcion;

    public CategoriaRecurso(String id, String descripcion) {
        actualizarDatos(id, descripcion);
    }

    public void actualizarDatos(String id, String descripcion) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El id de la categoría es obligatorio.");
        }
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripción de la categoría es obligatoria.");
        }
        this.id = id.trim();
        this.descripcion = descripcion.trim();
    }

    public String getId() {
        return id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CategoriaRecurso c)) return false;
        return id.equals(c.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(CategoriaRecurso otra) {
        return this.descripcion.compareToIgnoreCase(otra.descripcion);
    }

    @Override
    public String toString() {
        return descripcion;
    }
}