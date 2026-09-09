package view;

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

public class AgendaSemanalPanel extends JPanel {

    private final JLabel etiquetaSemana = new JLabel();
    private final DefaultTableModel modeloTabla = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);

    // Callbacks
    private Runnable onSemanaAnterior;
    private Runnable onSemanaActual;
    private Runnable onSemanaSiguiente;

    public AgendaSemanalPanel() {
        setLayout(new BorderLayout(EstiloUI.GAP, EstiloUI.GAP));
        setBorder(EstiloUI.margenEstandar());
        setBackground(EstiloUI.BACKGROUND);

        add(construirPanelNavegacion(), BorderLayout.NORTH);

        EstiloUI.estilizarTabla(tabla);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setPreferredSize(new Dimension(760, 260));
        scroll.setBorder(EstiloUI.crearTitledBorder("Agenda semanal"));
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel construirPanelNavegacion() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, EstiloUI.GAP, EstiloUI.GAP));
        panel.setBackground(EstiloUI.BACKGROUND);
        panel.setBorder(EstiloUI.crearTitledBorder("Navegacion"));

        JButton btnAnterior = new JButton("<< Semana anterior");
        JButton btnHoy = new JButton("Semana actual");
        JButton btnSiguiente = new JButton("Semana siguiente >>");

        EstiloUI.estilizarBoton(btnAnterior);
        EstiloUI.estilizarBoton(btnHoy);
        EstiloUI.estilizarBoton(btnSiguiente);
        EstiloUI.estilizarEtiquetaTitulo(etiquetaSemana);

        btnAnterior.addActionListener(e -> { if (onSemanaAnterior != null) onSemanaAnterior.run(); });
        btnHoy.addActionListener(e -> { if (onSemanaActual != null) onSemanaActual.run(); });
        btnSiguiente.addActionListener(e -> { if (onSemanaSiguiente != null) onSemanaSiguiente.run(); });

        JButton btnPdf = new JButton("Generar PDF");
        btnPdf.addActionListener(e -> GeneradorPdf.exportar(this, tabla,
                "Agenda Semanal", "agenda_semanal.pdf"));
        EstiloUI.estilizarBoton(btnPdf);

        panel.add(btnAnterior);
        panel.add(etiquetaSemana);
        panel.add(btnHoy);
        panel.add(btnSiguiente);
        panel.add(btnPdf);
        return panel;
    }

    // --- Callback setters ---
    public void setOnSemanaAnterior(Runnable cb) { this.onSemanaAnterior = cb; }
    public void setOnSemanaActual(Runnable cb) { this.onSemanaActual = cb; }
    public void setOnSemanaSiguiente(Runnable cb) { this.onSemanaSiguiente = cb; }

    // --- Public methods for controller ---
    public void cargarSemana(String etiqueta, Object[] columnas, Object[][] filas) {
        etiquetaSemana.setText(etiqueta);
        modeloTabla.setColumnIdentifiers(columnas);
        modeloTabla.setRowCount(0);
        for (Object[] fila : filas) {
            modeloTabla.addRow(fila);
        }
    }

    public JTable getTabla() { return tabla; }

    public void mostrarMensaje(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

    public void mostrarError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
