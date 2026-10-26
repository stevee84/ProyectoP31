package model;

import contrato.IServicioAplicacion;
import dto.RecursoDTO;

import java.util.List;

/** Fachada de recursos: solo delega en el contrato remoto. */
public class RecursoModel {

    private final IServicioAplicacion servicio;

    public RecursoModel(IServicioAplicacion servicio) {
        this.servicio = servicio;
    }

    public void registrar(String codigo, String idCategoria, String descripcion) {
        servicio.registrarRecurso(codigo, idCategoria, descripcion);
    }

    public void actualizar(String codigo, String idCategoria, String descripcion) {
        servicio.actualizarRecurso(codigo, idCategoria, descripcion);
    }

    public void eliminar(String codigo) {
        servicio.eliminarRecurso(codigo);
    }

    public RecursoDTO buscar(String codigo) {
        return servicio.buscarRecurso(codigo);
    }

    /** idCategoria null significa "todas las categorías". */
    public List<RecursoDTO> listar(String idCategoria) {
        return idCategoria == null ? servicio.listarRecursos() : servicio.listarRecursosPorCategoria(idCategoria);
    }
}
