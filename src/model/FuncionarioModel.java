package model;

import service.FuncionarioService;

import java.util.List;

/**
 * Fachada de la capa Model para funcionarios. El Controller solo conoce esta
 * clase, nunca importa "service" directamente.
 */
public class FuncionarioModel {

    private final FuncionarioService servicio;

    public FuncionarioModel(FuncionarioService servicio) {
        this.servicio = servicio;
    }

    public boolean registrarFuncionario(String nombre, String id, String telefono) {
        return servicio.registrarFuncionario(nombre, id, telefono);
    }

    public boolean actualizarFuncionario(String id, String nombre, String telefono) {
        return servicio.actualizarFuncionario(id, nombre, telefono);
    }

    public boolean eliminarFuncionario(String id) {
        return servicio.eliminarFuncionario(id);
    }

    public Funcionario buscarFuncionario(String id) {
        return servicio.buscarFuncionario(id);
    }

    public List<Funcionario> listarFuncionarios() {
        return servicio.listarFuncionarios();
    }
}
