package view;

import controller.UsuariosActividadesController;
import model.Funcionario;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.BorderFactory;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

/**
 * Panel de mantenimiento de funcionarios (alta, baja, modificacion y
 * busqueda), pensado para vivir como una pestana dentro del
 * {@code JTabbedPane} del frame principal ("SISTEMA DE RESERVAS"). Solo
 * depende de {@link UsuariosActividadesController}; no conoce
 * {@code ControladorReservaciones} ni el modelo directamente.
 */
public class FuncionarioPanel extends JPanel {

    private final UsuariosActividadesController controller;

    private final DefaultTableModel modeloTabla =
            new DefaultTableModel(new Object[]{"Identificacion", "Nombre", "Telefono"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
    private final JTable tabla = new JTable(modeloTabla);

    private final JTextField campoBusqueda = new JTextField(18);
    private final JTextField campoId = new JTextField(18);
    private final JTextField campoNombre = new JTextField(18);
    private final JTextField campoTelefono = new JTextField(18);

    private final JButton btnAgregar = new JButton("Agregar");
    private final JButton btnModificar = new JButton("Modificar");
    private final JButton btnEliminar = new JButton("Eliminar");
    private final JButton btnLimpiar = new JButton("Limpiar");

    public FuncionarioPanel(UsuariosActividadesController controller) {
        this.controller = controller;

        setLayout(new BorderLayout(EstiloUI.GAP, EstiloUI.GAP));
        setBorder(EstiloUI.margenEstandar());
        setBackground(EstiloUI.BACKGROUND);

        add(construirPanelBusqueda(), BorderLayout.NORTH);

        EstiloUI.estilizarTabla(tabla);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setPreferredSize(new Dimension(560, 220));
        scroll.setBorder(EstiloUI.crearTitledBorder("Listado de funcionarios"));
        add(scroll, BorderLayout.CENTER);

        add(construirPanelFormulario(), BorderLayout.SOUTH);

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccionEnFormulario();
            }
        });

        btnAgregar.addActionListener(e -> agregar());
        btnModificar.addActionListener(e -> modificar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        cargarFuncionarios(controller.listarFuncionarios());
        modoAlta();
    }

    private JPanel construirPanelBusqueda() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, EstiloUI.GAP, EstiloUI.GAP));
        panel.setBackground(EstiloUI.BACKGROUND);
        panel.setBorder(EstiloUI.crearTitledBorder("Busqueda"));

        JButton btnBuscar = new JButton("Buscar");
        JButton btnMostrarTodos = new JButton("Mostrar todos");

        JLabel lbl = new JLabel("Buscar:");
        EstiloUI.estilizarEtiqueta(lbl);
        EstiloUI.estilizarCampo(campoBusqueda);
        EstiloUI.estilizarBoton(btnBuscar);
        EstiloUI.estilizarBoton(btnMostrarTodos);

        btnBuscar.addActionListener(e -> {
            String texto = campoBusqueda.getText().trim();
            cargarFuncionarios(controller.buscarFuncionariosPorTexto(texto));
        });
        btnMostrarTodos.addActionListener(e -> {
            campoBusqueda.setText("");
            cargarFuncionarios(controller.listarFuncionarios());
        });

        panel.add(lbl);
        panel.add(campoBusqueda);
        panel.add(btnBuscar);
        panel.add(btnMostrarTodos);
        return panel;
    }

    private JPanel construirPanelFormulario() {
        JPanel panel = new JPanel(new BorderLayout(EstiloUI.GAP, EstiloUI.GAP));
        panel.setBackground(EstiloUI.BACKGROUND);
        panel.setBorder(EstiloUI.crearTitledBorder("Datos del funcionario"));

        JPanel campos = new JPanel(new GridBagLayout());
        campos.setBackground(EstiloUI.BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String[] etiquetas = {"Identificacion:", "Nombre:", "Telefono:"};
        JTextField[] fieldArray = {campoId, campoNombre, campoTelefono};
        for (int i = 0; i < etiquetas.length; i++) {
            JLabel lbl = new JLabel(etiquetas[i]);
            EstiloUI.estilizarEtiqueta(lbl);
            gbc.gridx = 0; gbc.gridy = i; gbc.anchor = GridBagConstraints.EAST;
            campos.add(lbl, gbc);
            EstiloUI.estilizarCampo(fieldArray[i]);
            gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
            campos.add(fieldArray[i], gbc);
        }

        JButton btnPdf = new JButton("Generar PDF");
        btnPdf.addActionListener(e -> GeneradorPdf.exportar(this, tabla,
                "Listado de Funcionarios", "funcionarios.pdf"));

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, EstiloUI.GAP, EstiloUI.GAP));
        botones.setBackground(EstiloUI.BACKGROUND);
        for (JButton btn : new JButton[]{btnAgregar, btnModificar, btnEliminar, btnLimpiar, btnPdf}) {
            EstiloUI.estilizarBoton(btn);
            botones.add(btn);
        }

        panel.add(campos, BorderLayout.CENTER);
        panel.add(botones, BorderLayout.SOUTH);
        return panel;
    }

    private void cargarFuncionarios(List<Funcionario> funcionarios) {
        modeloTabla.setRowCount(0);
        for (Funcionario f : funcionarios) {
            modeloTabla.addRow(new Object[]{f.getId(), f.getName(), f.getTelefono()});
        }
    }

    private void cargarSeleccionEnFormulario() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return;
        }
        campoId.setText(String.valueOf(modeloTabla.getValueAt(fila, 0)));
        campoNombre.setText(String.valueOf(modeloTabla.getValueAt(fila, 1)));
        campoTelefono.setText(String.valueOf(modeloTabla.getValueAt(fila, 2)));
        modoEdicion();
    }

    private void agregar() {
        try {
            controller.agregarFuncionario(campoNombre.getText().trim(), campoId.getText().trim(),
                    campoTelefono.getText().trim());
            cargarFuncionarios(controller.listarFuncionarios());
            limpiarFormulario();
        } catch (RuntimeException ex) {
            mostrarError(ex);
        }
    }

    private void modificar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un funcionario de la tabla.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            controller.actualizarFuncionario(campoId.getText().trim(), campoNombre.getText().trim(),
                    campoTelefono.getText().trim());
            cargarFuncionarios(controller.listarFuncionarios());
            limpiarFormulario();
        } catch (RuntimeException ex) {
            mostrarError(ex);
        }
    }

    private void eliminar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un funcionario de la tabla.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String id = String.valueOf(modeloTabla.getValueAt(fila, 0));
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Eliminar al funcionario " + id + "?", "Confirmar eliminacion",
                JOptionPane.YES_NO_OPTION);
        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            controller.eliminarFuncionario(id);
            cargarFuncionarios(controller.listarFuncionarios());
            limpiarFormulario();
        } catch (RuntimeException ex) {
            mostrarError(ex);
        }
    }

    private void limpiarFormulario() {
        tabla.clearSelection();
        campoId.setText("");
        campoNombre.setText("");
        campoTelefono.setText("");
        modoAlta();
    }

    private void modoAlta() {
        campoId.setEditable(true);
        btnAgregar.setEnabled(true);
        btnModificar.setEnabled(false);
        btnEliminar.setEnabled(false);
    }

    private void modoEdicion() {
        campoId.setEditable(false);
        btnAgregar.setEnabled(false);
        btnModificar.setEnabled(true);
        btnEliminar.setEnabled(true);
    }

    private void mostrarError(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
