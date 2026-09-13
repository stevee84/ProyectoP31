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
import javax.swing.table.TableColumn;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class FuncionarioPanel extends JPanel {

    private final DefaultTableModel modeloTabla =
            new DefaultTableModel(new Object[]{"Identificacion", "Nombre", "Telefono", "", ""}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return column >= 3; // solo columnas de botones
                }
            };
    private final JTable tabla = new JTable(modeloTabla);

    private final JTextField campoBusqueda = new JTextField(18);

    // Callbacks
    private Runnable onBuscar;
    private Runnable onMostrarTodos;
    private Runnable onAbrirAgregar;
    private Consumer<int[]> onAbrirModificar;
    private IntConsumer onEliminar;

    public FuncionarioPanel() {
        setLayout(new BorderLayout(EstiloUI.GAP, EstiloUI.GAP));
        setBorder(EstiloUI.margenEstandar());
        setBackground(EstiloUI.BACKGROUND);

        add(construirPanelBusqueda(), BorderLayout.NORTH);

        EstiloUI.estilizarTabla(tabla);
        configurarColumnasBotones();

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(EstiloUI.crearTitledBorder("Listado de funcionarios"));
        add(scroll, BorderLayout.CENTER);

        add(construirPanelInferior(), BorderLayout.SOUTH);
    }

    private void configurarColumnasBotones() {
        BotonTablaRenderer btnModificar = new BotonTablaRenderer(
                "Modificar", EstiloUI.PRIMARY, fila -> {
            if (onAbrirModificar != null) onAbrirModificar.accept(new int[]{fila});
        });
        BotonTablaRenderer btnEliminar = new BotonTablaRenderer(
                "Eliminar", EstiloUI.DANGER, fila -> {
            if (onEliminar != null) onEliminar.accept(fila);
        });

        TableColumn colMod = tabla.getColumnModel().getColumn(3);
        colMod.setCellRenderer(btnModificar);
        colMod.setCellEditor(btnModificar);
        colMod.setPreferredWidth(80);
        colMod.setMaxWidth(90);

        TableColumn colElim = tabla.getColumnModel().getColumn(4);
        colElim.setCellRenderer(btnEliminar);
        colElim.setCellEditor(btnEliminar);
        colElim.setPreferredWidth(80);
        colElim.setMaxWidth(90);
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

    private JPanel construirPanelInferior() {
        JButton btnAgregar = new JButton("Agregar");
        JButton btnPdf = new JButton("Generar PDF");
        btnPdf.addActionListener(e -> GeneradorPdf.exportar(this, tabla,
                "Listado de Funcionarios", "funcionarios.pdf"));

        EstiloUI.estilizarBotonPrimario(btnAgregar);
        EstiloUI.estilizarBoton(btnPdf);

        btnAgregar.addActionListener(e -> {
            if (onAbrirAgregar != null) onAbrirAgregar.run();
        });

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, EstiloUI.GAP, EstiloUI.GAP));
        panel.setBackground(EstiloUI.BACKGROUND);
        panel.add(btnAgregar);
        panel.add(btnPdf);
        return panel;
    }

    // --- Callback setters ---
    public void setOnBuscar(Runnable cb) { this.onBuscar = cb; }
    public void setOnMostrarTodos(Runnable cb) { this.onMostrarTodos = cb; }
    public void setOnAbrirAgregar(Runnable cb) { this.onAbrirAgregar = cb; }
    public void setOnAbrirModificar(Consumer<int[]> cb) { this.onAbrirModificar = cb; }
    public void setOnEliminar(IntConsumer cb) { this.onEliminar = cb; }

    // --- Getters ---
    public String getBusqueda() { return campoBusqueda.getText().trim(); }

    public String[] getDatosFila(int filaModelo) {
        return new String[]{
                String.valueOf(modeloTabla.getValueAt(filaModelo, 0)),
                String.valueOf(modeloTabla.getValueAt(filaModelo, 1)),
                String.valueOf(modeloTabla.getValueAt(filaModelo, 2))
        };
    }

    // --- Public methods for controller ---
    public void cargarDatos(List<Funcionario> funcionarios) {
        modeloTabla.setRowCount(0);
        for (Funcionario f : funcionarios) {
            modeloTabla.addRow(new Object[]{f.getId(), f.getName(), f.getTelefono(), "", ""});
        }
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
