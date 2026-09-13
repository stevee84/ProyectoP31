package model;

import service.CategoriaService;

import java.util.List;

/**
 * Fachada de la capa Model para categorias. El Controller solo conoce esta
 * clase, nunca importa "service" directamente.
 */
public class CategoriaModel {

    private final CategoriaService servicio;

    public CategoriaModel(CategoriaService servicio) {
        this.servicio = servicio;
    }

    public CategoriaRecurso registrar(String descripcion) {
        return servicio.registrar(descripcion);
    }

    public boolean actualizar(String id, String descripcion) {
        return servicio.actualizar(id, descripcion);
    }

    public boolean eliminar(String id) {
        return servicio.eliminar(id);
    }

    public CategoriaRecurso buscar(String id) {
        return servicio.buscar(id);
    }

    public List<CategoriaRecurso> listar() {
        return servicio.listar();
    }
}
