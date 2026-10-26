package view;

import dto.CategoriaDTO;
import dto.RecursoDTO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.ArrayList;
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

    private JComboBox<CategoriaDTO> comboFiltro;
    /** Recursos en el mismo orden que las filas del modelo de la tabla. */
    private final List<RecursoDTO> recursos = new ArrayList<>();

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

        JPanel panelNorte = new JPanel(new BorderLayout(EstiloUI.GAP, 0));
        panelNorte.setBackground(EstiloUI.BACKGROUND);
        panelNorte.add(new BarraBusquedaTabla(tablaRecursos, 3, 4), BorderLayout.NORTH);
        panelNorte.add(construirPanelFiltro(), BorderLayout.SOUTH);
        add(panelNorte, BorderLayout.NORTH);

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
                "Modificar", EstiloUI.PRIMARY, Iconos.lapiz(), fila -> {
            if (onAbrirModificar != null) onAbrirModificar.accept(new int[]{fila});
        });
        BotonTablaRenderer btnElim = new BotonTablaRenderer(
                "Eliminar", EstiloUI.DANGER, Iconos.basurero(), fila -> {
            if (onEliminar != null) onEliminar.accept(fila);
        });

        TableColumn colMod = tablaRecursos.getColumnModel().getColumn(3);
        colMod.setCellRenderer(btnMod);
        colMod.setCellEditor(btnMod);
        colMod.setPreferredWidth(40);
        colMod.setMaxWidth(50);

        TableColumn colElim = tablaRecursos.getColumnModel().getColumn(4);
        colElim.setCellRenderer(btnElim);
        colElim.setCellEditor(btnElim);
        colElim.setPreferredWidth(40);
        colElim.setMaxWidth(50);
    }

    private JPanel construirPanelFiltro() {
        comboFiltro = new JComboBox<>();
        comboFiltro.setFont(EstiloUI.NORMAL);
        comboFiltro.setRenderer(new CategoriaRenderer("Todas"));
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
        JButton btnAgregar = new JButton("Agregar", Iconos.agregar());
        JButton btnPdf = new JButton("Generar PDF", Iconos.pdf());
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
    /** null cuando se eligió "Todas". */
    public CategoriaDTO getCategoriaFiltro() { return (CategoriaDTO) comboFiltro.getSelectedItem(); }

    public RecursoDTO getRecurso(int filaModelo) {
        return recursos.get(filaModelo);
    }

    // --- Public methods for controller ---
    public void cargarRecursos(List<RecursoDTO> lista) {
        recursos.clear();
        recursos.addAll(lista);
        modeloTabla.setRowCount(0);
        for (RecursoDTO r : lista) {
            modeloTabla.addRow(new Object[]{
                    r.codigo(), r.categoria() != null ? r.categoria().descripcion() : "", r.descripcion(), "", ""
            });
        }
    }

    public void cargarCategorias(List<CategoriaDTO> categorias) {
        Object anterior = comboFiltro.getSelectedItem();
        comboFiltro.removeAllItems();
        comboFiltro.addItem(null);
        for (CategoriaDTO cat : categorias) {
            comboFiltro.addItem(cat);
        }
        comboFiltro.setSelectedItem(anterior);
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
