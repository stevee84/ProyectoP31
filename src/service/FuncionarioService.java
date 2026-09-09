package service;

import exception.RecursoEnUsoException;
import exception.ValidacionException;
import model.Administrador;
import model.Empleado;
import model.Funcionario;
import model.ModeloReservaciones;
import model.Reservacion;
import repository.PersistenciaXml;

import java.util.List;

public class FuncionarioService {

    private final ModeloReservaciones modelo;
    private PersistenciaXml persistencia;
    private final SesionService sesionService;

    public FuncionarioService(ModeloReservaciones modelo, SesionService sesionService) {
        this.modelo = modelo;
        this.sesionService = sesionService;
    }

    public FuncionarioService(ModeloReservaciones modelo, PersistenciaXml persistencia, SesionService sesionService) {
        this.modelo = modelo;
        this.persistencia = persistencia;
        this.sesionService = sesionService;
    }

    private void guardarDatos() {
        if (persistencia != null) {
            try {
                persistencia.guardarTodo();
            } catch (Exception e) {
                System.err.println("Error al guardar datos: " + e.getMessage());
            }
        }
    }

    public boolean registrarAdministrador(String nombre, String id) {
        sesionService.requireAdmin();
        boolean ok = modelo.registrarEmpleado(new Administrador(nombre, id));
        if (ok) guardarDatos();
        return ok;
    }

    public boolean registrarFuncionario(String nombre, String id, String telefono) {
        sesionService.requireAdmin();
        boolean ok = modelo.registrarEmpleado(new Funcionario(nombre, id, telefono));
        if (!ok) {
            throw new ValidacionException("Identificación duplicada.");
        }
        guardarDatos();
        return true;
    }

    public boolean actualizarFuncionario(String id, String nombre, String telefono) {
        sesionService.requireAdmin();
        Funcionario funcionario = buscarFuncionarioOInvalido(id);
        funcionario.actualizarDatos(nombre, id, telefono);
        guardarDatos();
        return true;
    }

    public boolean eliminarFuncionario(String id) {
        sesionService.requireAdmin();
        boolean tieneReservacionesActivas = listarReservacionesPorEmpleado(id).stream()
                .anyMatch(Reservacion::esActiva);
        if (tieneReservacionesActivas) {
            throw new RecursoEnUsoException("El funcionario tiene reservaciones activas.");
        }
        boolean ok = modelo.eliminarEmpleado(id);
        if (ok) guardarDatos();
        return ok;
    }

    public Funcionario buscarFuncionario(String id) {
        sesionService.validarSesion();
        return buscarFuncionarioOInvalido(id);
    }

    public List<Funcionario> listarFuncionarios() {
        sesionService.validarSesion();
        return modelo.listarFuncionarios();
    }

    public List<Funcionario> buscarPorTexto(String texto) {
        sesionService.validarSesion();
        return modelo.buscarFuncionariosPorTexto(texto);
    }

    private List<Reservacion> listarReservacionesPorEmpleado(String id) {
        Empleado empleado = modelo.buscarEmpleado(id);
        if (empleado == null) {
            return List.of();
        }
        return modelo.listarReservacionesPorEmpleado(empleado);
    }

    private Funcionario buscarFuncionarioOInvalido(String id) {
        Funcionario funcionario = modelo.buscarEmpleado(id) instanceof Funcionario f ? f : null;
        if (funcionario == null) {
            throw new ValidacionException("No existe un funcionario con id " + id + ".");
        }
        return funcionario;
    }
}
