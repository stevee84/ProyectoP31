package accesodatos;

import java.sql.Connection;
import java.sql.SQLException;

/** Permite sustituir solamente la apertura de conexiones en las pruebas. */
@FunctionalInterface
public interface ProveedorConexion {
    Connection abrir() throws SQLException;
}
