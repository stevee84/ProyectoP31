package model;

public class Funcionario extends Empleado {

    private String telefono;

    public Funcionario(String name, String id, String telefono) {
        super(name, id);
        actualizarTelefono(telefono);
    }

    public void actualizarDatos(String name, String id, String telefono) {
        super.actualizarDatos(name, id);
        actualizarTelefono(telefono);
    }

    public void actualizarTelefono(String telefono) {
        if (telefono == null || telefono.isBlank()) {
            throw new IllegalArgumentException("El teléfono es obligatorio.");
        }
        this.telefono = telefono.trim();
    }

    public String getTelefono() {
        return telefono;
    }

    @Override
    public String toString() {
        return String.format("Funcionario[id=%s, nombre='%s', telefono='%s', firstLog=%s]",
                getId(), getName(), telefono, isFirstLog());
    }
}