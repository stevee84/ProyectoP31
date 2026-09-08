package view;

import controller.RecursoController;
import model.CategoriaRecurso;
import model.Recurso;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class RecursosPanel extends JPanel {

    private RecursoController controlador;

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

    public RecursosPanel(RecursoController controlador) {
        this.controlador = controlador;

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

        cargarCategorias();
        cargarRecursos();

        // Recargar categorías y recursos cada vez que esta pestaña se hace visible
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                cargarCategorias();
                cargarRecursos();
            }
        });

        btnNuevo.addActionListener(e -> nuevoRecurso());
        btnGuardar.addActionListener(e -> guardarRecurso());
        btnLimpiar.addActionListener(e -> limpiarCampos());
        btnFiltrar.addActionListener(e -> filtrarRecursos());
        btnBorrar.addActionListener(e -> borrarRecurso());

        tablaRecursos.getSelectionModel()
                .addListSelectionListener(e -> {
                    if (!e.getValueIsAdjusting()) {
                        seleccionarRecurso();
                    }
                });
    }

    private void cargarCategorias() {
        comboFiltro.removeAllItems();
        comboCategoria.removeAllItems();
        for (CategoriaRecurso categoria : controlador.listarCategorias()) {
            comboFiltro.addItem(categoria);
            comboCategoria.addItem(categoria);
        }
    }

    private void nuevoRecurso() {
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
    }

    private void filtrarRecursos() {
        CategoriaRecurso categoria = (CategoriaRecurso) comboFiltro.getSelectedItem();
        if (categoria == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar una categoria.");
            return;
        }
        DefaultTableModel modeloTabla = (DefaultTableModel) tablaRecursos.getModel();
        modeloTabla.setRowCount(0);
        for (Recurso recurso : controlador.listarRecursosPorCategoria(categoria.getId())) {
            modeloTabla.addRow(new Object[]{
                    recurso.getCodigo(), recurso.getCategoria(), recurso.getDescripcion()
            });
        }
    }

    private void cargarRecursos() {
        DefaultTableModel modeloTabla = (DefaultTableModel) tablaRecursos.getModel();
        modeloTabla.setRowCount(0);
        for (Recurso recurso : controlador.listarRecursos()) {
            modeloTabla.addRow(new Object[]{
                    recurso.getCodigo(), recurso.getCategoria(), recurso.getDescripcion()
            });
        }
    }

    private void guardarRecurso() {
        String codigo = txtCodigo.getText();
        CategoriaRecurso categoria = (CategoriaRecurso) comboCategoria.getSelectedItem();
        String descripcion = txtDescripcion.getText();
        if (codigo.isBlank()) {
            JOptionPane.showMessageDialog(this, "Debe escribir el codigo del recurso.");
            return;
        }
        if (categoria == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar una categoria.");
            return;
        }
        if (descripcion.isBlank()) {
            JOptionPane.showMessageDialog(this, "Debe escribir una descripcion.");
            return;
        }
        try {
            int fila = tablaRecursos.getSelectedRow();
            if (fila == -1) {
                boolean registrado = controlador.registrarRecurso(codigo, categoria.getId(), descripcion);
                if (!registrado) {
                    JOptionPane.showMessageDialog(this, "Ya existe un recurso con ese codigo.");
                    return;
                }
                JOptionPane.showMessageDialog(this, "Recurso registrado correctamente.");
            } else {
                boolean modificado = controlador.modificarRecurso(codigo, categoria.getId(), descripcion);
                if (!modificado) {
                    JOptionPane.showMessageDialog(this, "No se encontro el recurso.");
                    return;
                }
                JOptionPane.showMessageDialog(this, "Recurso modificado correctamente.");
            }
            limpiarCampos();
        } catch (Exception error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarCampos() {
        txtCodigo.setText("");
        txtDescripcion.setText("");
        comboCategoria.setSelectedIndex(-1);
        tablaRecursos.clearSelection();
        txtCodigo.setEditable(false);
        comboCategoria.setEnabled(false);
        txtDescripcion.setEditable(false);
        btnGuardar.setEnabled(false);
        btnBorrar.setEnabled(false);
        cargarRecursos();
    }

    private void seleccionarRecurso() {
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
    }

    private void borrarRecurso() {
        String codigo = txtCodigo.getText();
        if (codigo.isBlank()) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un recurso.");
            return;
        }
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Desea borrar este recurso?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (respuesta == JOptionPane.YES_OPTION) {
            try {
                boolean eliminado = controlador.eliminarRecurso(codigo);
                if (eliminado) {
                    JOptionPane.showMessageDialog(this, "Recurso eliminado correctamente.");
                    limpiarCampos();
                } else {
                    JOptionPane.showMessageDialog(this, "No se encontro el recurso.");
                }
            } catch (RuntimeException error) {
                JOptionPane.showMessageDialog(this, error.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
