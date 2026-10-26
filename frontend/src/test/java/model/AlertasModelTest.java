package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static soporte.Datos.ANA;
import static soporte.Datos.LUIS;
import static soporte.Datos.reserva;

class AlertasModelTest {

    private AlertasModel modelo;
    private List<List<Alerta>> notificaciones;

    @BeforeEach
    void preparar() {
        modelo = new AlertasModel();
        notificaciones = new ArrayList<>();
        modelo.suscribir(notificaciones::add);
        notificaciones.clear(); // suscribir entrega el estado inicial; aquí solo interesan los cambios
    }

    private static Alerta alerta(int id, String actividad) {
        return new Alerta(reserva(id, LUIS, actividad), LocalDateTime.now());
    }

    @Test
    void suscribirEntregaElEstadoActual() {
        modelo.agregar(alerta(1, "Reunion"));
        List<List<Alerta>> recibido = new ArrayList<>();

        modelo.suscribir(recibido::add);

        assertEquals(1, recibido.size());
        assertEquals(1, recibido.get(0).size());
    }

    @Test
    void laAlertaMasRecienteQuedaArriba() {
        modelo.agregar(alerta(1, "Primera"));
        modelo.agregar(alerta(2, "Segunda"));

        assertEquals("Segunda", modelo.get(0).actividad());
        assertEquals("Primera", modelo.get(1).actividad());
        assertEquals(2, notificaciones.size());
    }

    @Test
    void laMismaReservaNoSeRepite() {
        modelo.agregar(alerta(7, "Reunion"));
        modelo.agregar(alerta(7, "Reunion"));

        assertEquals(1, modelo.cantidad());
        assertEquals(1, notificaciones.size());
    }

    @Test
    void descartarQuitaSoloEsaAlerta() {
        Alerta a = alerta(1, "Primera");
        Alerta b = alerta(2, "Segunda");
        modelo.agregar(a);
        modelo.agregar(b);

        assertTrue(modelo.descartar(a));

        assertEquals(List.of(b), modelo.getAlertas());
        assertFalse(modelo.descartar(a), "Descartar dos veces no cambia nada");
    }

    @Test
    void limpiarVaciaLaLista() {
        modelo.agregar(alerta(1, "Primera"));

        modelo.limpiar();

        assertEquals(0, modelo.cantidad());
        assertTrue(notificaciones.get(notificaciones.size() - 1).isEmpty());
    }

    @Test
    void laListaEntregadaNoSePuedeModificarDesdeAfuera() {
        modelo.agregar(alerta(1, "Primera"));

        assertThrows(UnsupportedOperationException.class, () -> modelo.getAlertas().clear());
        assertEquals(1, modelo.cantidad());
    }

    @Test
    void alertaFormateaLosDatosDelDetalle() {
        Alerta a = new Alerta(reserva(3, ANA, "Capacitacion"), LocalDateTime.now());

        assertEquals("Ana Mora - Capacitacion", a.titulo());
        assertEquals("Ana Mora", a.encargado());
        assertEquals("08:00", a.desde());
        assertEquals("10:00", a.hasta());
        assertEquals(List.of("Sala A, segundo piso", "Sala B, primer piso"), a.recursos());
        assertEquals(a.reserva().inicio().toLocalDate().toString(), a.fecha());
    }
}
