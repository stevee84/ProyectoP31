package view;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.BorderFactory;
import javax.swing.SwingConstants;
import javax.swing.Box;
import javax.swing.BoxLayout;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Font;

public class LoginFrame extends JFrame {

    private final JTextField campoId = new JTextField(18);
    private final JPasswordField campoClave = new JPasswordField(18);
    private final JButton btnIngresar = new JButton("Ingresar");

    // Callbacks
    private Runnable onLogin;

    public LoginFrame() {
        super("Sistema de Reservas - Iniciar sesion");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(EstiloUI.BACKGROUND);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(EstiloUI.BACKGROUND);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel lblTitulo = new JLabel("Sistema de Reservas", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(EstiloUI.PRIMARY);
        lblTitulo.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        JLabel lblSubtitulo = new JLabel("Inicie sesion para continuar", SwingConstants.CENTER);
        lblSubtitulo.setFont(EstiloUI.NORMAL);
        lblSubtitulo.setForeground(EstiloUI.TEXT_LIGHT);
        lblSubtitulo.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        mainPanel.add(lblTitulo);
        mainPanel.add(Box.createVerticalStrut(4));
        mainPanel.add(lblSubtitulo);
        mainPanel.add(Box.createVerticalStrut(24));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(EstiloUI.BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblId = new JLabel("Identificacion:");
        EstiloUI.estilizarEtiqueta(lblId);
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(lblId, gbc);

        EstiloUI.estilizarCampo(campoId);
        gbc.gridx = 1; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(campoId, gbc);

        JLabel lblClave = new JLabel("Contrasena:");
        EstiloUI.estilizarEtiqueta(lblClave);
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(lblClave, gbc);

        campoClave.setFont(EstiloUI.NORMAL);
        campoClave.setMargin(new Insets(2, 6, 2, 6));
        gbc.gridx = 1; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(campoClave, gbc);

        mainPanel.add(formPanel);
        mainPanel.add(Box.createVerticalStrut(16));

        EstiloUI.estilizarBotonPrimario(btnIngresar);
        btnIngresar.setPreferredSize(new Dimension(120, EstiloUI.BTN_HEIGHT));
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.setBackground(EstiloUI.BACKGROUND);
        btnPanel.add(btnIngresar);
        mainPanel.add(btnPanel);

        add(mainPanel, BorderLayout.CENTER);

        btnIngresar.addActionListener(e -> { if (onLogin != null) onLogin.run(); });
        campoClave.addActionListener(e -> { if (onLogin != null) onLogin.run(); });

        pack();
        setLocationRelativeTo(null);
    }

    // --- Callback setters ---
    public void setOnLogin(Runnable cb) { this.onLogin = cb; }

    // --- Getters ---
    public String getId() { return campoId.getText().trim(); }
    public String getPassword() { return new String(campoClave.getPassword()); }

    // --- Public methods for controller ---
    public void mostrarError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
        campoClave.setText("");
    }

    public void cerrar() {
        dispose();
    }
}
