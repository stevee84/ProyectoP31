package view;

import model.CategoriaRecurso;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class CalendarizacionRecursosPanel extends JPanel {

    private JTextField txtFecha;
    private JComboBox<CategoriaRecurso> comboCategoria;
    private JButton btnMostrar;

    private JTable tablaCalendarizacion;

    // Callbacks
    private Runnable onMostrar;
    private Runnable onVisible;

    public CalendarizacionRecursosPanel() {
        setLayout(new BorderLayout(EstiloUI.GAP, EstiloUI.GAP));
        setBorder(EstiloUI.margenEstandar());
        setBackground(EstiloUI.BACKGROUND);

        txtFecha = new JTextField(10);
        txtFecha.setText(LocalDate.now().toString());
        EstiloUI.estilizarCampo(txtFecha);

        comboCategoria = new JComboBox<>();
        comboCategoria.setFont(EstiloUI.NORMAL);

        btnMostrar = new JButton("Mostrar");

        JPanel panelSeleccion = new JPanel(new FlowLayout(FlowLayout.LEFT, EstiloUI.GAP, EstiloUI.GAP));
        panelSeleccion.setBackground(EstiloUI.BACKGROUND);
        panelSeleccion.setBorder(EstiloUI.crearTitledBorder("Consultar calendarizacion"));

        JLabel lblFecha = new JLabel("Fecha:");
        JLabel lblCat = new JLabel("Categoria:");
        EstiloUI.estilizarEtiqueta(lblFecha);
        EstiloUI.estilizarEtiqueta(lblCat);
        EstiloUI.estilizarBoton(btnMostrar);

        panelSeleccion.add(lblFecha);
        panelSeleccion.add(txtFecha);
        panelSeleccion.add(lblCat);
        panelSeleccion.add(comboCategoria);
        panelSeleccion.add(btnMostrar);

        DefaultTableModel modeloTabla = new DefaultTableModel(new String[]{"Hora"}, 0);

        tablaCalendarizacion = new JTable(modeloTabla);
        tablaCalendarizacion.setDefaultEditor(Object.class, null);
        tablaCalendarizacion.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        EstiloUI.estilizarTabla(tablaCalendarizacion);

        JScrollPane scrollTabla = new JScrollPane(tablaCalendarizacion);
        scrollTabla.setBorder(EstiloUI.crearTitledBorder("Calendarizacion de recursos"));

        JButton btnPdf = new JButton("Generar PDF");
        btnPdf.addActionListener(e -> GeneradorPdf.exportar(this, tablaCalendarizacion,
                "Calendarizacion de Recursos", "calendarizacion.pdf"));
        EstiloUI.estilizarBoton(btnPdf);
        panelSeleccion.add(btnPdf);

        add(panelSeleccion, BorderLayout.NORTH);
        add(scrollTabla, BorderLayout.CENTER);

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                if (onVisible != null) onVisible.run();
            }
        });

        btnMostrar.addActionListener(e -> { if (onMostrar != null) onMostrar.run(); });
    }

    // --- Callback setters ---
    public void setOnMostrar(Runnable cb) { this.onMostrar = cb; }
    public void setOnVisible(Runnable cb) { this.onVisible = cb; }

    // --- Getters ---
    public String getFecha() { return txtFecha.getText().trim(); }
    public CategoriaRecurso getCategoriaSeleccionada() { return (CategoriaRecurso) comboCategoria.getSelectedItem(); }

    // --- Public methods for controller ---
    public void cargarCategorias(List<CategoriaRecurso> categorias) {
        comboCategoria.removeAllItems();
        for (CategoriaRecurso categoria : categorias) {
            comboCategoria.addItem(categoria);
        }
    }

    public void mostrarCalendarizacion(String[] columnas, Object[][] datos) {
        DefaultTableModel modeloTabla = new DefaultTableModel(columnas, 0);
        for (Object[] fila : datos) {
            modeloTabla.addRow(fila);
        }
        tablaCalendarizacion.setModel(modeloTabla);
        tablaCalendarizacion.setDefaultEditor(Object.class, null);
        EstiloUI.estilizarTabla(tablaCalendarizacion);
        tablaCalendarizacion.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tablaCalendarizacion.getColumnModel().getColumn(0).setPreferredWidth(70);
        for (int i = 1; i < tablaCalendarizacion.getColumnCount(); i++) {
            tablaCalendarizacion.getColumnModel().getColumn(i).setPreferredWidth(250);
        }
        tablaCalendarizacion.setRowHeight(EstiloUI.ROW_HEIGHT);
    }

    public void mostrarMensaje(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

    public void mostrarError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
