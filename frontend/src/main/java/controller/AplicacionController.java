package controller;

import contrato.IServicioAplicacion;
import dto.UsuarioDTO;
import model.AlertasModel;
import model.CalendarizacionModel;
import model.CategoriaModel;
import model.EstadisticasModel;
import model.FuenteEventos;
import model.FuncionarioModel;
import model.RecursoModel;
import model.ReservaModel;
import model.SesionModel;
import util.Tarea;
import view.AgendaSemanalPanel;
import view.AlertasPanel;
import view.CalendarizacionRecursosPanel;
import view.CambioClaveDialog;
import view.CategoriasPanel;
import view.EstadisticasPanel;
import view.EstadisticasRecursosPanel;
import view.FuncionarioPanel;
import view.Iconos;
import view.LoginFrame;
import view.RecursosPanel;
import view.ReservaPanel;
import view.VentanaPrincipal;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador principal del Frontend: flujo login → ventana principal → cierre de sesión.
 *
 * <p>Solo conoce el contrato IServicioAplicacion (en ejecución, el ProxyServicio) y una
 * fuente de alertas. No hay XML, servicios locales ni referencias a la base de datos:
 * todo lo persistente vive en el Backend.
 */
public class AplicacionController {

    private final SesionModel sesionModel;
    private final FuncionarioModel funcionarioModel;
    private final CategoriaModel categoriaModel;
    private final RecursoModel recursoModel;
    private final ReservaModel reservaModel;
    private final EstadisticasModel estadisticasModel;
    private final CalendarizacionModel calendarizacionModel;
    private final AlertasModel alertasModel = new AlertasModel();

    private final FuenteEventos fuenteEventos;
    private final Runnable alSalir;

    private AlertasController alertasController;

    /**
     * @param servicio      contrato remoto (ProxyServicio) o un servicio falso en pruebas
     * @param fuenteEventos de dónde llegan las alertas asincrónicas
     * @param conexion      reabre el socket antes del login si se había perdido
     * @param alSalir       cierra la conexión y termina el proceso
     */
    public AplicacionController(IServicioAplicacion servicio, FuenteEventos fuenteEventos,
                                SesionModel.Conexion conexion, Runnable alSalir) {
        this.sesionModel = new SesionModel(servicio, conexion);
        this.funcionarioModel = new FuncionarioModel(servicio);
        this.categoriaModel = new CategoriaModel(servicio);
        this.recursoModel = new RecursoModel(servicio);
        this.reservaModel = new ReservaModel(servicio);
        this.estadisticasModel = new EstadisticasModel(servicio);
        this.calendarizacionModel = new CalendarizacionModel(servicio);
        this.fuenteEventos = fuenteEventos;
        this.alSalir = alSalir;
    }

    /** Punto de entrada: muestra el login. Debe llamarse en el hilo de Swing. */
    public void iniciar() {
        LoginFrame loginFrame = new LoginFrame();
        new LoginController(loginFrame, sesionModel, this::abrirVentanaPrincipal);
        loginFrame.setVisible(true);
    }

    private void abrirVentanaPrincipal(UsuarioDTO usuario) {
        boolean esAdmin = usuario.esAdministrador();

        List<JPanel> paneles = new ArrayList<>();
        List<String> nombres = new ArrayList<>();
        List<Icon> iconos = new ArrayList<>();

        if (esAdmin) {
            FuncionarioPanel funcionarioPanel = new FuncionarioPanel();
            new FuncionarioController(funcionarioPanel, funcionarioModel);
            agregar(paneles, nombres, iconos, funcionarioPanel, "Funcionarios", Iconos.personas());

            CategoriasPanel categoriasPanel = new CategoriasPanel();
            new CategoriaController(categoriasPanel, categoriaModel);
            agregar(paneles, nombres, iconos, categoriasPanel, "Categorias", Iconos.etiqueta());

            RecursosPanel recursosPanel = new RecursosPanel();
            new RecursoController(recursosPanel, recursoModel, categoriaModel);
            agregar(paneles, nombres, iconos, recursosPanel, "Recursos", Iconos.recurso());
        } else {
            ReservaPanel reservaPanel = new ReservaPanel();
            new ReservaController(reservaPanel, reservaModel, categoriaModel);
            agregar(paneles, nombres, iconos, reservaPanel, "Reservas", Iconos.calendario());
        }

        agregarTabsComunes(paneles, nombres, iconos);

        AlertasPanel alertasPanel = new AlertasPanel();
        alertasController = new AlertasController(alertasModel, alertasPanel, fuenteEventos, usuario.id());
        alertasController.iniciar();

        String rol = esAdmin ? "Administrador" : "Funcionario";
        VentanaPrincipal ventana = new VentanaPrincipal(
                paneles.toArray(new JPanel[0]),
                nombres.toArray(new String[0]),
                iconos.toArray(new Icon[0]),
                usuario.id(), rol, usuario.nombre(), alertasPanel);

        ventana.setOnCerrarSesion(() -> {
            if (!ventana.confirmarAccion("Desea cerrar la sesion?")) return;
            terminarSesion(() -> {
                ventana.dispose();
                iniciar();
            });
        });

        ventana.setOnSalir(() -> terminarSesion(() -> {
            ventana.dispose();
            alSalir.run();
        }));

        ventana.setOnCambiarClave(() -> {
            CambioClaveDialog dialogo = new CambioClaveDialog(ventana);
            dialogo.setOnGuardar(nueva -> Tarea.ejecutar(
                    () -> { sesionModel.cambiarClave(nueva); return null; },
                    r -> {
                        dialogo.marcarCambiada();
                        ventana.mostrarMensaje("Contrasena cambiada exitosamente.");
                    },
                    e -> dialogo.mostrarError(e.getMessage())));
            dialogo.setVisible(true);
        });

        ventana.setVisible(true);
    }

    /** Deja de escuchar alertas, vacía la lista y cierra la sesión en el servidor. */
    private void terminarSesion(Runnable despues) {
        if (alertasController != null) {
            alertasController.detener();
            alertasController = null;
        }
        // Aunque el servidor no responda, la sesión local termina igual.
        Tarea.ejecutar(() -> { sesionModel.cerrarSesion(); return null; },
                r -> despues.run(),
                e -> despues.run());
    }

    private void agregarTabsComunes(List<JPanel> paneles, List<String> nombres, List<Icon> iconos) {
        CalendarizacionRecursosPanel calendarizacionPanel = new CalendarizacionRecursosPanel();
        new CalendarizacionRecursosController(calendarizacionPanel, calendarizacionModel);
        agregar(paneles, nombres, iconos, calendarizacionPanel, "Calendarizacion", Iconos.grilla());

        AgendaSemanalPanel agendaPanel = new AgendaSemanalPanel();
        new AgendaController(agendaPanel, estadisticasModel);
        agregar(paneles, nombres, iconos, agendaPanel, "Actividades", Iconos.agenda());

        EstadisticasPanel estadisticasActPanel = new EstadisticasPanel();
        new EstadisticasController(estadisticasActPanel, estadisticasModel);

        EstadisticasRecursosPanel estadisticasRecPanel = new EstadisticasRecursosPanel();
        new EstadisticasRecursosController(estadisticasRecPanel, estadisticasModel);

        JTabbedPane tabsEstadisticas = new JTabbedPane();
        tabsEstadisticas.addTab("Recursos", Iconos.recurso(), estadisticasRecPanel);
        tabsEstadisticas.addTab("Actividades", Iconos.agenda(), estadisticasActPanel);

        JPanel panelEstadisticas = new JPanel(new BorderLayout());
        panelEstadisticas.add(tabsEstadisticas, BorderLayout.CENTER);
        agregar(paneles, nombres, iconos, panelEstadisticas, "Estadisticas", Iconos.estadisticas());
    }

    private static void agregar(List<JPanel> paneles, List<String> nombres, List<Icon> iconos,
                                JPanel panel, String nombre, Icon icono) {
        paneles.add(panel);
        nombres.add(nombre);
        iconos.add(icono);
    }

    /** Usado por las pruebas: alertas de la sesión abierta. */
    public AlertasModel getAlertasModel() {
        return alertasModel;
    }
}
