package falso;

import contrato.IServicioAplicacion;
import dto.EstadisticaCategoriaDTO;
import dto.FuncionarioDTO;
import dto.ReservaDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import protocolo.CodigoError;
import protocolo.ServicioException;
import servicio.ContextoSesion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Reglas del contrato aplicadas por el servicio en memoria, sin red.
 */
class ServicioAplicacionFalsoTest {

    private static final String SALAS = "CAT-000001";
    private static final LocalDateTime MANANA = LocalDate.now().plusDays(1).atTime(9, 0);
    private static final LocalDate HOY = LocalDate.now();

    private final FabricaServiciosFalsa fabrica = new FabricaServiciosFalsa();

    private IServicioAplicacion conSesion(String id) {
        ContextoSesion contexto = new ContextoSesion();
        IServicioAplicacion servicio = fabrica.crear(contexto);
        contexto.iniciar(servicio.iniciarSesion(id, id).usuario());
        return servicio;
    }

    private static void assertError(CodigoError esperado, Executable accion) {
        assertEquals(esperado, assertThrows(ServicioException.class, accion).getCodigo());
    }

    private static int reservar(IServicioAplicacion servicio) {
        return servicio.crearReservacion(List.of(SALAS), "Clase", MANANA, MANANA.plusHours(1)).reserva().id();
    }

    // ── cancelarReservacion ──────────────────────────────────────────

    @Test
    void autorCancelaSuReservaFutura() {
        IServicioAplicacion ana = conSesion("F001");
        int id = reservar(ana);
        ana.cancelarReservacion(id);
        assertError(CodigoError.VALIDACION, () -> ana.cancelarReservacion(id));
    }

    @Test
    void noSeCancelaReservaAjena() {
        int id = reservar(conSesion("F001"));
        assertError(CodigoError.PERMISO, () -> conSesion("F002").cancelarReservacion(id));
    }

    @Test
    void noSeCancelaReservaInexistente() {
        assertError(CodigoError.VALIDACION, () -> conSesion("F001").cancelarReservacion(999));
    }

    @Test
    void noSeCancelaReservaQueYaInicio() {
        IServicioAplicacion ana = conSesion("F001");
        int id = reservar(ana);
        // Se simula el paso del tiempo moviendo la reserva a un horario ya iniciado.
        ReservaDTO r = fabrica.datos.reservas.get(0);
        fabrica.datos.reservas.set(0, new ReservaDTO(r.id(), r.autor(), r.recursos(), r.descripcionActividad(),
                LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1), r.estado()));
        assertError(CodigoError.VALIDACION, () -> ana.cancelarReservacion(id));
    }

    // ── cambiarClave ─────────────────────────────────────────────────

    @Test
    void cambiarClaveExigeSesion() {
        IServicioAplicacion sinSesion = fabrica.crear(new ContextoSesion());
        assertError(CodigoError.SESION, () -> sinSesion.cambiarClave("nueva"));
    }

    @Test
    void cambiarClaveRechazaVacia() {
        IServicioAplicacion ana = conSesion("F001");
        assertError(CodigoError.VALIDACION, () -> ana.cambiarClave(null));
        assertError(CodigoError.VALIDACION, () -> ana.cambiarClave("  "));
    }

    @Test
    void funcionarioNuevoDebeCambiarClaveSoloLaPrimeraVez() {
        conSesion("ADMIN").registrarFuncionario(new FuncionarioDTO("F009", "Eva Solís", "8888-0009"));

        ContextoSesion contexto = new ContextoSesion();
        IServicioAplicacion eva = fabrica.crear(contexto);
        var primera = eva.iniciarSesion("F009", "F009");
        assertTrue(primera.requiereCambioClave());
        contexto.iniciar(primera.usuario());

        eva.cambiarClave("secreta");

        assertFalse(eva.iniciarSesion("F009", "secreta").requiereCambioClave());
        assertError(CodigoError.SESION, () -> eva.iniciarSesion("F009", "F009"));
    }

    // ── crearReservacion ─────────────────────────────────────────────

    @Test
    void reservaConDatosInvalidosSeRechaza() {
        IServicioAplicacion ana = conSesion("F001");
        LocalDateTime fin = MANANA.plusHours(1);
        assertError(CodigoError.VALIDACION, () -> ana.crearReservacion(List.of(), "Clase", MANANA, fin));
        assertError(CodigoError.VALIDACION, () -> ana.crearReservacion(null, "Clase", MANANA, fin));
        assertError(CodigoError.VALIDACION, () -> ana.crearReservacion(List.of(SALAS), " ", MANANA, fin));
        assertError(CodigoError.VALIDACION, () -> ana.crearReservacion(List.of(SALAS), "Clase", null, fin));
        assertError(CodigoError.VALIDACION, () -> ana.crearReservacion(List.of(SALAS), "Clase", MANANA, MANANA));
        assertError(CodigoError.VALIDACION, () -> ana.crearReservacion(List.of(SALAS), "Clase",
                MANANA.minusDays(2), fin.minusDays(2)));
    }

    @Test
    void reservaFueraDelHorarioSeRechaza() {
        IServicioAplicacion ana = conSesion("F001");
        LocalDateTime temprano = MANANA.withHour(IServicioAplicacion.HORA_INICIO - 1);
        LocalDateTime tarde = MANANA.withHour(IServicioAplicacion.HORA_FIN);
        assertError(CodigoError.VALIDACION, () -> ana.crearReservacion(List.of(SALAS), "Clase",
                temprano, temprano.plusHours(2)));
        assertError(CodigoError.VALIDACION, () -> ana.crearReservacion(List.of(SALAS), "Clase",
                tarde.minusHours(1), tarde.plusHours(1)));
        assertTrue(ana.crearReservacion(List.of(SALAS), "Clase", tarde.minusHours(1), tarde).exito());
    }

    // ── Consultas por rango ──────────────────────────────────────────

    @Test
    void rangosInvalidosSeRechazan() {
        IServicioAplicacion ana = conSesion("F001");
        LocalDate manana = HOY.plusDays(1);
        assertError(CodigoError.VALIDACION, () -> ana.contarPorSemana(null, manana));
        assertError(CodigoError.VALIDACION, () -> ana.contarPorSemana(manana, HOY));
        assertError(CodigoError.VALIDACION, () -> ana.contarReservasCategoria(SALAS, HOY, null));
        assertError(CodigoError.VALIDACION, () -> ana.contarReservasCategoria(SALAS, manana, HOY));
        assertError(CodigoError.VALIDACION, () -> ana.contarPorCategoria(null, null));
        assertError(CodigoError.VALIDACION, () -> ana.contarPorCategoria(manana, HOY));
    }

    @Test
    void estadisticasNoCuentanReservasCanceladas() {
        IServicioAplicacion ana = conSesion("F001");
        ana.cancelarReservacion(reservar(ana));
        LocalDate dia = MANANA.toLocalDate();

        assertEquals(0, ana.contarPorCategoria(dia, dia).get(0).cantidad());
        assertEquals(0, ana.contarPorSemana(dia, dia).get(0).cantidad());
    }

    @Test
    void contarPorCategoriaIncluyeCategoriasSinReservas() {
        IServicioAplicacion ana = conSesion("F001");
        reservar(ana);
        LocalDate dia = MANANA.toLocalDate();

        List<EstadisticaCategoriaDTO> conteo = ana.contarPorCategoria(dia, dia);

        assertEquals(ana.listarCategorias().size(), conteo.size());
        for (EstadisticaCategoriaDTO e : conteo) {
            assertEquals(ana.contarReservasCategoria(e.categoria().id(), dia, dia), e.cantidad());
        }
        assertEquals(1, conteo.get(0).cantidad());
        assertEquals(0, conteo.get(1).cantidad());
    }
}
