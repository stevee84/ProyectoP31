package model;

import contrato.IServicioAplicacion;
import dto.FuncionarioDTO;

import java.util.List;

/** Fachada de funcionarios: solo delega en el contrato remoto. */
public class FuncionarioModel {

    private final IServicioAplicacion servicio;

    public FuncionarioModel(IServicioAplicacion servicio) {
        this.servicio = servicio;
    }

    public void registrar(String id, String nombre, String telefono) {
        servicio.registrarFuncionario(new FuncionarioDTO(id, nombre, telefono));
    }

    public void actualizar(String id, String nombre, String telefono) {
        servicio.actualizarFuncionario(new FuncionarioDTO(id, nombre, telefono));
    }

    public void eliminar(String id) {
        servicio.eliminarFuncionario(id);
    }

    public FuncionarioDTO buscar(String id) {
        return servicio.buscarFuncionario(id);
    }

    public List<FuncionarioDTO> listar() {
        return servicio.listarFuncionarios();
    }
}
