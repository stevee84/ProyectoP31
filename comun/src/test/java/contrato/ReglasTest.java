package contrato;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ReglasTest {

    @Test
    void telefonoAdmiteDigitosEspaciosMasYGuion() {
        for (String valido : new String[]{"8888-1111", "88881111", "+506 8888 1111", " 2222-3333 ", "+1-202-555-0100"}) {
            assertTrue(Reglas.telefonoValido(valido), valido);
        }
        for (String invalido : new String[]{null, "", "123", "8888-11a1", "(506) 8888", "8888_1111", "+-- -- --",
                "1".repeat(21), "88.88.11.11"}) {
            assertFalse(Reglas.telefonoValido(invalido), String.valueOf(invalido));
        }
    }

    @Test
    void claveIgualIdIgnoraMayusculasYEspacios() {
        assertTrue(Reglas.claveIgualId("F001", "F001"));
        assertTrue(Reglas.claveIgualId(" admin ", "ADMIN"));
        assertFalse(Reglas.claveIgualId("F001x", "F001"));
        assertFalse(Reglas.claveIgualId(null, "F001"));
        assertFalse(Reglas.claveIgualId("F001", null));
    }
}
