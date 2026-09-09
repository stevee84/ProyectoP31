package view;

import model.Funcionario;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

public class FuncionarioPanel extends JPanel {

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

    // Callbacks
    private Runnable onBuscar;
    private Runnable onMostrarTodos;
    private Runnable onAgregar;
    private Runnable onModificar;
    private Runnable onEliminar;
    private Runnable onLimpiar;
    private Runnable onSeleccionar;

    public FuncionarioPanel() {
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
                int fila = tabla.getSelectedRow();
                if (fila >= 0) {
                    campoId.setText(String.valueOf(modeloTabla.getValueAt(fila, 0)));
                    campoNombre.setText(String.valueOf(modeloTabla.getValueAt(fila, 1)));
                    campoTelefono.setText(String.valueOf(modeloTabla.getValueAt(fila, 2)));
                    modoEdicion();
                }
                if (onSeleccionar != null) onSeleccionar.run();
            }
        });

        btnAgregar.addActionListener(e -> { if (onAgregar != null) onAgregar.run(); });
        btnModificar.addActionListener(e -> { if (onModificar != null) onModificar.run(); });
        btnEliminar.addActionListener(e -> { if (onEliminar != null) onEliminar.run(); });
        btnLimpiar.addActionListener(e -> { if (onLimpiar != null) onLimpiar.run(); });

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

        btnBuscar.addActionListener(e -> { if (onBuscar != null) onBuscar.run(); });
        btnMostrarTodos.addActionListener(e -> {
            campoBusqueda.setText("");
            if (onMostrarTodos != null) onMostrarTodos.run();
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

    // --- Callback setters ---
    public void setOnBuscar(Runnable cb) { this.onBuscar = cb; }
    public void setOnMostrarTodos(Runnable cb) { this.onMostrarTodos = cb; }
    public void setOnAgregar(Runnable cb) { this.onAgregar = cb; }
    public void setOnModificar(Runnable cb) { this.onModificar = cb; }
    public void setOnEliminar(Runnable cb) { this.onEliminar = cb; }
    public void setOnLimpiar(Runnable cb) { this.onLimpiar = cb; }
    public void setOnSeleccionar(Runnable cb) { this.onSeleccionar = cb; }

    // --- Getters ---
    public String getBusqueda() { return campoBusqueda.getText().trim(); }
    public String getId() { return campoId.getText().trim(); }
    public String getNombre() { return campoNombre.getText().trim(); }
    public String getTelefono() { return campoTelefono.getText().trim(); }
    public int getFilaSeleccionada() { return tabla.getSelectedRow(); }

    // --- Public methods for controller ---
    public void cargarDatos(List<Funcionario> funcionarios) {
        modeloTabla.setRowCount(0);
        for (Funcionario f : funcionarios) {
            modeloTabla.addRow(new Object[]{f.getId(), f.getName(), f.getTelefono()});
        }
    }

    public void limpiar() {
        tabla.clearSelection();
        campoId.setText("");
        campoNombre.setText("");
        campoTelefono.setText("");
        modoAlta();
    }

    public void modoAlta() {
        campoId.setEditable(true);
        btnAgregar.setEnabled(true);
        btnModificar.setEnabled(false);
        btnEliminar.setEnabled(false);
    }

    public void modoEdicion() {
        campoId.setEditable(false);
        btnAgregar.setEnabled(false);
        btnModificar.setEnabled(true);
        btnEliminar.setEnabled(true);
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
