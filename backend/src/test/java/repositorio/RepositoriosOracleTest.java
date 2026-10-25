package repositorio;

import dto.*;
import oracle.jdbc.OracleTypes;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/** Contrasta llamadas, parámetros y columnas con DB/02 sin requerir Oracle. */
class RepositoriosOracleTest {
    private final List<Llamada> llamadas = new ArrayList<>();
    private final Map<String, List<Map<String, Object>>> filas = new HashMap<>();
    private int sentenciasCerradas, cursoresAbiertos, cursoresCerrados;
    private final LocalDateTime inicio = LocalDateTime.of(2030, 3, 12, 9, 0);

    private static final class Llamada {
        final String sql;
        final Map<Integer, Object> parametros = new HashMap<>();
        final Map<Integer, Integer> salidas = new HashMap<>();
        Llamada(String sql) { this.sql = sql; }
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> tipo, java.lang.reflect.InvocationHandler h) {
        return (T) Proxy.newProxyInstance(tipo.getClassLoader(), new Class<?>[]{tipo}, h);
    }

    private Connection conexion() {
        return proxy(Connection.class, (p, m, args) -> {
            assertEquals("prepareCall", m.getName(), "El repositorio no debe cerrar ni confirmar la conexión");
            Llamada llamada = new Llamada((String) args[0]);
            llamadas.add(llamada);
            return proxy(CallableStatement.class, (s, metodo, a) -> {
                switch (metodo.getName()) {
                    case "setString", "setInt", "setTimestamp", "setDate" -> llamada.parametros.put((int) a[0], a[1]);
                    case "registerOutParameter" -> llamada.salidas.put((int) a[0], (int) a[1]);
                    case "execute" -> { return true; }
                    case "getInt" -> { return 42; }
                    case "getObject" -> {
                        assertEquals(OracleTypes.CURSOR, llamada.salidas.get((int) a[0]));
                        return cursor(filas.getOrDefault(llamada.sql, List.of()));
                    }
                    case "close" -> sentenciasCerradas++;
                    default -> throw new AssertionError(metodo.getName());
                }
                return null;
            });
        });
    }

    private ResultSet cursor(List<Map<String, Object>> datos) {
        cursoresAbiertos++;
        int[] fila = {-1};
        return proxy(ResultSet.class, (p, m, a) -> {
            if (m.getName().equals("next")) return ++fila[0] < datos.size();
            if (m.getName().equals("close")) { cursoresCerrados++; return null; }
            assertTrue(datos.get(fila[0]).containsKey(a[0]), "Columna incorrecta: " + a[0]);
            return datos.get(fila[0]).get(a[0]);
        });
    }

    private void cerrados() {
        assertEquals(llamadas.size(), sentenciasCerradas);
        assertEquals(cursoresAbiertos, cursoresCerrados);
    }

    private Map<String, Object> recurso() {
        return Map.of("CODIGO", "R1", "ID_CATEGORIA", "CAT-1", "DESCRIPCION", "Sala A", "DESCRIPCION_CATEGORIA", "Salas");
    }

    @Test
    void autenticacionUsaCuatroColumnasSinClaveYCrudEnviaTresParametros() throws Exception {
        String sql = "{? = call validarUsuario(?, ?)}";
        filas.put(sql, List.of(Map.of("ID", "F1", "NOMBRE", "Ana", "ROL", "FUNCIONARIO", "PRIMER_INGRESO", 1)));
        var repo = new RepositorioFuncionariosOracle(conexion());
        assertEquals(new ResultadoSesionDTO(new UsuarioDTO("F1", "Ana", Rol.FUNCIONARIO), true), repo.autenticar("F1", "clave"));
        assertEquals(Map.of(2, "F1", 3, "clave"), llamadas.get(0).parametros);
        filas.put(sql, List.of());
        assertNull(repo.autenticar("F1", "incorrecta"));
        repo.insertar(new FuncionarioDTO("F2", "Luis", "123"));
        assertEquals("{call insertarFuncionario(?, ?, ?)}", llamadas.get(2).sql);
        assertEquals(Map.of(1, "F2", 2, "Luis", 3, "123"), llamadas.get(2).parametros);
        repo.modificar(new FuncionarioDTO("F2", "Luis nuevo", "456"));
        repo.cambiarClave("F2", "nueva");
        assertEquals("{call cambiarClaveUsuario(?, ?)}", llamadas.get(4).sql);
        assertEquals(Map.of(1, "F2", 2, "nueva"), llamadas.get(4).parametros);
        repo.eliminar("F2");
        assertNull(repo.buscar("F2"));
        assertTrue(repo.listar().isEmpty());
        cerrados();
    }

    @Test
    void recursosMapeanCuatroColumnasYDisponibilidadEnviaTimestamps() throws Exception {
        String sql = "{? = call listarRecursosDisponibles(?, ?, ?)}";
        filas.put(sql, List.of(recurso()));
        var repo = new RepositorioRecursosOracle(conexion());
        var lista = repo.disponibles("CAT-1", inicio, inicio.plusHours(1));
        assertEquals(List.of(new RecursoDTO("R1", new CategoriaDTO("CAT-1", "Salas"), "Sala A")), lista);
        assertEquals(Map.of(2, "CAT-1", 3, Timestamp.valueOf(inicio), 4, Timestamp.valueOf(inicio.plusHours(1))), llamadas.get(0).parametros);
        assertThrows(UnsupportedOperationException.class, lista::clear);
        repo.insertar("R2", "CAT-1", "Sala B");
        repo.modificar("R2", "CAT-1", "Sala nueva");
        repo.eliminar("R2");
        assertNull(repo.buscar("R2"));
        assertTrue(repo.listar().isEmpty());
        assertTrue(repo.listarPorCategoria("CAT-2").isEmpty());
        cerrados();
    }

    @Test
    void reservaUsaCincoParametrosMapeaEstadoYCompletaRecursos() throws Exception {
        filas.put("{? = call buscarReserva(?)}", List.of(Map.of("ID", 42, "ID_USUARIO", "F1", "NOMBRE_USUARIO", "Ana",
                "ROL", "FUNCIONARIO", "DESCRIPCION_ACTIVIDAD", "Clase", "INICIO", Timestamp.valueOf(inicio),
                "FIN", Timestamp.valueOf(inicio.plusHours(1)), "ESTADO", "CANCELADA")));
        filas.put("{? = call listarRecursosReserva(?)}", List.of(recurso()));
        var repo = new RepositorioReservasOracle(conexion());
        assertEquals(42, repo.insertar("F1", "Clase", inicio, inicio.plusHours(1)));
        assertEquals(Types.INTEGER, llamadas.get(0).salidas.get(5));
        assertEquals(Map.of(1, "F1", 2, "Clase", 3, Timestamp.valueOf(inicio), 4, Timestamp.valueOf(inicio.plusHours(1))), llamadas.get(0).parametros);
        repo.asignar(42, "R1");
        assertEquals(Map.of(1, 42, 2, "R1"), llamadas.get(1).parametros);
        repo.cancelar(42, "F1");
        var reserva = repo.buscar(42);
        assertEquals(EstadoReserva.CANCELADA, reserva.estado());
        assertEquals(inicio, reserva.inicio());
        assertEquals(1, reserva.recursos().size());
        assertTrue(repo.listarUsuario("OTRO").isEmpty());
        assertTrue(repo.listarEnRango(inicio, inicio.plusDays(1)).isEmpty());
        cerrados();
    }

    @Test
    void estadisticasEnvianFechasYMapeanCantidadYCategoriaCero() throws Exception {
        filas.put("{? = call contarPorCategoria(?, ?)}", List.of(Map.of("ID_CATEGORIA", "CAT-1", "DESCRIPCION_CATEGORIA", "Salas", "CANTIDAD", 0)));
        filas.put("{? = call contarPorSemana(?, ?)}", List.of(Map.of("INICIO_SEMANA", java.sql.Date.valueOf("2030-03-11"),
                "FIN_SEMANA", java.sql.Date.valueOf("2030-03-17"), "CANTIDAD", 3)));
        var repo = new RepositorioConsultasOracle(conexion());
        var fecha = inicio.toLocalDate();
        assertEquals(List.of(new EstadisticaCategoriaDTO(new CategoriaDTO("CAT-1", "Salas"), 0)), repo.contarPorCategoria(fecha, fecha));
        assertEquals(Map.of(2, java.sql.Date.valueOf(fecha), 3, java.sql.Date.valueOf(fecha)), llamadas.get(0).parametros);
        assertEquals(3, repo.contarPorSemana(fecha, fecha).get(0).cantidad());
        cerrados();
    }
}
