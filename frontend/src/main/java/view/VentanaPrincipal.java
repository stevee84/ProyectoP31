package view;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Ventana principal: pestañas en el centro y área de alertas fija a la derecha.
 * Las alertas no forman parte de ninguna pestaña, así siguen visibles al cambiar de pestaña.
 */
public class VentanaPrincipal extends JFrame {

    private Runnable onCerrarSesion;
    private Runnable onCambiarClave;
    private Runnable onSalir;

    /**
     * @param idUsuario    identificación mostrada en el título
     * @param rol          "Administrador" o "Funcionario"
     * @param nombre       nombre del usuario de la sesión
     * @param panelAlertas área de alertas, siempre visible a la derecha
     */
    public VentanaPrincipal(JPanel[] paneles, String[] nombresPestanas, Icon[] iconos,
                            String idUsuario, String rol, String nombre, JPanel panelAlertas) {
        super("SISTEMA DE RESERVAS - " + idUsuario + " (" + rol.toUpperCase() + ")");

        // El controlador decide cómo salir (cerrar sesión y desconectar del servidor).
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (onSalir != null) onSalir.run();
                else dispose();
            }
        });
        setSize(1240, 700);
        setLocationRelativeTo(null);
        getContentPane().setBackground(EstiloUI.BACKGROUND);

        crearMenuBar();

        JLabel lblOpciones = new JLabel("Opciones: " + rol + " - " + nombre);
        lblOpciones.setFont(EstiloUI.BOLD);
        lblOpciones.setForeground(EstiloUI.TEXT);
        lblOpciones.setBorder(BorderFactory.createEmptyBorder(6, 10, 2, 10));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(EstiloUI.BOLD);
        for (int i = 0; i < paneles.length; i++) {
            Icon icono = (iconos != null && i < iconos.length) ? iconos[i] : null;
            tabs.addTab(nombresPestanas[i], icono, paneles[i]);
        }

        JPanel centro = new JPanel(new BorderLayout());
        centro.setBackground(EstiloUI.BACKGROUND);
        centro.setBorder(BorderFactory.createEmptyBorder(0, 6, 6, 6));
        centro.add(lblOpciones, BorderLayout.NORTH);
        centro.add(tabs, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(centro, BorderLayout.CENTER);
        add(panelAlertas, BorderLayout.EAST);
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

        JMenuItem itemSalir = new JMenuItem("Salir");
        itemSalir.setFont(EstiloUI.NORMAL);
        itemSalir.addActionListener(e -> {
            if (onSalir != null) onSalir.run();
        });

        menuSesion.add(itemCambiarClave);
        menuSesion.addSeparator();
        menuSesion.add(itemCerrarSesion);
        menuSesion.add(itemSalir);

        menuBar.add(menuSesion);
        setJMenuBar(menuBar);
    }

    // --- Callback setters ---
    public void setOnCerrarSesion(Runnable cb) { this.onCerrarSesion = cb; }
    public void setOnCambiarClave(Runnable cb) { this.onCambiarClave = cb; }
    public void setOnSalir(Runnable cb) { this.onSalir = cb; }

    public boolean confirmarAccion(String msg) {
        return JOptionPane.showConfirmDialog(this, msg, "Confirmar",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public void mostrarMensaje(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

    public void mostrarError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
