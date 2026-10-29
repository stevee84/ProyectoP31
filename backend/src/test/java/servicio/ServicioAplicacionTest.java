package servicio;

import accesodatos.ProveedorConexion;
import contrato.IServicioAplicacion;
import dto.*;
import ia.ExtractorReservaIA;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import contrato.Reglas;
import protocolo.*;
import repositorio.*;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.function.BiFunction;
import static org.junit.jupiter.api.Assertions.*;

class ServicioAplicacionTest {
    private static final Clock RELOJ = Clock.fixed(Instant.parse("2030-03-11T12:00:00Z"), ZoneOffset.UTC);
    private static final LocalDateTime INICIO = LocalDateTime.of(2030, 3, 12, 9, 0);
    private static final CategoriaDTO CAT = new CategoriaDTO("CAT-000001", "Sala");
    private static final UsuarioDTO AUTOR = new UsuarioDTO("F1", "Ana", Rol.FUNCIONARIO);
    private final ContextoSesion contexto = new ContextoSesion();
    private int aperturas, commits, rollbacks, cierres;
    private boolean autocommit = true;

    @SuppressWarnings("unchecked")
    private static <T> T doble(Class<T> tipo, BiFunction<String, Object[], Object> accion) {
        return (T) Proxy.newProxyInstance(tipo.getClassLoader(), new Class<?>[]{tipo},
                (p, metodo, args) -> accion.apply(metodo.getName(), args));
    }

    private Connection conexion() {
        aperturas++;
        return doble(Connection.class, (metodo, args) -> {
            switch (metodo) {
                case "setAutoCommit" -> autocommit = (boolean) args[0];
                case "commit" -> commits++;
                case "rollback" -> rollbacks++;
                case "close" -> cierres++;
                default -> fail("Llamada JDBC no esperada: " + metodo);
            }
            return null;
        });
    }

    private FabricaRepositoriosOracle repositorios() {
        return new FabricaRepositoriosOracle() {
            @Override public IRepositorioCategorias categorias(Connection c) {
                return doble(IRepositorioCategorias.class, (m, a) -> switch (m) {
                    case "listar" -> List.of(CAT);
                    case "buscar" -> CAT.id().equals(a[0]) ? CAT : null;
                    default -> throw new AssertionError(m);
                });
            }
        };
    }

    private IServicioAplicacion servicio(FabricaRepositoriosOracle repos, ExtractorReservaIA extractor) {
        return new FabricaServiciosOracle(this::conexion, repos, extractor, RELOJ).crear(contexto);
    }

    private IServicioAplicacion servicio() {
        return servicio(repositorios(), (f, cats) -> ResultadoExtraccionDTO.fallo("Sin IA de prueba"));
    }

    private static void error(CodigoError codigo, Executable accion) {
        assertEquals(codigo, assertThrows(ServicioException.class, accion).getCodigo());
    }

    @Test
    void todosLosMetodosSalvoLoginExigenSesionAntesDeValidarOAccederABase() throws Exception {
        IServicioAplicacion s = servicio();
        for (var metodo : IServicioAplicacion.class.getMethods()) {
            if (metodo.getName().equals("iniciarSesion")) continue;
            Object[] argumentos = Arrays.stream(metodo.getParameterTypes()).map(t -> t == int.class ? 1 : null).toArray();
            var e = assertThrows(java.lang.reflect.InvocationTargetException.class, () -> metodo.invoke(s, argumentos));
            assertEquals(CodigoError.SESION, ((ServicioException) e.getCause()).getCodigo(), metodo.getName());
        }
        assertEquals(0, aperturas);
    }

    @Test
    void consultasDeCatalogoPermitidasAlFuncionarioYAdministracionProtegida() {
        contexto.iniciar(AUTOR);
        IServicioAplicacion s = servicio();
        assertEquals(List.of(CAT), s.listarCategorias());
        error(CodigoError.PERMISO, () -> s.registrarCategoria("Sala"));
        error(CodigoError.PERMISO, () -> s.listarFuncionarios());
        error(CodigoError.PERMISO, () -> s.actualizarRecurso("R", CAT.id(), "Recurso"));
        contexto.iniciar(new UsuarioDTO("A", "Admin", Rol.ADMINISTRADOR));
        error(CodigoError.PERMISO, () -> s.crearReservacion(List.of(CAT.id()), "Clase", INICIO, INICIO.plusHours(1)));
        error(CodigoError.PERMISO, () -> s.extraerDatosDesdeFrase("Mañana"));
    }

    @Test
    void loginNoEscribeContextoYCierreLoDejaParaDespachador() {
        var resultado = new ResultadoSesionDTO(AUTOR, true);
        FabricaRepositoriosOracle repos = new FabricaRepositoriosOracle() {
            @Override public IRepositorioFuncionarios funcionarios(Connection c) {
                return doble(IRepositorioFuncionarios.class, (m, a) -> "correcta".equals(a[1]) ? resultado : null);
            }
        };
        IServicioAplicacion s = servicio(repos, (f, cats) -> null);
        assertEquals(resultado, s.iniciarSesion("F1", "correcta"));
        assertFalse(contexto.estaAutenticado());
        error(CodigoError.SESION, () -> s.iniciarSesion("F1", "incorrecta"));
        error(CodigoError.SESION, () -> s.iniciarSesion(null, "x"));
        contexto.iniciar(AUTOR);
        s.cerrarSesion();
        assertSame(AUTOR, contexto.getUsuario());
    }

    @Test
    void falloAlConectarNoSeDisfrazaDeCredencialesIncorrectas() {
        ProveedorConexion caida = () -> { throw new SQLException("SQL y secreto privados", "08006", 17002); };
        IServicioAplicacion s = new FabricaServiciosOracle(caida, repositorios(), (f, c) -> null, RELOJ).crear(contexto);
        ServicioException e = assertThrows(ServicioException.class, () -> s.iniciarSesion("F1", "clave"));
        assertEquals(CodigoError.PERSISTENCIA, e.getCodigo());
        assertFalse(e.getMessage().contains("secreto"));
    }

    @Test
    void cambioDeClaveUsaIdentidadDeSesionYConservaEspaciosDeClaveValida() {
        contexto.iniciar(AUTOR);
        List<Object> recibidos = new ArrayList<>();
        FabricaRepositoriosOracle repos = new FabricaRepositoriosOracle() {
            @Override public IRepositorioFuncionarios funcionarios(Connection c) {
                return doble(IRepositorioFuncionarios.class, (m, a) -> { recibidos.addAll(Arrays.asList(a)); return null; });
            }
        };
        IServicioAplicacion s = servicio(repos, (f, cats) -> null);
        for (String clave : Arrays.asList(null, "", " \t ", "x".repeat(256))) error(CodigoError.VALIDACION, () -> s.cambiarClave(clave));
        s.cambiarClave(" clave nueva ");
        // Primero se comprueba que no sea la clave actual y luego se guarda.
        assertEquals(List.of("F1", " clave nueva ", "F1", " clave nueva "), recibidos);
        assertEquals(1, commits);
        assertEquals(1, cierres);
        assertFalse(autocommit);
    }

    @Test
    void nuevaClaveNoPuedeSerElIdNiLaClaveActual() {
        contexto.iniciar(AUTOR);
        List<String> guardadas = new ArrayList<>();
        FabricaRepositoriosOracle repos = new FabricaRepositoriosOracle() {
            @Override public IRepositorioFuncionarios funcionarios(Connection c) {
                return doble(IRepositorioFuncionarios.class, (m, a) -> switch (m) {
                    case "autenticar" -> "actual".equals(a[1]) ? new ResultadoSesionDTO(AUTOR, false) : null;
                    case "cambiarClave" -> { guardadas.add((String) a[1]); yield null; }
                    default -> throw new AssertionError(m);
                });
            }
        };
        IServicioAplicacion s = servicio(repos, (f, cats) -> null);
        ServicioException igualId = assertThrows(ServicioException.class, () -> s.cambiarClave(" f1 "));
        assertEquals(CodigoError.VALIDACION, igualId.getCodigo());
        assertEquals(Reglas.MENSAJE_CLAVE_IGUAL_ID, igualId.getMessage());
        assertEquals(0, aperturas, "Se rechaza sin abrir conexión");
        ServicioException igualActual = assertThrows(ServicioException.class, () -> s.cambiarClave("actual"));
        assertEquals(Reglas.MENSAJE_CLAVE_IGUAL_ACTUAL, igualActual.getMessage());
        assertEquals(1, rollbacks);
        s.cambiarClave("otra");
        assertEquals(List.of("otra"), guardadas);
    }

    @Test
    void telefonoConFormatoInvalidoSeRechazaSinAbrirConexion() {
        contexto.iniciar(new UsuarioDTO("A", "Admin", Rol.ADMINISTRADOR));
        IServicioAplicacion s = servicio();
        for (String telefono : List.of("abc-1234", "8888_1111", "123", "(506)88881111", "+-- -- --", "1".repeat(21))) {
            ServicioException e = assertThrows(ServicioException.class,
                    () -> s.registrarFuncionario(new FuncionarioDTO("F", "Ana", telefono)), telefono);
            assertEquals(Reglas.MENSAJE_TELEFONO, e.getMessage());
        }
        error(CodigoError.VALIDACION, () -> s.actualizarFuncionario(new FuncionarioDTO("F", "Ana", "88a8-1111")));
        for (String valido : List.of("8888-1111", "+506 8888 1111", "88881111"))
            assertTrue(Reglas.telefonoValido(valido), valido);
        assertEquals(0, aperturas);
    }

    @Test
    void validaCamposYLongitudesDelEsquemaSinAbrirConexion() {
        contexto.iniciar(new UsuarioDTO("A", "Admin", Rol.ADMINISTRADOR));
        IServicioAplicacion s = servicio();
        error(CodigoError.VALIDACION, () -> s.registrarFuncionario(null));
        error(CodigoError.VALIDACION, () -> s.registrarFuncionario(new FuncionarioDTO("F", " ", "123")));
        error(CodigoError.VALIDACION, () -> s.registrarFuncionario(new FuncionarioDTO("F".repeat(51), "Ana", "123")));
        error(CodigoError.VALIDACION, () -> s.actualizarFuncionario(new FuncionarioDTO("F", "N".repeat(151), "123")));
        error(CodigoError.VALIDACION, () -> s.registrarFuncionario(new FuncionarioDTO("F", "Ana", "1".repeat(31))));
        error(CodigoError.VALIDACION, () -> s.registrarCategoria("x".repeat(201)));
        error(CodigoError.VALIDACION, () -> s.actualizarCategoria(null));
        error(CodigoError.VALIDACION, () -> s.registrarRecurso("R", " ", "Recurso"));
        assertEquals("La descripción es obligatoria.",
                assertThrows(ServicioException.class, () -> s.registrarCategoria(" ")).getMessage());
        assertEquals("El nombre es obligatorio.", assertThrows(ServicioException.class,
                () -> s.registrarFuncionario(new FuncionarioDTO("F", " ", "123"))).getMessage());
        error(CodigoError.VALIDACION, () -> s.buscarRecurso("x".repeat(51)));
        assertEquals(0, aperturas);
    }

    @Test
    void validaCategoriasDescripcionYHorarioAntesDePersistir() {
        contexto.iniciar(AUTOR);
        IServicioAplicacion s = servicio();
        for (List<String> ids : Arrays.asList(null, List.<String>of(), Arrays.asList((String) null), List.of(" "), List.of("x".repeat(21))))
            error(CodigoError.VALIDACION, () -> s.crearReservacion(ids, "Clase", INICIO, INICIO.plusHours(1)));
        for (String descripcion : Arrays.asList(null, "  ", "x".repeat(501)))
            error(CodigoError.VALIDACION, () -> s.crearReservacion(List.of(CAT.id()), descripcion, INICIO, INICIO.plusHours(1)));
        LocalDateTime[][] invalidos = {{null, INICIO}, {INICIO, null}, {INICIO, INICIO}, {INICIO, INICIO.minusHours(1)},
                {INICIO.minusDays(2), INICIO.minusDays(2).plusHours(1)}, {INICIO, INICIO.plusDays(1)},
                {INICIO.withHour(6), INICIO}, {INICIO, INICIO.withHour(18).plusNanos(1)}};
        for (LocalDateTime[] rango : invalidos)
            error(CodigoError.VALIDACION, () -> s.crearReservacion(List.of(CAT.id()), "Clase", rango[0], rango[1]));
        assertEquals(0, aperturas);
    }

    @Test
    void validaRangosYFechasDeConsultas() {
        contexto.iniciar(AUTOR);
        IServicioAplicacion s = servicio();
        LocalDate fecha = INICIO.toLocalDate();
        error(CodigoError.VALIDACION, () -> s.contarPorSemana(null, fecha));
        error(CodigoError.VALIDACION, () -> s.contarPorSemana(fecha, fecha.minusDays(1)));
        error(CodigoError.VALIDACION, () -> s.contarPorCategoria(fecha, null));
        error(CodigoError.VALIDACION, () -> s.contarReservasCategoria(CAT.id(), fecha.plusDays(1), fecha));
        error(CodigoError.VALIDACION, () -> s.obtenerAgendaSemana(null));
        error(CodigoError.VALIDACION, () -> s.obtenerCalendarizacion(null, CAT.id()));
        assertEquals(0, aperturas);
    }

    @Test
    void categoriaInexistenteEsValidacionYDeshaceOperacion() {
        contexto.iniciar(AUTOR);
        error(CodigoError.VALIDACION, () -> servicio().crearReservacion(List.of("CAT-X"), "Clase", INICIO, INICIO.plusHours(1)));
        assertEquals(1, rollbacks);
        assertEquals(0, commits);
        assertEquals(1, cierres);
    }

    @Test
    void extraccionRecibeCatalogoRealYSeEjecutaConConexionYaCerrada() {
        contexto.iniciar(AUTOR);
        IServicioAplicacion s = servicio(repositorios(), (frase, categorias) -> {
            assertEquals("mañana una sala", frase);
            assertEquals(List.of(CAT), categorias);
            assertEquals(aperturas, cierres);
            assertEquals(1, commits);
            return new ResultadoExtraccionDTO(true, null, INICIO.toLocalDate(), null, null, List.of(CAT.id(), CAT.id()), null);
        });
        var resultado = s.extraerDatosDesdeFrase("mañana una sala");
        assertTrue(resultado.exito());
        assertNull(resultado.horaInicio());
        assertEquals(List.of(CAT.id(), CAT.id()), resultado.idsCategorias());
    }

    @Test
    void extraccionInvalidaNoInventaCategoriasNiCreaReservas() {
        contexto.iniciar(AUTOR);
        IServicioAplicacion s = servicio(repositorios(), (frase, categorias) ->
                new ResultadoExtraccionDTO(true, "Clase", null, null, null, List.of("INVENTADA"), null));
        assertFalse(s.extraerDatosDesdeFrase("una sala").exito());
        error(CodigoError.VALIDACION, () -> s.extraerDatosDesdeFrase(" "));
        assertEquals(1, aperturas);
    }

    @Test
    void traduceErroresSegunOperacionSinRevelarMensajeDelDriver() {
        Map<Integer, CodigoError> escritura = Map.of(1, CodigoError.VALIDACION, 2291, CodigoError.VALIDACION,
                20001, CodigoError.VALIDACION, 20002, CodigoError.VALIDACION, 12899, CodigoError.VALIDACION,
                942, CodigoError.PERSISTENCIA, 1400, CodigoError.PERSISTENCIA);
        escritura.forEach((codigo, esperado) -> {
            var e = ErroresOracle.traducir(new SQLException("SQL privado", "x", codigo), ErroresOracle.Accion.ESCRITURA);
            assertEquals(esperado, e.getCodigo());
            assertFalse(e.getMessage().contains("SQL privado"));
        });
        assertEquals(CodigoError.RECURSO_EN_USO, ErroresOracle.traducir(new SQLException("", "", 2292), ErroresOracle.Accion.ELIMINACION).getCodigo());
        assertEquals("Ya existe un registro con ese identificador.",
                ErroresOracle.traducir(new SQLException("", "", 1), ErroresOracle.Accion.ESCRITURA).getMessage());
        assertEquals("No existe un registro con ese identificador.",
                ErroresOracle.traducir(new SQLException("", "", 20001), ErroresOracle.Accion.ELIMINACION).getMessage());
        assertFalse(ErroresOracle.traducir(new SQLException("", "", 20002), ErroresOracle.Accion.ESCRITURA)
                .getMessage().contains("horario"));
        assertTrue(ErroresOracle.traducir(new SQLException("", "", 20002), ErroresOracle.Accion.RESERVA)
                .getMessage().contains("horario"));
        assertEquals(CodigoError.PERSISTENCIA, ErroresOracle.traducir(new SQLException("", "", 2292), ErroresOracle.Accion.CONSULTA).getCodigo());
        assertEquals(CodigoError.PERMISO, ErroresOracle.traducir(new SQLException("", "", 20003), ErroresOracle.Accion.CANCELACION).getCodigo());
        assertEquals(CodigoError.VALIDACION, ErroresOracle.traducir(new SQLException("", "", 20003), ErroresOracle.Accion.RESERVA).getCodigo());
        assertEquals(CodigoError.DISPONIBILIDAD, ErroresOracle.traducir(new SQLException("", "", 20004), ErroresOracle.Accion.RESERVA).getCodigo());
        assertEquals(CodigoError.PERSISTENCIA, ErroresOracle.traducir(new SQLException("", "", 20004), ErroresOracle.Accion.ESCRITURA).getCodigo());
    }
}
