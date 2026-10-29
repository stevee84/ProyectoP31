package servicio;

import accesodatos.ConexionOracle;
import contrato.IServicioAplicacion;
import dto.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.function.Executable;
import protocolo.*;
import repositorio.*;
import soporte.DatosOracle;
import java.lang.reflect.*;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "ORACLE_IT", matches = "(?i)true")
class ServicioAplicacionOracleIT {
    private DatosOracle d;
    private FabricaServiciosOracle fabrica;
    private IServicioAplicacion autor, otro, admin;

    @BeforeEach
    void preparar() throws Exception {
        d = DatosOracle.crear();
        fabrica = new FabricaServiciosOracle();
        autor = d.sesion(fabrica, d.autor);
        otro = d.sesion(fabrica, d.otro);
        admin = d.sesion(fabrica, d.admin);
    }

    @AfterEach
    void limpiar() throws Exception { if (d != null) d.close(); }

    private static void error(CodigoError codigo, Executable accion) {
        assertEquals(codigo, assertThrows(ServicioException.class, accion).getCodigo());
    }

    private ResultadoReservaDTO reservar(IServicioAplicacion s, List<String> categorias, LocalDateTime inicio) {
        return s.crearReservacion(categorias, "Actividad temporal", inicio, inicio.plusHours(1));
    }

    @Test
    void confirmaCabeceraYAsignacionesVisiblesDesdeOtraConexion() throws Exception {
        var resultado = reservar(autor, List.of(d.categoriaA.id(), d.categoriaB.id()), d.inicio);
        assertTrue(resultado.exito());
        assertEquals(2, resultado.reserva().recursos().size());
        assertEquals(1, d.cantidadReservas());
        assertEquals(2, d.cantidadAsignaciones());
        assertEquals(List.of(resultado.reserva()), autor.listarReservacionesSesion());
        assertTrue(otro.listarReservacionesSesion().isEmpty());
        try (Connection c = ConexionOracle.abrir()) {
            assertEquals(resultado.reserva(), new RepositorioReservasOracle(c).buscar(resultado.reserva().id()));
        }
    }

    @Test
    void faltaNormalNoDejaCabeceraNiAsignacionesYValidaTodasLasCategorias() throws Exception {
        var resultado = reservar(autor, List.of(d.categoriaA.id(), d.categoriaVacia.id()), d.inicio);
        assertFalse(resultado.exito());
        assertEquals(List.of(d.categoriaVacia), resultado.categoriasNoDisponibles());
        assertEquals(0, d.cantidadReservas());
        assertEquals(0, d.cantidadAsignaciones());
        error(CodigoError.VALIDACION, () -> reservar(autor, List.of(d.categoriaVacia.id(), "CAT-INEXISTENTE"), d.inicio));
    }

    @Test
    void categoriasRepetidasExigenRecursosDiferentesYSeCuentanUnaSolaVez() throws Exception {
        List<String> dos = List.of(d.categoriaA.id(), d.categoriaA.id());
        assertFalse(reservar(autor, dos, d.inicio).exito());
        assertEquals(0, d.cantidadReservas());
        admin.registrarRecurso(d.recursoC, d.categoriaA.id(), "Segundo recurso temporal");
        var resultado = reservar(autor, dos, d.inicio);
        assertTrue(resultado.exito());
        assertEquals(2, resultado.reserva().recursos().stream().map(RecursoDTO::codigo).distinct().count());
        assertEquals(1, autor.contarReservasCategoria(d.categoriaA.id(), d.inicio.toLocalDate(), d.inicio.toLocalDate()));
    }

    @Test
    void falloRealEnSegundaAsignacionRevierteInclusoLaPrimera() throws Exception {
        AtomicInteger asignaciones = new AtomicInteger();
        FabricaRepositoriosOracle repos = new FabricaRepositoriosOracle() {
            @Override public IRepositorioReservas reservas(Connection c) {
                IRepositorioReservas real = super.reservas(c);
                return (IRepositorioReservas) Proxy.newProxyInstance(getClass().getClassLoader(),
                        new Class<?>[]{IRepositorioReservas.class}, (proxy, metodo, args) -> {
                            // Primera asignación REAL en Oracle; la segunda falla en la rutina REAL.
                            if (metodo.getName().equals("asignar") && asignaciones.incrementAndGet() == 2)
                                return invocar(real, metodo, new Object[]{args[0], "J2IT-NO-EXISTE-" + UUID.randomUUID()});
                            return invocar(real, metodo, args);
                        });
            }
        };
        var conFallo = new FabricaServiciosOracle(ConexionOracle::abrir, repos,
                (f, cats) -> ResultadoExtraccionDTO.fallo("Sin IA en pruebas"), Clock.systemDefaultZone());
        var servicio = d.sesion(conFallo, d.autor);
        error(CodigoError.VALIDACION, () -> reservar(servicio, List.of(d.categoriaA.id(), d.categoriaB.id()), d.inicio));
        assertEquals(2, asignaciones.get());
        assertEquals(0, d.cantidadReservas());
        assertEquals(0, d.cantidadAsignaciones());
        assertTrue(reservar(autor, List.of(d.categoriaA.id(), d.categoriaB.id()), d.inicio).exito());
    }

    private static Object invocar(Object real, Method metodo, Object[] args) throws Throwable {
        try { return metodo.invoke(real, args); } catch (InvocationTargetException e) { throw e.getCause(); }
    }

    @Test
    void solapamientoEstrictoYHorariosContiguos() {
        assertTrue(reservar(autor, List.of(d.categoriaA.id()), d.inicio).exito());
        assertFalse(reservar(otro, List.of(d.categoriaA.id()), d.inicio.plusMinutes(30)).exito());
        assertTrue(reservar(otro, List.of(d.categoriaA.id()), d.inicio.plusHours(1)).exito());
        assertEquals("Disponible", autor.obtenerCalendarizacion(d.inicio.toLocalDate(), d.categoriaA.id()).celdas().get(4).get(0));
    }

    @Test
    void cancelarPropiaAjenaRepetidaEIniciadaConservaHistorialYLiberaDisponibilidad() throws Exception {
        var r = reservar(autor, List.of(d.categoriaA.id()), d.inicio).reserva();
        error(CodigoError.PERMISO, () -> otro.cancelarReservacion(r.id()));
        autor.cancelarReservacion(r.id());
        error(CodigoError.VALIDACION, () -> autor.cancelarReservacion(r.id()));
        assertEquals(EstadoReserva.CANCELADA, autor.listarReservacionesSesion().get(0).estado());
        assertEquals(1, d.cantidadAsignaciones(), "Cancelar no borra relaciones históricas");
        assertTrue(reservar(otro, List.of(d.categoriaA.id()), d.inicio).exito());
        var iniciada = reservar(autor, List.of(d.categoriaB.id()), d.inicio).reserva();
        try (Connection c = ConexionOracle.abrir(); PreparedStatement s = c.prepareStatement(
                "UPDATE TRESERVAS SET INICIO=LOCALTIMESTAMP-INTERVAL '1' HOUR, FIN=LOCALTIMESTAMP+INTERVAL '1' HOUR WHERE ID=? AND ID_USUARIO=?")) {
            s.setInt(1, iniciada.id());
            s.setString(2, d.autor);
            assertEquals(1, s.executeUpdate());
        }
        error(CodigoError.VALIDACION, () -> autor.cancelarReservacion(iniciada.id()));
        error(CodigoError.VALIDACION, () -> autor.cancelarReservacion(Integer.MAX_VALUE));
    }

    @Test
    void estadisticasAgendaYCalendarioExcluyenCanceladasEIncluyenCeros() {
        LocalDate dia = d.inicio.toLocalDate();
        var base = autor.contarPorSemana(dia, dia).get(0).cantidad();
        var r = reservar(autor, List.of(d.categoriaA.id()), d.inicio).reserva();
        assertEquals(base + 1, autor.contarPorSemana(dia, dia).get(0).cantidad());
        assertEquals(1, autor.contarReservasCategoria(d.categoriaA.id(), dia, dia));
        assertEquals(0, autor.contarReservasCategoria(d.categoriaVacia.id(), dia, dia));
        assertTrue(autor.contarPorCategoria(dia, dia).contains(new EstadisticaCategoriaDTO(d.categoriaVacia, 0)));
        var agenda = autor.obtenerAgendaSemana(dia);
        assertEquals(dia.with(DayOfWeek.MONDAY), agenda.inicioSemana());
        assertTrue(agenda.actividades().get(dia.getDayOfWeek()).get(9).stream()
                .anyMatch(info -> info.nombreFuncionario().equals(r.autor().nombre()) && info.recursos().contains(d.descripcionA)));
        assertFalse(autor.obtenerCalendarizacion(dia, d.categoriaA.id()).celdas().get(2).get(0).equals("Disponible"));
        autor.cancelarReservacion(r.id());
        assertEquals(base, autor.contarPorSemana(dia, dia).get(0).cantidad());
        assertEquals(0, autor.contarReservasCategoria(d.categoriaA.id(), dia, dia));
        assertEquals("Disponible", autor.obtenerCalendarizacion(dia, d.categoriaA.id()).celdas().get(2).get(0));
        assertTrue(autor.obtenerAgendaSemana(dia).actividades().get(dia.getDayOfWeek()).get(9).stream()
                .noneMatch(info -> info.recursos().contains(d.descripcionA)));
        // Comparación con la consulta SQL real: no asumir que la BD del proyecto está vacía.
        try (Connection c = ConexionOracle.abrir()) {
            var existentes = new RepositorioConsultasOracle(c).contarPorSemana(dia, dia.plusWeeks(2));
            var completas = autor.contarPorSemana(dia, dia.plusWeeks(2));
            assertEquals(3, completas.size());
            for (var semana : completas) assertEquals(existentes.stream()
                    .filter(e -> e.inicioSemana().equals(semana.inicioSemana())).mapToInt(EstadisticaSemanaDTO::cantidad).findFirst().orElse(0), semana.cantidad());
        } catch (SQLException e) { fail(e); }
    }

    @Test
    void restriccionesNoBorranHistorialYDuplicadosSonValidacion() {
        reservar(autor, List.of(d.categoriaA.id()), d.inicio);
        error(CodigoError.RECURSO_EN_USO, () -> admin.eliminarFuncionario(d.autor));
        error(CodigoError.RECURSO_EN_USO, () -> admin.eliminarRecurso(d.recursoA));
        error(CodigoError.RECURSO_EN_USO, () -> admin.eliminarCategoria(d.categoriaA.id()));
        error(CodigoError.VALIDACION, () -> admin.registrarFuncionario(new FuncionarioDTO(d.autor, "Duplicado", "8888-0123")));
        error(CodigoError.VALIDACION, () -> admin.registrarRecurso(d.recursoA, d.categoriaA.id(), "Duplicado"));
        assertEquals(1, autor.listarReservacionesSesion().size());
    }

    @Test
    void cambioClaveSeConfirmaYActualizarFuncionarioNoLaSobrescribe() {
        autor.cambiarClave("Nueva-clave-temporal");
        admin.actualizarFuncionario(new FuncionarioDTO(d.autor, "Nombre actualizado", "8888-1234"));
        IServicioAplicacion nuevo = fabrica.crear(new ContextoSesion());
        error(CodigoError.SESION, () -> nuevo.iniciarSesion(d.autor, d.autor));
        var login = nuevo.iniciarSesion(d.autor, "Nueva-clave-temporal");
        assertFalse(login.requiereCambioClave());
        assertEquals("Nombre actualizado", login.usuario().nombre());
    }

    @Test
    void clavesSeGuardanConHashYNuncaEnTextoPlano() throws Exception {
        String nueva = "Clave-IT-" + UUID.randomUUID();
        error(CodigoError.VALIDACION, () -> autor.cambiarClave(d.autor));
        autor.cambiarClave(nueva);
        error(CodigoError.VALIDACION, () -> autor.cambiarClave(nueva));
        try (Connection c = ConexionOracle.abrir(); PreparedStatement s = c.prepareStatement(
                "SELECT ID, CLAVE FROM TUSUARIOS WHERE ID IN (?,?,?)")) {
            s.setString(1, d.autor);
            s.setString(2, d.otro);
            s.setString(3, d.admin);
            try (ResultSet r = s.executeQuery()) {
                int filas = 0;
                while (r.next()) {
                    filas++;
                    String clave = r.getString("CLAVE");
                    assertTrue(clave.startsWith("pbkdf2$"), r.getString("ID"));
                    assertFalse(clave.contains(r.getString("ID")), "La columna no contiene la clave inicial");
                    assertFalse(clave.contains(nueva), "La columna no contiene la clave nueva");
                }
                assertEquals(3, filas);
            }
        }
        IServicioAplicacion nuevo = fabrica.crear(new ContextoSesion());
        assertFalse(nuevo.iniciarSesion(d.autor, nueva).requiereCambioClave());
        error(CodigoError.SESION, () -> nuevo.iniciarSesion(d.autor, d.autor));
        assertTrue(nuevo.iniciarSesion(d.otro, d.otro).requiereCambioClave(), "Clave inicial = id, verificada con hash");
    }

    @Test
    void descripcionDeCategoriaEsUnicaSinDistinguirMayusculasNiEspacios() {
        String repetida = "  " + d.categoriaA.descripcion().toUpperCase() + " ";
        ServicioException alta = assertThrows(ServicioException.class, () -> admin.registrarCategoria(repetida));
        assertEquals(CodigoError.VALIDACION, alta.getCodigo());
        assertEquals("Ya existe una categoría con esa descripción.", alta.getMessage());
        ServicioException cambio = assertThrows(ServicioException.class, () -> admin.actualizarCategoria(
                new CategoriaDTO(d.categoriaB.id(), d.categoriaA.descripcion().toLowerCase())));
        assertEquals("Ya existe una categoría con esa descripción.", cambio.getMessage());
        admin.actualizarCategoria(new CategoriaDTO(d.categoriaB.id(), d.categoriaB.descripcion() + " "));
        assertEquals(d.categoriaB, admin.buscarCategoria(d.categoriaB.id()), "Conservar la propia descripción se permite");
        error(CodigoError.VALIDACION, () -> admin.registrarFuncionario(new FuncionarioDTO(d.autor + "X", "Sin formato", "88-AB")));
    }

    @Test
    @Timeout(60)
    void dosServiciosDistintosCompitenPorUltimoRecursoSinResiduos() throws Exception {
        ExecutorService hilos = Executors.newFixedThreadPool(2);
        CountDownLatch preparados = new CountDownLatch(2);
        CountDownLatch salida = new CountDownLatch(1);
        try {
            Callable<ResultadoReservaDTO> a = () -> { preparados.countDown(); salida.await(); return reservar(autor, List.of(d.categoriaA.id(), d.categoriaB.id()), d.inicio); };
            Callable<ResultadoReservaDTO> b = () -> { preparados.countDown(); salida.await(); return reservar(otro, List.of(d.categoriaA.id()), d.inicio); };
            Future<ResultadoReservaDTO> fa = hilos.submit(a);
            Future<ResultadoReservaDTO> fb = hilos.submit(b);
            assertTrue(preparados.await(5, TimeUnit.SECONDS));
            salida.countDown();
            var ra = fa.get(40, TimeUnit.SECONDS);
            var rb = fb.get(40, TimeUnit.SECONDS);
            assertTrue(ra.exito() ^ rb.exito());
            assertEquals(1, d.cantidadReservas());
            assertEquals(ra.exito() ? 2 : 1, d.cantidadAsignaciones());
            assertTrue((ra.exito() ? otro : autor).listarReservacionesSesion().isEmpty());
        } finally {
            salida.countDown();
            hilos.shutdownNow();
            assertTrue(hilos.awaitTermination(10, TimeUnit.SECONDS));
        }
    }
}
