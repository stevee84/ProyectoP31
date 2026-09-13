package controller;

import model.CategoriaModel;
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
        dialog.setOnGuardar(datos -> {
            try {
                modelo.registrar(datos[1]);
                dialog.cerrar();
                view.mostrarMensaje("Categoria registrada.");
                cargarDatos();
            } catch (Exception e) {
                dialog.mostrarError(e.getMessage());
            }
        });
        dialog.setVisible(true);
    }

    private void abrirDialogoModificar(int filaModelo) {
        String[] datos = view.getDatosFila(filaModelo);
        CategoriaDialog dialog = new CategoriaDialog(getFrame(), true, datos[0], datos[1]);
        dialog.setOnGuardar(nuevos -> {
            try {
                modelo.actualizar(nuevos[0], nuevos[1]);
                dialog.cerrar();
                view.mostrarMensaje("Categoria modificada.");
                cargarDatos();
            } catch (Exception e) {
                dialog.mostrarError(e.getMessage());
            }
        });
        dialog.setVisible(true);
    }

    private void eliminar(int filaModelo) {
        String[] datos = view.getDatosFila(filaModelo);
        String id = datos[0];
        if (!view.confirmarAccion("Desea eliminar la categoria " + id + "?")) {
            return;
        }
        try {
            modelo.eliminar(id);
            view.mostrarMensaje("Categoria eliminada.");
            cargarDatos();
        } catch (Exception e) {
            view.mostrarError(e.getMessage());
        }
    }

    private void cargarDatos() {
        try {
            view.cargarDatos(modelo.listar());
        } catch (Exception e) {
            view.mostrarError(e.getMessage());
        }
    }
}
