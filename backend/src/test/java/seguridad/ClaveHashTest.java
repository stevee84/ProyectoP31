package seguridad;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClaveHashTest {

    @Test
    void generaFormatoConSalAleatoriaYSinTextoPlano() {
        String a = ClaveHash.generar("Secreta-1");
        String b = ClaveHash.generar("Secreta-1");
        assertTrue(a.matches("pbkdf2[$][0-9]+[$][A-Za-z0-9+/=]+[$][A-Za-z0-9+/=]+"), a);
        assertNotEquals(a, b, "Cada hash lleva su propia sal");
        assertFalse(a.contains("Secreta-1"));
        assertTrue(a.length() <= 255, "Cabe en TUSUARIOS.CLAVE");
    }

    @Test
    void verificaSoloLaClaveCorrecta() {
        String guardado = ClaveHash.generar("Secreta-1");
        assertTrue(ClaveHash.verificar("Secreta-1", guardado));
        assertFalse(ClaveHash.verificar("secreta-1", guardado));
        assertFalse(ClaveHash.verificar("", guardado));
        assertFalse(ClaveHash.verificar(null, guardado));
    }

    @Test
    void valoresMalFormadosOTextoPlanoNoAutentican() {
        for (String guardado : new String[]{null, "", "ADMIN", "pbkdf2$x$y$z", "pbkdf2$0$AA==$AA==", "otro$1$AA==$AA==",
                "pbkdf2$1$***$AA=="}) {
            assertFalse(ClaveHash.verificar("ADMIN", guardado), String.valueOf(guardado));
        }
    }

    @Test
    void lasIteracionesGuardadasFormanParteDeLaVerificacion() {
        String guardado = ClaveHash.generar("x");
        String[] partes = guardado.split("[$]");
        assertEquals(String.valueOf(ClaveHash.ITERACIONES), partes[1]);
        String otrasIteraciones = String.join("$", partes[0], "1000", partes[2], partes[3]);
        assertFalse(ClaveHash.verificar("x", otrasIteraciones));
    }
}
