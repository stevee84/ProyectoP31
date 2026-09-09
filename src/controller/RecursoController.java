package controller;

import model.CategoriaRecurso;
import model.Recurso;
import service.CategoriaService;
import service.RecursoService;
import view.RecursosPanel;

import java.util.List;

public class RecursoController {

    private final RecursosPanel view;
    private final RecursoService service;
    private final CategoriaService categoriaService;

    public RecursoController(RecursosPanel view, RecursoService service, CategoriaService categoriaService) {
        this.view = view;
        this.service = service;
        this.categoriaService = categoriaService;

        view.setOnGuardar(this::guardar);
        view.setOnBorrar(this::borrar);
        view.setOnFiltrar(this::filtrar);
        view.setOnLimpiar(this::limpiar);
        view.setOnVisible(this::cargarCategorias);
        view.setOnNuevo(this::nuevo);
        view.setOnSeleccionar(this::seleccionar);

        cargarCategorias();
        cargarRecursos();
    }

    private void nuevo() {
        // La vista ya limpia los campos y habilita la edicion.
        // Este hook queda disponible para logica adicional del controlador.
    }

    private void seleccionar() {
        // La vista ya carga los datos de la fila seleccionada en los campos.
        // Este hook queda disponible para logica adicional del controlador.
    }

    private void guardar() {
        String codigo = view.getCodigo();
        CategoriaRecurso cat = view.getCategoriaSeleccionada();
        String desc = view.getDescripcion();

        if (codigo.isBlank()) {
            view.mostrarError("El codigo es obligatorio.");
            return;
        }
        if (cat == null) {
            view.mostrarError("Seleccione una categoria.");
            return;
        }
        if (desc.isBlank()) {
            view.mostrarError("La descripcion es obligatoria.");
            return;
        }
        try {
            if (view.getFilaSeleccionada() == -1) {
                boolean ok = service.registrar(codigo, cat.getId(), desc);
                if (ok) {
                    view.mostrarMensaje("Recurso registrado.");
                } else {
                    view.mostrarError("No se pudo registrar el recurso (codigo duplicado o categoria inexistente).");
                }
            } else {
                boolean ok = service.actualizar(codigo, cat.getId(), desc);
                if (ok) {
                    view.mostrarMensaje("Recurso modificado.");
                } else {
                    view.mostrarError("No se pudo modificar el recurso.");
                }
            }
            limpiar();
        } catch (Exception e) {
            view.mostrarError(e.getMessage());
        }
    }

    private void borrar() {
        String codigo = view.getCodigo();
        if (codigo.isBlank()) {
            view.mostrarError("Seleccione un recurso para borrar.");
            return;
        }
        if (!view.confirmarAccion("Desea eliminar el recurso " + codigo + "?")) {
            return;
        }
        try {
            boolean ok = service.eliminar(codigo);
            if (ok) {
                view.mostrarMensaje("Recurso eliminado.");
            } else {
                view.mostrarError("No se pudo eliminar el recurso.");
            }
            limpiar();
        } catch (Exception e) {
            view.mostrarError(e.getMessage());
        }
    }

    private void filtrar() {
        try {
            CategoriaRecurso cat = view.getCategoriaFiltro();
            if (cat == null) {
                cargarRecursos();
            } else {
                view.cargarRecursos(service.listarPorCategoria(cat.getId()));
            }
        } catch (Exception e) {
            view.mostrarError(e.getMessage());
        }
    }

    private void limpiar() {
        view.limpiar();
        cargarRecursos();
    }

    private void cargarCategorias() {
        try {
            view.cargarCategorias(categoriaService.listar());
        } catch (Exception e) {
            view.mostrarError(e.getMessage());
        }
    }

    private void cargarRecursos() {
        try {
            view.cargarRecursos(service.listar());
        } catch (Exception e) {
            view.mostrarError(e.getMessage());
        }
    }
}
