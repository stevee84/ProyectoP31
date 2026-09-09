package view;

import model.CategoriaRecurso;
import model.Recurso;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class RecursosPanel extends JPanel {

    private JComboBox<CategoriaRecurso> comboFiltro;
    private JComboBox<CategoriaRecurso> comboCategoria;

    private JTextField txtCodigo;
    private JTextField txtDescripcion;

    private JButton btnFiltrar;
    private JButton btnNuevo;
    private JButton btnGuardar;
    private JButton btnBorrar;
    private JButton btnLimpiar;

    private JTable tablaRecursos;

    // Callbacks
    private Runnable onFiltrar;
    private Runnable onNuevo;
    private Runnable onGuardar;
    private Runnable onBorrar;
    private Runnable onLimpiar;
    private Runnable onSeleccionar;
    private Runnable onVisible;

    public RecursosPanel() {
        setLayout(new BorderLayout(EstiloUI.GAP, EstiloUI.GAP));
        setBorder(EstiloUI.margenEstandar());
        setBackground(EstiloUI.BACKGROUND);

        comboFiltro = new JComboBox<>();
        comboFiltro.setFont(EstiloUI.NORMAL);
        btnFiltrar = new JButton("Filtrar");

        JPanel panelFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT, EstiloUI.GAP, EstiloUI.GAP));
        panelFiltro.setBackground(EstiloUI.BACKGROUND);
        panelFiltro.setBorder(EstiloUI.crearTitledBorder("Filtrar recursos"));
        JLabel lblCat = new JLabel("Categoria:");
        EstiloUI.estilizarEtiqueta(lblCat);
        EstiloUI.estilizarBoton(btnFiltrar);
        panelFiltro.add(lblCat);
        panelFiltro.add(comboFiltro);
        panelFiltro.add(btnFiltrar);

        txtCodigo = new JTextField(18);
        txtCodigo.setEditable(false);
        comboCategoria = new JComboBox<>();
        comboCategoria.setFont(EstiloUI.NORMAL);
        comboCategoria.setEnabled(false);
        txtDescripcion = new JTextField(18);
        txtDescripcion.setEditable(false);

        EstiloUI.estilizarCampo(txtCodigo);
        EstiloUI.estilizarCampo(txtDescripcion);

        JPanel panelCampos = new JPanel(new GridBagLayout());
        panelCampos.setBackground(EstiloUI.BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String[] etiquetas = {"Codigo:", "Categoria:", "Descripcion:"};
        Component[] campos = {txtCodigo, comboCategoria, txtDescripcion};
        for (int i = 0; i < etiquetas.length; i++) {
            JLabel lbl = new JLabel(etiquetas[i]);
            EstiloUI.estilizarEtiqueta(lbl);
            gbc.gridx = 0; gbc.gridy = i; gbc.anchor = GridBagConstraints.EAST;
            panelCampos.add(lbl, gbc);
            gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
            panelCampos.add(campos[i], gbc);
        }

        btnNuevo = new JButton("Nuevo");
        btnGuardar = new JButton("Guardar");
        btnBorrar = new JButton("Borrar");
        btnLimpiar = new JButton("Limpiar");
        btnGuardar.setEnabled(false);
        btnBorrar.setEnabled(false);

        JButton btnPdf = new JButton("Generar PDF");
        btnPdf.addActionListener(e -> GeneradorPdf.exportar(this, tablaRecursos,
                "Listado de Recursos", "recursos.pdf"));

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, EstiloUI.GAP, EstiloUI.GAP));
        panelBotones.setBackground(EstiloUI.BACKGROUND);
        for (JButton btn : new JButton[]{btnNuevo, btnGuardar, btnBorrar, btnLimpiar, btnPdf}) {
            EstiloUI.estilizarBoton(btn);
            panelBotones.add(btn);
        }

        JPanel panelFormulario = new JPanel(new BorderLayout(EstiloUI.GAP, EstiloUI.GAP));
        panelFormulario.setBackground(EstiloUI.BACKGROUND);
        panelFormulario.setBorder(EstiloUI.crearTitledBorder("Recurso"));
        panelFormulario.add(panelCampos, BorderLayout.CENTER);
        panelFormulario.add(panelBotones, BorderLayout.SOUTH);

        DefaultTableModel modeloTabla = new DefaultTableModel(
                new String[]{"Codigo", "Categoria", "Descripcion"}, 0
        );
        tablaRecursos = new JTable(modeloTabla);
        tablaRecursos.setDefaultEditor(Object.class, null);
        EstiloUI.estilizarTabla(tablaRecursos);

        JScrollPane scrollTabla = new JScrollPane(tablaRecursos);
        scrollTabla.setBorder(EstiloUI.crearTitledBorder("Listado"));

        JPanel panelCentro = new JPanel(new BorderLayout(EstiloUI.GAP, EstiloUI.GAP));
        panelCentro.setBackground(EstiloUI.BACKGROUND);
        panelCentro.add(panelFormulario, BorderLayout.NORTH);
        panelCentro.add(scrollTabla, BorderLayout.CENTER);

        add(panelFiltro, BorderLayout.NORTH);
        add(panelCentro, BorderLayout.CENTER);

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                if (onVisible != null) onVisible.run();
            }
        });

        btnNuevo.addActionListener(e -> {
            tablaRecursos.clearSelection();
            txtCodigo.setText("");
            txtDescripcion.setText("");
            comboCategoria.setSelectedIndex(-1);
            txtCodigo.setEditable(true);
            comboCategoria.setEnabled(true);
            txtDescripcion.setEditable(true);
            btnGuardar.setEnabled(true);
            btnBorrar.setEnabled(false);
            txtCodigo.requestFocus();
            if (onNuevo != null) onNuevo.run();
        });
        btnGuardar.addActionListener(e -> { if (onGuardar != null) onGuardar.run(); });
        btnLimpiar.addActionListener(e -> { if (onLimpiar != null) onLimpiar.run(); });
        btnFiltrar.addActionListener(e -> { if (onFiltrar != null) onFiltrar.run(); });
        btnBorrar.addActionListener(e -> { if (onBorrar != null) onBorrar.run(); });

        tablaRecursos.getSelectionModel()
                .addListSelectionListener(e -> {
                    if (!e.getValueIsAdjusting()) {
                        int fila = tablaRecursos.getSelectedRow();
                        if (fila != -1) {
                            txtCodigo.setText(tablaRecursos.getValueAt(fila, 0).toString());
                            CategoriaRecurso categoria = (CategoriaRecurso) tablaRecursos.getValueAt(fila, 1);
                            comboCategoria.setSelectedItem(categoria);
                            txtDescripcion.setText(tablaRecursos.getValueAt(fila, 2).toString());
                            txtCodigo.setEditable(false);
                            comboCategoria.setEnabled(true);
                            txtDescripcion.setEditable(true);
                            btnGuardar.setEnabled(true);
                            btnBorrar.setEnabled(true);
                        } else {
                            txtCodigo.setText("");
                            txtDescripcion.setText("");
                            comboCategoria.setSelectedIndex(-1);
                            txtCodigo.setEditable(false);
                            comboCategoria.setEnabled(false);
                            txtDescripcion.setEditable(false);
                            btnGuardar.setEnabled(false);
                            btnBorrar.setEnabled(false);
                        }
                        if (onSeleccionar != null) onSeleccionar.run();
                    }
                });
    }

    // --- Callback setters ---
    public void setOnFiltrar(Runnable cb) { this.onFiltrar = cb; }
    public void setOnNuevo(Runnable cb) { this.onNuevo = cb; }
    public void setOnGuardar(Runnable cb) { this.onGuardar = cb; }
    public void setOnBorrar(Runnable cb) { this.onBorrar = cb; }
    public void setOnLimpiar(Runnable cb) { this.onLimpiar = cb; }
    public void setOnSeleccionar(Runnable cb) { this.onSeleccionar = cb; }
    public void setOnVisible(Runnable cb) { this.onVisible = cb; }

    // --- Getters ---
    public String getCodigo() { return txtCodigo.getText().trim(); }
    public String getDescripcion() { return txtDescripcion.getText().trim(); }
    public CategoriaRecurso getCategoriaSeleccionada() { return (CategoriaRecurso) comboCategoria.getSelectedItem(); }
    public CategoriaRecurso getCategoriaFiltro() { return (CategoriaRecurso) comboFiltro.getSelectedItem(); }
    public int getFilaSeleccionada() { return tablaRecursos.getSelectedRow(); }

    // --- Public methods for controller ---
    public void cargarRecursos(List<Recurso> recursos) {
        DefaultTableModel modeloTabla = (DefaultTableModel) tablaRecursos.getModel();
        modeloTabla.setRowCount(0);
        for (Recurso recurso : recursos) {
            modeloTabla.addRow(new Object[]{
                    recurso.getCodigo(), recurso.getCategoria(), recurso.getDescripcion()
            });
        }
    }

    public void cargarCategorias(List<CategoriaRecurso> categorias) {
        comboFiltro.removeAllItems();
        comboCategoria.removeAllItems();
        for (CategoriaRecurso categoria : categorias) {
            comboFiltro.addItem(categoria);
            comboCategoria.addItem(categoria);
        }
    }

    public void limpiar() {
        txtCodigo.setText("");
        txtDescripcion.setText("");
        comboCategoria.setSelectedIndex(-1);
        tablaRecursos.clearSelection();
        txtCodigo.setEditable(false);
        comboCategoria.setEnabled(false);
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
