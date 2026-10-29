package soporte;

import accesodatos.ConexionOracle;
import contrato.IServicioAplicacion;
import dto.*;
import repositorio.*;
import seguridad.ClaveHash;
import servicio.ContextoSesion;
import servicio.IFabricaServicios;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Datos propios confirmados para pruebas con varias conexiones. Nunca usa ADMIN. */
public final class DatosOracle implements AutoCloseable {
    private final String marca = "J2IT-" + UUID.randomUUID().toString().replace("-", "");
    public final String admin = marca + "-A";
    public final String autor = marca + "-F";
    public final String otro = marca + "-G";
    public final String recursoA = marca + "-R";
    public final String recursoB = marca + "-S";
    public final String recursoC = marca + "-T";
    public final String descripcionA = "Recurso temporal A " + marca;
    public final String descripcionB = "Recurso temporal B " + marca;
    public CategoriaDTO categoriaA, categoriaB, categoriaVacia;
    public LocalDateTime inicio;
    private final List<String> categorias = new ArrayList<>();
    // Si la JVM se detiene a mitad de una prueba (Ctrl+C, fin del proceso), limpiar igual.
    private final Thread limpiezaAlSalir = new Thread(this::limpiarAlSalir, "limpieza-J2IT");
    private boolean cerrado;

    private DatosOracle() { }

    public static DatosOracle crear() throws SQLException {
        DatosOracle d = new DatosOracle();
        Runtime.getRuntime().addShutdownHook(d.limpiezaAlSalir);
        try {
            d.inicializar();
            return d;
        } catch (SQLException | RuntimeException e) {
            // También cubre un fallo al cerrar la conexión después del commit de preparación.
            try { d.close(); } catch (SQLException limpieza) { e.addSuppressed(limpieza); }
            throw e;
        }
    }

    private void inicializar() throws SQLException {
        try (Connection c = ConexionOracle.abrir()) {
            c.setAutoCommit(false);
            try {
                // Fecha del propio Oracle para que las pruebas no dependan del reloj de Windows/WSL.
                try (Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT LOCALTIMESTAMP FROM DUAL")) {
                    r.next();
                    inicio = r.getTimestamp(1).toLocalDateTime().toLocalDate().plusDays(14).atTime(9, 0);
                }
                try (PreparedStatement s = c.prepareStatement("INSERT INTO TUSUARIOS (ID,NOMBRE,CLAVE,ROL,PRIMER_INGRESO) VALUES (?,?,?,'ADMINISTRADOR',1)")) {
                    s.setString(1, admin);
                    s.setString(2, "Administrador temporal " + marca);
                    s.setString(3, ClaveHash.generar(admin));
                    s.executeUpdate();
                }
                IRepositorioFuncionarios funcionarios = new RepositorioFuncionariosOracle(c);
                funcionarios.insertar(new FuncionarioDTO(autor, "Autor temporal", "8888-1111"));
                funcionarios.insertar(new FuncionarioDTO(otro, "Otro temporal", "8888-2222"));
                IRepositorioCategorias cats = new RepositorioCategoriasOracle(c);
                categoriaA = cats.insertar(marca + " Categoría A");
                categorias.add(categoriaA.id());
                categoriaB = cats.insertar(marca + " Categoría B");
                categorias.add(categoriaB.id());
                categoriaVacia = cats.insertar(marca + " Categoría vacía");
                categorias.add(categoriaVacia.id());
                IRepositorioRecursos recursos = new RepositorioRecursosOracle(c);
                recursos.insertar(recursoA, categoriaA.id(), descripcionA);
                recursos.insertar(recursoB, categoriaB.id(), descripcionB);
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public IServicioAplicacion sesion(IFabricaServicios fabrica, String id) {
        ContextoSesion contexto = new ContextoSesion();
        IServicioAplicacion servicio = fabrica.crear(contexto);
        contexto.iniciar(servicio.iniciarSesion(id, id).usuario());
        return servicio;
    }

    public int cantidadReservas() throws SQLException {
        return contar("SELECT COUNT(*) FROM TRESERVAS WHERE ID_USUARIO IN (?,?)", autor, otro);
    }

    public int cantidadAsignaciones() throws SQLException {
        return contar("SELECT COUNT(*) FROM TRESERVA_RECURSO WHERE ID_RESERVA IN (SELECT ID FROM TRESERVAS WHERE ID_USUARIO IN (?,?))", autor, otro);
    }

    private int contar(String sql, String a, String b) throws SQLException {
        try (Connection c = ConexionOracle.abrir(); PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1, a);
            s.setString(2, b);
            try (ResultSet r = s.executeQuery()) { r.next(); return r.getInt(1); }
        }
    }

    @Override
    public synchronized void close() throws SQLException {
        if (cerrado) return;
        // Un @Timeout de JUnit interrumpe el hilo: con la marca puesta el driver puede fallar
        // y la limpieza no se haría. Se quita durante la limpieza y se restaura después.
        boolean interrumpido = Thread.interrupted();
        try {
            limpiar();
            cerrado = true;
            try {
                Runtime.getRuntime().removeShutdownHook(limpiezaAlSalir);
            } catch (IllegalStateException | IllegalArgumentException yaSaliendo) {
                // La JVM ya se está cerrando o el gancho no estaba registrado.
            }
        } finally {
            if (interrumpido) Thread.currentThread().interrupt();
        }
    }

    private void limpiarAlSalir() {
        synchronized (this) {
            if (cerrado) return;
        }
        try {
            limpiar();
        } catch (SQLException e) {
            System.err.println("No se pudieron limpiar los datos " + marca + ": código " + e.getErrorCode());
        }
    }

    private void limpiar() throws SQLException {
        // Solo IDs exactos de esta instancia; no LIKE, TRUNCATE ni reinicio de secuencias.
        // Cada DELETE se intenta aunque falle otro: un error no deja el resto como basura.
        SQLException primero = null;
        try (Connection c = ConexionOracle.abrir()) {
            c.setAutoCommit(false);
            primero = ejecutar(c, primero, "DELETE FROM TRESERVA_RECURSO WHERE ID_RESERVA IN (SELECT ID FROM TRESERVAS WHERE ID_USUARIO IN (?,?))", autor, otro);
            primero = ejecutar(c, primero, "DELETE FROM TRESERVAS WHERE ID_USUARIO IN (?,?)", autor, otro);
            primero = ejecutar(c, primero, "DELETE FROM TRECURSOS WHERE CODIGO IN (?,?,?)", recursoA, recursoB, recursoC);
            for (String id : categorias) primero = ejecutar(c, primero, "DELETE FROM TCATEGORIAS WHERE ID=?", id);
            primero = ejecutar(c, primero, "DELETE FROM TUSUARIOS WHERE ID IN (?,?,?)", admin, autor, otro);
            c.commit();
        }
        if (primero != null) throw primero;
    }

    private static SQLException ejecutar(Connection c, SQLException previo, String sql, String... valores) {
        try (PreparedStatement s = c.prepareStatement(sql)) {
            for (int i = 0; i < valores.length; i++) s.setString(i + 1, valores[i]);
            s.executeUpdate();
            return previo;
        } catch (SQLException e) {
            if (previo == null) return e;
            previo.addSuppressed(e);
            return previo;
        }
    }
}
