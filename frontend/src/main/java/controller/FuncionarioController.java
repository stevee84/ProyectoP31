package controller;

import model.FuncionarioModel;
import util.Tarea;
import view.FuncionarioDialog;
import view.FuncionarioPanel;

import javax.swing.SwingUtilities;
import java.awt.Frame;

public class FuncionarioController {

    private final FuncionarioPanel view;
    private final FuncionarioModel modelo;

    public FuncionarioController(FuncionarioPanel view, FuncionarioModel modelo) {
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
        FuncionarioDialog dialog = new FuncionarioDialog(getFrame(), false, "", "", "");
        // datos = {id, nombre, telefono}
        dialog.setOnGuardar(datos -> Tarea.ejecutar(
                () -> { modelo.registrar(datos[0], datos[1], datos[2]); return null; },
                r -> {
                    dialog.cerrar();
                    view.mostrarMensaje("Funcionario registrado. Su clave inicial es su identificacion.");
                    cargarDatos();
                },
                e -> dialog.mostrarError(e.getMessage())));
        dialog.setVisible(true);
    }

    private void abrirDialogoModificar(int filaModelo) {
        String[] datos = view.getDatosFila(filaModelo);
        FuncionarioDialog dialog = new FuncionarioDialog(getFrame(), true, datos[0], datos[1], datos[2]);
        dialog.setOnGuardar(nuevos -> Tarea.ejecutar(
                () -> { modelo.actualizar(nuevos[0], nuevos[1], nuevos[2]); return null; },
                r -> {
                    dialog.cerrar();
                    view.mostrarMensaje("Funcionario modificado.");
                    cargarDatos();
                },
                e -> dialog.mostrarError(e.getMessage())));
        dialog.setVisible(true);
    }

    private void eliminar(int filaModelo) {
        String id = view.getDatosFila(filaModelo)[0];
        if (!view.confirmarAccion("Desea eliminar al funcionario " + id + "?")) {
            return;
        }
        Tarea.ejecutar(() -> { modelo.eliminar(id); return null; },
                r -> {
                    view.mostrarMensaje("Funcionario eliminado.");
                    cargarDatos();
                },
                e -> view.mostrarError(e.getMessage()));
    }

    private void cargarDatos() {
        Tarea.ejecutar(modelo::listar, view::cargarDatos, e -> view.mostrarError(e.getMessage()));
    }
}
