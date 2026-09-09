package view;

import model.Administrador;
import model.Empleado;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;

public class VentanaPrincipal extends JFrame {

    // Callbacks
    private Runnable onCerrarSesion;
    private Runnable onCambiarClave;

    public VentanaPrincipal(JPanel[] paneles, String[] nombresPestanas,
                            boolean isAdmin, String userId) {
        super("SISTEMA DE RESERVAS - " + userId
                + " (" + (isAdmin ? "Administrador" : "Funcionario") + ")");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 650);
        setLocationRelativeTo(null);
        getContentPane().setBackground(EstiloUI.BACKGROUND);

        crearMenuBar();

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(EstiloUI.BOLD);
        for (int i = 0; i < paneles.length; i++) {
            tabs.addTab(nombresPestanas[i], paneles[i]);
        }
        add(tabs);
    }

    private void crearMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setFont(EstiloUI.NORMAL);

        JMenu menuSesion = new JMenu("Sesion");
        menuSesion.setFont(EstiloUI.NORMAL);

        JMenuItem itemCambiarClave = new JMenuItem("Cambiar contrasena");
        itemCambiarClave.setFont(EstiloUI.NORMAL);
        itemCambiarClave.addActionListener(e -> {
            if (onCambiarClave != null) onCambiarClave.run();
        });

        JMenuItem itemCerrarSesion = new JMenuItem("Cerrar sesion");
        itemCerrarSesion.setFont(EstiloUI.NORMAL);
        itemCerrarSesion.addActionListener(e -> {
            if (onCerrarSesion != null) onCerrarSesion.run();
        });

        menuSesion.add(itemCambiarClave);
        menuSesion.addSeparator();
        menuSesion.add(itemCerrarSesion);

        menuBar.add(menuSesion);
        setJMenuBar(menuBar);
    }

    // --- Callback setters ---
    public void setOnCerrarSesion(Runnable cb) { this.onCerrarSesion = cb; }
    public void setOnCambiarClave(Runnable cb) { this.onCambiarClave = cb; }

    public boolean confirmarAccion(String msg) {
        return JOptionPane.showConfirmDialog(this, msg, "Confirmar",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}
