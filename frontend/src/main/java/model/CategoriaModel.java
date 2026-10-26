package model;

import contrato.IServicioAplicacion;
import dto.CategoriaDTO;

import java.util.List;

/** Fachada de categorías: solo delega en el contrato remoto. */
public class CategoriaModel {

    private final IServicioAplicacion servicio;

    public CategoriaModel(IServicioAplicacion servicio) {
        this.servicio = servicio;
    }

    public CategoriaDTO registrar(String descripcion) {
        return servicio.registrarCategoria(descripcion);
    }

    public void actualizar(String id, String descripcion) {
        servicio.actualizarCategoria(new CategoriaDTO(id, descripcion));
    }

    public void eliminar(String id) {
        servicio.eliminarCategoria(id);
    }

    public CategoriaDTO buscar(String id) {
        return servicio.buscarCategoria(id);
    }

    public List<CategoriaDTO> listar() {
        return servicio.listarCategorias();
    }
}
