package controller;

import model.Administrador;
import model.CategoriaRecurso;

import java.util.List;

public class CategoriaController {

    private final ControladorReservaciones controladorGeneral;

    public CategoriaController(ControladorReservaciones controladorGeneral) {
        if (controladorGeneral == null) {
            throw new IllegalArgumentException("El controlador general es obligatorio.");
        }
        this.controladorGeneral = controladorGeneral;
    }

    public List<CategoriaRecurso> listar() {
        validarAdministrador();
        return controladorGeneral.listarCategorias();
    }

    public List<CategoriaRecurso> buscarPorDescripcion(String descripcion) {
        validarAdministrador();
        String texto = descripcion == null ? "" : descripcion.trim();
        return controladorGeneral.buscarCategoriasPorDescripcion(texto);
    }

    public CategoriaRecurso guardar(String id, String descripcion) {
        validarAdministrador();

        if (id == null || id.isBlank()) {
            return controladorGeneral.registrarCategoria(descripcion);
        }

        boolean actualizada = controladorGeneral.actualizarCategoria(id, descripcion);
        if (!actualizada) {
            throw new IllegalArgumentException("No existe una categoría con id " + id + ".");
        }
        return controladorGeneral.buscarCategoria(id);
    }

    public void eliminar(String id) {
        validarAdministrador();

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar una categoría.");
        }
        if (!controladorGeneral.eliminarCategoria(id)) {
            throw new IllegalArgumentException("No existe una categoría con id " + id + ".");
        }
    }

    private void validarAdministrador() {
        if (!(controladorGeneral.getSesionActual() instanceof Administrador)) {
            throw new IllegalStateException(
                    "Esta funcionalidad solo puede ejecutarla un administrador."
            );
        }
    }
}
