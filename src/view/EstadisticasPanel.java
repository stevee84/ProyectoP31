package view;

import controller.UsuariosActividadesController;
import controller.UsuariosActividadesController.EstadisticaSemana;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel de estadisticas de actividades: cuenta reservaciones por semana
 * en un rango de fechas y las muestra en tabla y en un grafico de barras.
 */
public class EstadisticasPanel extends JPanel {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_CORTO = DateTimeFormatter.ofPattern("dd/MM");

    private final UsuariosActividadesController controller;

    private final JTextField campoDesde = new JTextField(10);
    private final JTextField campoHasta = new JTextField(10);
    private final JButton btnGenerar = new JButton("Generar");

    private final DefaultTableModel modeloTabla =
            new DefaultTableModel(new Object[]{"Semana", "Cantidad de actividades"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
    private final JTable tabla = new JTable(modeloTabla);
    private final GraficaRecursosPanel grafico = crearGraficoActividades();

    private static GraficaRecursosPanel crearGraficoActividades() {
        GraficaRecursosPanel g = new GraficaRecursosPanel();
        g.configurar("Actividades Realizadas", "Semana", new java.awt.Color(192, 57, 43));
        return g;
    }

    public EstadisticasPanel(UsuariosActividadesController controller) {
        this.controller = controller;

        setLayout(new BorderLayout(EstiloUI.GAP, EstiloUI.GAP));
        setBorder(EstiloUI.margenEstandar());
        setBackground(EstiloUI.BACKGROUND);

        add(construirPanelFiltro(), BorderLayout.NORTH);

        EstiloUI.estilizarTabla(tabla);
        JScrollPane scrollTabla = new JScrollPane(tabla);
        scrollTabla.setPreferredSize(new Dimension(760, 140));
        scrollTabla.setBorder(EstiloUI.crearTitledBorder("Resultados por semana"));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollTabla, grafico);
        split.setResizeWeight(0.4);
        add(split, BorderLayout.CENTER);

        btnGenerar.addActionListener(e -> generar());

        LocalDate hoy = LocalDate.now();
        campoDesde.setText(FORMATO_FECHA.format(hoy.minusWeeks(2)));
        campoHasta.setText(FORMATO_FECHA.format(hoy.plusWeeks(2)));
        generar();
    }

    private JPanel construirPanelFiltro() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, EstiloUI.GAP, EstiloUI.GAP));
        panel.setBackground(EstiloUI.BACKGROUND);
        panel.setBorder(EstiloUI.crearTitledBorder("Periodo de consulta"));

        JLabel lblDesde = new JLabel("Desde (dd/MM/aaaa):");
        JLabel lblHasta = new JLabel("Hasta (dd/MM/aaaa):");
        EstiloUI.estilizarEtiqueta(lblDesde);
        EstiloUI.estilizarEtiqueta(lblHasta);
        EstiloUI.estilizarCampo(campoDesde);
        EstiloUI.estilizarCampo(campoHasta);
        EstiloUI.estilizarBoton(btnGenerar);

        JButton btnPdf = new JButton("Generar PDF");
        btnPdf.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Funcionalidad de PDF pendiente de implementacion.", "PDF", JOptionPane.INFORMATION_MESSAGE));
        EstiloUI.estilizarBoton(btnPdf);

        panel.add(lblDesde);
        panel.add(campoDesde);
        panel.add(lblHasta);
        panel.add(campoHasta);
        panel.add(btnGenerar);
        panel.add(btnPdf);
        return panel;
    }

    private void generar() {
        LocalDate desde;
        LocalDate hasta;
        try {
            desde = LocalDate.parse(campoDesde.getText().trim(), FORMATO_FECHA);
            hasta = LocalDate.parse(campoHasta.getText().trim(), FORMATO_FECHA);
        } catch (DateTimeParseException ex) {
            mostrarError("Las fechas deben tener el formato dd/MM/aaaa.");
            return;
        }

        try {
            List<EstadisticaSemana> estadisticas = controller.contarPorSemana(desde, hasta);
            cargarTabla(estadisticas);

            List<String> etiquetas = new ArrayList<>();
            List<Integer> cantidades = new ArrayList<>();
            for (EstadisticaSemana e : estadisticas) {
                String semana = FORMATO_CORTO.format(e.inicioSemana()) + " - " + FORMATO_CORTO.format(e.finSemana());
                etiquetas.add(semana);
                cantidades.add(e.cantidad());
            }
            grafico.actualizarDatos(etiquetas, cantidades);
        } catch (RuntimeException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void cargarTabla(List<EstadisticaSemana> estadisticas) {
        modeloTabla.setRowCount(0);
        for (EstadisticaSemana e : estadisticas) {
            String semana = FORMATO_CORTO.format(e.inicioSemana()) + " - " + FORMATO_CORTO.format(e.finSemana());
            modeloTabla.addRow(new Object[]{semana, e.cantidad()});
        }
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
