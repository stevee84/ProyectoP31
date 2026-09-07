package view;

import controller.CalendarizacionRecursosController;
import controller.CategoriaController;
import controller.ControladorReservaciones;
import controller.EstadisticasRecursosController;
import controller.RecursoController;
import controller.ReservaController;
import controller.UsuariosActividadesController;
import model.Administrador;
import model.Empleado;
import model.ExtractorReservaIA;
import model.ExtractorReservaIAFalso;
import model.ExtractorReservaIAGemini;
import model.ResultadoExtraccionIA;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

/**
 * Ventana principal del sistema. Se muestra despues de un login exitoso
 * y arma las pestanas segun el rol del usuario autenticado.
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
        setSize(950, 650);
        setLocationRelativeTo(null);
        getContentPane().setBackground(EstiloUI.BACKGROUND);

        crearMenuBar();
        crearPestanas();
    }

    private void crearMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setFont(EstiloUI.NORMAL);

        JMenu menuSesion = new JMenu("Sesion");
        menuSesion.setFont(EstiloUI.NORMAL);

        JMenuItem itemCambiarClave = new JMenuItem("Cambiar contrasena");
        itemCambiarClave.setFont(EstiloUI.NORMAL);
        itemCambiarClave.addActionListener(e -> {
            CambioClaveDialog.solicitarCambio(this, usuariosCtrl);
        });

        JMenuItem itemCerrarSesion = new JMenuItem("Cerrar sesion");
        itemCerrarSesion.setFont(EstiloUI.NORMAL);
        itemCerrarSesion.addActionListener(e -> cerrarSesion());

        menuSesion.add(itemCambiarClave);
        menuSesion.addSeparator();
        menuSesion.add(itemCerrarSesion);

        menuBar.add(menuSesion);
        setJMenuBar(menuBar);
    }

    private void crearPestanas() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(EstiloUI.BOLD);

        if (usuario instanceof Administrador) {
            tabs.addTab("Funcionarios", new FuncionarioPanel(usuariosCtrl));
            tabs.addTab("Categorias", new CategoriasPanel(new CategoriaController(controlador)));
            tabs.addTab("Recursos", new RecursosPanel(new RecursoController(controlador)));
            tabs.addTab("Calendarizacion",
                    new CalendarizacionRecursosPanel(new CalendarizacionRecursosController(controlador)));
            tabs.addTab("Actividades", new AgendaSemanalPanel(usuariosCtrl));
            tabs.addTab("Estadisticas", crearPanelEstadisticas());
        } else {
            tabs.addTab("Reservas", new ReservaPanel(crearReservaController()));
            tabs.addTab("Calendarizacion",
                    new CalendarizacionRecursosPanel(new CalendarizacionRecursosController(controlador)));
            tabs.addTab("Actividades", new AgendaSemanalPanel(usuariosCtrl));
            tabs.addTab("Estadisticas", crearPanelEstadisticas());
        }

        add(tabs);
    }

    /**
     * Crea un panel con sub-pestanas para las estadisticas de actividades
     * y las estadisticas de recursos.
     */
    private JTabbedPane crearPanelEstadisticas() {
        JTabbedPane subTabs = new JTabbedPane();
        subTabs.setFont(EstiloUI.NORMAL);
        subTabs.addTab("Actividades", new EstadisticasPanel(usuariosCtrl));
        subTabs.addTab("Recursos",
                new EstadisticasRecursosPanel(new EstadisticasRecursosController(controlador)));
        return subTabs;
    }

    private ReservaController crearReservaController() {
        ExtractorReservaIA extractor;
        String geminiKey = System.getenv("GEMINI_API_KEY");
        if (geminiKey != null && !geminiKey.isBlank()) {
            extractor = new ExtractorReservaIAGemini(geminiKey);
        } else {
            extractor = new ExtractorReservaIAFalso(
                    ResultadoExtraccionIA.fallo("No se configuro GEMINI_API_KEY. Complete el formulario manualmente."));
        }
        return new ReservaController(controlador, extractor);
    }

    private void cerrarSesion() {
        int opcion = JOptionPane.showConfirmDialog(this,
                "¿Desea cerrar la sesion actual?", "Cerrar sesion",
                JOptionPane.YES_NO_OPTION);
        if (opcion == JOptionPane.YES_OPTION) {
            controlador.guardarDatos();
            controlador.cerrarSesion();
            dispose();
            LoginFrame login = new LoginFrame(usuariosCtrl, empleado -> {
                new VentanaPrincipal(controlador, usuariosCtrl, empleado).setVisible(true);
            });
            login.setVisible(true);
        }
    }
}
