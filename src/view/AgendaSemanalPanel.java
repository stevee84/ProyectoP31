package view;

import consulta.InfoReserva;
import controller.UsuariosActividadesController;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Panel de la agenda semanal (pestana "Actividades" del mockup): muestra en
 * una tabla dia x hora las actividades programadas, con navegacion entre
 * semanas.
 */
public class AgendaSemanalPanel extends JPanel {

    private static final DateTimeFormatter FORMATO_DIA = DateTimeFormatter.ofPattern("dd/MM");

    private final UsuariosActividadesController controller;
    private LocalDate semanaActual = LocalDate.now();

    private final JLabel etiquetaSemana = new JLabel();
    private final DefaultTableModel modeloTabla = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);

    public AgendaSemanalPanel(UsuariosActividadesController controller) {
        this.controller = controller;

        setLayout(new BorderLayout(EstiloUI.GAP, EstiloUI.GAP));
        setBorder(EstiloUI.margenEstandar());
        setBackground(EstiloUI.BACKGROUND);

        add(construirPanelNavegacion(), BorderLayout.NORTH);

        EstiloUI.estilizarTabla(tabla);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setPreferredSize(new Dimension(760, 260));
        scroll.setBorder(EstiloUI.crearTitledBorder("Agenda semanal"));
        add(scroll, BorderLayout.CENTER);

        cargarSemana();
    }

    private JPanel construirPanelNavegacion() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, EstiloUI.GAP, EstiloUI.GAP));
        panel.setBackground(EstiloUI.BACKGROUND);
        panel.setBorder(EstiloUI.crearTitledBorder("Navegacion"));

        JButton btnAnterior = new JButton("⬅️ Semana anterior");
        JButton btnHoy = new JButton("📅 Semana actual");
        JButton btnSiguiente = new JButton("Semana siguiente ➡️");

        EstiloUI.estilizarBoton(btnAnterior);
        EstiloUI.estilizarBoton(btnHoy);
        EstiloUI.estilizarBoton(btnSiguiente);
        EstiloUI.estilizarEtiquetaTitulo(etiquetaSemana);

        btnAnterior.addActionListener(e -> {
            semanaActual = semanaActual.minusWeeks(1);
            cargarSemana();
        });
        btnHoy.addActionListener(e -> {
            semanaActual = LocalDate.now();
            cargarSemana();
        });
        btnSiguiente.addActionListener(e -> {
            semanaActual = semanaActual.plusWeeks(1);
            cargarSemana();
        });

        JButton btnPdf = new JButton("📄 Generar PDF");
        btnPdf.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Funcionalidad de PDF pendiente de implementacion.", "PDF", JOptionPane.INFORMATION_MESSAGE));
        EstiloUI.estilizarBoton(btnPdf);

        panel.add(btnAnterior);
        panel.add(etiquetaSemana);
        panel.add(btnHoy);
        panel.add(btnSiguiente);
        panel.add(btnPdf);
        return panel;
    }

    private void cargarSemana() {
        LocalDate lunes = semanaActual.with(DayOfWeek.MONDAY);
        LocalDate domingo = lunes.plusDays(6);
        etiquetaSemana.setText("Semana del " + FORMATO_DIA.format(lunes) + " al " + FORMATO_DIA.format(domingo));

        Map<DayOfWeek, Map<Integer, List<InfoReserva>>> matriz = controller.obtenerMatrizSemana(semanaActual);

        DayOfWeek[] dias = DayOfWeek.values();
        Object[] columnas = new Object[dias.length + 1];
        columnas[0] = "Hora";
        for (int i = 0; i < dias.length; i++) {
            LocalDate fecha = lunes.plusDays(i);
            columnas[i + 1] = nombreDia(dias[i]) + " " + FORMATO_DIA.format(fecha);
        }
        modeloTabla.setColumnIdentifiers(columnas);
        modeloTabla.setRowCount(0);

        for (int hora = UsuariosActividadesController.HORA_INICIO; hora < UsuariosActividadesController.HORA_FIN; hora++) {
            Object[] fila = new Object[dias.length + 1];
            fila[0] = String.format("%02d:00", hora);
            for (int i = 0; i < dias.length; i++) {
                fila[i + 1] = formatearCelda(matriz.get(dias[i]).get(hora));
            }
            modeloTabla.addRow(fila);
        }
    }

    private String formatearCelda(List<InfoReserva> reservas) {
        if (reservas == null || reservas.isEmpty()) {
            return "";
        }
        StringBuilder texto = new StringBuilder();
        for (InfoReserva r : reservas) {
            if (texto.length() > 0) {
                texto.append(" | ");
            }
            texto.append(r.descripcionActividad()).append(" (").append(r.nombreFuncionario()).append(")");
        }
        return texto.toString();
    }

    private String nombreDia(DayOfWeek dia) {
        return switch (dia) {
            case MONDAY -> "Lunes";
            case TUESDAY -> "Martes";
            case WEDNESDAY -> "Miercoles";
            case THURSDAY -> "Jueves";
            case FRIDAY -> "Viernes";
            case SATURDAY -> "Sabado";
            case SUNDAY -> "Domingo";
        };
    }
}
