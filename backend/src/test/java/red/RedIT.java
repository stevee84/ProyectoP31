package red;

import dto.EstadisticaCategoriaDTO;
import dto.FuncionarioDTO;
import dto.ReservaDTO;
import dto.ResultadoReservaDTO;
import falso.FabricaServiciosFalsa;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import protocolo.CodigoError;
import protocolo.EventoReservaCreada;
import protocolo.Operacion;
import protocolo.Peticion;
import protocolo.ServicioException;
import proxy.ProxyServicio;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Escenarios del enunciado con sockets reales: servidor + servicio falso + varios Proxy.
 * Ana (F001), Luis (F002) y Carla (F003) son funcionarios; ADMIN es administrador.
 */
class RedIT {

    private static final String SALAS = "CAT-000001";
    private static final String LAPTOPS = "CAT-000002";
    private static final LocalDateTime MANANA = LocalDate.now().plusDays(1).atTime(9, 0);

    private Servidor servidor;
    private final List<ProxyServicio> clientes = new ArrayList<>();

    @BeforeEach
    void iniciar() throws Exception {
        servidor = new Servidor(0, new FabricaServiciosFalsa());
        servidor.iniciar();
    }

    @AfterEach
    void detener() {
        clientes.forEach(ProxyServicio::desconectar);
        servidor.detener();
    }

    private ProxyServicio cliente() throws Exception {
        ProxyServicio proxy = new ProxyServicio(3000);
        proxy.conectar("localhost", servidor.getPuerto());
        clientes.add(proxy);
        return proxy;
    }

    private ProxyServicio conSesion(String id) throws Exception {
        ProxyServicio proxy = cliente();
        proxy.iniciarSesion(id, id);
        return proxy;
    }

    private static BlockingQueue<EventoReservaCreada> escuchar(ProxyServicio proxy) {
        BlockingQueue<EventoReservaCreada> alertas = new LinkedBlockingQueue<>();
        proxy.agregarReceptor(alertas::add);
        return alertas;
    }

    @Test
    void sinSesionNoSeEntreganDatos() throws Exception {
        ServicioException e = assertThrows(ServicioException.class, () -> cliente().listarFuncionarios());
        assertEquals(CodigoError.SESION, e.getCodigo());
    }

    @Test
    void credencialesIncorrectasSeRechazan() throws Exception {
        ProxyServicio proxy = cliente();
        ServicioException e = assertThrows(ServicioException.class, () -> proxy.iniciarSesion("F001", "mala"));
        assertEquals(CodigoError.SESION, e.getCodigo());
    }

    @Test
    void funcionarioNoPuedeEjecutarCrudDeAdministrador() throws Exception {
        ProxyServicio ana = conSesion("F001");
        ServicioException e = assertThrows(ServicioException.class, () -> ana.registrarCategoria("Proyector"));
        assertEquals(CodigoError.PERMISO, e.getCodigo());
    }

    @Test
    void reservaConfirmadaAlertaALosDemasYNoAlAutor() throws Exception {
        ProxyServicio ana = conSesion("F001");
        BlockingQueue<EventoReservaCreada> alertasAna = escuchar(ana);
        BlockingQueue<EventoReservaCreada> alertasLuis = escuchar(conSesion("F002"));
        BlockingQueue<EventoReservaCreada> alertasAdmin = escuchar(conSesion("ADMIN"));

        ResultadoReservaDTO resultado = ana.crearReservacion(List.of(SALAS), "Clase", MANANA, MANANA.plusHours(2));

        assertTrue(resultado.exito());
        assertEquals(resultado.reserva(), alertasAdmin.poll(2, TimeUnit.SECONDS).reserva());
        EventoReservaCreada evento = alertasLuis.poll(2, TimeUnit.SECONDS);
        assertNotNull(evento);
        assertEquals(resultado.reserva(), evento.reserva());
        assertNotNull(evento.emitido());
        ReservaDTO reserva = evento.reserva();
        assertNotNull(reserva.autor().nombre());
        assertFalse(reserva.recursos().isEmpty());
        reserva.recursos().forEach(r -> assertNotNull(r.categoria()));
        assertNotNull(reserva.descripcionActividad());
        assertNotNull(reserva.inicio());
        assertNotNull(reserva.fin());

        assertNull(alertasAna.poll(300, TimeUnit.MILLISECONDS), "El autor no recibe su propia alerta");
        assertTrue(alertasLuis.isEmpty() && alertasAdmin.isEmpty(), "Una sola alerta por reserva");
    }

    @Test
    void reservaFallidaNoGeneraAlertas() throws Exception {
        ProxyServicio ana = conSesion("F001");
        BlockingQueue<EventoReservaCreada> alertasLuis = escuchar(conSesion("F002"));
        ana.crearReservacion(List.of(SALAS), "Primera", MANANA, MANANA.plusHours(1));
        alertasLuis.poll(2, TimeUnit.SECONDS);

        ResultadoReservaDTO segunda = ana.crearReservacion(List.of(SALAS), "Choca", MANANA, MANANA.plusHours(1));

        assertFalse(segunda.exito());
        assertNull(alertasLuis.poll(300, TimeUnit.MILLISECONDS));
    }

    @Test
    void dosFuncionariosCompitenPorElUltimoRecurso() throws Exception {
        ProxyServicio ana = conSesion("F001");
        ProxyServicio luis = conSesion("F002");
        CountDownLatch salida = new CountDownLatch(1);

        CompletableFuture<ResultadoReservaDTO> a = CompletableFuture.supplyAsync(() -> {
            esperar(salida);
            return ana.crearReservacion(List.of(LAPTOPS, SALAS), "Ana", MANANA, MANANA.plusHours(1));
        });
        CompletableFuture<ResultadoReservaDTO> b = CompletableFuture.supplyAsync(() -> {
            esperar(salida);
            return luis.crearReservacion(List.of(LAPTOPS), "Luis", MANANA, MANANA.plusHours(1));
        });
        salida.countDown();

        boolean exitoAna = a.get(5, TimeUnit.SECONDS).exito();
        boolean exitoLuis = b.get(5, TimeUnit.SECONDS).exito();
        assertTrue(exitoAna ^ exitoLuis, "Solo uno obtiene la laptop");
        if (!exitoAna) {
            assertTrue(ana.listarReservacionesSesion().isEmpty(), "Sin asignaciones parciales");
        }
    }

    @Test
    void funcionarioNoPuedeCancelarReservaAjena() throws Exception {
        ProxyServicio ana = conSesion("F001");
        ProxyServicio luis = conSesion("F002");
        int id = ana.crearReservacion(List.of(SALAS), "Clase", MANANA, MANANA.plusHours(1)).reserva().id();

        ServicioException e = assertThrows(ServicioException.class, () -> luis.cancelarReservacion(id));
        assertEquals(CodigoError.PERMISO, e.getCodigo());
    }

    @Test
    void cerrarSesionDeUnClienteNoAfectaAOtro() throws Exception {
        ProxyServicio ana = conSesion("F001");
        ProxyServicio luis = conSesion("F002");

        luis.cerrarSesion();

        assertFalse(ana.listarCategorias().isEmpty());
        ServicioException e = assertThrows(ServicioException.class, luis::listarCategorias);
        assertEquals(CodigoError.SESION, e.getCodigo());
    }

    @Test
    void desconexionNoTumbaElServidor() throws Exception {
        ProxyServicio ana = conSesion("F001");
        BlockingQueue<EventoReservaCreada> alertasCarla = escuchar(conSesion("F003"));
        conSesion("F002").desconectar();

        assertTrue(ana.crearReservacion(List.of(SALAS), "Clase", MANANA, MANANA.plusHours(1)).exito());
        assertNotNull(alertasCarla.poll(2, TimeUnit.SECONDS), "Los demás siguen recibiendo alertas");
        assertNotNull(cliente().iniciarSesion("ADMIN", "ADMIN"), "El servidor acepta nuevas conexiones");
    }

    @Test
    void clienteQueNoLeeNoFrenaAlAutorNiALosDemas() throws Exception {
        // Luis inicia sesión con un socket crudo y nunca vuelve a leer: su búfer TCP se llena.
        Socket luis = new Socket("localhost", servidor.getPuerto());
        ObjectOutputStream salidaLuis = new ObjectOutputStream(luis.getOutputStream());
        salidaLuis.flush();
        new ObjectInputStream(luis.getInputStream());
        salidaLuis.writeObject(new Peticion(1, Operacion.INICIAR_SESION, new Object[]{"F002", "F002"}));
        salidaLuis.flush();
        Thread.sleep(200);

        ProxyServicio ana = conSesion("F001");
        BlockingQueue<EventoReservaCreada> alertasCarla = escuchar(conSesion("F003"));
        for (int i = 0; i < 400; i++) {
            LocalDateTime inicio = MANANA.plusDays(i);
            assertTrue(ana.crearReservacion(List.of(SALAS), "Clase " + i, inicio, inicio.plusHours(1)).exito());
        }

        assertFalse(ana.listarCategorias().isEmpty(), "El autor sigue atendido");
        for (int i = 0; i < 400; i++) {
            assertNotNull(alertasCarla.poll(2, TimeUnit.SECONDS), "Carla recibe todas las alertas");
        }
        luis.close();
    }

    @Test
    void servidorRechazaReservaSinCategorias() throws Exception {
        ProxyServicio ana = conSesion("F001");
        for (List<String> categorias : Arrays.asList(null, List.<String>of())) {
            ServicioException e = assertThrows(ServicioException.class,
                    () -> ana.crearReservacion(categorias, "Clase", MANANA, MANANA.plusHours(1)));
            assertEquals(CodigoError.VALIDACION, e.getCodigo());
        }
    }

    @Test
    void contarPorCategoriaViajaPorLaRed() throws Exception {
        ProxyServicio ana = conSesion("F001");
        ana.crearReservacion(List.of(SALAS), "Clase", MANANA, MANANA.plusHours(1));

        List<EstadisticaCategoriaDTO> conteo = ana.contarPorCategoria(MANANA.toLocalDate(), MANANA.toLocalDate());

        assertEquals(2, conteo.size());
        assertEquals(1, conteo.get(0).cantidad());
        assertEquals(0, conteo.get(1).cantidad());
    }

    @Test
    void solicitudesConcurrentesNoMezclanRespuestas() throws Exception {
        ProxyServicio admin = conSesion("ADMIN");
        List<CompletableFuture<Boolean>> verificaciones = new ArrayList<>();
        for (String id : List.of("F001", "F002", "F003")) {
            for (int i = 0; i < 20; i++) {
                verificaciones.add(CompletableFuture.supplyAsync(() -> {
                    FuncionarioDTO f = admin.buscarFuncionario(id);
                    return f.id().equals(id);
                }));
            }
        }
        for (CompletableFuture<Boolean> v : verificaciones) {
            assertTrue(v.get(5, TimeUnit.SECONDS));
        }
    }

    private static void esperar(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
