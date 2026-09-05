package view;

import controller.CalendarizacionRecursosController;
import controller.CategoriaController;
import controller.ControladorReservaciones;
import controller.EstadisticasRecursosController;
import controller.RecursoController;
import controller.UsuariosActividadesController;
import model.Administrador;
import model.Empleado;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

/**
 * Ventana principal del sistema. Se muestra después de un login exitoso
 * y arma las pestañas según el rol del usuario autenticado.
 */
public class VentanaPrincipal extends JFrame {

    private final ControladorReservaciones controlador;
    private final UsuariosActividadesController usuariosCtrl;
    private final Empleado usuario;

    public VentanaPrincipal(ControladorReservaciones controlador,
                            UsuariosActividadesController usuariosCtrl,
                            Empleado usuario) {
        super("SISTEMA DE RESERVAS - " + usuario.getId()
                + " (" + (usuario instanceof Administrador ? "Administrador" : "Funcionario") + ")");
        this.controlador = controlador;
        this.usuariosCtrl = usuariosCtrl;
        this.usuario = usuario;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        crearMenuBar();
        crearPestanas();
    }

    private void crearMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menuSesion = new JMenu("Sesión");

        JMenuItem itemCambiarClave = new JMenuItem("Cambiar contraseña");
        itemCambiarClave.addActionListener(e -> {
            CambioClaveDialog.solicitarCambio(this, usuariosCtrl);
        });

        JMenuItem itemCerrarSesion = new JMenuItem("Cerrar sesión");
        itemCerrarSesion.addActionListener(e -> cerrarSesion());

        menuSesion.add(itemCambiarClave);
        menuSesion.addSeparator();
        menuSesion.add(itemCerrarSesion);

        menuBar.add(menuSesion);
        setJMenuBar(menuBar);
    }

    private void crearPestanas() {
        JTabbedPane tabs = new JTabbedPane();

        if (usuario instanceof Administrador) {
            // Pestañas de administrador
            tabs.addTab("Funcionarios", new FuncionarioPanel(usuariosCtrl));
            tabs.addTab("Categorías", new CategoriasPanel(new CategoriaController(controlador)));
            tabs.addTab("Recursos", new RecursosPanel(new RecursoController(controlador)));
            tabs.addTab("Calendarización",
                    new CalendarizacionRecursosPanel(new CalendarizacionRecursosController(controlador)));
            tabs.addTab("Actividades", new AgendaSemanalPanel(usuariosCtrl));
            tabs.addTab("Estadísticas", crearPanelEstadisticas());
        } else {
            // Pestañas de funcionario
            // TODO: crear ReservasPanel cuando esté disponible
            tabs.addTab("Reservas", new javax.swing.JPanel());
            tabs.addTab("Calendarización",
                    new CalendarizacionRecursosPanel(new CalendarizacionRecursosController(controlador)));
            tabs.addTab("Actividades", new AgendaSemanalPanel(usuariosCtrl));
            tabs.addTab("Estadísticas", crearPanelEstadisticas());
        }

        add(tabs);
    }

    /**
     * Crea un panel con sub-pestañas para las estadísticas de actividades
     * y las estadísticas de recursos.
     */
    private JTabbedPane crearPanelEstadisticas() {
        JTabbedPane subTabs = new JTabbedPane();
        subTabs.addTab("Actividades", new EstadisticasPanel(usuariosCtrl));
        subTabs.addTab("Recursos",
                new EstadisticasRecursosPanel(new EstadisticasRecursosController(controlador)));
        return subTabs;
    }

    private void cerrarSesion() {
        int opcion = JOptionPane.showConfirmDialog(this,
                "¿Desea cerrar la sesión actual?", "Cerrar sesión",
                JOptionPane.YES_NO_OPTION);
        if (opcion == JOptionPane.YES_OPTION) {
            controlador.cerrarSesion();
            dispose();
            // Volver a mostrar el login reutilizando el mismo controller
            LoginFrame login = new LoginFrame(usuariosCtrl, empleado -> {
                new VentanaPrincipal(controlador, usuariosCtrl, empleado).setVisible(true);
            });
            login.setVisible(true);
        }
    }
}
