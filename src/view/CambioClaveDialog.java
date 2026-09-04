package view;

import controller.UsuariosActividadesController;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.FlowLayout;
import java.awt.GridLayout;

/**
 * Diálogo modal para cambiar la contraseña (usado tanto en el cambio
 * obligatorio del primer login como, más adelante, desde el menú
 * "Cambiar clave"). Se abre con {@link #solicitarCambio} y devuelve si el
 * cambio se completó o se canceló.
 */
public class CambioClaveDialog extends JDialog {

    private final UsuariosActividadesController controller;
    private final JPasswordField campoNueva = new JPasswordField(15);
    private final JPasswordField campoConfirmar = new JPasswordField(15);
    private boolean cambiada = false;

    private CambioClaveDialog(Frame propietario, UsuariosActividadesController controller) {
        super(propietario, "Cambio de contraseña", true);
        this.controller = controller;

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        JPanel campos = new JPanel(new GridLayout(2, 2, 4, 4));
        campos.add(new JLabel("Nueva contraseña:"));
        campos.add(campoNueva);
        campos.add(new JLabel("Confirmar contraseña:"));
        campos.add(campoConfirmar);
        add(campos, BorderLayout.CENTER);

        JButton btnGuardar = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");
        btnGuardar.addActionListener(e -> guardar());
        btnCancelar.addActionListener(e -> dispose());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER));
        botones.add(btnGuardar);
        botones.add(btnCancelar);
        add(botones, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(propietario);
    }

    private void guardar() {
        String nueva = new String(campoNueva.getPassword());
        String confirmar = new String(campoConfirmar.getPassword());

        if (nueva.isBlank()) {
            JOptionPane.showMessageDialog(this, "La contraseña no puede estar vacía.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!nueva.equals(confirmar)) {
            JOptionPane.showMessageDialog(this, "Las contraseñas no coinciden.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            controller.cambiarContrasena(nueva);
            cambiada = true;
            dispose();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Abre el diálogo de forma modal y bloquea hasta que se cierre.
     *
     * @return true si la contraseña se cambió correctamente, false si se canceló.
     */
    public static boolean solicitarCambio(Frame propietario, UsuariosActividadesController controller) {
        CambioClaveDialog dialogo = new CambioClaveDialog(propietario, controller);
        dialogo.setVisible(true);
        return dialogo.cambiada;
    }
}
