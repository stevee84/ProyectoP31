package view;

import model.CategoriaRecurso;
import model.Recurso;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class RecursosPanel extends JPanel {

    private final DefaultTableModel modeloTabla =
            new DefaultTableModel(new String[]{"Codigo", "Categoria", "Descripcion", "", ""}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return column >= 3;
                }
            };
    private final JTable tablaRecursos = new JTable(modeloTabla);

    private JComboBox<CategoriaRecurso> comboFiltro;

    // Callbacks
    private Runnable onFiltrar;
    private Runnable onAbrirAgregar;
    private Consumer<int[]> onAbrirModificar;
    private IntConsumer onEliminar;
    private Runnable onVisible;

    public RecursosPanel() {
        setLayout(new BorderLayout(EstiloUI.GAP, EstiloUI.GAP));
        setBorder(EstiloUI.margenEstandar());
        setBackground(EstiloUI.BACKGROUND);

        add(construirPanelFiltro(), BorderLayout.NORTH);

        EstiloUI.estilizarTabla(tablaRecursos);
        configurarColumnasBotones();

        JScrollPane scroll = new JScrollPane(tablaRecursos);
        scroll.setBorder(EstiloUI.crearTitledBorder("Listado de recursos"));
        add(scroll, BorderLayout.CENTER);

        add(construirPanelInferior(), BorderLayout.SOUTH);

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                if (onVisible != null) onVisible.run();
            }
        });
    }

    private void configurarColumnasBotones() {
        BotonTablaRenderer btnMod = new BotonTablaRenderer(
                "Modificar", EstiloUI.PRIMARY, fila -> {
            if (onAbrirModificar != null) onAbrirModificar.accept(new int[]{fila});
        });
        BotonTablaRenderer btnElim = new BotonTablaRenderer(
                "Eliminar", EstiloUI.DANGER, fila -> {
            if (onEliminar != null) onEliminar.accept(fila);
        });

        TableColumn colMod = tablaRecursos.getColumnModel().getColumn(3);
        colMod.setCellRenderer(btnMod);
        colMod.setCellEditor(btnMod);
        colMod.setPreferredWidth(80);
        colMod.setMaxWidth(90);

        TableColumn colElim = tablaRecursos.getColumnModel().getColumn(4);
        colElim.setCellRenderer(btnElim);
        colElim.setCellEditor(btnElim);
        colElim.setPreferredWidth(80);
        colElim.setMaxWidth(90);
    }

    private JPanel construirPanelFiltro() {
        comboFiltro = new JComboBox<>();
        comboFiltro.setFont(EstiloUI.NORMAL);
        JButton btnFiltrar = new JButton("Filtrar");

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, EstiloUI.GAP, EstiloUI.GAP));
        panel.setBackground(EstiloUI.BACKGROUND);
        panel.setBorder(EstiloUI.crearTitledBorder("Filtrar recursos"));
        JLabel lblCat = new JLabel("Categoria:");
        EstiloUI.estilizarEtiqueta(lblCat);
        EstiloUI.estilizarBoton(btnFiltrar);

        btnFiltrar.addActionListener(e -> { if (onFiltrar != null) onFiltrar.run(); });

        panel.add(lblCat);
        panel.add(comboFiltro);
        panel.add(btnFiltrar);
        return panel;
    }

    private JPanel construirPanelInferior() {
        JButton btnAgregar = new JButton("Agregar");
        JButton btnPdf = new JButton("Generar PDF");
        btnPdf.addActionListener(e -> GeneradorPdf.exportar(this, tablaRecursos,
                "Listado de Recursos", "recursos.pdf"));

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
    public void setOnFiltrar(Runnable cb) { this.onFiltrar = cb; }
    public void setOnAbrirAgregar(Runnable cb) { this.onAbrirAgregar = cb; }
    public void setOnAbrirModificar(Consumer<int[]> cb) { this.onAbrirModificar = cb; }
    public void setOnEliminar(IntConsumer cb) { this.onEliminar = cb; }
    public void setOnVisible(Runnable cb) { this.onVisible = cb; }

    // --- Getters ---
    public CategoriaRecurso getCategoriaFiltro() { return (CategoriaRecurso) comboFiltro.getSelectedItem(); }

    public Object[] getDatosFila(int filaModelo) {
        return new Object[]{
                modeloTabla.getValueAt(filaModelo, 0),  // codigo (String)
                modeloTabla.getValueAt(filaModelo, 1),  // CategoriaRecurso
                modeloTabla.getValueAt(filaModelo, 2)   // descripcion (String)
        };
    }

    // --- Public methods for controller ---
    public void cargarRecursos(List<Recurso> recursos) {
        modeloTabla.setRowCount(0);
        for (Recurso r : recursos) {
            modeloTabla.addRow(new Object[]{
                    r.getCodigo(), r.getCategoria(), r.getDescripcion(), "", ""
            });
        }
    }

    public void cargarCategorias(List<CategoriaRecurso> categorias) {
        comboFiltro.removeAllItems();
        for (CategoriaRecurso cat : categorias) {
            comboFiltro.addItem(cat);
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
