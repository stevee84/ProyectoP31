package service;

import model.Empleado;
import model.ModeloReservaciones;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SesionServiceTest {

    @Test
    void testLoginExitoso() {
        ModeloReservaciones modelo = new ModeloReservaciones();
        SesionService sesion = new SesionService(modelo, null);

        SesionService.ResultadoSesion resultado = sesion.iniciarSesion("ADMIN", "ADMIN");

        assertNotNull(resultado.empleado());
        assertEquals("ADMIN", resultado.empleado().getId());
        assertNotNull(sesion.getSesionActual());
    }

    @Test
    void testLoginFallido() {
        ModeloReservaciones modelo = new ModeloReservaciones();
        SesionService sesion = new SesionService(modelo, null);

        SesionService.ResultadoSesion resultado = sesion.iniciarSesion("ADMIN", "claveIncorrecta");

        assertNull(resultado.empleado());
        assertNull(sesion.getSesionActual());
    }

    @Test
    void testCambiarContrasena() {
        ModeloReservaciones modelo = new ModeloReservaciones();
        SesionService sesion = new SesionService(modelo, null);

        sesion.iniciarSesion("ADMIN", "ADMIN");
        sesion.cambiarContrasena("NuevaClave123");

        sesion.cerrarSesion();
        SesionService.ResultadoSesion resultado = sesion.iniciarSesion("ADMIN", "NuevaClave123");
        assertNotNull(resultado.empleado());

        Empleado emp = resultado.empleado();
        assertNotNull(emp);
    }
}
