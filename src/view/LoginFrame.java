package view;

import controller.ControladorReservaciones;
import controller.UsuariosActividadesController;
import model.Empleado;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.function.Consumer;

/**
 * Ventana de inicio de sesión. Es el único punto de entrada que todavía
 * necesita ser un {@code JFrame} propio (el resto de las pantallas del
 * módulo son paneles embebibles) porque se muestra antes de que exista
 * cualquier ventana principal. Al iniciar sesión con éxito (y, si aplica,
 * completar el cambio de contraseña obligatorio del primer login), se
 * cierra y delega en {@code alIniciarSesion} qué hacer después — así no
 * asume cómo será la ventana principal final (aún no construida).
 */
public class LoginFrame extends JFrame {

    private final UsuariosActividadesController controller;
    private final Consumer<Empleado> alIniciarSesion;

    private final JTextField campoId = new JTextField(15);
    private final JPasswordField campoClave = new JPasswordField(15);
    private final JButton btnIngresar = new JButton("Ingresar");

    public LoginFrame(UsuariosActividadesController controller, Consumer<Empleado> alIniciarSesion) {
        super("Sistema de Reservas - Iniciar sesión");
        this.controller = controller;
        this.alIniciarSesion = alIniciarSesion;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        JPanel campos = new JPanel(new GridLayout(2, 2, 4, 4));
        campos.add(new JLabel("Identificación:"));
        campos.add(campoId);
        campos.add(new JLabel("Contraseña:"));
        campos.add(campoClave);
        add(campos, BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER));
        botones.add(btnIngresar);
        add(botones, BorderLayout.SOUTH);

        btnIngresar.addActionListener(e -> ingresar());
        campoClave.addActionListener(e -> ingresar());

        pack();
        setLocationRelativeTo(null);
    }

    private void ingresar() {
        String id = campoId.getText().trim();
        String pass = new String(campoClave.getPassword());

        ControladorReservaciones.ResultadoSesion resultado = controller.iniciarSesion(id, pass);
        if (resultado.empleado() == null) {
            JOptionPane.showMessageDialog(this, "Identificación o contraseña incorrecta.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            campoClave.setText("");
            return;
        }

        if (resultado.requiereCambioContraseña()) {
            boolean cambiada = CambioClaveDialog.solicitarCambio(this, controller);
            if (!cambiada) {
                controller.cerrarSesion();
                campoClave.setText("");
                return;
            }
        }

        dispose();
        alIniciarSesion.accept(resultado.empleado());
    }
}
