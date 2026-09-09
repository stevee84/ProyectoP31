package view;

import javax.swing.*;
import java.awt.*;
import model.CategoriaRecurso;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class CategoriasPanel extends JPanel {
    private JTextField txtBusqueda;
    private JTextField txtId;
    private JTextField txtDescripcion;

    private JButton btnBuscar;
    private JButton btnNuevo;
    private JButton btnGuardar;
    private JButton btnBorrar;
    private JButton btnLimpiar;

    private JTable tablaCategorias;

    // Callbacks
    private Runnable onBuscar;
    private Runnable onNuevo;
    private Runnable onGuardar;
    private Runnable onBorrar;
    private Runnable onLimpiar;
    private Runnable onSeleccionar;

    public CategoriasPanel() {
        setLayout(new BorderLayout(EstiloUI.GAP, EstiloUI.GAP));
        setBorder(EstiloUI.margenEstandar());
        setBackground(EstiloUI.BACKGROUND);

        txtBusqueda = new JTextField(20);
        btnBuscar = new JButton("Buscar");

        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, EstiloUI.GAP, EstiloUI.GAP));
        panelBusqueda.setBackground(EstiloUI.BACKGROUND);
        panelBusqueda.setBorder(EstiloUI.crearTitledBorder("Busqueda"));
        JLabel lblDesc = new JLabel("Descripcion:");
        EstiloUI.estilizarEtiqueta(lblDesc);
        EstiloUI.estilizarCampo(txtBusqueda);
        EstiloUI.estilizarBoton(btnBuscar);
        panelBusqueda.add(lblDesc);
        panelBusqueda.add(txtBusqueda);
        panelBusqueda.add(btnBuscar);

        txtId = new JTextField(18);
        txtId.setEditable(false);
        txtDescripcion = new JTextField(18);
        txtDescripcion.setEditable(false);
        EstiloUI.estilizarCampo(txtId);
        EstiloUI.estilizarCampo(txtDescripcion);

        JPanel panelCampos = new JPanel(new GridBagLayout());
        panelCampos.setBackground(EstiloUI.BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblId = new JLabel("ID:");
        JLabel lblDescCampo = new JLabel("Descripcion:");
        EstiloUI.estilizarEtiqueta(lblId);
        EstiloUI.estilizarEtiqueta(lblDescCampo);

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        panelCampos.add(lblId, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panelCampos.add(txtId, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        panelCampos.add(lblDescCampo, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panelCampos.add(txtDescripcion, gbc);

        btnNuevo = new JButton("Nuevo");
        btnGuardar = new JButton("Guardar");
        btnBorrar = new JButton("Borrar");
        btnLimpiar = new JButton("Limpiar");

        btnGuardar.setEnabled(false);
        btnBorrar.setEnabled(false);

        JButton btnPdf = new JButton("Generar PDF");
        btnPdf.addActionListener(e -> GeneradorPdf.exportar(this, tablaCategorias,
                "Listado de Categorias", "categorias.pdf"));

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, EstiloUI.GAP, EstiloUI.GAP));
        panelBotones.setBackground(EstiloUI.BACKGROUND);
        for (JButton btn : new JButton[]{btnNuevo, btnGuardar, btnBorrar, btnLimpiar, btnPdf}) {
            EstiloUI.estilizarBoton(btn);
            panelBotones.add(btn);
        }

        JPanel panelFormulario = new JPanel(new BorderLayout(EstiloUI.GAP, EstiloUI.GAP));
        panelFormulario.setBackground(EstiloUI.BACKGROUND);
        panelFormulario.setBorder(EstiloUI.crearTitledBorder("Categoria"));
        panelFormulario.add(panelCampos, BorderLayout.CENTER);
        panelFormulario.add(panelBotones, BorderLayout.SOUTH);

        DefaultTableModel modeloTabla = new DefaultTableModel(
                new String[]{"ID", "Descripcion"}, 0
        );

        tablaCategorias = new JTable(modeloTabla);
        tablaCategorias.setDefaultEditor(Object.class, null);
        EstiloUI.estilizarTabla(tablaCategorias);

        JScrollPane scrollTabla = new JScrollPane(tablaCategorias);
        scrollTabla.setBorder(EstiloUI.crearTitledBorder("Listado"));

        JPanel panelCentro = new JPanel(new BorderLayout(EstiloUI.GAP, EstiloUI.GAP));
        panelCentro.setBackground(EstiloUI.BACKGROUND);
        panelCentro.add(panelFormulario, BorderLayout.NORTH);
        panelCentro.add(scrollTabla, BorderLayout.CENTER);

        add(panelBusqueda, BorderLayout.NORTH);
        add(panelCentro, BorderLayout.CENTER);

        btnBuscar.addActionListener(e -> { if (onBuscar != null) onBuscar.run(); });
        btnNuevo.addActionListener(e -> {
            tablaCategorias.clearSelection();
            txtId.setText("");
            txtDescripcion.setText("");
            txtDescripcion.setEditable(true);
            btnGuardar.setEnabled(true);
            btnBorrar.setEnabled(false);
            txtDescripcion.requestFocus();
            if (onNuevo != null) onNuevo.run();
        });
        btnLimpiar.addActionListener(e -> { if (onLimpiar != null) onLimpiar.run(); });
        btnGuardar.addActionListener(e -> { if (onGuardar != null) onGuardar.run(); });
        btnBorrar.addActionListener(e -> { if (onBorrar != null) onBorrar.run(); });

        tablaCategorias.getSelectionModel()
                .addListSelectionListener(e -> {
                    if (!e.getValueIsAdjusting()) {
                        int fila = tablaCategorias.getSelectedRow();
                        if (fila != -1) {
                            txtId.setText(tablaCategorias.getValueAt(fila, 0).toString());
                            txtDescripcion.setText(tablaCategorias.getValueAt(fila, 1).toString());
                            txtDescripcion.setEditable(true);
                            btnGuardar.setEnabled(true);
                            btnBorrar.setEnabled(true);
                        } else {
                            txtId.setText("");
                            txtDescripcion.setText("");
                            txtDescripcion.setEditable(false);
                            btnGuardar.setEnabled(false);
                            btnBorrar.setEnabled(false);
                        }
                        if (onSeleccionar != null) onSeleccionar.run();
                    }
                });
    }

    // --- Callback setters ---
    public void setOnBuscar(Runnable cb) { this.onBuscar = cb; }
    public void setOnNuevo(Runnable cb) { this.onNuevo = cb; }
    public void setOnGuardar(Runnable cb) { this.onGuardar = cb; }
    public void setOnBorrar(Runnable cb) { this.onBorrar = cb; }
    public void setOnLimpiar(Runnable cb) { this.onLimpiar = cb; }
    public void setOnSeleccionar(Runnable cb) { this.onSeleccionar = cb; }

    // --- Getters ---
    public String getBusqueda() { return txtBusqueda.getText().trim(); }
    public String getId() { return txtId.getText().trim(); }
    public String getDescripcion() { return txtDescripcion.getText().trim(); }
    public int getFilaSeleccionada() { return tablaCategorias.getSelectedRow(); }

    // --- Public methods for controller ---
    public void cargarDatos(List<CategoriaRecurso> categorias) {
        DefaultTableModel modeloTabla = (DefaultTableModel) tablaCategorias.getModel();
        modeloTabla.setRowCount(0);
        for (CategoriaRecurso categoria : categorias) {
            modeloTabla.addRow(new Object[]{
                    categoria.getId(),
                    categoria.getDescripcion()
            });
        }
    }

    public void limpiar() {
        txtBusqueda.setText("");
        txtId.setText("");
        txtDescripcion.setText("");
        tablaCategorias.clearSelection();
        txtDescripcion.setEditable(false);
        btnGuardar.setEnabled(false);
        btnBorrar.setEnabled(false);
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
