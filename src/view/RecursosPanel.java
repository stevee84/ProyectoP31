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

        setLayout(new BorderLayout(10, 10));
        setBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );

        comboFiltro = new JComboBox<>();
        btnFiltrar = new JButton("Filtrar");

        JPanel panelFiltro = new JPanel(new FlowLayout());
        panelFiltro.setBorder(
                BorderFactory.createTitledBorder(
                        "Filtrar recursos"
                )
        );

        panelFiltro.add(new JLabel("Categoría:"));
        panelFiltro.add(comboFiltro);
        panelFiltro.add(btnFiltrar);

        txtCodigo = new JTextField(15);
        txtCodigo.setEditable(false);

        comboCategoria = new JComboBox<>();
        comboCategoria.setEnabled(false);

        txtDescripcion = new JTextField(15);
        txtDescripcion.setEditable(false);

        JPanel panelCampos = new JPanel(
                new GridLayout(3, 2, 5, 5)
        );

        panelCampos.add(new JLabel("Código:"));
        panelCampos.add(txtCodigo);

        panelCampos.add(new JLabel("Categoría:"));
        panelCampos.add(comboCategoria);

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

        JPanel panelFormulario = new JPanel(
                new BorderLayout()
        );

        panelFormulario.setBorder(
                BorderFactory.createTitledBorder("Recurso")
        );

        panelFormulario.add(
                panelCampos,
                BorderLayout.CENTER
        );

        panelFormulario.add(
                panelBotones,
                BorderLayout.SOUTH
        );

        DefaultTableModel modeloTabla =
                new DefaultTableModel(
                        new String[]{
                                "Código",
                                "Categoría",
                                "Descripción"
                        },
                        0
                );

        tablaRecursos = new JTable(modeloTabla);
        tablaRecursos.setDefaultEditor(
                Object.class,
                null
        );

        JScrollPane scrollTabla =
                new JScrollPane(tablaRecursos);

        scrollTabla.setBorder(
                BorderFactory.createTitledBorder("Listado")
        );

        JPanel panelCentro = new JPanel(
                new BorderLayout(10, 10)
        );

        panelCentro.add(
                panelFormulario,
                BorderLayout.NORTH
        );

        panelCentro.add(
                scrollTabla,
                BorderLayout.CENTER
        );

        add(panelFiltro, BorderLayout.NORTH);
        add(panelCentro, BorderLayout.CENTER);

        cargarCategorias();
        cargarRecursos();
        btnNuevo.addActionListener(e -> nuevoRecurso());
        btnGuardar.addActionListener(e -> guardarRecurso());
        btnLimpiar.addActionListener(e -> limpiarCampos());
        btnFiltrar.addActionListener(e -> filtrarRecursos());
        btnBorrar.addActionListener(e -> borrarRecurso());

        tablaRecursos.getSelectionModel()
                .addListSelectionListener(
                        e -> seleccionarRecurso()
                );
    }
    private void cargarCategorias() {
        comboFiltro.removeAllItems();
        comboCategoria.removeAllItems();

        for (CategoriaRecurso categoria :
                controlador.listarCategorias()) {

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
        CategoriaRecurso categoria =
                (CategoriaRecurso)
                        comboFiltro.getSelectedItem();

        if (categoria == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Debe seleccionar una categoría."
            );
            return;
        }

        DefaultTableModel modeloTabla =
                (DefaultTableModel) tablaRecursos.getModel();

        modeloTabla.setRowCount(0);

        for (Recurso recurso :
                controlador.listarRecursosPorCategoria(
                        categoria.getId()
                )) {

            modeloTabla.addRow(new Object[]{
                    recurso.getCodigo(),
                    recurso.getCategoria(),
                    recurso.getDescripcion()
            });
        }
    }

    private void cargarRecursos() {
        DefaultTableModel modeloTabla =
                (DefaultTableModel) tablaRecursos.getModel();

        modeloTabla.setRowCount(0);

        for (Recurso recurso :
                controlador.listarRecursos()) {

            modeloTabla.addRow(new Object[]{
                    recurso.getCodigo(),
                    recurso.getCategoria(),
                    recurso.getDescripcion()
            });
        }
    }
    private void guardarRecurso() {
        String codigo = txtCodigo.getText();

        CategoriaRecurso categoria =
                (CategoriaRecurso)
                        comboCategoria.getSelectedItem();

        String descripcion = txtDescripcion.getText();

        if (codigo.isBlank()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Debe escribir el código del recurso."
            );
            return;
        }

        if (categoria == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Debe seleccionar una categoría."
            );
            return;
        }

        if (descripcion.isBlank()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Debe escribir una descripción."
            );
            return;
        }

        int fila = tablaRecursos.getSelectedRow();

        if (fila == -1) {
            boolean registrado =
                    controlador.registrarRecurso(
                            codigo,
                            categoria.getId(),
                            descripcion
                    );

            if (!registrado) {
                JOptionPane.showMessageDialog(
                        this,
                        "Ya existe un recurso con ese código."
                );
                return;
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Recurso registrado correctamente."
            );
        } else {
            boolean modificado =
                    controlador.modificarRecurso(
                            codigo,
                            categoria.getId(),
                            descripcion
                    );

            if (!modificado) {
                JOptionPane.showMessageDialog(
                        this,
                        "No se encontró el recurso."
                );
                return;
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Recurso modificado correctamente."
            );
        }

        limpiarCampos();
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
            txtCodigo.setText(
                    tablaRecursos.getValueAt(
                            fila,
                            0
                    ).toString()
            );

            CategoriaRecurso categoria =
                    (CategoriaRecurso)
                            tablaRecursos.getValueAt(
                                    fila,
                                    1
                            );

            comboCategoria.setSelectedItem(categoria);

            txtDescripcion.setText(
                    tablaRecursos.getValueAt(
                            fila,
                            2
                    ).toString()
            );

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
            JOptionPane.showMessageDialog(
                    this,
                    "Debe seleccionar un recurso."
            );
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Desea borrar este recurso?",
                "Confirmar",
                JOptionPane.YES_NO_OPTION
        );

        if (respuesta == JOptionPane.YES_OPTION) {
            boolean eliminado =
                    controlador.eliminarRecurso(codigo);

            if (eliminado) {
                JOptionPane.showMessageDialog(
                        this,
                        "Recurso eliminado correctamente."
                );

                limpiarCampos();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "No se encontró el recurso."
                );
            }
        }
    }

}
