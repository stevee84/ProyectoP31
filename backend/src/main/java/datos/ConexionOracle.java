package datos;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Abre una conexion nueva.
 * Quien la solicita debe cerrarla.
 */
public final class ConexionOracle {

    private ConexionOracle() {
    }

    public static Connection abrir() throws SQLException {
        String url = variable(
                "DB_URL",
                "jdbc:oracle:thin:@localhost:1521:XE"
        );

        String usuario = variable("DB_USER", "RESERVAS_P2");
        String clave = variable("DB_PASSWORD", null);

        if (clave == null) {
            throw new SQLException("Falta la clave de Oracle: copie oracle.properties.ejemplo como "
                    + ARCHIVO.toAbsolutePath() + " y complete DB_PASSWORD (o defina la variable de entorno).");
        }

        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                    "No se encontro el driver Oracle. Recargue el proyecto Maven.",
                    e
            );
        }

        Properties propiedades = new Properties();
        propiedades.setProperty("user", usuario);
        propiedades.setProperty("password", clave);

        propiedades.setProperty(
                "oracle.net.CONNECT_TIMEOUT",
                "10000"
        );

        propiedades.setProperty(
                "oracle.jdbc.ReadTimeout",
                "30000"
        );

        return DriverManager.getConnection(url, propiedades);
    }

    /** Archivo opcional en la carpeta desde la que se ejecuta (la raíz del proyecto). */
    static final Path ARCHIVO = Path.of("oracle.properties");

    /** Primero la variable de entorno, después oracle.properties, por último el valor por defecto. */
    private static String variable(
            String nombre,
            String valorPredeterminado
    ) {
        String valor = System.getenv(nombre);

        if (valor == null || valor.isBlank()) {
            valor = archivo().getProperty(nombre);
        }

        if (valor == null || valor.isBlank()) {
            return valorPredeterminado;
        }

        return valor.trim();
    }

    private static Properties archivo() {
        Properties propiedades = new Properties();
        if (Files.isRegularFile(ARCHIVO)) {
            try (Reader lector = Files.newBufferedReader(ARCHIVO)) {
                propiedades.load(lector);
            } catch (IOException e) {
                // Archivo ilegible: se sigue con las variables de entorno y los valores por defecto.
            }
        }
        return propiedades;
    }
}
