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
            System.err.println("No se pudo conectar a Oracle. Revise DB_URL, DB_USER, DB_PASSWORD y el driver. Código JDBC: " + e.getErrorCode());
            return;
        }
        Servidor servidor = new Servidor(Configuracion.getInstancia().getPuerto(), new FabricaServiciosOracle());
        servidor.iniciar();
        Runtime.getRuntime().addShutdownHook(new Thread(servidor::detener));
        System.out.println("Servidor Oracle de reservas escuchando en el puerto " + servidor.getPuerto());
    }
}
