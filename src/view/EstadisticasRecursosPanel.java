package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class EstadisticasRecursosPanel extends JPanel {

    private JTextField txtDesde;
    private JTextField txtHasta;
    private JButton btnCalcular;

    private JTable tablaEstadisticas;
    private GraficaRecursosPanel panelGrafica;

    // Callbacks
    private Runnable onCalcular;

    public EstadisticasRecursosPanel() {
        setLayout(new BorderLayout(EstiloUI.GAP, EstiloUI.GAP));
        setBorder(EstiloUI.margenEstandar());
        setBackground(EstiloUI.BACKGROUND);

        LocalDate fechaActual = LocalDate.now();

        txtDesde = new JTextField(10);
        txtDesde.setText(fechaActual.withDayOfMonth(1).toString());
        txtHasta = new JTextField(10);
        txtHasta.setText(fechaActual.toString());
        EstiloUI.estilizarCampo(txtDesde);
        EstiloUI.estilizarCampo(txtHasta);

        btnCalcular = new JButton("Cargar");

        JPanel panelFechas = new JPanel(new FlowLayout(FlowLayout.LEFT, EstiloUI.GAP, EstiloUI.GAP));
        panelFechas.setBackground(EstiloUI.BACKGROUND);
        panelFechas.setBorder(EstiloUI.crearTitledBorder("Fechas Desde y Hasta"));

        JLabel lblDesde = new JLabel("Desde:");
        JLabel lblHasta = new JLabel("Hasta:");
        EstiloUI.estilizarEtiqueta(lblDesde);
        EstiloUI.estilizarEtiqueta(lblHasta);
        EstiloUI.estilizarBoton(btnCalcular);

        JButton btnPdf = new JButton("Generar PDF");
        btnPdf.addActionListener(e -> GeneradorPdf.exportar(this, tablaEstadisticas,
                "Estadisticas de Recursos", "estadisticas_recursos.pdf"));
        EstiloUI.estilizarBoton(btnPdf);

        panelFechas.add(lblDesde);
        panelFechas.add(txtDesde);
        panelFechas.add(lblHasta);
        panelFechas.add(txtHasta);
        panelFechas.add(btnCalcular);
        panelFechas.add(btnPdf);

        DefaultTableModel modeloTabla = new DefaultTableModel(
                new String[]{"Categoria", "Cantidad"}, 0
        );
        tablaEstadisticas = new JTable(modeloTabla);
        tablaEstadisticas.setDefaultEditor(Object.class, null);
        EstiloUI.estilizarTabla(tablaEstadisticas);

        JScrollPane scrollTabla = new JScrollPane(tablaEstadisticas);
        scrollTabla.setBorder(EstiloUI.crearTitledBorder("Estadisticas"));

        panelGrafica = new GraficaRecursosPanel();
        panelGrafica.configurar("Recursos Usados", "Recurso", new java.awt.Color(41, 128, 185));

        JPanel panelCentro = new JPanel(new GridLayout(1, 2, EstiloUI.GAP, EstiloUI.GAP));
        panelCentro.setBackground(EstiloUI.BACKGROUND);
        panelCentro.add(scrollTabla);
        panelCentro.add(panelGrafica);

        add(panelFechas, BorderLayout.NORTH);
        add(panelCentro, BorderLayout.CENTER);

        btnCalcular.addActionListener(e -> { if (onCalcular != null) onCalcular.run(); });
    }

    // --- Callback setters ---
    public void setOnCalcular(Runnable cb) { this.onCalcular = cb; }

    // --- Getters ---
    public String getDesde() { return txtDesde.getText().trim(); }
    public String getHasta() { return txtHasta.getText().trim(); }

    // --- Public methods for controller ---
    public void cargarDatos(List<String> nombres, List<Integer> cantidades) {
        DefaultTableModel modeloTabla = (DefaultTableModel) tablaEstadisticas.getModel();
        modeloTabla.setRowCount(0);
        for (int i = 0; i < nombres.size(); i++) {
            modeloTabla.addRow(new Object[]{nombres.get(i), cantidades.get(i)});
        }
        panelGrafica.actualizarDatos(nombres, cantidades);
    }

    public void mostrarMensaje(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

    public void mostrarError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
