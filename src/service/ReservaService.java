package service;

import exception.SesionException;
import exception.ValidacionException;
import model.Empleado;
import model.EstadoReservacion;
import model.ExtractorReservaIA;
import model.ModeloReservaciones;
import model.Recurso;
import model.Reservacion;
import model.ResultadoExtraccionIA;
import model.ResultadoReserva;
import model.SolicitudReserva;
import repository.PersistenciaXml;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ReservaService {

    private final ModeloReservaciones modelo;
    private PersistenciaXml persistencia;
    private final SesionService sesionService;
    private final ExtractorReservaIA extractorIA;

    public ReservaService(ModeloReservaciones modelo, PersistenciaXml persistencia, SesionService sesionService,
                           ExtractorReservaIA extractorIA) {
        this.modelo = modelo;
        this.persistencia = persistencia;
        this.sesionService = sesionService;
        this.extractorIA = extractorIA;
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

    public ResultadoReserva crearReservacion(List<String> idsCategorias, String descripcionActividad,
                                              LocalDateTime inicio, LocalDateTime fin) {
        sesionService.requireFuncionario();
        Empleado sesionActual = sesionService.getSesionActual();
        SolicitudReserva solicitud = new SolicitudReserva(sesionActual, idsCategorias,
                descripcionActividad, inicio, fin);
        ResultadoReserva resultado = modelo.reservarPorCategorias(solicitud);
        if (resultado.esExito()) guardarDatos();
        return resultado;
    }

    public void cancelarReservacion(int id) {
        sesionService.requireFuncionario();
        Empleado sesionActual = sesionService.getSesionActual();
        Reservacion reservacion = modelo.buscarReservacion(id);
        if (reservacion == null) {
            throw new ValidacionException("No existe una reservación con id " + id + ".");
        }
        if (!reservacion.getEmpleado().equals(sesionActual)) {
            throw new SesionException("Solo puede cancelar sus propias reservaciones.");
        }
        reservacion.cancelar();
        guardarDatos();
    }

    public List<Reservacion> listarReservacionesSesionActual() {
        sesionService.requireFuncionario();
        return modelo.listarReservacionesPorEmpleado(sesionService.getSesionActual());
    }

    public List<Reservacion> listarReservaciones() {
        sesionService.validarSesion();
        return modelo.listarReservaciones();
    }

    public List<Reservacion> listarReservacionesActivas() {
        sesionService.validarSesion();
        return modelo.listarReservacionesActivas();
    }

    public List<Reservacion> listarPorEstado(EstadoReservacion estado) {
        sesionService.validarSesion();
        return modelo.listarReservacionesPorEstado(estado);
    }

    public List<Reservacion> listarPorRecurso(String codigoRecurso) {
        sesionService.validarSesion();
        Recurso recurso = modelo.buscarRecurso(codigoRecurso);
        if (recurso == null) {
            return List.of();
        }
        return modelo.listarReservacionesPorRecurso(recurso);
    }

    public List<Reservacion> listarPorEmpleado(String idEmpleado) {
        sesionService.validarSesion();
        Empleado empleado = modelo.buscarEmpleado(idEmpleado);
        if (empleado == null) {
            return List.of();
        }
        return modelo.listarReservacionesPorEmpleado(empleado);
    }

    public List<Reservacion> listarPorCategoria(String idCategoria) {
        sesionService.validarSesion();
        return modelo.listarReservacionesPorCategoria(idCategoria);
    }

    public List<Reservacion> listarEnFecha(LocalDate fecha) {
        sesionService.validarSesion();
        return modelo.listarReservacionesEnFecha(fecha);
    }

    public List<Reservacion> listarEnRango(LocalDateTime inicio, LocalDateTime fin) {
        sesionService.validarSesion();
        return modelo.listarReservacionesEnRango(inicio, fin);
    }

    public ResultadoExtraccionIA extraerDatosDesdeFrase(String frase) {
        List<model.CategoriaRecurso> categorias = modelo.listarCategorias();
        return extractorIA.extraer(frase, categorias);
    }
}
