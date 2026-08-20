package model;

import java.util.Objects;

public abstract class Empleado implements Comparable<Empleado> {

    protected String name;
    protected String id;
    protected String pass;
    protected boolean firstLog;

    public Empleado(String name, String id) {
        actualizarDatos(name, id);
        this.pass = this.id;
        this.firstLog = true;
    }

    public void actualizarDatos(String name, String id) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("La identificación es obligatoria.");
        }
        this.name = name.trim();
        this.id = id.trim();
    }

    public void cambiarContraseña(String nueva) {
        if (nueva == null || nueva.isBlank()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía.");
        }
        this.pass = nueva;
        this.firstLog = false;
    }

    public boolean verificarContraseña(String pass) {
        return pass != null && this.pass.equals(pass);
    }

    public void setFirstLog(boolean firstLog) {
        this.firstLog = firstLog;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getPass() {
        return pass;
    }

    public boolean isFirstLog() {
        return firstLog;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Empleado e)) return false;
        return Objects.equals(id, e.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public int compareTo(Empleado otro) {
        return this.name.compareToIgnoreCase(otro.name);
    }

    @Override
    public String toString() {
        return String.format("Empleado[id=%s, nombre='%s', firstLog=%s]", id, name, firstLog);
    }
}