package controller;

import model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ControladorReservaciones {

    private final ModeloReservaciones modelo;
    private Empleado sesionActual;

    public ControladorReservaciones() {
        this.modelo = new ModeloReservaciones();
    }

    public ResultadoSesion iniciarSesion(String id, String pass) {
        Empleado empleado = modelo.validarCredenciales(id, pass);
        if (empleado == null) {
            return new ResultadoSesion(null, false);
        }
        this.sesionActual = empleado;
        return new ResultadoSesion(empleado, empleado.isFirstLog());
    }

    public void cambiarContraseña(String nueva) {
        validarSesion();
        sesionActual.cambiarContraseña(nueva);
    }

    public void cerrarSesion() {
        this.sesionActual = null;
    }

    public Empleado getSesionActual() {
        return sesionActual;
    }

    public boolean registrarAdministrador(String nombre, String id) {
        requireAdmin();
        return modelo.registrarEmpleado(new Administrador(nombre, id));
    }

    public boolean registrarFuncionario(String nombre, String id, String telefono) {
        requireAdmin();
        return modelo.registrarEmpleado(new Funcionario(nombre, id, telefono));
    }

    public boolean actualizarFuncionario(String id, String nombre, String telefono) {
        requireAdmin();
        Funcionario funcionario = buscarFuncionarioOInvalido(id);
        funcionario.actualizarDatos(nombre, id, telefono);
        return true;
    }

    public boolean eliminarFuncionario(String id) {
        requireAdmin();
        return modelo.eliminarEmpleado(id);
    }

    public Funcionario buscarFuncionario(String id) {
        validarSesion();
        Funcionario funcionario = modelo.buscarEmpleado(id) instanceof Funcionario f ? f : null;
        if (funcionario == null) {
            throw new IllegalArgumentException("No existe un funcionario con id " + id + ".");
        }
        return funcionario;
    }

    public List<Funcionario> listarFuncionarios() {
        validarSesion();
        return modelo.listarFuncionarios();
    }

    public List<Funcionario> buscarFuncionariosPorTexto(String texto) {
        validarSesion();
        return modelo.buscarFuncionariosPorTexto(texto);
    }

    public CategoriaRecurso registrarCategoria(String descripcion) {
        requireAdmin();
        return modelo.registrarCategoria(descripcion);
    }

    public boolean actualizarCategoria(String id, String descripcion) {
        requireAdmin();
        return modelo.actualizarCategoria(id, descripcion);
    }

    public boolean eliminarCategoria(String id) {
        requireAdmin();
        return modelo.eliminarCategoria(id);
    }

    public CategoriaRecurso buscarCategoria(String id) {
        validarSesion();
        CategoriaRecurso categoria = modelo.buscarCategoria(id);
        if (categoria == null) {
            throw new IllegalArgumentException("No existe una categoría con id " + id + ".");
        }
        return categoria;
    }

    public List<CategoriaRecurso> listarCategorias() {
        validarSesion();
        return modelo.listarCategorias();
    }

    public List<CategoriaRecurso> buscarCategoriasPorDescripcion(String texto) {
        validarSesion();
        return modelo.buscarCategoriasPorDescripcion(texto);
    }

    public boolean registrarRecurso(String codigo, String idCategoria, String descripcion) {
        requireAdmin();
        return modelo.registrarRecurso(codigo, idCategoria, descripcion);
    }

    public boolean actualizarRecurso(String codigo, String idCategoria, String descripcion) {
        requireAdmin();
        return modelo.actualizarRecurso(codigo, idCategoria, descripcion);
    }

    public boolean eliminarRecurso(String codigo) {
        requireAdmin();
        return modelo.eliminarRecurso(codigo);
    }

    public Recurso buscarRecurso(String codigo) {
        validarSesion();
        Recurso recurso = modelo.buscarRecurso(codigo);
        if (recurso == null) {
            throw new IllegalArgumentException("No existe un recurso con código " + codigo + ".");
        }
        return recurso;
    }

    public List<Recurso> listarRecursos() {
        validarSesion();
        return modelo.listarRecursos();
    }

    public List<Recurso> listarRecursosPorCategoria(String idCategoria) {
        validarSesion();
        return modelo.listarRecursosPorCategoria(idCategoria);
    }

    public ResultadoReserva crearReservacion(List<String> idsCategorias, String descripcionActividad, LocalDateTime inicio, LocalDateTime fin) {
        requireFuncionario();
        SolicitudReserva solicitud = new SolicitudReserva(sesionActual, idsCategorias, descripcionActividad, inicio, fin);
        return modelo.reservarPorCategorias(solicitud);
    }

    public void cancelarReservacion(int id) {
        requireFuncionario();
        Reservacion reservacion = modelo.buscarReservacion(id);
        if (reservacion == null) {
            throw new IllegalArgumentException("No existe una reservación con id " + id + ".");
        }
        if (!reservacion.getEmpleado().equals(sesionActual)) {
            throw new IllegalStateException("Solo puede cancelar sus propias reservaciones.");
        }
        reservacion.cancelar();
    }

    public List<Reservacion> listarReservacionesSesionActual() {
        requireFuncionario();
        return modelo.listarReservacionesPorEmpleado(sesionActual);
    }

    public List<Reservacion> listarReservaciones() {
        validarSesion();
        return modelo.listarReservaciones();
    }

    public List<Reservacion> listarReservacionesActivas() {
        validarSesion();
        return modelo.listarReservacionesActivas();
    }

    public List<Reservacion> listarReservacionesPorEstado(EstadoReservacion estado) {
        validarSesion();
        return modelo.listarReservacionesPorEstado(estado);
    }

    public List<Reservacion> listarReservacionesPorRecurso(String codigoRecurso) {
        validarSesion();
        Recurso recurso = modelo.buscarRecurso(codigoRecurso);
        if (recurso == null) {
            return List.of();
        }
        return modelo.listarReservacionesPorRecurso(recurso);
    }

    public List<Reservacion> listarReservacionesPorEmpleado(String idEmpleado) {
        validarSesion();
        Empleado empleado = modelo.buscarEmpleado(idEmpleado);
        if (empleado == null) {
            return List.of();
        }
        return modelo.listarReservacionesPorEmpleado(empleado);
    }

    public List<Reservacion> listarReservacionesPorCategoria(String idCategoria) {
        validarSesion();
        return modelo.listarReservacionesPorCategoria(idCategoria);
    }

    public List<Reservacion> listarReservacionesEnFecha(LocalDate fecha) {
        validarSesion();
        return modelo.listarReservacionesEnFecha(fecha);
    }

    public List<Reservacion> listarReservacionesEnRango(LocalDateTime inicio, LocalDateTime fin) {
        validarSesion();
        return modelo.listarReservacionesEnRango(inicio, fin);
    }

    public int contarEmpleados() {
        return modelo.contarEmpleados();
    }

    public int contarCategorias() {
        return modelo.contarCategorias();
    }

    public int contarRecursos() {
        return modelo.contarRecursos();
    }

    public int contarReservaciones() {
        return modelo.contarReservaciones();
    }

    private void validarSesion() {
        if (sesionActual == null) {
            throw new IllegalStateException("Debe iniciar sesión.");
        }
    }

    private void requireAdmin() {
        validarSesion();
        if (!(sesionActual instanceof Administrador)) {
            throw new IllegalStateException("Esta funcionalidad solo puede ejecutarla un administrador.");
        }
    }

    private void requireFuncionario() {
        validarSesion();
        if (!(sesionActual instanceof Funcionario)) {
            throw new IllegalStateException("Esta funcionalidad solo puede ejecutarla un funcionario.");
        }
    }

    private Funcionario buscarFuncionarioOInvalido(String id) {
        Funcionario funcionario = modelo.buscarEmpleado(id) instanceof Funcionario f ? f : null;
        if (funcionario == null) {
            throw new IllegalArgumentException("No existe un funcionario con id " + id + ".");
        }
        return funcionario;
    }

    public record ResultadoSesion(Empleado empleado, boolean requiereCambioContraseña) {
    }
}