package repositorio;

import accesodatos.ConexionOracle;
import dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import soporte.DatosOracle;
import java.sql.*;
import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "ORACLE_IT", matches = "(?i)true")
class RepositoriosOracleIT {
    @Test
    void funcionariosAutenticacionCambioDeClaveYDatosPersonales() throws Exception {
        try (DatosOracle d = DatosOracle.crear(); Connection c = ConexionOracle.abrir()) {
            c.setAutoCommit(false);
            try {
                IRepositorioFuncionarios repo = new RepositorioFuncionariosOracle(c);
                var primera = repo.autenticar(d.autor, d.autor);
                assertTrue(primera.requiereCambioClave());
                assertEquals(Rol.FUNCIONARIO, primera.usuario().rol());
                assertNull(repo.autenticar(d.autor, "incorrecta"));
                assertNull(repo.buscar(d.admin));
                assertTrue(repo.listar().stream().noneMatch(f -> f.id().equals(d.admin)));
                assertThrows(UnsupportedOperationException.class, () -> repo.listar().clear());
                repo.cambiarClave(d.autor, "Nueva-clave-temporal");
                repo.modificar(new FuncionarioDTO(d.autor, "Nombre modificado", "12345"));
                var nueva = repo.autenticar(d.autor, "Nueva-clave-temporal");
                assertNotNull(nueva);
                assertFalse(nueva.requiereCambioClave());
                assertEquals("Nombre modificado", nueva.usuario().nombre());
                assertNull(repo.autenticar(d.autor, d.autor));
                assertEquals("12345", repo.buscar(d.autor).telefono());
                assertEquals(1, assertThrows(SQLException.class, () -> repo.insertar(new FuncionarioDTO(d.autor, "Duplicado", "123"))).getErrorCode());
                repo.eliminar(d.otro);
                assertNull(repo.buscar(d.otro));
                assertFalse(c.isClosed());
            } finally { c.rollback(); }
        }
    }

    @Test
    void recursosMapeanCategoriaCrudDisponibilidadYListasInmutables() throws Exception {
        try (DatosOracle d = DatosOracle.crear(); Connection c = ConexionOracle.abrir()) {
            c.setAutoCommit(false);
            try {
                IRepositorioRecursos repo = new RepositorioRecursosOracle(c);
                RecursoDTO recurso = repo.buscar(d.recursoA);
                assertEquals(d.categoriaA, recurso.categoria());
                assertTrue(repo.listar().contains(recurso));
                assertEquals(java.util.List.of(recurso), repo.listarPorCategoria(d.categoriaA.id()));
                assertThrows(UnsupportedOperationException.class, () -> repo.listar().clear());
                assertThrows(UnsupportedOperationException.class, () -> repo.disponibles(d.categoriaA.id(), d.inicio, d.inicio.plusHours(1)).clear());
                repo.insertar(d.recursoC, d.categoriaA.id(), "Nuevo");
                repo.modificar(d.recursoC, d.categoriaB.id(), "Modificado");
                assertEquals(new RecursoDTO(d.recursoC, d.categoriaB, "Modificado"), repo.buscar(d.recursoC));
                assertEquals(1, assertThrows(SQLException.class, () -> repo.insertar(d.recursoC, d.categoriaA.id(), "Duplicado")).getErrorCode());
                repo.eliminar(d.recursoC);
                assertNull(repo.buscar(d.recursoC));
                assertFalse(c.isClosed());
            } finally { c.rollback(); }
        }
    }

    @Test
    void repositorioReservasNoConfirmaYRollbackRevierteCabeceraYAsignaciones() throws Exception {
        try (DatosOracle d = DatosOracle.crear(); Connection c = ConexionOracle.abrir()) {
            c.setAutoCommit(false);
            try {
                IRepositorioReservas repo = new RepositorioReservasOracle(c);
                int id = repo.insertar(d.autor, "Transacción JDBC", d.inicio, d.inicio.plusHours(1));
                repo.asignar(id, d.recursoA);
                ReservaDTO r = repo.buscar(id);
                assertEquals(d.inicio, r.inicio());
                assertEquals(EstadoReserva.ACTIVA, r.estado());
                assertEquals(d.autor, r.autor().id());
                assertEquals(1, r.recursos().size());
                assertEquals(0, d.cantidadReservas(), "Otra conexión no debe ver una reserva sin commit");
                c.rollback();
                assertNull(repo.buscar(id));
                assertTrue(repo.recursos(id).isEmpty());
            } finally { c.rollback(); }
        }
    }
}
