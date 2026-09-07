package view;

import controller.UsuariosActividadesController;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Dialogo modal para cambiar la contrasena (usado tanto en el cambio
 * obligatorio del primer login como desde el menu "Cambiar clave").
 */
public class CambioClaveDialog extends JDialog {

    private final UsuariosActividadesController controller;
    private final JPasswordField campoNueva = new JPasswordField(18);
    private final JPasswordField campoConfirmar = new JPasswordField(18);
    private boolean cambiada = false;

    private CambioClaveDialog(Frame propietario, UsuariosActividadesController controller) {
        super(propietario, "Cambio de contrasena", true);
        this.controller = controller;

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(EstiloUI.BACKGROUND);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(EstiloUI.BACKGROUND);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel lblTitulo = new JLabel("Cambiar contrasena", SwingConstants.CENTER);
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

        JLabel lblNueva = new JLabel("Nueva contrasena:");
        JLabel lblConfirmar = new JLabel("Confirmar contrasena:");
        EstiloUI.estilizarEtiqueta(lblNueva);
        EstiloUI.estilizarEtiqueta(lblConfirmar);

        campoNueva.setFont(EstiloUI.NORMAL);
        campoNueva.setMargin(new Insets(2, 6, 2, 6));
        campoConfirmar.setFont(EstiloUI.NORMAL);
        campoConfirmar.setMargin(new Insets(2, 6, 2, 6));

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        campos.add(lblNueva, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        campos.add(campoNueva, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        campos.add(lblConfirmar, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        campos.add(campoConfirmar, gbc);

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
        String nueva = new String(campoNueva.getPassword());
        String confirmar = new String(campoConfirmar.getPassword());

        if (nueva.isBlank()) {
            JOptionPane.showMessageDialog(this, "La contrasena no puede estar vacia.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!nueva.equals(confirmar)) {
            JOptionPane.showMessageDialog(this, "Las contrasenas no coinciden.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            controller.cambiarContrasena(nueva);
            cambiada = true;
            dispose();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Abre el dialogo de forma modal y bloquea hasta que se cierre.
     *
     * @return true si la contrasena se cambio correctamente, false si se cancelo.
     */
    public static boolean solicitarCambio(Frame propietario, UsuariosActividadesController controller) {
        CambioClaveDialog dialogo = new CambioClaveDialog(propietario, controller);
        dialogo.setVisible(true);
        return dialogo.cambiada;
    }
}
