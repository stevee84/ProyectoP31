package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import model.CategoriaRecurso;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class CategoriasPanel extends JPanel {

    private final DefaultTableModel modeloTabla =
            new DefaultTableModel(new String[]{"ID", "Descripcion", "", ""}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return column >= 2;
                }
            };
    private final JTable tablaCategorias = new JTable(modeloTabla);

    // Callbacks
    private Runnable onAbrirAgregar;
    private Consumer<int[]> onAbrirModificar;
    private IntConsumer onEliminar;

    public CategoriasPanel() {
        setLayout(new BorderLayout(EstiloUI.GAP, EstiloUI.GAP));
        setBorder(EstiloUI.margenEstandar());
        setBackground(EstiloUI.BACKGROUND);

        add(new BarraBusquedaTabla(tablaCategorias, 2, 3), BorderLayout.NORTH);

        EstiloUI.estilizarTabla(tablaCategorias);
        configurarColumnasBotones();

        JScrollPane scroll = new JScrollPane(tablaCategorias);
        scroll.setBorder(EstiloUI.crearTitledBorder("Listado de categorias"));
        add(scroll, BorderLayout.CENTER);

        add(construirPanelInferior(), BorderLayout.SOUTH);
    }

    private void configurarColumnasBotones() {
        BotonTablaRenderer btnMod = new BotonTablaRenderer(
                "Modificar", EstiloUI.PRIMARY, Iconos.lapiz(), fila -> {
            if (onAbrirModificar != null) onAbrirModificar.accept(new int[]{fila});
        });
        BotonTablaRenderer btnElim = new BotonTablaRenderer(
                "Eliminar", EstiloUI.DANGER, Iconos.basurero(), fila -> {
            if (onEliminar != null) onEliminar.accept(fila);
        });

        TableColumn colMod = tablaCategorias.getColumnModel().getColumn(2);
        colMod.setCellRenderer(btnMod);
        colMod.setCellEditor(btnMod);
        colMod.setPreferredWidth(40);
        colMod.setMaxWidth(50);

        TableColumn colElim = tablaCategorias.getColumnModel().getColumn(3);
        colElim.setCellRenderer(btnElim);
        colElim.setCellEditor(btnElim);
        colElim.setPreferredWidth(40);
        colElim.setMaxWidth(50);
    }

    private JPanel construirPanelInferior() {
        JButton btnAgregar = new JButton("Agregar", Iconos.agregar());
        JButton btnPdf = new JButton("Generar PDF", Iconos.pdf());
        btnPdf.addActionListener(e -> GeneradorPdf.exportar(this, tablaCategorias,
                "Listado de Categorias", "categorias.pdf"));

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
    public void setOnAbrirAgregar(Runnable cb) { this.onAbrirAgregar = cb; }
    public void setOnAbrirModificar(Consumer<int[]> cb) { this.onAbrirModificar = cb; }
    public void setOnEliminar(IntConsumer cb) { this.onEliminar = cb; }

    // --- Getters ---
    public String[] getDatosFila(int filaModelo) {
        return new String[]{
                String.valueOf(modeloTabla.getValueAt(filaModelo, 0)),
                String.valueOf(modeloTabla.getValueAt(filaModelo, 1))
        };
    }

    // --- Public methods for controller ---
    public void cargarDatos(List<CategoriaRecurso> categorias) {
        modeloTabla.setRowCount(0);
        for (CategoriaRecurso c : categorias) {
            modeloTabla.addRow(new Object[]{c.getId(), c.getDescripcion(), "", ""});
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
