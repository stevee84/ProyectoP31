package servicio;

import contrato.IServicioAplicacion;
import dto.*;
import org.junit.jupiter.api.Test;
import protocolo.*;
import repositorio.*;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/** Comprueba la coordinación del servicio sin sustituir las IT reales contra Oracle. */
class ReservasTransaccionTest {
    private final CategoriaDTO cat = new CategoriaDTO("CAT-1", "Sala");
    private final UsuarioDTO usuario = new UsuarioDTO("F", "Ana", Rol.FUNCIONARIO);
    private final LocalDateTime inicio = LocalDateTime.of(2030, 3, 12, 7, 0);
    private final Clock reloj = Clock.fixed(Instant.parse("2030-03-11T12:00:00Z"), ZoneOffset.UTC);
    private final List<RecursoDTO> libres = new ArrayList<>();
    private final List<String> eventos = new ArrayList<>();
    private final List<RecursoDTO> asignados = new ArrayList<>();
    private ReservaDTO cabecera;
    private int fallarEnAsignacion;
    private boolean fallarCommit;
    private boolean fallarRollback;
    private boolean fallarClose;
    private Connection conexionEsperada;

    @FunctionalInterface
    interface Llamada { Object ejecutar(String metodo, Object[] args) throws SQLException; }

    @SuppressWarnings("unchecked")
    private static <T> T doble(Class<T> tipo, Llamada llamada) {
        return (T) Proxy.newProxyInstance(tipo.getClassLoader(), new Class<?>[]{tipo},
                (p, m, args) -> llamada.ejecutar(m.getName(), args));
    }

    private IServicioAplicacion servicio() {
        ContextoSesion contexto = new ContextoSesion();
        contexto.iniciar(usuario);
        FabricaRepositoriosOracle repos = new FabricaRepositoriosOracle() {
            @Override public IRepositorioCategorias categorias(Connection c) {
                assertSame(conexionEsperada, c);
                return doble(IRepositorioCategorias.class, (m, a) -> cat);
            }
            @Override public IRepositorioRecursos recursos(Connection c) {
                assertSame(conexionEsperada, c);
                return doble(IRepositorioRecursos.class, (m, a) -> List.copyOf(libres));
            }
            @Override public IRepositorioReservas reservas(Connection c) {
                assertSame(conexionEsperada, c);
                return doble(IRepositorioReservas.class, (m, a) -> {
                    switch (m) {
                        case "insertar" -> {
                            eventos.add("insertar");
                            cabecera = new ReservaDTO(1, usuario, List.of(), (String) a[1], (LocalDateTime) a[2],
                                    (LocalDateTime) a[3], EstadoReserva.ACTIVA);
                            return 1;
                        }
                        case "asignar" -> {
                            eventos.add("asignar " + a[1]);
                            if (fallarEnAsignacion > 0 && asignados.size() + 1 == fallarEnAsignacion)
                                throw new SQLException("detalle interno que no viaja al cliente", "", 20004);
                            asignados.add(libres.stream().filter(r -> r.codigo().equals(a[1])).findFirst().orElseThrow());
                            return null;
                        }
                        case "buscar" -> { return new ReservaDTO(cabecera.id(), cabecera.autor(), asignados,
                                cabecera.descripcionActividad(), cabecera.inicio(), cabecera.fin(), cabecera.estado()); }
                        default -> throw new AssertionError(m);
                    }
                });
            }
        };
        return new FabricaServiciosOracle(() -> {
            conexionEsperada = doble(Connection.class, (m, a) -> {
                switch (m) {
                    case "setAutoCommit" -> assertEquals(false, a[0]);
                    case "commit" -> {
                        if (fallarCommit) throw new SQLException("commit fallido", "08006", 17002);
                        eventos.add("commit");
                    }
                    case "rollback" -> {
                        eventos.add("rollback");
                        if (fallarRollback) throw new SQLException("rollback fallido", "08006", 17002);
                        cabecera = null;
                        asignados.clear();
                    }
                    case "close" -> {
                        eventos.add("close");
                        if (fallarClose) throw new SQLException("close fallido", "08006", 17002);
                    }
                    default -> throw new AssertionError(m);
                }
                return null;
            });
            return conexionEsperada;
        }, repos, (f, c) -> null, reloj).crear(contexto);
    }

    @Test
    void repetidasAsignanDistintosEnOrdenEstableYCommitPrecedeAlResultado() {
        libres.add(new RecursoDTO("Z", cat, "Último"));
        libres.add(new RecursoDTO("A", cat, "Primero"));
        var resultado = servicio().crearReservacion(List.of(cat.id(), cat.id()), "Clase", inicio, inicio.withHour(18));
        assertTrue(resultado.exito());
        assertEquals(List.of("insertar", "asignar A", "asignar Z", "commit", "close"), eventos);
        assertEquals(List.of("A", "Z"), resultado.reserva().recursos().stream().map(RecursoDTO::codigo).toList());
    }

    @Test
    void insuficienciaNormalNoLlegaAInsertarNiAsignaDosVecesElMismoRecurso() {
        libres.add(new RecursoDTO("A", cat, "Sala"));
        var resultado = servicio().crearReservacion(List.of(cat.id(), cat.id(), cat.id()), "Clase", inicio, inicio.plusHours(1));
        assertFalse(resultado.exito());
        assertEquals(List.of(cat, cat), resultado.categoriasNoDisponibles());
        assertNull(cabecera);
        assertTrue(asignados.isEmpty());
        assertEquals(List.of("commit", "close"), eventos); // Transacción sin escrituras.
    }

    @Test
    void segundaAsignacionFallidaDeshaceTodoYTraduceError() {
        libres.add(new RecursoDTO("B", cat, "Sala B"));
        libres.add(new RecursoDTO("A", cat, "Sala A"));
        fallarEnAsignacion = 2;
        var s = servicio();
        var e = assertThrows(ServicioException.class, () -> s.crearReservacion(List.of(cat.id(), cat.id()), "Clase", inicio, inicio.plusHours(1)));
        assertEquals(CodigoError.DISPONIBILIDAD, e.getCodigo());
        assertEquals(List.of("insertar", "asignar A", "asignar B", "rollback", "close"), eventos);
        assertNull(cabecera);
        assertTrue(asignados.isEmpty());
    }

    @Test
    void commitFallidoNoDevuelveExitoYIntentaRollback() {
        libres.add(new RecursoDTO("A", cat, "Sala"));
        fallarCommit = true;
        var s = servicio();
        var e = assertThrows(ServicioException.class, () -> s.crearReservacion(List.of(cat.id()), "Clase", inicio, inicio.plusHours(1)));
        assertEquals(CodigoError.PERSISTENCIA, e.getCodigo());
        assertEquals(List.of("insertar", "asignar A", "rollback", "close"), eventos);
        assertNull(cabecera);
    }

    @Test
    void falloDeCierrePosteriorAlCommitNoAnunciaFracasoDeReservaConfirmada() {
        libres.add(new RecursoDTO("A", cat, "Sala"));
        fallarClose = true;
        assertTrue(servicio().crearReservacion(List.of(cat.id()), "Clase", inicio, inicio.plusHours(1)).exito());
        assertEquals(List.of("insertar", "asignar A", "commit", "close"), eventos);
    }

    @Test
    void rollbackFallidoEsPersistenciaAunqueFallaOriginalSeaDisponibilidad() {
        libres.add(new RecursoDTO("A", cat, "Sala"));
        fallarEnAsignacion = 1;
        fallarRollback = true;
        var s = servicio();
        var e = assertThrows(ServicioException.class, () -> s.crearReservacion(List.of(cat.id()), "Clase", inicio, inicio.plusHours(1)));
        assertEquals(CodigoError.PERSISTENCIA, e.getCodigo());
        assertEquals(List.of("insertar", "asignar A", "rollback", "close"), eventos);
    }
}
