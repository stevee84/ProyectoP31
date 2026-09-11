package service;

import exception.SesionException;
import model.Administrador;
import model.Empleado;
import model.Funcionario;
import model.ModeloReservaciones;
import repository.PersistenciaXml;

public class SesionService {

    private final ModeloReservaciones modelo;
    private PersistenciaXml persistencia;
    private Empleado sesionActual;

    public SesionService(ModeloReservaciones modelo, PersistenciaXml persistencia) {
        this.modelo = modelo;
        this.persistencia = persistencia;
    }

    /** Guarda todos los datos a XML. Seguro de llamar desde cualquier hilo. */
    public void guardarDatos() {
        if (persistencia != null) {
            try {
                persistencia.guardarTodo();
            } catch (Exception e) {
                System.err.println("Error al guardar datos: " + e.getMessage());
            }
        }
    }

    public ResultadoSesion iniciarSesion(String id, String pass) {
        Empleado empleado = modelo.validarCredenciales(id, pass);
        if (empleado == null) {
            return new ResultadoSesion(null, false);
        }
        this.sesionActual = empleado;
        return new ResultadoSesion(empleado, empleado.isFirstLog());
    }

    public void cerrarSesion() {
        this.sesionActual = null;
    }

    public void cambiarContrasena(String nueva) {
        validarSesion();
        sesionActual.cambiarContraseña(nueva);
        guardarDatos();
    }

    public Empleado getSesionActual() {
        return sesionActual;
    }

    public void validarSesion() {
        if (sesionActual == null) {
            throw new SesionException("Debe iniciar sesión.");
        }
    }

    public void requireAdmin() {
        validarSesion();
        if (!(sesionActual instanceof Administrador)) {
            throw new SesionException("Esta funcionalidad solo puede ejecutarla un administrador.");
        }
    }

    public void requireFuncionario() {
        validarSesion();
        if (!(sesionActual instanceof Funcionario)) {
            throw new SesionException("Esta funcionalidad solo puede ejecutarla un funcionario.");
        }
    }

    public record ResultadoSesion(Empleado empleado, boolean requiereCambioContraseña) {
    }
}
