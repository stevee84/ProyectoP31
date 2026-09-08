package view;

import controller.CategoriaController;
import javax.swing.*;
import java.awt.*;
import model.CategoriaRecurso;
import javax.swing.table.DefaultTableModel;

public class CategoriasPanel extends JPanel {
    private CategoriaController controlador;
    private JTextField txtBusqueda;
    private JTextField txtId;
    private JTextField txtDescripcion;

    private JButton btnBuscar;
    private JButton btnNuevo;
    private JButton btnGuardar;
    private JButton btnBorrar;
    private JButton btnLimpiar;

    private JTable tablaCategorias;

    public CategoriasPanel(CategoriaController controlador) {
        this.controlador = controlador;
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
        btnPdf.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Funcionalidad de PDF pendiente de implementacion.", "PDF", JOptionPane.INFORMATION_MESSAGE));

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

        cargarCategorias();

        btnBuscar.addActionListener(e -> buscarCategorias());
        btnNuevo.addActionListener(e -> nuevaCategoria());
        btnLimpiar.addActionListener(e -> limpiarCampos());
        btnGuardar.addActionListener(e -> guardarCategoria());
        btnBorrar.addActionListener(e -> borrarCategoria());

        tablaCategorias.getSelectionModel()
                .addListSelectionListener(e -> {
                    if (!e.getValueIsAdjusting()) {
                        seleccionarCategoria();
                    }
                });
    }

    public void cargarCategorias() {
        DefaultTableModel modeloTabla =
                (DefaultTableModel) tablaCategorias.getModel();
        modeloTabla.setRowCount(0);
        for (CategoriaRecurso categoria : controlador.listarCategorias()) {
            modeloTabla.addRow(new Object[]{
                    categoria.getId(),
                    categoria.getDescripcion()
            });
        }
    }

    private void buscarCategorias() {
        DefaultTableModel modeloTabla =
                (DefaultTableModel) tablaCategorias.getModel();
        modeloTabla.setRowCount(0);
        String descripcion = txtBusqueda.getText();
        for (CategoriaRecurso categoria : controlador.buscarCategorias(descripcion)) {
            modeloTabla.addRow(new Object[]{
                    categoria.getId(),
                    categoria.getDescripcion()
            });
        }
    }

    private void nuevaCategoria() {
        tablaCategorias.clearSelection();
        txtId.setText("");
        txtDescripcion.setText("");
        txtDescripcion.setEditable(true);
        btnGuardar.setEnabled(true);
        btnBorrar.setEnabled(false);
        txtDescripcion.requestFocus();
    }

    private void limpiarCampos() {
        txtBusqueda.setText("");
        txtId.setText("");
        txtDescripcion.setText("");
        tablaCategorias.clearSelection();
        txtDescripcion.setEditable(false);
        btnGuardar.setEnabled(false);
        btnBorrar.setEnabled(false);
        cargarCategorias();
    }

    private void seleccionarCategoria() {
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
    }

    private void guardarCategoria() {
        String id = txtId.getText();
        String descripcion = txtDescripcion.getText();
        if (descripcion.isBlank()) {
            JOptionPane.showMessageDialog(this, "Debe escribir una descripcion.");
            return;
        }
        try {
            if (id.isBlank()) {
                controlador.registrarCategoria(descripcion);
                JOptionPane.showMessageDialog(this, "Categoria registrada correctamente.");
            } else {
                controlador.modificarCategoria(id, descripcion);
                JOptionPane.showMessageDialog(this, "Categoria modificada correctamente.");
            }
            limpiarCampos();
        } catch (Exception error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void borrarCategoria() {
        String id = txtId.getText();
        if (id.isBlank()) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar una categoria.");
            return;
        }
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Desea borrar esta categoria?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (respuesta == JOptionPane.YES_OPTION) {
            try {
                boolean eliminada = controlador.eliminarCategoria(id);
                if (eliminada) {
                    JOptionPane.showMessageDialog(this, "Categoria eliminada correctamente.");
                    limpiarCampos();
                }
            } catch (RuntimeException error) {
                JOptionPane.showMessageDialog(this, error.getMessage());
            }
        }
    }
}
