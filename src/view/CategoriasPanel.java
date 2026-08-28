package view;

import javax.swing.*;
import java.awt.*;

public class CategoriasPanel extends JPanel {

    private JTextField txtBusqueda;
    private JTextField txtId;
    private JTextField txtDescripcion;

    private JButton btnBuscar;
    private JButton btnGuardar;
    private JButton btnBorrar;
    private JButton btnLimpiar;

    private JTable tablaCategorias;

    public CategoriasPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        txtBusqueda = new JTextField(20);
        btnBuscar = new JButton("Buscar");

        JPanel panelBusqueda = new JPanel(new FlowLayout());
        panelBusqueda.setBorder(BorderFactory.createTitledBorder("Búsqueda"));
        panelBusqueda.add(new JLabel("Descripción:"));
        panelBusqueda.add(txtBusqueda);
        panelBusqueda.add(btnBuscar);

        txtId = new JTextField(15);
        txtId.setEditable(false);
        txtDescripcion = new JTextField(15);

        JPanel panelCampos = new JPanel(new GridLayout(2, 2, 5, 5));
        panelCampos.add(new JLabel("ID:"));
        panelCampos.add(txtId);
        panelCampos.add(new JLabel("Descripción:"));
        panelCampos.add(txtDescripcion);

        btnGuardar = new JButton("Guardar");
        btnBorrar = new JButton("Borrar");
        btnLimpiar = new JButton("Limpiar");

        JPanel panelBotones = new JPanel(new FlowLayout());
        panelBotones.add(btnGuardar);
        panelBotones.add(btnBorrar);
        panelBotones.add(btnLimpiar);

        JPanel panelFormulario = new JPanel(new BorderLayout());
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Categoría"));
        panelFormulario.add(panelCampos, BorderLayout.CENTER);
        panelFormulario.add(panelBotones, BorderLayout.SOUTH);

        tablaCategorias = new JTable(
                new Object[][]{},
                new String[]{"ID", "Descripción"}
        );

        JScrollPane scrollTabla = new JScrollPane(tablaCategorias);
        scrollTabla.setBorder(BorderFactory.createTitledBorder("Listado"));

        JPanel panelCentro = new JPanel(new BorderLayout(10, 10));
        panelCentro.add(panelFormulario, BorderLayout.NORTH);
        panelCentro.add(scrollTabla, BorderLayout.CENTER);

        add(panelBusqueda, BorderLayout.NORTH);
        add(panelCentro, BorderLayout.CENTER);
    }
}
