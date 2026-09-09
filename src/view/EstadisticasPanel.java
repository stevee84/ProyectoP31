package view;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EstadisticasPanel extends JPanel {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final JTextField campoDesde = new JTextField(10);
    private final JTextField campoHasta = new JTextField(10);
    private final JButton btnGenerar = new JButton("Cargar");

    private final DefaultTableModel modeloTabla =
            new DefaultTableModel(new Object[]{"Semana", "Cantidad"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
    private final JTable tabla = new JTable(modeloTabla);
    private final GraficaRecursosPanel grafico;

    // Callbacks
    private Runnable onGenerar;

    public EstadisticasPanel() {
        grafico = new GraficaRecursosPanel();
        grafico.configurar("Actividades Realizadas", "Semana", new Color(192, 57, 43));

        setLayout(new BorderLayout(EstiloUI.GAP, EstiloUI.GAP));
        setBorder(EstiloUI.margenEstandar());
        setBackground(EstiloUI.BACKGROUND);

        add(construirPanelFiltro(), BorderLayout.NORTH);

        EstiloUI.estilizarTabla(tabla);
        JScrollPane scrollTabla = new JScrollPane(tabla);
        scrollTabla.setBorder(EstiloUI.crearTitledBorder("Estadisticas"));

        JPanel panelCentro = new JPanel(new GridLayout(1, 2, EstiloUI.GAP, EstiloUI.GAP));
        panelCentro.setBackground(EstiloUI.BACKGROUND);
        panelCentro.add(scrollTabla);
        panelCentro.add(grafico);

        add(panelCentro, BorderLayout.CENTER);

        btnGenerar.addActionListener(e -> { if (onGenerar != null) onGenerar.run(); });

        LocalDate hoy = LocalDate.now();
        campoDesde.setText(FORMATO_FECHA.format(hoy.minusWeeks(2)));
        campoHasta.setText(FORMATO_FECHA.format(hoy.plusWeeks(2)));
    }

    private JPanel construirPanelFiltro() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, EstiloUI.GAP, EstiloUI.GAP));
        panel.setBackground(EstiloUI.BACKGROUND);
        panel.setBorder(EstiloUI.crearTitledBorder("Fechas Desde y Hasta"));

        JLabel lblDesde = new JLabel("Desde (dd/MM/aaaa):");
        JLabel lblHasta = new JLabel("Hasta (dd/MM/aaaa):");
        EstiloUI.estilizarEtiqueta(lblDesde);
        EstiloUI.estilizarEtiqueta(lblHasta);
        EstiloUI.estilizarCampo(campoDesde);
        EstiloUI.estilizarCampo(campoHasta);
        EstiloUI.estilizarBoton(btnGenerar);

        JButton btnPdf = new JButton("Generar PDF");
        btnPdf.addActionListener(e -> GeneradorPdf.exportar(this, tabla,
                "Estadisticas de Actividades", "estadisticas_actividades.pdf"));
        EstiloUI.estilizarBoton(btnPdf);

        panel.add(lblDesde);
        panel.add(campoDesde);
        panel.add(lblHasta);
        panel.add(campoHasta);
        panel.add(btnGenerar);
        panel.add(btnPdf);
        return panel;
    }

    // --- Callback setters ---
    public void setOnGenerar(Runnable cb) { this.onGenerar = cb; }

    // --- Getters ---
    public String getDesde() { return campoDesde.getText().trim(); }
    public String getHasta() { return campoHasta.getText().trim(); }

    // --- Public methods for controller ---
    public void cargarTabla(List<Object[]> filas) {
        modeloTabla.setRowCount(0);
        for (Object[] fila : filas) {
            modeloTabla.addRow(fila);
        }
    }

    public void actualizarGrafico(List<String> etiquetas, List<Integer> cantidades) {
        grafico.actualizarDatos(etiquetas, cantidades);
    }

    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
