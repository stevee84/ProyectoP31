package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import model.CategoriaRecurso;

import java.util.List;

public class ReservaPanel extends JPanel {

    private JTable tablaReservas;

    private JTextField txtActividad;
    private JTextField txtFecha;
    private JTextField txtHoraInicio;
    private JTextField txtHoraFin;
    private JTextField txtFrase;
    private JButton btnLlenarConIA;
    private JList<CategoriaRecurso> listaCategorias;

    // Callbacks
    private Runnable onAplicar;
    private Runnable onCancelar;
    private Runnable onLimpiar;
    private Runnable onExtraerIA;
    private Runnable onVisible;

    public ReservaPanel() {
        setLayout(new BorderLayout(EstiloUI.GAP, EstiloUI.GAP));
        setBorder(EstiloUI.margenEstandar());
        setBackground(EstiloUI.BACKGROUND);

        // --- Frase en lenguaje natural + boton para llenar con IA ---
        txtFrase = new JTextField();
        EstiloUI.estilizarCampo(txtFrase);
        btnLlenarConIA = new JButton("Extraer con IA");
        EstiloUI.estilizarBotonPrimario(btnLlenarConIA);
        btnLlenarConIA.addActionListener(e -> { if (onExtraerIA != null) onExtraerIA.run(); });

        JPanel panelFrase = new JPanel(new BorderLayout(EstiloUI.GAP, EstiloUI.GAP));
        panelFrase.setBackground(EstiloUI.BACKGROUND);
        JLabel lblFrase = new JLabel("Frase:");
        EstiloUI.estilizarEtiqueta(lblFrase);
        panelFrase.add(lblFrase, BorderLayout.WEST);
        panelFrase.add(txtFrase, BorderLayout.CENTER);
        panelFrase.add(btnLlenarConIA, BorderLayout.EAST);

        // --- Campos de texto del formulario ---
        txtActividad = new JTextField(20);
        txtFecha = new JTextField("aaaa-mm-dd", 10);
        txtHoraInicio = new JTextField("HH:mm", 6);
        txtHoraFin = new JTextField("HH:mm", 6);
        EstiloUI.estilizarCampo(txtActividad);
        EstiloUI.estilizarCampo(txtFecha);
        EstiloUI.estilizarCampo(txtHoraInicio);
        EstiloUI.estilizarCampo(txtHoraFin);

        JPanel panelCampos = new JPanel(new GridBagLayout());
        panelCampos.setBackground(EstiloUI.BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String[] etiquetas = {"Actividad:", "Fecha:", "Hora inicio:", "Hora fin:"};
        JTextField[] campos = {txtActividad, txtFecha, txtHoraInicio, txtHoraFin};
        for (int i = 0; i < etiquetas.length; i++) {
            JLabel lbl = new JLabel(etiquetas[i]);
            EstiloUI.estilizarEtiqueta(lbl);
            gbc.gridx = (i % 2) * 2;
            gbc.gridy = i / 2;
            gbc.anchor = GridBagConstraints.EAST;
            gbc.weightx = 0;
            panelCampos.add(lbl, gbc);
            gbc.gridx = (i % 2) * 2 + 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 1.0;
            panelCampos.add(campos[i], gbc);
        }

        JPanel panelCamposCompleto = new JPanel(new BorderLayout(EstiloUI.GAP, EstiloUI.GAP));
        panelCamposCompleto.setBackground(EstiloUI.BACKGROUND);
        panelCamposCompleto.add(panelFrase, BorderLayout.NORTH);
        panelCamposCompleto.add(panelCampos, BorderLayout.SOUTH);

        // --- Lista de categorias, seleccion multiple ---
        listaCategorias = new JList<>();
        listaCategorias.setFont(EstiloUI.NORMAL);
        listaCategorias.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listaCategorias.setVisibleRowCount(4);
        listaCategorias.setSelectionBackground(EstiloUI.PRIMARY);
        listaCategorias.setSelectionForeground(Color.WHITE);
        listaCategorias.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof CategoriaRecurso categoria) {
                    setText(categoria.getDescripcion());
                }
                return c;
            }
        });
        JScrollPane scrollCategorias = new JScrollPane(listaCategorias);
        scrollCategorias.setBorder(EstiloUI.crearTitledBorder("Categorias requeridas (seleccion multiple)"));

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                if (onVisible != null) onVisible.run();
            }
        });

        JButton btnAplicar = new JButton("Aplicar");
        JButton btnCancelarSeleccionada = new JButton("Cancelar reserva");
        JButton btnLimpiarBtn = new JButton("Limpiar");
        JButton btnGenerarPdf = new JButton("Generar PDF");
        btnGenerarPdf.addActionListener(e -> GeneradorPdf.exportar(this, tablaReservas,
                "Mis Reservas", "reservas.pdf"));

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, EstiloUI.GAP, EstiloUI.GAP));
        panelBotones.setBackground(EstiloUI.BACKGROUND);
        for (JButton btn : new JButton[]{btnAplicar, btnCancelarSeleccionada, btnLimpiarBtn, btnGenerarPdf}) {
            EstiloUI.estilizarBoton(btn);
            panelBotones.add(btn);
        }

        btnAplicar.addActionListener(e -> { if (onAplicar != null) onAplicar.run(); });
        btnCancelarSeleccionada.addActionListener(e -> { if (onCancelar != null) onCancelar.run(); });
        btnLimpiarBtn.addActionListener(e -> { if (onLimpiar != null) onLimpiar.run(); });

        JPanel panelFormulario = new JPanel(new BorderLayout(EstiloUI.GAP, EstiloUI.GAP));
        panelFormulario.setBackground(EstiloUI.BACKGROUND);
        panelFormulario.setBorder(EstiloUI.crearTitledBorder("Nueva reserva"));
        panelFormulario.add(panelCamposCompleto, BorderLayout.NORTH);
        panelFormulario.add(scrollCategorias, BorderLayout.CENTER);
        panelFormulario.add(panelBotones, BorderLayout.SOUTH);

        // --- Tabla "Mis reservas" ---
        DefaultTableModel modeloTabla = new DefaultTableModel(
                new String[]{"Id", "Actividad", "Fecha", "Horario", "Recursos", "Estado"}, 0
        );
        tablaReservas = new JTable(modeloTabla);
        tablaReservas.setDefaultEditor(Object.class, null);
        EstiloUI.estilizarTabla(tablaReservas);

        JScrollPane scrollTabla = new JScrollPane(tablaReservas);
        scrollTabla.setBorder(EstiloUI.crearTitledBorder("Mis reservas"));

        add(panelFormulario, BorderLayout.NORTH);
        add(scrollTabla, BorderLayout.CENTER);
    }

    // --- Callback setters ---
    public void setOnAplicar(Runnable cb) { this.onAplicar = cb; }
    public void setOnCancelar(Runnable cb) { this.onCancelar = cb; }
    public void setOnLimpiar(Runnable cb) { this.onLimpiar = cb; }
    public void setOnExtraerIA(Runnable cb) { this.onExtraerIA = cb; }
    public void setOnVisible(Runnable cb) { this.onVisible = cb; }

    // --- Getters ---
    public String getActividad() { return txtActividad.getText().trim(); }
    public String getFecha() { return txtFecha.getText().trim(); }
    public String getHoraInicio() { return txtHoraInicio.getText().trim(); }
    public String getHoraFin() { return txtHoraFin.getText().trim(); }
    public String getFrase() { return txtFrase.getText().trim(); }
    public List<CategoriaRecurso> getCategoriasSeleccionadas() { return listaCategorias.getSelectedValuesList(); }
    public int getReservaSeleccionadaId() {
        int fila = tablaReservas.getSelectedRow();
        if (fila == -1) return -1;
        return (int) tablaReservas.getValueAt(fila, 0);
    }

    // --- Public methods for controller ---
    public void cargarReservas(Object[][] datos) {
        DefaultTableModel modeloTabla = (DefaultTableModel) tablaReservas.getModel();
        modeloTabla.setRowCount(0);
        for (Object[] fila : datos) {
            modeloTabla.addRow(fila);
        }
    }

    public void cargarCategorias(List<CategoriaRecurso> categorias) {
        listaCategorias.setListData(categorias.toArray(new CategoriaRecurso[0]));
    }

    public void limpiar() {
        txtFrase.setText("");
        txtActividad.setText("");
        txtFecha.setText("aaaa-mm-dd");
        txtHoraInicio.setText("HH:mm");
        txtHoraFin.setText("HH:mm");
        listaCategorias.clearSelection();
    }

    public void setEstadoBotonIA(boolean enabled, String texto) {
        btnLlenarConIA.setEnabled(enabled);
        btnLlenarConIA.setText(texto);
    }

    public void llenarFormulario(String actividad, String fecha, String horaInicio, String horaFin) {
        if (actividad != null) txtActividad.setText(actividad);
        if (fecha != null) txtFecha.setText(fecha);
        if (horaInicio != null) txtHoraInicio.setText(horaInicio);
        if (horaFin != null) txtHoraFin.setText(horaFin);
    }

    public void seleccionarCategoriasPorId(List<String> idsCategorias, List<CategoriaRecurso> todas) {
        int[] indices = java.util.stream.IntStream.range(0, todas.size())
                .filter(i -> idsCategorias.contains(todas.get(i).getId()))
                .toArray();
        listaCategorias.setSelectedIndices(indices);
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
