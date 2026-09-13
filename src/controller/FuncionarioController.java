package controller;

import model.Funcionario;
import service.FuncionarioService;
import view.FuncionarioDialog;
import view.FuncionarioPanel;

import javax.swing.SwingUtilities;
import java.awt.Frame;
import java.util.List;

public class FuncionarioController {

    private final FuncionarioPanel view;
    private final FuncionarioService service;

    public FuncionarioController(FuncionarioPanel view, FuncionarioService service) {
        this.view = view;
        this.service = service;

        view.setOnAbrirAgregar(this::abrirDialogoAgregar);
        view.setOnAbrirModificar(args -> {
            if (args.length > 0) abrirDialogoModificar(args[0]);
        });
        view.setOnEliminar(this::eliminar);
        view.setOnBuscar(this::buscar);
        view.setOnMostrarTodos(this::cargarDatos);

        cargarDatos();
    }

    private Frame getFrame() {
        return (Frame) SwingUtilities.getWindowAncestor(view);
    }

    private void abrirDialogoAgregar() {
        FuncionarioDialog dialog = new FuncionarioDialog(getFrame(), false, "", "", "");
        dialog.setOnGuardar(datos -> {
            try {
                service.registrarFuncionario(datos[1], datos[0], datos[2]);
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
                service.actualizarFuncionario(nuevos[0], nuevos[1], nuevos[2]);
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
            service.eliminarFuncionario(id);
            view.mostrarMensaje("Funcionario eliminado.");
            cargarDatos();
        } catch (Exception e) {
            view.mostrarError(e.getMessage());
        }
    }

    private void buscar() {
        String texto = view.getBusqueda();
        try {
            List<Funcionario> resultado;
            if (texto.isBlank()) {
                resultado = service.listarFuncionarios();
            } else {
                resultado = service.buscarPorTexto(texto);
            }
            view.cargarDatos(resultado);
        } catch (Exception e) {
            view.mostrarError(e.getMessage());
        }
    }

    private void cargarDatos() {
        try {
            view.cargarDatos(service.listarFuncionarios());
        } catch (Exception e) {
            view.mostrarError(e.getMessage());
        }
    }
}
