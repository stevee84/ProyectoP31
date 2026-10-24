package red;

import contrato.IServicioAplicacion;
import dto.ResultadoReservaDTO;
import falso.FabricaServiciosFalsa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import protocolo.CodigoError;
import protocolo.EventoReservaCreada;
import protocolo.Operacion;
import protocolo.Peticion;
import protocolo.Respuesta;
import servicio.ContextoSesion;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DespachadorTest {

    private static final String SALAS = "CAT-000001";
    private static final LocalDateTime MANANA = LocalDate.now().plusDays(1).atTime(9, 0);

    private ContextoSesion contexto;
    private PublicadorEventos publicador;
    private Despachador despachador;
    private final List<EventoReservaCreada> alertasLuis = new ArrayList<>();
    private final List<EventoReservaCreada> alertasAna = new ArrayList<>();

    @BeforeEach
    void preparar() {
        contexto = new ContextoSesion();
        publicador = new PublicadorEventos();
        publicador.agregar(observador("F001", alertasAna));
        publicador.agregar(observador("F002", alertasLuis));
        despachador = new Despachador(new FabricaServiciosFalsa().crear(contexto), contexto, publicador);
    }

    private static IObservadorEventos observador(String id, List<EventoReservaCreada> alertas) {
        return new IObservadorEventos() {
            public String getIdUsuario() {
                return id;
            }

            public void actualizar(EventoReservaCreada evento) {
                alertas.add(evento);
            }
        };
    }

    private Respuesta atender(Operacion operacion, Object... datos) {
        return despachador.atender(new Peticion(7, operacion, datos));
    }

    private Respuesta reservar() {
        return atender(Operacion.CREAR_RESERVACION, List.of(SALAS), "Clase", MANANA, MANANA.plusHours(1));
    }

    private static void assertError(Respuesta respuesta, CodigoError codigo) {
        assertFalse(respuesta.exito());
        assertEquals(codigo, respuesta.codigoError());
        assertEquals(7, respuesta.idPeticion());
    }

    /** Servicio que falla con un error inesperado en cualquier operación salvo el inicio de sesión. */
    private static IServicioAplicacion conFalla(IServicioAplicacion real, RuntimeException falla) {
        return (IServicioAplicacion) Proxy.newProxyInstance(IServicioAplicacion.class.getClassLoader(),
                new Class<?>[]{IServicioAplicacion.class}, (p, metodo, args) -> {
                    if (!metodo.getName().equals("iniciarSesion")) {
                        throw falla;
                    }
                    try {
                        return metodo.invoke(real, args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                });
    }

    @Test
    void sinSesionSeRechaza() {
        assertError(atender(Operacion.LISTAR_CATEGORIAS), CodigoError.SESION);
    }

    @Test
    void operacionNulaEsPeticionMalFormada() {
        Respuesta respuesta = atender(null);
        assertError(respuesta, CodigoError.VALIDACION);
        assertEquals("Petición mal formada.", respuesta.mensaje());
    }

    @Test
    void datosNulosOIncorrectosSonPeticionMalFormada() {
        assertError(despachador.atender(new Peticion(7, Operacion.INICIAR_SESION, null)), CodigoError.VALIDACION);
        assertError(atender(Operacion.INICIAR_SESION, "F001"), CodigoError.VALIDACION);
        assertError(atender(Operacion.INICIAR_SESION, 1, 2), CodigoError.VALIDACION);
        assertFalse(contexto.estaAutenticado());
    }

    @Test
    void errorDelServicioConservaCodigoYMensaje() {
        atender(Operacion.INICIAR_SESION, "F001", "F001");
        Respuesta respuesta = atender(Operacion.REGISTRAR_CATEGORIA, "Proyector");
        assertError(respuesta, CodigoError.PERMISO);
        assertEquals("Solo el administrador puede realizar esta acción.", respuesta.mensaje());
    }

    @Test
    void errorInesperadoNoRevelaDetalles() {
        IServicioAplicacion servicio = conFalla(new FabricaServiciosFalsa().crear(contexto),
                new IllegalStateException("ORA-00942 tabla secreta"));
        despachador = new Despachador(servicio, contexto, publicador);
        atender(Operacion.INICIAR_SESION, "F001", "F001");

        Respuesta respuesta = atender(Operacion.LISTAR_CATEGORIAS);

        assertError(respuesta, CodigoError.INTERNO);
        assertEquals("Ocurrió un error interno en el servidor.", respuesta.mensaje());
    }

    @Test
    void iniciarYCerrarSesionActualizanElContexto() {
        assertTrue(atender(Operacion.INICIAR_SESION, "F001", "F001").exito());
        assertEquals("F001", contexto.getUsuario().id());

        assertTrue(atender(Operacion.CERRAR_SESION).exito());
        assertFalse(contexto.estaAutenticado());
    }

    @Test
    void reservaExitosaAlertaALosDemasYNoAlAutor() {
        atender(Operacion.INICIAR_SESION, "F001", "F001");

        Respuesta respuesta = reservar();

        assertTrue(((ResultadoReservaDTO) respuesta.resultado()).exito());
        assertEquals(1, alertasLuis.size());
        assertTrue(alertasAna.isEmpty());
    }

    @Test
    void reservaFallidaNoAlertaANadie() {
        atender(Operacion.INICIAR_SESION, "F001", "F001");
        reservar();
        alertasLuis.clear();

        Respuesta respuesta = reservar();

        assertFalse(((ResultadoReservaDTO) respuesta.resultado()).exito());
        assertTrue(alertasLuis.isEmpty() && alertasAna.isEmpty());
    }

    @Test
    void fallaDelPublicadorNoConvierteLaReservaEnError() {
        despachador = new Despachador(new FabricaServiciosFalsa().crear(contexto), contexto,
                new PublicadorEventos() {
                    @Override
                    public void notificar(EventoReservaCreada evento, String idAutor) {
                        throw new IllegalStateException("falla al difundir");
                    }
                });
        atender(Operacion.INICIAR_SESION, "F001", "F001");

        Respuesta respuesta = reservar();

        assertTrue(respuesta.exito());
        assertTrue(((ResultadoReservaDTO) respuesta.resultado()).exito());
    }
}
