package controller;

import model.FuncionarioModel;
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
        dialog.setOnGuardar(datos -> {
            try {
                modelo.registrarFuncionario(datos[1], datos[0], datos[2]);
                dialog.cerrar();
                view.mostrarMensaje("Funcionario registrado.");
                cargarDatos();
            } catch (Exception e) {
                dialog.mostrarError(e.getMessage());
            }
        });
        dialog.setVisible(true);
    }

    private void abrirDialogoModificar(int filaModelo) {
        String[] datos = view.getDatosFila(filaModelo);
        FuncionarioDialog dialog = new FuncionarioDialog(getFrame(), true, datos[0], datos[1], datos[2]);
        dialog.setOnGuardar(nuevos -> {
            try {
                modelo.actualizarFuncionario(nuevos[0], nuevos[1], nuevos[2]);
                dialog.cerrar();
                view.mostrarMensaje("Funcionario modificado.");
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
        if (!view.confirmarAccion("Desea eliminar al funcionario " + id + "?")) {
            return;
        }
        try {
            modelo.eliminarFuncionario(id);
            view.mostrarMensaje("Funcionario eliminado.");
            cargarDatos();
        } catch (Exception e) {
            view.mostrarError(e.getMessage());
        }
    }

    private void cargarDatos() {
        try {
            view.cargarDatos(modelo.listarFuncionarios());
        } catch (Exception e) {
            view.mostrarError(e.getMessage());
        }
    }
}
