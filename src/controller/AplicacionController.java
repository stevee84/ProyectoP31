package controller;

import consulta.ReservaConsultaAdapter;
import model.Administrador;
import model.Empleado;
import model.ExtractorReservaIA;
import model.ExtractorReservaIAGemini;
import model.ModeloReservaciones;
import model.ResultadoExtraccionIA;
import repository.PersistenciaXml;
import service.CalendarizacionService;
import service.CategoriaService;
import service.EstadisticasService;
import service.FuncionarioService;
import service.RecursoService;
import service.ReservaService;
import service.SesionService;
import view.AgendaSemanalPanel;
import view.CalendarizacionRecursosPanel;
import view.CambioClaveDialog;
import view.CategoriasPanel;
import view.EstadisticasPanel;
import view.EstadisticasRecursosPanel;
import view.FuncionarioPanel;
import view.LoginFrame;
import view.RecursosPanel;
import view.ReservaPanel;
import view.VentanaPrincipal;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador principal de la aplicacion. Se encarga de:
 * - Cargar/crear el modelo y la persistencia
 * - Crear todos los services
 * - Manejar el flujo login → ventana principal → logout
 * - Crear y conectar views con sus controllers
 */
public class AplicacionController {

    private static final String DATA_DIR = "data";

    private final ModeloReservaciones modelo;
    private final PersistenciaXml persistencia;

    // Services
    private final SesionService sesionService;
    private final FuncionarioService funcionarioService;
    private final CategoriaService categoriaService;
    private final RecursoService recursoService;
    private final ReservaService reservaService;
    private final EstadisticasService estadisticasService;
    private final CalendarizacionService calendarizacionService;

    public AplicacionController() {
        this.modelo = cargarModelo();
        this.persistencia = new PersistenciaXml(modelo, DATA_DIR);

        // Crear services
        this.sesionService = new SesionService(modelo, persistencia);
        this.funcionarioService = new FuncionarioService(modelo, persistencia, sesionService);
        this.categoriaService = new CategoriaService(modelo, persistencia, sesionService);
        this.recursoService = new RecursoService(modelo, persistencia, sesionService);

        ExtractorReservaIA extractorIA;
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey != null && !apiKey.isBlank()) {
            extractorIA = new ExtractorReservaIAGemini(apiKey);
        } else {
            extractorIA = (frase, cats) -> ResultadoExtraccionIA.fallo(
                    "No se configuro GEMINI_API_KEY. La extraccion con IA no esta disponible.");
        }

        this.reservaService = new ReservaService(modelo, persistencia, sesionService, extractorIA);
        ReservaConsultaAdapter reservaConsulta = new ReservaConsultaAdapter(modelo);
        this.estadisticasService = new EstadisticasService(modelo, reservaConsulta);
        this.calendarizacionService = new CalendarizacionService(modelo);

        // Guardar al cerrar la aplicacion
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                persistencia.guardarTodo();
            } catch (Exception e) {
                System.err.println("Error al guardar datos en shutdown: " + e.getMessage());
            }
        }));
    }

    /** Punto de entrada: muestra el login. */
    public void iniciar() {
        LoginFrame loginFrame = new LoginFrame();
        new LoginController(loginFrame, sesionService, this::abrirVentanaPrincipal);
        loginFrame.setVisible(true);
    }

    private ModeloReservaciones cargarModelo() {
        File dataDirFile = new File(DATA_DIR);
        String[] archivos = dataDirFile.list();
        boolean existenDatos = dataDirFile.exists() && archivos != null && archivos.length > 0;

        ModeloReservaciones mod;
        if (existenDatos) {
            mod = new ModeloReservaciones(true);
            try {
                new PersistenciaXml(mod, DATA_DIR).cargarTodo();
            } catch (Exception e) {
                int opcion = JOptionPane.showConfirmDialog(null,
                        "Error al cargar los datos guardados:\n" + e.getMessage()
                                + "\n\n¿Desea iniciar con datos por defecto?",
                        "Error de carga", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
                if (opcion == JOptionPane.YES_OPTION) {
                    mod = new ModeloReservaciones();
                } else {
                    System.exit(0);
                    return new ModeloReservaciones(); // unreachable, satisface compilador
                }
            }
        } else {
            mod = new ModeloReservaciones();
        }
        return mod;
    }

    private void abrirVentanaPrincipal(Empleado usuario) {
        boolean isAdmin = usuario instanceof Administrador;

        List<JPanel> paneles = new ArrayList<>();
        List<String> nombres = new ArrayList<>();

        if (isAdmin) {
            FuncionarioPanel funcionarioPanel = new FuncionarioPanel();
            new FuncionarioController(funcionarioPanel, funcionarioService);
            paneles.add(funcionarioPanel);
            nombres.add("Funcionarios");

            CategoriasPanel categoriasPanel = new CategoriasPanel();
            new CategoriaController(categoriasPanel, categoriaService);
            paneles.add(categoriasPanel);
            nombres.add("Categorias");

            RecursosPanel recursosPanel = new RecursosPanel();
            new RecursoController(recursosPanel, recursoService, categoriaService);
            paneles.add(recursosPanel);
            nombres.add("Recursos");
        } else {
            ReservaPanel reservaPanel = new ReservaPanel();
            new ReservaController(reservaPanel, reservaService, categoriaService);
            paneles.add(reservaPanel);
            nombres.add("Reservas");
        }

        // Tabs comunes: Calendarizacion, Actividades, Estadisticas
        agregarTabsComunes(paneles, nombres);

        VentanaPrincipal ventana = new VentanaPrincipal(
                paneles.toArray(new JPanel[0]),
                nombres.toArray(new String[0]),
                isAdmin,
                usuario.getId()
        );

        ventana.setOnCerrarSesion(() -> {
            sesionService.cerrarSesion();
            ventana.dispose();
            iniciar();
        });

        ventana.setOnCambiarClave(() -> {
            CambioClaveDialog dialogo = new CambioClaveDialog(ventana);
            dialogo.setOnGuardar(nueva -> {
                try {
                    sesionService.cambiarContrasena(nueva);
                    dialogo.marcarCambiada();
                    JOptionPane.showMessageDialog(ventana, "Contrasena cambiada exitosamente.");
                } catch (Exception e) {
                    dialogo.mostrarError(e.getMessage());
                }
            });
            dialogo.setVisible(true);
        });

        ventana.setVisible(true);
    }

    private void agregarTabsComunes(List<JPanel> paneles, List<String> nombres) {
        CalendarizacionRecursosPanel calendarizacionPanel = new CalendarizacionRecursosPanel();
        new CalendarizacionRecursosController(calendarizacionPanel, calendarizacionService);
        paneles.add(calendarizacionPanel);
        nombres.add("Calendarizacion");

        AgendaSemanalPanel agendaPanel = new AgendaSemanalPanel();
        new AgendaController(agendaPanel, estadisticasService);
        paneles.add(agendaPanel);
        nombres.add("Actividades");

        EstadisticasPanel estadisticasActPanel = new EstadisticasPanel();
        new EstadisticasController(estadisticasActPanel, estadisticasService);

        EstadisticasRecursosPanel estadisticasRecPanel = new EstadisticasRecursosPanel();
        new EstadisticasRecursosController(estadisticasRecPanel, estadisticasService);

        JTabbedPane tabsEstadisticas = new JTabbedPane();
        tabsEstadisticas.addTab("Actividades", estadisticasActPanel);
        tabsEstadisticas.addTab("Recursos", estadisticasRecPanel);

        JPanel panelEstadisticas = new JPanel(new java.awt.BorderLayout());
        panelEstadisticas.add(tabsEstadisticas, java.awt.BorderLayout.CENTER);
        paneles.add(panelEstadisticas);
        nombres.add("Estadisticas");
    }
}
