package repositorio;

import accesodatos.ConexionOracle;
import dto.CategoriaDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas reales contra Oracle.
 * Se activan con la variable de entorno ORACLE_IT=true.
 */
@EnabledIfEnvironmentVariable(
        named = "ORACLE_IT",
        matches = "(?i)true"
)
class RepositorioCategoriasOracleIT {

    @Test
    void conectaConElUsuarioConfigurado() throws Exception {
        String esperado = System.getenv("DB_USER");

        if (esperado == null || esperado.isBlank()) {
            esperado = "RESERVAS_P2";
        }

        try (Connection conexion = ConexionOracle.abrir();
             Statement sentencia = conexion.createStatement();
             ResultSet resultado = sentencia.executeQuery(
                     "SELECT USER FROM DUAL"
             )) {

            assertTrue(resultado.next());

            assertEquals(
                    esperado.trim().toUpperCase(Locale.ROOT),
                    resultado.getString(1)
            );
        }
    }

    @Test
    void ejecutaCrudSinDejarDatosTemporales() throws Exception {
        try (Connection conexion = ConexionOracle.abrir()) {
            conexion.setAutoCommit(false);

            try {
                IRepositorioCategorias repositorio =
                        new RepositorioCategoriasOracle(conexion);

                String descripcion =
                        "Prueba JDBC " + UUID.randomUUID();

                // Insertar y buscar.
                CategoriaDTO creada = repositorio.insertar(
                        "  " + descripcion + "  "
                );

                assertTrue(
                        creada.id().matches("CAT-[0-9]{6,}")
                );

                assertEquals(
                        new CategoriaDTO(creada.id(), descripcion),
                        repositorio.buscar(creada.id())
                );

                // Listar y comprobar que la lista es inmutable.
                var lista = repositorio.listar();

                assertTrue(lista.contains(creada));

                assertThrows(
                        UnsupportedOperationException.class,
                        () -> lista.add(creada)
                );

                // Modificar.
                CategoriaDTO modificada = new CategoriaDTO(
                        creada.id(),
                        "Modificada " + descripcion
                );

                repositorio.modificar(modificada);

                assertEquals(
                        modificada,
                        repositorio.buscar(creada.id())
                );

                // Eliminar.
                repositorio.eliminar(creada.id());

                assertNull(
                        repositorio.buscar(creada.id())
                );
            } finally {
                conexion.rollback();
            }
        }
    }

    @Test
    void rollbackDeshaceLaInsercionDelRepositorio() throws Exception {
        try (Connection conexion = ConexionOracle.abrir()) {
            conexion.setAutoCommit(false);

            try {
                IRepositorioCategorias repositorio =
                        new RepositorioCategoriasOracle(conexion);

                CategoriaDTO creada = repositorio.insertar(
                        "Prueba rollback JDBC " + UUID.randomUUID()
                );

                assertNotNull(
                        repositorio.buscar(creada.id())
                );

                conexion.rollback();

                assertNull(
                        repositorio.buscar(creada.id())
                );
            } finally {
                conexion.rollback();
            }
        }
    }
}