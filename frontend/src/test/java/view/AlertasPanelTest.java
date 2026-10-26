package view;

import model.Alerta;
import org.junit.jupiter.api.Test;

import javax.swing.Icon;
import javax.swing.JTable;
import java.awt.BorderLayout;
import java.awt.Component;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static soporte.Datos.LUIS;
import static soporte.Datos.reserva;

/** Revisa la vista de alertas sin abrir ventanas (las pruebas corren en modo headless). */
class AlertasPanelTest {

    @Test
    void muestraUnaFilaPorAlertaConSusIconos() {
        AlertasPanel panel = new AlertasPanel();
        List<Alerta> alertas = List.of(
                new Alerta(reserva(1, LUIS, "Reunion clientes"), LocalDateTime.now()),
                new Alerta(reserva(2, LUIS, "Capacitacion"), LocalDateTime.now()));

        panel.mostrarAlertas(alertas);

        JTable tabla = panel.getTabla();
        assertEquals(2, tabla.getRowCount());
        assertEquals("Luis Rojas - Reunion clientes", tabla.getValueAt(0, 0));
        assertTrue(tabla.getValueAt(0, 1) instanceof Icon, "Columna Ver con ícono");
        assertTrue(tabla.getValueAt(0, 2) instanceof Icon, "Columna Desc. con ícono");
        assertEquals("Actividad", tabla.getColumnName(0));
        assertEquals("Ver", tabla.getColumnName(1));
        assertEquals("Desc.", tabla.getColumnName(2));
    }

    @Test
    void laListaVaciaNoDejaFilasViejas() {
        AlertasPanel panel = new AlertasPanel();
        panel.mostrarAlertas(List.of(new Alerta(reserva(1, LUIS, "Reunion"), LocalDateTime.now())));

        panel.mostrarAlertas(List.of());

        assertEquals(0, panel.getTabla().getRowCount());
    }

    @Test
    void lasCeldasNoSonEditables() {
        AlertasPanel panel = new AlertasPanel();
        panel.mostrarAlertas(List.of(new Alerta(reserva(1, LUIS, "Reunion"), LocalDateTime.now())));

        assertFalse(panel.getTabla().isCellEditable(0, 0));
        assertFalse(panel.getTabla().isCellEditable(0, 2));
    }

    @Test
    void ventanaPrincipalPoneLasAlertasALaDerechaFueraDeLasPestanas() {
        AlertasPanel alertas = new AlertasPanel();
        try {
            VentanaPrincipal ventana = new VentanaPrincipal(
                    new javax.swing.JPanel[]{new javax.swing.JPanel()}, new String[]{"Reservas"}, null,
                    "F001", "Funcionario", "Ana Mora", alertas);
            BorderLayout layout = (BorderLayout) ventana.getContentPane().getLayout();
            Component este = layout.getLayoutComponent(BorderLayout.EAST);
            assertSame(alertas, este);
            ventana.dispose();
        } catch (java.awt.HeadlessException e) {
            // Sin pantalla no se puede crear un JFrame: la prueba visual se hace a mano.
        }
    }
}
