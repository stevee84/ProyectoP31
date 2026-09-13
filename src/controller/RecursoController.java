package controller;

import model.CategoriaModel;
import model.CategoriaRecurso;
import model.RecursoModel;
import view.RecursoDialog;
import view.RecursosPanel;

import javax.swing.SwingUtilities;
import java.awt.Frame;
import java.util.List;

public class RecursoController {

    private final RecursosPanel view;
    private final RecursoModel modelo;
    private final CategoriaModel categoriaModelo;

    public RecursoController(RecursosPanel view, RecursoModel modelo, CategoriaModel categoriaModelo) {
        this.view = view;
        this.modelo = modelo;
        this.categoriaModelo = categoriaModelo;

        view.setOnAbrirAgregar(this::abrirDialogoAgregar);
        view.setOnAbrirModificar(args -> {
            if (args.length > 0) abrirDialogoModificar(args[0]);
        });
        view.setOnEliminar(this::eliminar);
        view.setOnFiltrar(this::filtrar);
        view.setOnVisible(this::cargarCategorias);

        cargarCategorias();
        cargarRecursos();
    }

    private Frame getFrame() {
        return (Frame) SwingUtilities.getWindowAncestor(view);
    }

    private List<CategoriaRecurso> obtenerCategorias() {
        return categoriaModelo.listar();
    }

    private void abrirDialogoAgregar() {
        RecursoDialog dialog = new RecursoDialog(getFrame(), false,
                "", null, "", obtenerCategorias());
        dialog.setOnGuardar(datos -> {
            try {
                boolean ok = modelo.registrar(datos[0], datos[1], datos[2]);
                if (ok) {
                    dialog.cerrar();
                    view.mostrarMensaje("Recurso registrado.");
                    cargarRecursos();
                } else {
                    dialog.mostrarError("No se pudo registrar (codigo duplicado o categoria inexistente).");
                }
            } catch (Exception e) {
                dialog.mostrarError(e.getMessage());
            }
        });
        dialog.setVisible(true);
    }

    private void abrirDialogoModificar(int filaModelo) {
        Object[] datos = view.getDatosFila(filaModelo);
        String codigo = String.valueOf(datos[0]);
        CategoriaRecurso cat = (CategoriaRecurso) datos[1];
        String desc = String.valueOf(datos[2]);

        RecursoDialog dialog = new RecursoDialog(getFrame(), true,
                codigo, cat, desc, obtenerCategorias());
        dialog.setOnGuardar(nuevos -> {
            try {
                boolean ok = modelo.actualizar(nuevos[0], nuevos[1], nuevos[2]);
                if (ok) {
                    dialog.cerrar();
                    view.mostrarMensaje("Recurso modificado.");
                    cargarRecursos();
                } else {
                    dialog.mostrarError("No se pudo modificar el recurso.");
                }
            } catch (Exception e) {
                dialog.mostrarError(e.getMessage());
            }
        });
        dialog.setVisible(true);
    }

    private void eliminar(int filaModelo) {
        Object[] datos = view.getDatosFila(filaModelo);
        String codigo = String.valueOf(datos[0]);
        if (!view.confirmarAccion("Desea eliminar el recurso " + codigo + "?")) {
            return;
        }
        try {
            boolean ok = modelo.eliminar(codigo);
            if (ok) {
                view.mostrarMensaje("Recurso eliminado.");
            } else {
                view.mostrarError("No se pudo eliminar el recurso.");
            }
            cargarRecursos();
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
                view.cargarRecursos(modelo.listarPorCategoria(cat.getId()));
            }
        } catch (Exception e) {
            view.mostrarError(e.getMessage());
        }
    }

    private void cargarCategorias() {
        try {
            view.cargarCategorias(categoriaModelo.listar());
        } catch (Exception e) {
            view.mostrarError(e.getMessage());
        }
    }

    private void cargarRecursos() {
        try {
            view.cargarRecursos(modelo.listar());
        } catch (Exception e) {
            view.mostrarError(e.getMessage());
        }
    }
}
