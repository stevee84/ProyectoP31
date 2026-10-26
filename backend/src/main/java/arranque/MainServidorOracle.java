package arranque;

import accesodatos.ConexionOracle;
import protocolo.Configuracion;
import red.Servidor;
import servicio.FabricaServiciosOracle;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/** Punto de entrada real sin modificar el servidor ni el protocolo de Steve. */
public final class MainServidorOracle {
    private MainServidorOracle() { }

    public static void main(String[] args) throws IOException {
        // Fallar antes de aceptar clientes si falta configuración o no conecta Oracle.
        try (Connection conexion = ConexionOracle.abrir()) {
            if (!conexion.isValid(5)) throw new SQLException("Conexión no válida.");
        } catch (SQLException e) {
            // errorCode 0: error propio de ConexionOracle (p. ej. falta DB_PASSWORD); su mensaje no contiene la clave.
            System.err.println(e.getErrorCode() == 0 && e.getMessage() != null
                    ? "No se pudo conectar a Oracle: " + e.getMessage()
                    : "No se pudo conectar a Oracle. Revise DB_URL, DB_USER, DB_PASSWORD y el driver. Código JDBC: " + e.getErrorCode());
            System.exit(1);
            return;
        }
        int puerto = Configuracion.getInstancia().getPuerto();
        Servidor servidor = new Servidor(puerto, new FabricaServiciosOracle());
        try {
            servidor.iniciar();
        } catch (IOException e) {
            System.err.println("No se pudo abrir el puerto " + puerto + ": ya está en uso o no está disponible. Cierre el otro proceso o cambie el puerto en la configuración.");
            System.exit(2);
            return;
        }
        Runtime.getRuntime().addShutdownHook(new Thread(servidor::detener));
        System.out.println("Servidor Oracle de reservas escuchando en el puerto " + servidor.getPuerto());
    }
}
