package service;

import exception.ValidacionException;
import model.ModeloReservaciones;
import model.Recurso;
import repository.PersistenciaXml;

import java.util.List;

public class RecursoService {

    private final ModeloReservaciones modelo;
    private PersistenciaXml persistencia;
    private final SesionService sesionService;

    public RecursoService(ModeloReservaciones modelo, SesionService sesionService) {
        this.modelo = modelo;
        this.sesionService = sesionService;
    }

    public RecursoService(ModeloReservaciones modelo, PersistenciaXml persistencia, SesionService sesionService) {
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

    public boolean registrar(String codigo, String idCategoria, String descripcion) {
        sesionService.requireAdmin();
        boolean ok = modelo.registrarRecurso(codigo, idCategoria, descripcion);
        if (ok) guardarDatos();
        return ok;
    }

    public boolean actualizar(String codigo, String idCategoria, String descripcion) {
        sesionService.requireAdmin();
        boolean ok = modelo.actualizarRecurso(codigo, idCategoria, descripcion);
        if (ok) guardarDatos();
        return ok;
    }

    public boolean eliminar(String codigo) {
        sesionService.requireAdmin();
        boolean ok = modelo.eliminarRecurso(codigo);
        if (ok) guardarDatos();
        return ok;
    }

    public Recurso buscar(String codigo) {
        sesionService.validarSesion();
        Recurso recurso = modelo.buscarRecurso(codigo);
        if (recurso == null) {
            throw new ValidacionException("No existe un recurso con código " + codigo + ".");
        }
        return recurso;
    }

    public List<Recurso> listar() {
        sesionService.validarSesion();
        return modelo.listarRecursos();
    }

    public List<Recurso> listarPorCategoria(String idCategoria) {
        sesionService.validarSesion();
        return modelo.listarRecursosPorCategoria(idCategoria);
    }
}
