package model;

import service.RecursoService;

import java.util.List;

/**
 * Fachada de la capa Model para recursos. El Controller solo conoce esta
 * clase, nunca importa "service" directamente.
 */
public class RecursoModel {

    private final RecursoService servicio;

    public RecursoModel(RecursoService servicio) {
        this.servicio = servicio;
    }

    public boolean registrar(String codigo, String idCategoria, String descripcion) {
        return servicio.registrar(codigo, idCategoria, descripcion);
    }

    public boolean actualizar(String codigo, String idCategoria, String descripcion) {
        return servicio.actualizar(codigo, idCategoria, descripcion);
    }

    public boolean eliminar(String codigo) {
        return servicio.eliminar(codigo);
    }

    public Recurso buscar(String codigo) {
        return servicio.buscar(codigo);
    }

    public List<Recurso> listar() {
        return servicio.listar();
    }

    public List<Recurso> listarPorCategoria(String idCategoria) {
        return servicio.listarPorCategoria(idCategoria);
    }
}
