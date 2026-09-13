package view;

import model.CategoriaRecurso;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Dialogo modal para crear una nueva reservacion.
 * Incluye la frase de IA, campos manuales y lista de categorias.
 */
public class ReservaDialog extends JDialog {

    private final JTextField txtFrase = new JTextField();
    private final JButton btnLlenarConIA = new JButton("Extraer con IA");
    private final JTextField txtActividad = new JTextField(20);
    private final JTextField txtFecha = new JTextField("aaaa-mm-dd", 10);
    private final JTextField txtHoraInicio = new JTextField("HH:mm", 6);
    private final JTextField txtHoraFin = new JTextField("HH:mm", 6);
    private final JList<CategoriaRecurso> listaCategorias = new JList<>();

    // Callbacks
    private Runnable onAplicar;
    private Runnable onExtraerIA;

    public ReservaDialog(Frame propietario, List<CategoriaRecurso> categorias) {
        super(propietario, "Nueva Reserva", true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(true);
        getContentPane().setBackground(EstiloUI.BACKGROUND);

        JPanel mainPanel = new JPanel(new BorderLayout(EstiloUI.GAP, EstiloUI.GAP));
        mainPanel.setBackground(EstiloUI.BACKGROUND);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Titulo
        JLabel lblTitulo = new JLabel("Nueva Reserva", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(EstiloUI.PRIMARY);

        // Frase IA
        EstiloUI.estilizarCampo(txtFrase);
        EstiloUI.estilizarBotonPrimario(btnLlenarConIA);
        btnLlenarConIA.addActionListener(e -> { if (onExtraerIA != null) onExtraerIA.run(); });

        JPanel panelFrase = new JPanel(new BorderLayout(EstiloUI.GAP, EstiloUI.GAP));
        panelFrase.setBackground(EstiloUI.BACKGROUND);
        JLabel lblFrase = new JLabel("Frase:");
        EstiloUI.estilizarEtiqueta(lblFrase);
        panelFrase.add(lblFrase, BorderLayout.WEST);
        panelFrase.add(txtFrase, BorderLayout.CENTER);
        panelFrase.add(btnLlenarConIA, BorderLayout.EAST);

        // Campos manuales
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

        // Top section: titulo + frase + campos
        JPanel panelTop = new JPanel(new BorderLayout(EstiloUI.GAP, EstiloUI.GAP));
        panelTop.setBackground(EstiloUI.BACKGROUND);
        panelTop.add(lblTitulo, BorderLayout.NORTH);
        JPanel panelFormFields = new JPanel(new BorderLayout(EstiloUI.GAP, EstiloUI.GAP));
        panelFormFields.setBackground(EstiloUI.BACKGROUND);
        panelFormFields.add(panelFrase, BorderLayout.NORTH);
        panelFormFields.add(panelCampos, BorderLayout.SOUTH);
        panelTop.add(panelFormFields, BorderLayout.CENTER);

        // Lista categorias
        listaCategorias.setFont(EstiloUI.NORMAL);
        listaCategorias.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listaCategorias.setVisibleRowCount(5);
        listaCategorias.setSelectionBackground(EstiloUI.PRIMARY);
        listaCategorias.setSelectionForeground(Color.WHITE);
        listaCategorias.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof CategoriaRecurso cat) {
                    setText(cat.getDescripcion());
                }
                return c;
            }
        });
        listaCategorias.setListData(categorias.toArray(new CategoriaRecurso[0]));

        JScrollPane scrollCategorias = new JScrollPane(listaCategorias);
        scrollCategorias.setBorder(EstiloUI.crearTitledBorder("Categorias requeridas (seleccion multiple)"));

        // Botones
        JButton btnAplicar = new JButton("Aplicar");
        JButton btnCancelar = new JButton("Cancelar");
        EstiloUI.estilizarBotonPrimario(btnAplicar);
        EstiloUI.estilizarBoton(btnCancelar);
        btnAplicar.addActionListener(e -> { if (onAplicar != null) onAplicar.run(); });
        btnCancelar.addActionListener(e -> dispose());

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, EstiloUI.GAP, EstiloUI.GAP));
        panelBotones.setBackground(EstiloUI.BACKGROUND);
        panelBotones.add(btnAplicar);
        panelBotones.add(btnCancelar);

        mainPanel.add(panelTop, BorderLayout.NORTH);
        mainPanel.add(scrollCategorias, BorderLayout.CENTER);
        mainPanel.add(panelBotones, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
        setPreferredSize(new Dimension(520, 420));
        pack();
        setLocationRelativeTo(propietario);
    }

    // --- Callback setters ---
    public void setOnAplicar(Runnable cb) { this.onAplicar = cb; }
    public void setOnExtraerIA(Runnable cb) { this.onExtraerIA = cb; }

    // --- Getters ---
    public String getActividad() { return txtActividad.getText().trim(); }
    public String getFecha() { return txtFecha.getText().trim(); }
    public String getHoraInicio() { return txtHoraInicio.getText().trim(); }
    public String getHoraFin() { return txtHoraFin.getText().trim(); }
    public String getFrase() { return txtFrase.getText().trim(); }
    public List<CategoriaRecurso> getCategoriasSeleccionadas() { return listaCategorias.getSelectedValuesList(); }

    // --- Public methods ---
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

    public void cerrar() { dispose(); }

    public void mostrarMensaje(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

    public void mostrarError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
