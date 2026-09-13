package view;

import model.CategoriaRecurso;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Dialogo modal para agregar o modificar un recurso.
 * Callback devuelve {codigo, idCategoria, descripcion}.
 */
public class RecursoDialog extends JDialog {

    private final JTextField campoCodigo = new JTextField(18);
    private final JComboBox<CategoriaRecurso> comboCategoria = new JComboBox<>();
    private final JTextField campoDescripcion = new JTextField(18);

    private Consumer<String[]> onGuardar;

    public RecursoDialog(Frame propietario, boolean esModificar,
                          String codigo, CategoriaRecurso categoriaActual, String descripcion,
                          List<CategoriaRecurso> categorias) {
        super(propietario, esModificar ? "Modificar Recurso" : "Agregar Recurso", true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(EstiloUI.BACKGROUND);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(EstiloUI.BACKGROUND);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel lblTitulo = new JLabel(esModificar ? "Modificar Recurso" : "Agregar Recurso",
                SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(EstiloUI.PRIMARY);
        lblTitulo.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        mainPanel.add(lblTitulo);
        mainPanel.add(Box.createVerticalStrut(16));

        // Cargar categorias en el combo
        for (CategoriaRecurso cat : categorias) {
            comboCategoria.addItem(cat);
        }
        comboCategoria.setFont(EstiloUI.NORMAL);

        JPanel campos = new JPanel(new GridBagLayout());
        campos.setBackground(EstiloUI.BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String[] etiquetas = {"Codigo:", "Categoria:", "Descripcion:"};
        Component[] fieldArray = {campoCodigo, comboCategoria, campoDescripcion};
        for (int i = 0; i < etiquetas.length; i++) {
            JLabel lbl = new JLabel(etiquetas[i]);
            EstiloUI.estilizarEtiqueta(lbl);
            gbc.gridx = 0; gbc.gridy = i; gbc.anchor = GridBagConstraints.EAST;
            campos.add(lbl, gbc);
            gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
            campos.add(fieldArray[i], gbc);
        }

        EstiloUI.estilizarCampo(campoCodigo);
        EstiloUI.estilizarCampo(campoDescripcion);

        if (esModificar) {
            campoCodigo.setText(codigo != null ? codigo : "");
            campoCodigo.setEditable(false);
            campoDescripcion.setText(descripcion != null ? descripcion : "");
            if (categoriaActual != null) comboCategoria.setSelectedItem(categoriaActual);
        }

        mainPanel.add(campos);
        mainPanel.add(Box.createVerticalStrut(12));

        JButton btnGuardar = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");
        EstiloUI.estilizarBotonPrimario(btnGuardar);
        EstiloUI.estilizarBoton(btnCancelar);
        btnGuardar.addActionListener(e -> guardar());
        btnCancelar.addActionListener(e -> dispose());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, EstiloUI.GAP, EstiloUI.GAP));
        botones.setBackground(EstiloUI.BACKGROUND);
        botones.add(btnGuardar);
        botones.add(btnCancelar);
        mainPanel.add(botones);

        add(mainPanel, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(propietario);
    }

    private void guardar() {
        String codigo = campoCodigo.getText().trim();
        CategoriaRecurso cat = (CategoriaRecurso) comboCategoria.getSelectedItem();
        String desc = campoDescripcion.getText().trim();

        if (codigo.isBlank()) {
            JOptionPane.showMessageDialog(this, "El codigo es obligatorio.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (cat == null) {
            JOptionPane.showMessageDialog(this, "Seleccione una categoria.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (desc.isBlank()) {
            JOptionPane.showMessageDialog(this, "La descripcion es obligatoria.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (onGuardar != null) {
            onGuardar.accept(new String[]{codigo, cat.getId(), desc});
        }
    }

    public void setOnGuardar(Consumer<String[]> cb) { this.onGuardar = cb; }
    public void cerrar() { dispose(); }
    public void mostrarError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
