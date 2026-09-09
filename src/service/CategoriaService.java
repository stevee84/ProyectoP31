package service;

import exception.ValidacionException;
import model.CategoriaRecurso;
import model.ModeloReservaciones;
import repository.PersistenciaXml;

import java.util.List;

public class CategoriaService {

    private final ModeloReservaciones modelo;
    private PersistenciaXml persistencia;
    private final SesionService sesionService;

    public CategoriaService(ModeloReservaciones modelo, SesionService sesionService) {
        this.modelo = modelo;
        this.sesionService = sesionService;
    }

    public CategoriaService(ModeloReservaciones modelo, PersistenciaXml persistencia, SesionService sesionService) {
        this.modelo = modelo;
        this.persistencia = persistencia;
        this.sesionService = sesionService;
    }

    private void guardarDatos() {
        if (persistencia != null) {
            try {
                persistencia.guardarTodo();
            } catch (Exception e) {
                System.err.println("Error al guardar datos: " + e.getMessage());
            }
        }
    }

    public CategoriaRecurso registrar(String descripcion) {
        sesionService.requireAdmin();
        CategoriaRecurso cat = modelo.registrarCategoria(descripcion);
        guardarDatos();
        return cat;
    }

    public boolean actualizar(String id, String descripcion) {
        sesionService.requireAdmin();
        boolean ok = modelo.actualizarCategoria(id, descripcion);
        if (ok) guardarDatos();
        return ok;
    }

    public boolean eliminar(String id) {
        sesionService.requireAdmin();
        boolean ok = modelo.eliminarCategoria(id);
        if (ok) guardarDatos();
        return ok;
    }

    public CategoriaRecurso buscar(String id) {
        sesionService.validarSesion();
        CategoriaRecurso categoria = modelo.buscarCategoria(id);
        if (categoria == null) {
            throw new ValidacionException("No existe una categoría con id " + id + ".");
        }
        return categoria;
    }

    public List<CategoriaRecurso> listar() {
        sesionService.validarSesion();
        return modelo.listarCategorias();
    }

    public List<CategoriaRecurso> buscarPorDescripcion(String texto) {
        sesionService.validarSesion();
        return modelo.buscarCategoriasPorDescripcion(texto);
    }
}
