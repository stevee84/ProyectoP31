package view;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Dialogo modal para agregar o modificar un funcionario.
 * Recibe un callback con los datos {id, nombre, telefono} al guardar.
 */
public class FuncionarioDialog extends JDialog {

    private final JTextField campoId = new JTextField(18);
    private final JTextField campoNombre = new JTextField(18);
    private final JTextField campoTelefono = new JTextField(18);

    private Consumer<String[]> onGuardar;

    /**
     * @param propietario ventana padre
     * @param esModificar true si es edicion (id no editable)
     * @param id          valor inicial del id (null o vacio para alta)
     * @param nombre      valor inicial del nombre
     * @param telefono    valor inicial del telefono
     */
    public FuncionarioDialog(Frame propietario, boolean esModificar,
                              String id, String nombre, String telefono) {
        super(propietario, esModificar ? "Modificar Funcionario" : "Agregar Funcionario", true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(EstiloUI.BACKGROUND);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(EstiloUI.BACKGROUND);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel lblTitulo = new JLabel(esModificar ? "Modificar Funcionario" : "Agregar Funcionario",
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

        if (esModificar) {
            campoId.setText(id != null ? id : "");
            campoId.setEditable(false);
            campoNombre.setText(nombre != null ? nombre : "");
            campoTelefono.setText(telefono != null ? telefono : "");
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
        String id = campoId.getText().trim();
        String nombre = campoNombre.getText().trim();
        String telefono = campoTelefono.getText().trim();

        if (id.isBlank() || nombre.isBlank() || telefono.isBlank()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (onGuardar != null) {
            onGuardar.accept(new String[]{id, nombre, telefono});
        }
    }

    public void setOnGuardar(Consumer<String[]> cb) { this.onGuardar = cb; }

    public void cerrar() { dispose(); }

    public void mostrarError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
