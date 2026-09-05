package controller;

import model.CategoriaRecurso;
import java.util.List;

public class CategoriaController {

    private ControladorReservaciones controlador;

    public CategoriaController(ControladorReservaciones controlador) {
        this.controlador = controlador;
    }

    public List<CategoriaRecurso> listarCategorias() {
        return controlador.listarCategorias();
    }

    public List<CategoriaRecurso> buscarCategorias(String descripcion) {
        return controlador.buscarCategoriasPorDescripcion(descripcion);
    }

    public CategoriaRecurso registrarCategoria(String descripcion) {
        return controlador.registrarCategoria(descripcion);
    }

    public boolean modificarCategoria(String id, String descripcion) {
        return controlador.actualizarCategoria(id, descripcion);
    }

    public boolean eliminarCategoria(String id) {
        return controlador.eliminarCategoria(id);
    }
}