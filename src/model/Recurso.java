package model;

import java.util.Objects;

public class Recurso implements Comparable<Recurso> {

    private String codigo;
    private CategoriaRecurso categoria;
    private String descripcion;

    public Recurso(String codigo, CategoriaRecurso categoria, String descripcion) {
        actualizarDatos(codigo, categoria, descripcion);
    }

    public void actualizarDatos(String codigo, CategoriaRecurso categoria, String descripcion) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código del recurso es obligatorio.");
        }
        if (categoria == null) {
            throw new IllegalArgumentException("La categoría del recurso es obligatoria.");
        }
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripción del recurso es obligatoria.");
        }
        this.codigo = codigo.trim();
        this.categoria = categoria;
        this.descripcion = descripcion.trim();
    }

    public String getCodigo() {
        return codigo;
    }

    public CategoriaRecurso getCategoria() {
        return categoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Recurso r)) return false;
        return codigo.equals(r.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    @Override
    public int compareTo(Recurso otro) {
        return this.descripcion.compareToIgnoreCase(otro.descripcion);
    }

    @Override
    public String toString() {
        return String.format("Recurso[codigo=%s, categoria='%s', descripcion='%s']",
                codigo, categoria.getDescripcion(), descripcion);
    }
}