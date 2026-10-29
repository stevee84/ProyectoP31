package view;

import contrato.Reglas;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Reglas del diálogo sin abrir ventanas (válido también sin pantalla). */
class CambioClaveDialogTest {

    @Test
    void rechazaVaciaDistintaConfirmacionIdYClaveActual() {
        assertNotNull(CambioClaveDialog.validar(" ", " ", "F001", "F001"));
        assertEquals("Las contraseñas no coinciden.", CambioClaveDialog.validar("Nueva-1", "Nueva-2", "F001", null));
        assertEquals(Reglas.MENSAJE_CLAVE_IGUAL_ID, CambioClaveDialog.validar("f001", "f001", "F001", null));
        assertEquals(Reglas.MENSAJE_CLAVE_IGUAL_ACTUAL, CambioClaveDialog.validar("Actual-1", "Actual-1", "F001", "Actual-1"));
        assertNull(CambioClaveDialog.validar("Nueva-1", "Nueva-1", "F001", "F001"));
        assertNull(CambioClaveDialog.validar("Nueva-1", "Nueva-1", null, null), "Sin datos, el servidor decide");
    }
}
