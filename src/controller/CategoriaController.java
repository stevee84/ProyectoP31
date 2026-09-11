package controller;

import model.CategoriaRecurso;
import service.CategoriaService;
import view.CategoriasPanel;

import java.util.List;

public class CategoriaController {

    private final CategoriasPanel view;
    private final CategoriaService service;

    public CategoriaController(CategoriasPanel view, CategoriaService service) {
        this.view = view;
        this.service = service;

        view.setOnGuardar(this::guardar);
        view.setOnBorrar(this::borrar);
        view.setOnBuscar(this::buscar);
        view.setOnLimpiar(this::limpiar);

        cargarDatos();
    }

    private void guardar() {
        String id = view.getId();
        String desc = view.getDescripcion();
        if (desc.isBlank()) {
            view.mostrarError("Debe escribir una descripcion.");
            return;
        }
        try {
            if (id.isBlank()) {
                service.registrar(desc);
                view.mostrarMensaje("Categoria registrada.");
            } else {
                service.actualizar(id, desc);
                view.mostrarMensaje("Categoria modificada.");
            }
            limpiar();
        } catch (Exception e) {
            view.mostrarError(e.getMessage());
        }
    }

    private void borrar() {
        String id = view.getId();
        if (id.isBlank()) {
            view.mostrarError("Seleccione una categoria para borrar.");
            return;
        }
        if (!view.confirmarAccion("Desea eliminar la categoria " + id + "?")) {
            return;
        }
        try {
            service.eliminar(id);
            view.mostrarMensaje("Categoria eliminada.");
            limpiar();
        } catch (Exception e) {
            view.mostrarError(e.getMessage());
        }
    }

    private void buscar() {
        String texto = view.getBusqueda();
        try {
            List<CategoriaRecurso> resultado;
            if (texto.isBlank()) {
                resultado = service.listar();
            } else {
                resultado = service.buscarPorDescripcion(texto);
            }
            view.cargarDatos(resultado);
        } catch (Exception e) {
            view.mostrarError(e.getMessage());
        }
    }

    private void limpiar() {
        view.limpiar();
        cargarDatos();
    }

    private void cargarDatos() {
        try {
            view.cargarDatos(service.listar());
        } catch (Exception e) {
            view.mostrarError(e.getMessage());
        }
    }
}
