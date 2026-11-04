package app;

import datos.ConexionOracle;
import protocolo.Configuracion;
import red.Servidor;
import servicio.FabricaServiciosOracle;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/** Punto de entrada real sin modificar el servidor ni el protocolo de Steve. */
public final class MainServidor {
    private MainServidor() { }

    /** Traduce los fallos de conexión más comunes; nunca muestra la clave. */
    private static String motivo(SQLException e) {
        return switch (e.getErrorCode()) {
            // Error propio de ConexionOracle (por ejemplo, falta la clave): su mensaje ya explica qué hacer.
            case 0 -> e.getMessage() != null ? e.getMessage() : "error desconocido.";
            case 1017 -> "usuario o clave incorrectos. Revise DB_USER y DB_PASSWORD en oracle.properties"
                    + " (la clave es la del usuario del proyecto, no la de SYSTEM).";
            case 28000 -> "la cuenta del usuario está bloqueada. Desbloquéela como SYSTEM:"
                    + " ALTER USER RESERVAS_P2 ACCOUNT UNLOCK;";
            case 12505, 12514 -> "Oracle no reconoce el SID o servicio de DB_URL (por defecto XE).";
            case 17002 -> "no responde Oracle en el host y puerto de DB_URL. Verifique que el servicio esté iniciado.";
            default -> "código JDBC " + e.getErrorCode() + ". Revise DB_URL, DB_USER y DB_PASSWORD en oracle.properties.";
        };
    }

    public static void main(String[] args) throws IOException {
        // Fallar antes de aceptar clientes si falta configuración o no conecta Oracle.
        try (Connection conexion = ConexionOracle.abrir()) {
            if (!conexion.isValid(5)) throw new SQLException("Conexión no válida.");
        } catch (SQLException e) {
            // errorCode 0: error propio de ConexionOracle (p. ej. falta DB_PASSWORD); su mensaje no contiene la clave.
            System.err.println("No se pudo conectar a Oracle: " + motivo(e));
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
