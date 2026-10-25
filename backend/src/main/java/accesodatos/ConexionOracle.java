package accesodatos;

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
        String clave = System.getenv("DB_PASSWORD");

        if (clave == null || clave.isEmpty()) {
            throw new SQLException(
                    "Falta DB_PASSWORD en las variables de entorno de IntelliJ."
            );
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

    private static String variable(
            String nombre,
            String valorPredeterminado
    ) {
        String valor = System.getenv(nombre);

        if (valor == null || valor.isBlank()) {
            return valorPredeterminado;
        }

        return valor.trim();
    }
}
