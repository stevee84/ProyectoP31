package controller;

import model.CategoriaModel;
import util.Tarea;
import view.CategoriaDialog;
import view.CategoriasPanel;

import javax.swing.SwingUtilities;
import java.awt.Frame;

public class CategoriaController {

    private final CategoriasPanel view;
    private final CategoriaModel modelo;

    public CategoriaController(CategoriasPanel view, CategoriaModel modelo) {
        this.view = view;
        this.modelo = modelo;

        view.setOnAbrirAgregar(this::abrirDialogoAgregar);
        view.setOnAbrirModificar(args -> {
            if (args.length > 0) abrirDialogoModificar(args[0]);
        });
        view.setOnEliminar(this::eliminar);

        cargarDatos();
    }

    private Frame getFrame() {
        return (Frame) SwingUtilities.getWindowAncestor(view);
    }

    private void abrirDialogoAgregar() {
        CategoriaDialog dialog = new CategoriaDialog(getFrame(), false, null, "");
        dialog.setOnGuardar(datos -> Tarea.ejecutar(
                () -> modelo.registrar(datos[1]),
                creada -> {
                    dialog.cerrar();
                    view.mostrarMensaje("Categoria registrada con id " + creada.id() + ".");
                    cargarDatos();
                },
                e -> dialog.mostrarError(e.getMessage())));
        dialog.setVisible(true);
    }

    private void abrirDialogoModificar(int filaModelo) {
        String[] datos = view.getDatosFila(filaModelo);
        CategoriaDialog dialog = new CategoriaDialog(getFrame(), true, datos[0], datos[1]);
        dialog.setOnGuardar(nuevos -> Tarea.ejecutar(
                () -> { modelo.actualizar(nuevos[0], nuevos[1]); return null; },
                r -> {
                    dialog.cerrar();
                    view.mostrarMensaje("Categoria modificada.");
                    cargarDatos();
                },
                e -> dialog.mostrarError(e.getMessage())));
        dialog.setVisible(true);
    }

    private void eliminar(int filaModelo) {
        String id = view.getDatosFila(filaModelo)[0];
        if (!view.confirmarAccion("Desea eliminar la categoria " + id + "?")) {
            return;
        }
        Tarea.ejecutar(() -> { modelo.eliminar(id); return null; },
                r -> {
                    view.mostrarMensaje("Categoria eliminada.");
                    cargarDatos();
                },
                e -> view.mostrarError(e.getMessage()));
    }

    private void cargarDatos() {
        Tarea.ejecutar(modelo::listar, view::cargarDatos, e -> view.mostrarError(e.getMessage()));
    }
}
