package controller;

import model.Funcionario;
import service.FuncionarioService;
import view.FuncionarioPanel;

import java.util.List;

public class FuncionarioController {

    private final FuncionarioPanel view;
    private final FuncionarioService service;

    public FuncionarioController(FuncionarioPanel view, FuncionarioService service) {
        this.view = view;
        this.service = service;

        view.setOnAgregar(this::agregar);
        view.setOnModificar(this::modificar);
        view.setOnEliminar(this::eliminar);
        view.setOnBuscar(this::buscar);
        view.setOnMostrarTodos(this::cargarDatos);
        view.setOnLimpiar(this::limpiar);

        cargarDatos();
    }

    private void agregar() {
        String id = view.getId();
        String nombre = view.getNombre();
        String telefono = view.getTelefono();

        if (id.isBlank() || nombre.isBlank() || telefono.isBlank()) {
            view.mostrarError("Todos los campos son obligatorios.");
            return;
        }
        try {
            service.registrarFuncionario(nombre, id, telefono);
            view.mostrarMensaje("Funcionario registrado.");
            limpiar();
        } catch (Exception e) {
            view.mostrarError(e.getMessage());
        }
    }

    private void modificar() {
        String id = view.getId();
        String nombre = view.getNombre();
        String telefono = view.getTelefono();

        if (id.isBlank() || nombre.isBlank() || telefono.isBlank()) {
            view.mostrarError("Todos los campos son obligatorios.");
            return;
        }
        try {
            service.actualizarFuncionario(id, nombre, telefono);
            view.mostrarMensaje("Funcionario modificado.");
            limpiar();
        } catch (Exception e) {
            view.mostrarError(e.getMessage());
        }
    }

    private void eliminar() {
        String id = view.getId();
        if (id.isBlank()) {
            view.mostrarError("Seleccione un funcionario para eliminar.");
            return;
        }
        if (!view.confirmarAccion("Desea eliminar al funcionario " + id + "?")) {
            return;
        }
        try {
            service.eliminarFuncionario(id);
            view.mostrarMensaje("Funcionario eliminado.");
            limpiar();
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

    private void limpiar() {
        view.limpiar();
        cargarDatos();
    }

    private void cargarDatos() {
        try {
            view.cargarDatos(service.listarFuncionarios());
        } catch (Exception e) {
            view.mostrarError(e.getMessage());
        }
    }
}
