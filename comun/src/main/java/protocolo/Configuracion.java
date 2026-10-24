package protocolo;

import java.io.ObjectInputFilter;

/**
 * Datos de conexión compartidos por cliente y servidor (Singleton).
 * Se pueden cambiar al arrancar: -Dservidor.host=... -Dservidor.puerto=... -Dservidor.timeout=...
 */
public final class Configuracion {

    /** Al leer del socket solo se aceptan clases del protocolo y del JDK, con límites de tamaño. */
    public static final ObjectInputFilter FILTRO_MENSAJES = ObjectInputFilter.Config.createFilter(
            "maxbytes=1000000;maxarray=10000;maxdepth=20;protocolo.*;dto.*;java.base/*;!*");

    private static Configuracion instancia;

    private final String host;
    private final int puerto;
    private final int timeoutMs;

    private Configuracion() {
        host = System.getProperty("servidor.host", "localhost");
        puerto = Integer.getInteger("servidor.puerto", 5000);
        timeoutMs = Integer.getInteger("servidor.timeout", 10_000);
    }

    public static synchronized Configuracion getInstancia() {
        if (instancia == null) {
            instancia = new Configuracion();
        }
        return instancia;
    }

    public String getHost() {
        return host;
    }

    public int getPuerto() {
        return puerto;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }
}
