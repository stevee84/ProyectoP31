package controller;

import model.Empleado;
import model.Funcionario;
import model.Reservacion;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ControladorReservacionesTest {

    private ControladorReservaciones controlador;

    @BeforeEach
    void setUp() {
        controlador = new ControladorReservaciones();
    }

    @Test
    void testIniciarSesionAdmin() {
        ControladorReservaciones.ResultadoSesion resultado =
                controlador.iniciarSesion("ADMIN", "ADMIN");

        assertNotNull(resultado.empleado());
        assertEquals("ADMIN", resultado.empleado().getId());
    }

    @Test
    void testIniciarSesionFuncionario() {
        // Primero registrar como admin
        controlador.iniciarSesion("ADMIN", "ADMIN");
        controlador.registrarFuncionario("Juan Pérez", "FJUAN", "8888-0000");
        controlador.cerrarSesion();

        // Ahora iniciar sesión como funcionario (contraseña por defecto = id)
        ControladorReservaciones.ResultadoSesion resultado =
                controlador.iniciarSesion("FJUAN", "FJUAN");

        assertNotNull(resultado.empleado());
        assertTrue(resultado.empleado() instanceof Funcionario);
        assertTrue(resultado.requiereCambioContraseña());
    }

    @Test
    void testAccesoSinSesion() {
        // Sin iniciar sesión, listar categorías debe lanzar excepción
        assertThrows(IllegalStateException.class, () -> {
            controlador.listarCategorias();
        });
    }

    @Test
    void testAdminPuedeCrearFuncionario() {
        controlador.iniciarSesion("ADMIN", "ADMIN");

        boolean registrado = controlador.registrarFuncionario("Ana", "FANA", "7777-0000");
        assertTrue(registrado);

        List<Funcionario> funcionarios = controlador.listarFuncionarios();
        assertTrue(funcionarios.stream().anyMatch(f -> f.getId().equals("FANA")));
    }

    @Test
    void testFuncionarioNoPuedeCrearFuncionario() {
        // Registrar funcionario como admin
        controlador.iniciarSesion("ADMIN", "ADMIN");
        controlador.registrarFuncionario("Luis", "FLUIS", "6666-0000");
        controlador.cerrarSesion();

        // Iniciar sesión como funcionario
        controlador.iniciarSesion("FLUIS", "FLUIS");

        // Intentar crear otro funcionario debe fallar
        assertThrows(IllegalStateException.class, () -> {
            controlador.registrarFuncionario("Otro", "FOTRO", "5555-0000");
        });
    }

    @Test
    void testCrearYCancelarReservacion() {
        // Setup: admin crea recurso y funcionario
        controlador.iniciarSesion("ADMIN", "ADMIN");

        String catId = controlador.listarCategorias().get(0).getId();
        controlador.registrarRecurso("REC-TEST", catId, "Recurso test");
        controlador.registrarFuncionario("Tester", "FTEST", "4444-0000");
        controlador.cerrarSesion();

        // Funcionario crea reservación
        controlador.iniciarSesion("FTEST", "FTEST");

        LocalDateTime manana = LocalDate.now().plusDays(1).atTime(9, 0);
        Reservacion reservacion = controlador.crearReservacion(
                List.of(catId), "Actividad test", manana, manana.plusHours(1));

        assertNotNull(reservacion);
        assertTrue(reservacion.esActiva());

        // Cancelar
        controlador.cancelarReservacion(reservacion.getId());
        assertFalse(reservacion.esActiva());
    }
}
