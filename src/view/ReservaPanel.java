package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.function.IntConsumer;

public class ReservaPanel extends JPanel {

    private final DefaultTableModel modeloTabla =
            new DefaultTableModel(new String[]{"Id", "Actividad", "Fecha", "Horario", "Recursos", "Estado", ""}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return column == 6; // solo columna de boton cancelar
                }
            };
    private final JTable tablaReservas = new JTable(modeloTabla);

    // Callbacks
    private Runnable onAbrirNueva;
    private IntConsumer onCancelar;
    private Runnable onVisible;

    public ReservaPanel() {
        setLayout(new BorderLayout(EstiloUI.GAP, EstiloUI.GAP));
        setBorder(EstiloUI.margenEstandar());
        setBackground(EstiloUI.BACKGROUND);

        EstiloUI.estilizarTabla(tablaReservas);
        configurarBotonCancelar();

        JScrollPane scroll = new JScrollPane(tablaReservas);
        scroll.setBorder(EstiloUI.crearTitledBorder("Mis reservas"));
        add(scroll, BorderLayout.CENTER);

        add(construirPanelInferior(), BorderLayout.SOUTH);

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                if (onVisible != null) onVisible.run();
            }
        });
    }

    private void configurarBotonCancelar() {
        BotonTablaRenderer btnCancelar = new BotonTablaRenderer(
                "Cancelar", EstiloUI.DANGER, fila -> {
            if (onCancelar != null) onCancelar.accept(fila);
        });

        TableColumn col = tablaReservas.getColumnModel().getColumn(6);
        col.setCellRenderer(btnCancelar);
        col.setCellEditor(btnCancelar);
        col.setPreferredWidth(80);
        col.setMaxWidth(90);
    }

    private JPanel construirPanelInferior() {
        JButton btnNueva = new JButton("Nueva Reserva");
        JButton btnPdf = new JButton("Generar PDF");
        btnPdf.addActionListener(e -> GeneradorPdf.exportar(this, tablaReservas,
                "Mis Reservas", "reservas.pdf"));

        EstiloUI.estilizarBotonPrimario(btnNueva);
        EstiloUI.estilizarBoton(btnPdf);

        btnNueva.addActionListener(e -> {
            if (onAbrirNueva != null) onAbrirNueva.run();
        });

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, EstiloUI.GAP, EstiloUI.GAP));
        panel.setBackground(EstiloUI.BACKGROUND);
        panel.add(btnNueva);
        panel.add(btnPdf);
        return panel;
    }

    // --- Callback setters ---
    public void setOnAbrirNueva(Runnable cb) { this.onAbrirNueva = cb; }
    public void setOnCancelar(IntConsumer cb) { this.onCancelar = cb; }
    public void setOnVisible(Runnable cb) { this.onVisible = cb; }

    // --- Getters ---
    public int getReservaIdEnFila(int filaModelo) {
        return (int) modeloTabla.getValueAt(filaModelo, 0);
    }

    // --- Public methods for controller ---
    public void cargarReservas(Object[][] datos) {
        modeloTabla.setRowCount(0);
        for (Object[] fila : datos) {
            // Agregar columna vacia para el boton
            Object[] filaConBoton = new Object[fila.length + 1];
            System.arraycopy(fila, 0, filaConBoton, 0, fila.length);
            filaConBoton[fila.length] = "";
            modeloTabla.addRow(filaConBoton);
        }
    }

    public void mostrarMensaje(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

    public void mostrarError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public boolean confirmarAccion(String msg) {
        return JOptionPane.showConfirmDialog(this, msg, "Confirmar",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}
