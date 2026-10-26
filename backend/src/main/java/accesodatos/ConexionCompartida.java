package accesodatos;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Reutiliza una sola conexión física entre operaciones.
 * Abrir y cerrar una conexión por operación agota el listener de Oracle XE
 * (ORA-12519) con muchas operaciones seguidas. Es seguro porque las operaciones
 * de una fábrica ya se serializan con su bloqueo y cada una termina con commit o
 * rollback. Cerrar la conexión devuelta no cierra la física; si deja de ser
 * válida, se abre otra.
 */
public final class ConexionCompartida implements ProveedorConexion {
    private final ProveedorConexion origen;
    private Connection fisica;

    public ConexionCompartida(ProveedorConexion origen) {
        this.origen = origen;
    }

    @Override
    public synchronized Connection abrir() throws SQLException {
        if (fisica == null || !fisica.isValid(2)) {
            if (fisica != null) {
                try { fisica.close(); } catch (SQLException ignorada) { /* ya estaba rota */ }
            }
            fisica = origen.abrir();
        }
        Connection actual = fisica;
        return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class<?>[]{Connection.class},
                (proxy, metodo, args) -> {
                    if (metodo.getName().equals("close")) return null;
                    try {
                        return metodo.invoke(actual, args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                });
    }
}
