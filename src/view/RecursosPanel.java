package view;

import controller.RecursoController;
import model.CategoriaRecurso;

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
    }
}
