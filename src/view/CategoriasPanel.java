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
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        txtBusqueda = new JTextField(20);
        btnBuscar = new JButton("Buscar");

        JPanel panelBusqueda = new JPanel(new FlowLayout());
        panelBusqueda.setBorder(
                BorderFactory.createTitledBorder("Búsqueda")
        );
        panelBusqueda.add(new JLabel("Descripción:"));
        panelBusqueda.add(txtBusqueda);
        panelBusqueda.add(btnBuscar);

        txtId = new JTextField(15);
        txtId.setEditable(false);

        txtDescripcion = new JTextField(15);
        txtDescripcion.setEditable(false);

        JPanel panelCampos = new JPanel(
                new GridLayout(2, 2, 5, 5)
        );
        panelCampos.add(new JLabel("ID:"));
        panelCampos.add(txtId);
        panelCampos.add(new JLabel("Descripción:"));
        panelCampos.add(txtDescripcion);

        btnNuevo = new JButton("Nuevo");
        btnGuardar = new JButton("Guardar");
        btnBorrar = new JButton("Borrar");
        btnLimpiar = new JButton("Limpiar");

        btnGuardar.setEnabled(false);
        btnBorrar.setEnabled(false);

        JPanel panelBotones = new JPanel(new FlowLayout());
        panelBotones.add(btnNuevo);
        panelBotones.add(btnGuardar);
        panelBotones.add(btnBorrar);
        panelBotones.add(btnLimpiar);

        JPanel panelFormulario = new JPanel(new BorderLayout());
        panelFormulario.setBorder(
                BorderFactory.createTitledBorder("Categoría")
        );
        panelFormulario.add(
                panelCampos,
                BorderLayout.CENTER
        );
        panelFormulario.add(
                panelBotones,
                BorderLayout.SOUTH
        );

        DefaultTableModel modeloTabla = new DefaultTableModel(
                new String[]{"ID", "Descripción"},
                0
        );

        tablaCategorias = new JTable(modeloTabla);
        tablaCategorias.setDefaultEditor(Object.class, null);

        JScrollPane scrollTabla =
                new JScrollPane(tablaCategorias);

        scrollTabla.setBorder(
                BorderFactory.createTitledBorder("Listado")
        );

        JPanel panelCentro =
                new JPanel(new BorderLayout(10, 10));

        panelCentro.add(
                panelFormulario,
                BorderLayout.NORTH
        );
        panelCentro.add(
                scrollTabla,
                BorderLayout.CENTER
        );

        add(panelBusqueda, BorderLayout.NORTH);
        add(panelCentro, BorderLayout.CENTER);

        cargarCategorias();

        btnBuscar.addActionListener(
                e -> buscarCategorias()
        );
        btnNuevo.addActionListener(
                e -> nuevaCategoria()
        );
        btnLimpiar.addActionListener(
                e -> limpiarCampos()
        );
        btnGuardar.addActionListener(
                e -> guardarCategoria()
        );
        btnBorrar.addActionListener(
                e -> borrarCategoria()
        );

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

        for (CategoriaRecurso categoria :
                controlador.listarCategorias()) {

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

        for (CategoriaRecurso categoria :
                controlador.buscarCategorias(descripcion)) {

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
            txtId.setText(
                    tablaCategorias.getValueAt(
                            fila,
                            0
                    ).toString()
            );

            txtDescripcion.setText(
                    tablaCategorias.getValueAt(
                            fila,
                            1
                    ).toString()
            );

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
            JOptionPane.showMessageDialog(
                    this,
                    "Debe escribir una descripción."
            );
            return;
        }

        try {
            if (id.isBlank()) {
                controlador.registrarCategoria(descripcion);

                JOptionPane.showMessageDialog(
                        this,
                        "Categoría registrada correctamente."
                );
            } else {
                controlador.modificarCategoria(
                        id,
                        descripcion
                );

                JOptionPane.showMessageDialog(
                        this,
                        "Categoría modificada correctamente."
                );
            }

            limpiarCampos();
        } catch (Exception error) {
            JOptionPane.showMessageDialog(
                    this,
                    error.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void borrarCategoria() {
        String id = txtId.getText();

        if (id.isBlank()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Debe seleccionar una categoría."
            );
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Desea borrar esta categoría?",
                "Confirmar",
                JOptionPane.YES_NO_OPTION
        );

        if (respuesta == JOptionPane.YES_OPTION) {
            try {
                boolean eliminada =
                        controlador.eliminarCategoria(id);

                if (eliminada) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Categoría eliminada correctamente."
                    );

                    limpiarCampos();
                }
            } catch (IllegalStateException error) {
                JOptionPane.showMessageDialog(
                        this,
                        error.getMessage()
                );
            }
        }
    }
}