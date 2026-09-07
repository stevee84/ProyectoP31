package model;

import exception.ValidacionException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class SolicitudReservaTest {

    private Empleado empleado() {
        return new Funcionario("Test", "FUNC01", "12345678");
    }

    @Test
    void testSolicitudReservaDuracionCero() {
        LocalDateTime fecha = LocalDate.now().plusDays(1).atTime(10, 0);
        assertThrows(ValidacionException.class, () ->
                new SolicitudReserva(empleado(), List.of("CAT1"), "Actividad", fecha, fecha));
    }

    @Test
    void testSolicitudReservaFechaPasada() {
        LocalDateTime pasado = LocalDateTime.now().minusDays(1);
        LocalDateTime fin = pasado.plusHours(1);
        assertThrows(ValidacionException.class, () ->
                new SolicitudReserva(empleado(), List.of("CAT1"), "Actividad", pasado, fin));
    }
}
