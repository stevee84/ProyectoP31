package view;

import contrato.Reglas;

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
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.function.Consumer;

public class CambioClaveDialog extends JDialog {

    private JButton btnGuardar;

    private final JPasswordField campoNueva = new JPasswordField(18);
    private final JPasswordField campoConfirmar = new JPasswordField(18);
    private boolean cambiada = false;
    /** Opcionales: si se conocen, el diálogo adelanta las reglas que el servidor aplica igual. */
    private String idUsuario;
    private String claveActual;

    // Callback
    private Consumer<String> onGuardar;

    public CambioClaveDialog(Frame propietario) {
        super(propietario, "Cambio de contraseña", true);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(EstiloUI.BACKGROUND);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(EstiloUI.BACKGROUND);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel lblTitulo = new JLabel("Cambiar contraseña", SwingConstants.CENTER);
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

        JLabel lblNueva = new JLabel("Nueva contraseña:");
        JLabel lblConfirmar = new JLabel("Confirmar contraseña:");
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

        btnGuardar = new JButton("Guardar");
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

        String error = validar(nueva, confirmar, idUsuario, claveActual);
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (onGuardar != null) {
            onGuardar.accept(nueva);
        }
    }

    /** Mensaje de error de la clave nueva, o null si es aceptable. id y actual pueden ser null. */
    public static String validar(String nueva, String confirmar, String idUsuario, String claveActual) {
        if (nueva == null || nueva.isBlank()) return "La contraseña no puede estar vacía.";
        if (!nueva.equals(confirmar)) return "Las contraseñas no coinciden.";
        if (Reglas.claveIgualId(nueva, idUsuario)) return Reglas.MENSAJE_CLAVE_IGUAL_ID;
        if (nueva.equals(claveActual)) return Reglas.MENSAJE_CLAVE_IGUAL_ACTUAL;
        return null;
    }

    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }
    public void setClaveActual(String claveActual) { this.claveActual = claveActual; }

    /** Mientras el servidor responde, evita guardar dos veces. */
    public void setOcupado(boolean ocupado) {
        btnGuardar.setEnabled(!ocupado);
        btnGuardar.setText(ocupado ? "Guardando..." : "Guardar");
        setCursor(ocupado ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
    }

    // --- Callback setter ---
    public void setOnGuardar(Consumer<String> cb) { this.onGuardar = cb; }

    // --- Getters ---
    public String getNueva() { return new String(campoNueva.getPassword()); }
    public String getConfirmar() { return new String(campoConfirmar.getPassword()); }

    // --- Public methods ---
    public void mostrarError(String msg) {
        setOcupado(false);
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void marcarCambiada() {
        this.cambiada = true;
        dispose();
    }

    public boolean fueCambiada() {
        return cambiada;
    }
}
