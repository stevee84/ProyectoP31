package controller;

import model.CategoriaRecurso;
import model.Recurso;

import java.util.List;

public class RecursoController {

    private ControladorReservaciones controlador;

    public RecursoController(
            ControladorReservaciones controlador
    ) {
        this.controlador = controlador;
    }

    public List<CategoriaRecurso> listarCategorias() {
        return controlador.listarCategorias();
    }

    public List<Recurso> listarRecursos() {
        return controlador.listarRecursos();
    }

    public List<Recurso> listarRecursosPorCategoria(
            String idCategoria
    ) {
        return controlador.listarRecursosPorCategoria(
                idCategoria
        );
    }

    public boolean registrarRecurso(
            String codigo,
            String idCategoria,
            String descripcion
    ) {
        return controlador.registrarRecurso(
                codigo,
                idCategoria,
                descripcion
        );
    }

    public boolean modificarRecurso(
            String codigo,
            String idCategoria,
            String descripcion
    ) {
        return controlador.actualizarRecurso(
                codigo,
                idCategoria,
                descripcion
        );
    }

    public boolean eliminarRecurso(String codigo) {
        return controlador.eliminarRecurso(codigo);
    }
}
