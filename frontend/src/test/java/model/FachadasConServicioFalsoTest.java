package model;

import dto.ReservaDTO;
import dto.ResultadoReservaDTO;
import dto.ResultadoSesionDTO;
import falso.ServicioClienteFalso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import protocolo.CodigoError;
import protocolo.ServicioException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Las fachadas XxxModel del Frontend contra ServicioClienteFalso: delegan en el contrato
 * y dejan pasar los errores remotos con su código, sin validar reglas por su cuenta.
 */
class FachadasConServicioFalsoTest {

    private ServicioClienteFalso servicio;
    private AtomicInteger conexiones;
    private SesionModel sesion;

    @BeforeEach
    void preparar() {
        servicio = new ServicioClienteFalso();
        conexiones = new AtomicInteger();
        sesion = new SesionModel(servicio, conexiones::incrementAndGet);
    }

    private static CodigoError codigo(Runnable accion) {
        return assertThrows(ServicioException.class, accion::run).getCodigo();
    }

    @Test
    void loginAseguraLaConexionYRecuerdaElUsuario() {
        ResultadoSesionDTO r = sesion.iniciarSesion("F001", "F001");

        assertEquals(1, conexiones.get());
        assertEquals("Ana Mora", sesion.getUsuarioActual().nombre());
        assertFalse(r.requiereCambioClave());
    }

    @Test
    void credencialesIncorrectasLleganComoErrorDeSesion() {
        assertEquals(CodigoError.SESION, codigo(() -> sesion.iniciarSesion("F001", "mala")));
        assertNull(sesion.getUsuarioActual());
    }

    @Test
    void cerrarSesionOlvidaElUsuarioAunqueElServidorFalle() {
        sesion.iniciarSesion("F001", "F001");
        sesion.cerrarSesion();
        assertNull(sesion.getUsuarioActual());

        // Ya sin sesión el servicio rechaza el cierre, pero el estado local queda limpio.
        assertEquals(CodigoError.SESION, codigo(sesion::cerrarSesion));
        assertNull(sesion.getUsuarioActual());
    }

    @Test
    void funcionarioNuevoDebeCambiarClaveEnElPrimerIngreso() {
        sesion.iniciarSesion("ADMIN", "ADMIN");
        new FuncionarioModel(servicio).registrar("F010", "Marta Solis", "8888-0010");
        sesion.cerrarSesion();

        assertTrue(sesion.iniciarSesion("F010", "F010").requiereCambioClave());
        // Igual que el servidor: sin cambiar la clave no se opera.
        assertEquals(CodigoError.SESION, codigo(servicio::listarCategorias));
        assertEquals(CodigoError.VALIDACION, codigo(() -> sesion.cambiarClave("F010")));
        sesion.cambiarClave("nueva");
        assertFalse(servicio.listarCategorias().isEmpty());
        assertEquals(CodigoError.VALIDACION, codigo(() -> sesion.cambiarClave("nueva")));
        sesion.cerrarSesion();

        assertFalse(sesion.iniciarSesion("F010", "nueva").requiereCambioClave());
    }

    @Test
    void elFuncionarioNoPuedeAdministrarCatalogos() {
        sesion.iniciarSesion("F001", "F001");

        assertEquals(CodigoError.PERMISO, codigo(() -> new CategoriaModel(servicio).registrar("Nueva")));
        assertEquals(CodigoError.PERMISO, codigo(() -> new FuncionarioModel(servicio).listar()));
        assertEquals(CodigoError.PERMISO, codigo(() -> new RecursoModel(servicio).eliminar("SALA-01")));
    }

    @Test
    void borrarCategoriaConRecursosInformaRecursoEnUso() {
        sesion.iniciarSesion("ADMIN", "ADMIN");
        CategoriaModel categorias = new CategoriaModel(servicio);
        String idSalas = categorias.listar().get(0).id();

        assertEquals(CodigoError.RECURSO_EN_USO, codigo(() -> categorias.eliminar(idSalas)));
    }

    @Test
    void reservaSinDisponibilidadDevuelveFracasoNoExcepcion() {
        sesion.iniciarSesion("F001", "F001");
        ReservaModel reservas = new ReservaModel(servicio);
        String idSalas = servicio.listarCategorias().get(0).id();
        LocalDateTime inicio = LocalDate.now().plusDays(1).atTime(8, 0); // las dos salas: SALA-01 ocupada
        List<String> dosSalas = List.of(idSalas, idSalas);

        ResultadoReservaDTO primera = reservas.crear(List.of(idSalas), "Mia", inicio, inicio.plusHours(1));
        ResultadoReservaDTO segunda = reservas.crear(dosSalas, "Sin espacio", inicio, inicio.plusHours(1));

        assertTrue(primera.exito());
        assertEquals("SALA-02", primera.reserva().recursos().get(0).codigo());
        assertFalse(segunda.exito());
        assertEquals(1, reservas.listarMias().size(), "Una reserva fallida no deja asignaciones parciales");
    }

    @Test
    void noSePuedeCancelarUnaReservaAjena() {
        sesion.iniciarSesion("F001", "F001");
        // La reserva 1 de los datos de ejemplo es de Luis Rojas.
        assertEquals(CodigoError.PERMISO, codigo(() -> new ReservaModel(servicio).cancelar(1)));
    }

    @Test
    void cancelarUnaReservaPropiaFutura() {
        sesion.iniciarSesion("F001", "F001");
        ReservaModel reservas = new ReservaModel(servicio);
        String idLaptops = servicio.listarCategorias().get(1).id();
        LocalDateTime inicio = LocalDate.now().plusDays(3).atTime(14, 0);
        ReservaDTO creada = reservas.crear(List.of(idLaptops), "Taller", inicio, inicio.plusHours(1)).reserva();

        reservas.cancelar(creada.id());

        assertEquals("CANCELADA", reservas.listarMias().get(0).estado().name());
    }

    @Test
    void laCalendarizacionSePideEnUnaSolaLlamada() {
        sesion.iniciarSesion("F001", "F001");
        CalendarizacionModel calendario = new CalendarizacionModel(servicio);
        String idSalas = calendario.listarCategorias().get(0).id();
        int antes = servicio.getLlamadas();

        var matriz = calendario.obtener(LocalDate.now().plusDays(1), idSalas);

        assertEquals(1, servicio.getLlamadas() - antes);
        assertEquals("Reunion clientes - Luis Rojas", matriz.celdas().get(1).get(0));
    }

    @Test
    void extraccionIAFalsaProponeDatosEditables() {
        sesion.iniciarSesion("F001", "F001");

        var resultado = new ReservaModel(servicio).extraerConIA("Reunion de equipo");

        assertTrue(resultado.exito());
        assertEquals("Reunion de equipo", resultado.descripcionActividad());
        assertFalse(resultado.idsCategorias().isEmpty());
    }

    @Test
    void rangoInvalidoEnEstadisticasEsErrorDeValidacion() {
        sesion.iniciarSesion("F001", "F001");
        EstadisticasModel estadisticas = new EstadisticasModel(servicio);

        assertEquals(CodigoError.VALIDACION,
                codigo(() -> estadisticas.contarPorCategoria(LocalDate.now(), LocalDate.now().minusDays(1))));
    }
}
