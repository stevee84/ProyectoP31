package accesodatos;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ConexionCompartidaTest {

    /** Conexión falsa: cuenta cierres y responde isValid según el indicador. */
    private static Connection falsa(AtomicBoolean valida, AtomicInteger cierres) {
        return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class<?>[]{Connection.class},
                (p, m, a) -> switch (m.getName()) {
                    case "isValid" -> valida.get();
                    case "close" -> { cierres.incrementAndGet(); yield null; }
                    case "getAutoCommit" -> false;
                    default -> null;
                });
    }

    @Test
    void reutilizaLaConexionYCerrarNoCierraLaFisica() throws Exception {
        AtomicInteger aperturas = new AtomicInteger(), cierres = new AtomicInteger();
        AtomicBoolean valida = new AtomicBoolean(true);
        ConexionCompartida compartida = new ConexionCompartida(() -> { aperturas.incrementAndGet(); return falsa(valida, cierres); });

        for (int i = 0; i < 100; i++) {
            try (Connection c = compartida.abrir()) {
                assertFalse(c.getAutoCommit());
            }
        }
        assertEquals(1, aperturas.get());
        assertEquals(0, cierres.get());
    }

    @Test
    void reabreSiLaConexionDejaDeSerValida() throws Exception {
        AtomicInteger aperturas = new AtomicInteger(), cierres = new AtomicInteger();
        AtomicBoolean valida = new AtomicBoolean(true);
        ConexionCompartida compartida = new ConexionCompartida(() -> { aperturas.incrementAndGet(); return falsa(valida, cierres); });

        compartida.abrir().close();
        valida.set(false);
        compartida.abrir().close();
        assertEquals(2, aperturas.get());
        assertEquals(1, cierres.get());
    }
}
