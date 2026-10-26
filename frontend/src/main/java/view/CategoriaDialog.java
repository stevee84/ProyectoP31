package view;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Dialogo modal para agregar o modificar una categoria.
 * Callback devuelve {id, descripcion} — id vacio si es alta nueva.
 */
public class CategoriaDialog extends JDialog {

    private final JTextField campoId = new JTextField(18);
    private final JTextField campoDescripcion = new JTextField(18);

    private Consumer<String[]> onGuardar;

    public CategoriaDialog(Frame propietario, boolean esModificar, String id, String descripcion) {
        super(propietario, esModificar ? "Modificar Categoria" : "Agregar Categoria", true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(EstiloUI.BACKGROUND);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(EstiloUI.BACKGROUND);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel lblTitulo = new JLabel(esModificar ? "Modificar Categoria" : "Agregar Categoria",
                SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(EstiloUI.PRIMARY);
        lblTitulo.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        mainPanel.add(lblTitulo);
        mainPanel.add(Box.createVerticalStrut(16));

        JPanel campos = new JPanel(new GridBagLayout());
        campos.setBackground(EstiloUI.BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblId = new JLabel("ID:");
        JLabel lblDesc = new JLabel("Descripcion:");
        EstiloUI.estilizarEtiqueta(lblId);
        EstiloUI.estilizarEtiqueta(lblDesc);
        EstiloUI.estilizarCampo(campoId);
        EstiloUI.estilizarCampo(campoDescripcion);

        campoId.setEditable(false);
        campoId.setText(id != null ? id : "(autogenerado)");
        campoDescripcion.setText(descripcion != null ? descripcion : "");

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        campos.add(lblId, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        campos.add(campoId, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        campos.add(lblDesc, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        campos.add(campoDescripcion, gbc);

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
        String desc = campoDescripcion.getText().trim();
        if (desc.isBlank()) {
            JOptionPane.showMessageDialog(this, "La descripcion es obligatoria.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (onGuardar != null) {
            String id = campoId.getText().trim();
            if ("(autogenerado)".equals(id)) id = "";
            onGuardar.accept(new String[]{id, desc});
        }
    }

    public void setOnGuardar(Consumer<String[]> cb) { this.onGuardar = cb; }
    public void cerrar() { dispose(); }
    public void mostrarError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
