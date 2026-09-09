package service;

import model.CategoriaRecurso;
import model.EstadoReservacion;
import model.ModeloReservaciones;
import model.ResultadoReserva;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ReservaServiceTest {

    @Test
    void testCrearReservacion() {
        ModeloReservaciones modelo = new ModeloReservaciones();
        SesionService sesion = new SesionService(modelo, null);
        sesion.iniciarSesion("ADMIN", "ADMIN");

        CategoriaService catService = new CategoriaService(modelo, null, sesion);
        RecursoService recursoService = new RecursoService(modelo, null, sesion);
        FuncionarioService funcionarioService = new FuncionarioService(modelo, null, sesion);

        CategoriaRecurso categoria = catService.registrar("Salas");
        recursoService.registrar("S001", categoria.getId(), "Sala 1");
        funcionarioService.registrarFuncionario("Juan Perez", "F001", "8888-8888");

        sesion.cerrarSesion();
        sesion.iniciarSesion("F001", "F001");

        ReservaService reservaService = new ReservaService(modelo, null, sesion, null);

        LocalDateTime inicio = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0);
        LocalDateTime fin = inicio.plusHours(1);

        ResultadoReserva resultado = reservaService.crearReservacion(
                List.of(categoria.getId()), "Reunion de equipo", inicio, fin);

        assertTrue(resultado.esExito());
        assertNotNull(resultado.getReservacion());
    }

    @Test
    void testCancelarReservacion() {
        ModeloReservaciones modelo = new ModeloReservaciones();
        SesionService sesion = new SesionService(modelo, null);
        sesion.iniciarSesion("ADMIN", "ADMIN");

        CategoriaService catService = new CategoriaService(modelo, null, sesion);
        RecursoService recursoService = new RecursoService(modelo, null, sesion);
        FuncionarioService funcionarioService = new FuncionarioService(modelo, null, sesion);

        CategoriaRecurso categoria = catService.registrar("Salas");
        recursoService.registrar("S002", categoria.getId(), "Sala 2");
        funcionarioService.registrarFuncionario("Ana Lopez", "F002", "7777-7777");

        sesion.cerrarSesion();
        sesion.iniciarSesion("F002", "F002");

        ReservaService reservaService = new ReservaService(modelo, null, sesion, null);

        LocalDateTime inicio = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0);
        LocalDateTime fin = inicio.plusHours(1);

        ResultadoReserva resultado = reservaService.crearReservacion(
                List.of(categoria.getId()), "Capacitacion", inicio, fin);
        assertTrue(resultado.esExito());
        int id = resultado.getReservacion().getId();

        reservaService.cancelarReservacion(id);

        boolean cancelada = reservaService.listarReservacionesSesionActual().stream()
                .anyMatch(r -> r.getId() == id && r.getEstado() == EstadoReservacion.CANCELADA);
        assertTrue(cancelada);
    }
}
