package controller;

import service.CategoriaService;
import view.CategoriaDialog;
import view.CategoriasPanel;

import javax.swing.SwingUtilities;
import java.awt.Frame;

public class CategoriaController {

    private final CategoriasPanel view;
    private final CategoriaService service;

    public CategoriaController(CategoriasPanel view, CategoriaService service) {
        this.view = view;
        this.service = service;

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
                service.registrar(datos[1]);
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
                service.actualizar(nuevos[0], nuevos[1]);
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
            service.eliminar(id);
            view.mostrarMensaje("Categoria eliminada.");
            cargarDatos();
        } catch (Exception e) {
            view.mostrarError(e.getMessage());
        }
    }

    private void cargarDatos() {
        try {
            view.cargarDatos(service.listar());
        } catch (Exception e) {
            view.mostrarError(e.getMessage());
        }
    }
}
