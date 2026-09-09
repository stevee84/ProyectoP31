package service;

import exception.RecursoEnUsoException;
import exception.SesionException;
import exception.ValidacionException;
import model.CategoriaRecurso;
import model.Coleccion;
import model.ModeloReservaciones;
import model.Recurso;
import model.ResultadoReserva;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class StressTest {

    private ModeloReservaciones modelo() {
        return new ModeloReservaciones();
    }

    private SesionService sesionAdmin(ModeloReservaciones modelo) {
        SesionService sesion = new SesionService(modelo, null);
        sesion.iniciarSesion("ADMIN", "ADMIN");
        return sesion;
    }

    @Test
    void testRegistrarCategoriaDuplicada() {
        ModeloReservaciones modelo = modelo();
        SesionService sesion = sesionAdmin(modelo);
        CategoriaService catService = new CategoriaService(modelo, null, sesion);

        CategoriaRecurso c1 = catService.registrar("Salas VIP");
        CategoriaRecurso c2 = catService.registrar("Salas VIP");

        assertNotNull(c1);
        assertNotNull(c2);
        assertNotEquals(c1.getId(), c2.getId());
        assertEquals(c1.getDescripcion(), c2.getDescripcion());
    }

    @Test
    void testEliminarCategoriaConRecursos() {
        ModeloReservaciones modelo = modelo();
        SesionService sesion = sesionAdmin(modelo);
        CategoriaService catService = new CategoriaService(modelo, null, sesion);
        RecursoService recursoService = new RecursoService(modelo, null, sesion);

        CategoriaRecurso categoria = catService.registrar("Con recursos");
        recursoService.registrar("REC-CAT-1", categoria.getId(), "Recurso asociado");

        assertThrows(RecursoEnUsoException.class, () -> catService.eliminar(categoria.getId()));
    }

    @Test
    void testEliminarRecursoConReservacionActiva() {
        ModeloReservaciones modelo = modelo();
        SesionService sesion = sesionAdmin(modelo);
        CategoriaService catService = new CategoriaService(modelo, null, sesion);
        RecursoService recursoService = new RecursoService(modelo, null, sesion);
        FuncionarioService funcionarioService = new FuncionarioService(modelo, null, sesion);

        CategoriaRecurso categoria = catService.registrar("Salas reservables");
        recursoService.registrar("REC-RES-1", categoria.getId(), "Sala reservable");
        funcionarioService.registrarFuncionario("Carlos Mora", "FSTRESS1", "8000-0001");

        sesion.cerrarSesion();
        sesion.iniciarSesion("FSTRESS1", "FSTRESS1");
        ReservaService reservaService = new ReservaService(modelo, null, sesion, null);

        LocalDateTime inicio = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0);
        LocalDateTime fin = inicio.plusHours(1);
        ResultadoReserva resultado = reservaService.crearReservacion(
                List.of(categoria.getId()), "Reunion", inicio, fin);
        assertTrue(resultado.esExito());

        sesion.cerrarSesion();
        sesion.iniciarSesion("ADMIN", "ADMIN");

        assertThrows(RecursoEnUsoException.class, () -> recursoService.eliminar("REC-RES-1"));
    }

    @Test
    void testCrearReservacionSinCategorias() {
        ModeloReservaciones modelo = modelo();
        SesionService sesion = sesionAdmin(modelo);
        FuncionarioService funcionarioService = new FuncionarioService(modelo, null, sesion);
        funcionarioService.registrarFuncionario("Sin Cat", "FSTRESS2", "8000-0002");

        sesion.cerrarSesion();
        sesion.iniciarSesion("FSTRESS2", "FSTRESS2");
        ReservaService reservaService = new ReservaService(modelo, null, sesion, null);

        LocalDateTime inicio = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0);
        LocalDateTime fin = inicio.plusHours(1);

        assertThrows(ValidacionException.class, () ->
                reservaService.crearReservacion(List.of(), "Sin categorias", inicio, fin));
    }

    @Test
    void testCrearReservacionFechaInvalida() {
        ModeloReservaciones modelo = modelo();
        SesionService sesion = sesionAdmin(modelo);
        CategoriaService catService = new CategoriaService(modelo, null, sesion);
        RecursoService recursoService = new RecursoService(modelo, null, sesion);
        FuncionarioService funcionarioService = new FuncionarioService(modelo, null, sesion);

        CategoriaRecurso categoria = catService.registrar("Fecha invalida");
        recursoService.registrar("REC-FECHA-1", categoria.getId(), "Recurso");
        funcionarioService.registrarFuncionario("Fecha Mala", "FSTRESS3", "8000-0003");

        sesion.cerrarSesion();
        sesion.iniciarSesion("FSTRESS3", "FSTRESS3");
        ReservaService reservaService = new ReservaService(modelo, null, sesion, null);

        LocalDateTime inicio = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        LocalDateTime fin = inicio.minusHours(2);

        assertThrows(ValidacionException.class, () ->
                reservaService.crearReservacion(List.of(categoria.getId()), "Fin antes de inicio", inicio, fin));
    }

    @Test
    void testCancelarReservacionDeOtroUsuario() {
        ModeloReservaciones modelo = modelo();
        SesionService sesion = sesionAdmin(modelo);
        CategoriaService catService = new CategoriaService(modelo, null, sesion);
        RecursoService recursoService = new RecursoService(modelo, null, sesion);
        FuncionarioService funcionarioService = new FuncionarioService(modelo, null, sesion);

        CategoriaRecurso categoria = catService.registrar("Otro usuario");
        recursoService.registrar("REC-OTRO-1", categoria.getId(), "Recurso");
        funcionarioService.registrarFuncionario("Usuario A", "FSTRESS4A", "8000-0004");
        funcionarioService.registrarFuncionario("Usuario B", "FSTRESS4B", "8000-0005");

        sesion.cerrarSesion();
        sesion.iniciarSesion("FSTRESS4A", "FSTRESS4A");
        ReservaService reservaService = new ReservaService(modelo, null, sesion, null);

        LocalDateTime inicio = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0);
        LocalDateTime fin = inicio.plusHours(1);
        ResultadoReserva resultado = reservaService.crearReservacion(
                List.of(categoria.getId()), "Reunion A", inicio, fin);
        assertTrue(resultado.esExito());
        int id = resultado.getReservacion().getId();

        sesion.cerrarSesion();
        sesion.iniciarSesion("FSTRESS4B", "FSTRESS4B");

        assertThrows(SesionException.class, () -> reservaService.cancelarReservacion(id));
    }

    @Test
    void testOperacionesSinSesion() {
        ModeloReservaciones modelo = modelo();
        SesionService sesion = new SesionService(modelo, null);
        CategoriaService catService = new CategoriaService(modelo, null, sesion);
        RecursoService recursoService = new RecursoService(modelo, null, sesion);
        FuncionarioService funcionarioService = new FuncionarioService(modelo, null, sesion);
        ReservaService reservaService = new ReservaService(modelo, null, sesion, null);

        assertThrows(SesionException.class, () -> catService.registrar("X"));
        assertThrows(SesionException.class, () -> catService.listar());
        assertThrows(SesionException.class, () -> recursoService.registrar("X", "CAT-000001", "desc"));
        assertThrows(SesionException.class, () -> recursoService.listar());
        assertThrows(SesionException.class, () -> funcionarioService.registrarFuncionario("N", "I", "T"));
        assertThrows(SesionException.class, () -> funcionarioService.listarFuncionarios());
        assertThrows(SesionException.class, () ->
                reservaService.crearReservacion(List.of("CAT-000001"), "d",
                        LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(1)));
        assertThrows(SesionException.class, () -> reservaService.cancelarReservacion(1));
        assertThrows(SesionException.class, () -> reservaService.listarReservaciones());
    }

    @Test
    void testFuncionarioNoPuedeCrearCategoria() {
        ModeloReservaciones modelo = modelo();
        SesionService sesion = sesionAdmin(modelo);
        FuncionarioService funcionarioService = new FuncionarioService(modelo, null, sesion);
        funcionarioService.registrarFuncionario("Empleado Raso", "FSTRESS5", "8000-0006");

        sesion.cerrarSesion();
        sesion.iniciarSesion("FSTRESS5", "FSTRESS5");
        CategoriaService catService = new CategoriaService(modelo, null, sesion);

        assertThrows(SesionException.class, () -> catService.registrar("No deberia poder"));
    }

    @Test
    void testEliminarFuncionarioConReservasActivas() {
        ModeloReservaciones modelo = modelo();
        SesionService sesion = sesionAdmin(modelo);
        CategoriaService catService = new CategoriaService(modelo, null, sesion);
        RecursoService recursoService = new RecursoService(modelo, null, sesion);
        FuncionarioService funcionarioService = new FuncionarioService(modelo, null, sesion);

        CategoriaRecurso categoria = catService.registrar("Con reservas");
        recursoService.registrar("REC-ELIM-1", categoria.getId(), "Recurso");
        funcionarioService.registrarFuncionario("Con Reserva", "FSTRESS6", "8000-0007");

        sesion.cerrarSesion();
        sesion.iniciarSesion("FSTRESS6", "FSTRESS6");
        ReservaService reservaService = new ReservaService(modelo, null, sesion, null);
        LocalDateTime inicio = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0);
        LocalDateTime fin = inicio.plusHours(1);
        ResultadoReserva resultado = reservaService.crearReservacion(
                List.of(categoria.getId()), "Reunion", inicio, fin);
        assertTrue(resultado.esExito());

        sesion.cerrarSesion();
        sesion.iniciarSesion("ADMIN", "ADMIN");

        assertThrows(RecursoEnUsoException.class, () -> funcionarioService.eliminarFuncionario("FSTRESS6"));
    }

    @Test
    void testCambiarContrasenaYRelogin() {
        ModeloReservaciones modelo = modelo();
        SesionService sesion = sesionAdmin(modelo);
        FuncionarioService funcionarioService = new FuncionarioService(modelo, null, sesion);
        funcionarioService.registrarFuncionario("Cambio Clave", "FSTRESS7", "8000-0008");

        sesion.cerrarSesion();
        sesion.iniciarSesion("FSTRESS7", "FSTRESS7");
        sesion.cambiarContrasena("ClaveNueva99");
        sesion.cerrarSesion();

        SesionService.ResultadoSesion resultado = sesion.iniciarSesion("FSTRESS7", "ClaveNueva99");
        assertNotNull(resultado.empleado());
        assertFalse(resultado.requiereCambioContraseña());

        SesionService.ResultadoSesion fallido = sesion.iniciarSesion("FSTRESS7", "FSTRESS7");
        assertNull(fallido.empleado());
    }

    @Test
    void testLoginConCredencialesVacias() {
        ModeloReservaciones modelo = modelo();
        SesionService sesion = new SesionService(modelo, null);

        SesionService.ResultadoSesion resultado = sesion.iniciarSesion("", "");
        assertNull(resultado.empleado());
        assertFalse(resultado.requiereCambioContraseña());
        assertNull(sesion.getSesionActual());
    }

    @Test
    void testColeccionOperacionesEdge() {
        Coleccion<String> coleccion = new Coleccion<>();

        assertNull(coleccion.buscar(s -> s.equals("algo")));
        assertTrue(coleccion.filtrar(s -> true).listar().isEmpty());
        assertFalse(coleccion.eliminarSi(s -> true));
        assertEquals(0, coleccion.tamanio());
        assertTrue(coleccion.estaVacia());
    }

    @Test
    void testRegistrarRecursoConCategoriaInexistente() {
        ModeloReservaciones modelo = modelo();
        SesionService sesion = sesionAdmin(modelo);
        RecursoService recursoService = new RecursoService(modelo, null, sesion);

        assertThrows(ValidacionException.class, () ->
                recursoService.registrar("REC-INEX-1", "CAT-999999", "Recurso fantasma"));
    }

    @Test
    void testEstadisticasServiceFechasInvalidas() {
        ModeloReservaciones modelo = modelo();
        consulta.ReservaConsulta consulta = new consulta.ReservaConsultaAdapter(modelo);
        EstadisticasService estadisticasService = new EstadisticasService(modelo, consulta);

        LocalDate desde = LocalDate.now();
        LocalDate hasta = desde.minusDays(5);

        assertThrows(ValidacionException.class, () -> estadisticasService.contarPorSemana(desde, hasta));
    }

    @Test
    void testMultiplesReservacionesMismoHorario() {
        ModeloReservaciones modelo = modelo();
        SesionService sesion = sesionAdmin(modelo);
        CategoriaService catService = new CategoriaService(modelo, null, sesion);
        RecursoService recursoService = new RecursoService(modelo, null, sesion);
        FuncionarioService funcionarioService = new FuncionarioService(modelo, null, sesion);

        CategoriaRecurso categoria = catService.registrar("Multi horario");
        recursoService.registrar("REC-MULTI-1", categoria.getId(), "Recurso 1");
        recursoService.registrar("REC-MULTI-2", categoria.getId(), "Recurso 2");
        funcionarioService.registrarFuncionario("Multi Uno", "FSTRESS8A", "8000-0009");
        funcionarioService.registrarFuncionario("Multi Dos", "FSTRESS8B", "8000-0010");
        funcionarioService.registrarFuncionario("Multi Tres", "FSTRESS8C", "8000-0011");

        LocalDateTime inicio = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0);
        LocalDateTime fin = inicio.plusHours(1);

        sesion.cerrarSesion();
        sesion.iniciarSesion("FSTRESS8A", "FSTRESS8A");
        ReservaService reservaServiceA = new ReservaService(modelo, null, sesion, null);
        ResultadoReserva r1 = reservaServiceA.crearReservacion(List.of(categoria.getId()), "Reunion 1", inicio, fin);
        assertTrue(r1.esExito());

        sesion.cerrarSesion();
        sesion.iniciarSesion("FSTRESS8B", "FSTRESS8B");
        ReservaService reservaServiceB = new ReservaService(modelo, null, sesion, null);
        ResultadoReserva r2 = reservaServiceB.crearReservacion(List.of(categoria.getId()), "Reunion 2", inicio, fin);
        assertTrue(r2.esExito());

        sesion.cerrarSesion();
        sesion.iniciarSesion("FSTRESS8C", "FSTRESS8C");
        ReservaService reservaServiceC = new ReservaService(modelo, null, sesion, null);
        ResultadoReserva r3 = reservaServiceC.crearReservacion(List.of(categoria.getId()), "Reunion 3", inicio, fin);
        assertFalse(r3.esExito());
    }
}
