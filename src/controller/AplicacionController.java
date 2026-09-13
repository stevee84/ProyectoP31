package controller;

import consulta.ReservaConsultaAdapter;
import model.Administrador;
import model.CalendarizacionModel;
import model.CategoriaModel;
import model.Empleado;
import model.EstadisticasModel;
import model.ExtractorReservaIA;
import model.ExtractorReservaIAGemini;
import model.FuncionarioModel;
import model.ModeloReservaciones;
import model.RecursoModel;
import model.ReservaModel;
import model.ResultadoExtraccionIA;
import model.SesionModel;
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

import view.Iconos;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador principal de la aplicacion. Se encarga de:
 * - Cargar/crear el modelo y la persistencia
 * - Crear todos los services y sus fachadas Model
 * - Manejar el flujo login → ventana principal → logout
 * - Crear y conectar views con sus controllers
 *
 * Los sub-controllers solo reciben fachadas Model (nunca services).
 */
public class AplicacionController {

    private static final String DATA_DIR = "data";

    private final ModeloReservaciones modelo;
    private final PersistenciaXml persistencia;

    // Fachadas Model (wrappean services, son lo que reciben los controllers)
    private final SesionModel sesionModel;
    private final FuncionarioModel funcionarioModel;
    private final CategoriaModel categoriaModel;
    private final RecursoModel recursoModel;
    private final ReservaModel reservaModel;
    private final EstadisticasModel estadisticasModel;
    private final CalendarizacionModel calendarizacionModel;

    public AplicacionController() {
        this.modelo = cargarModelo();
        this.persistencia = new PersistenciaXml(modelo, DATA_DIR);

        // Crear services (capa interna, no expuesta a controllers)
        SesionService sesionService = new SesionService(modelo, persistencia);
        FuncionarioService funcionarioService = new FuncionarioService(modelo, persistencia, sesionService);
        CategoriaService categoriaService = new CategoriaService(modelo, persistencia, sesionService);
        RecursoService recursoService = new RecursoService(modelo, persistencia, sesionService);

        ExtractorReservaIA extractorIA;
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey != null && !apiKey.isBlank()) {
            extractorIA = new ExtractorReservaIAGemini(apiKey);
        } else {
            extractorIA = (frase, cats) -> ResultadoExtraccionIA.fallo(
                    "No se configuro GEMINI_API_KEY. La extraccion con IA no esta disponible.");
        }

        ReservaService reservaService = new ReservaService(modelo, persistencia, sesionService, extractorIA);
        ReservaConsultaAdapter reservaConsulta = new ReservaConsultaAdapter(modelo);
        EstadisticasService estadisticasService = new EstadisticasService(modelo, reservaConsulta);
        CalendarizacionService calendarizacionService = new CalendarizacionService(modelo);

        // Crear fachadas Model (lo que ven los controllers)
        this.sesionModel = new SesionModel(sesionService);
        this.funcionarioModel = new FuncionarioModel(funcionarioService);
        this.categoriaModel = new CategoriaModel(categoriaService);
        this.recursoModel = new RecursoModel(recursoService);
        this.reservaModel = new ReservaModel(reservaService);
        this.estadisticasModel = new EstadisticasModel(estadisticasService);
        this.calendarizacionModel = new CalendarizacionModel(calendarizacionService);

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
        new LoginController(loginFrame, sesionModel, this::abrirVentanaPrincipal);
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
        List<Icon> iconos = new ArrayList<>();

        if (isAdmin) {
            FuncionarioPanel funcionarioPanel = new FuncionarioPanel();
            new FuncionarioController(funcionarioPanel, funcionarioModel);
            paneles.add(funcionarioPanel);
            nombres.add("Funcionarios");
            iconos.add(Iconos.personas());

            CategoriasPanel categoriasPanel = new CategoriasPanel();
            new CategoriaController(categoriasPanel, categoriaModel);
            paneles.add(categoriasPanel);
            nombres.add("Categorias");
            iconos.add(Iconos.etiqueta());

            RecursosPanel recursosPanel = new RecursosPanel();
            new RecursoController(recursosPanel, recursoModel, categoriaModel);
            paneles.add(recursosPanel);
            nombres.add("Recursos");
            iconos.add(Iconos.recurso());
        } else {
            ReservaPanel reservaPanel = new ReservaPanel();
            new ReservaController(reservaPanel, reservaModel, categoriaModel);
            paneles.add(reservaPanel);
            nombres.add("Reservas");
            iconos.add(Iconos.calendario());
        }

        // Tabs comunes: Calendarizacion, Actividades, Estadisticas
        agregarTabsComunes(paneles, nombres, iconos);

        VentanaPrincipal ventana = new VentanaPrincipal(
                paneles.toArray(new JPanel[0]),
                nombres.toArray(new String[0]),
                iconos.toArray(new Icon[0]),
                isAdmin,
                usuario.getId()
        );

        ventana.setOnCerrarSesion(() -> {
            sesionModel.cerrarSesion();
            ventana.dispose();
            iniciar();
        });

        ventana.setOnCambiarClave(() -> {
            CambioClaveDialog dialogo = new CambioClaveDialog(ventana);
            dialogo.setOnGuardar(nueva -> {
                try {
                    sesionModel.cambiarContrasena(nueva);
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

    private void agregarTabsComunes(List<JPanel> paneles, List<String> nombres,
                                    List<Icon> iconos) {
        CalendarizacionRecursosPanel calendarizacionPanel = new CalendarizacionRecursosPanel();
        new CalendarizacionRecursosController(calendarizacionPanel, calendarizacionModel);
        paneles.add(calendarizacionPanel);
        nombres.add("Calendarizacion");
        iconos.add(Iconos.grilla());

        AgendaSemanalPanel agendaPanel = new AgendaSemanalPanel();
        new AgendaController(agendaPanel, estadisticasModel);
        paneles.add(agendaPanel);
        nombres.add("Actividades");
        iconos.add(Iconos.agenda());

        EstadisticasPanel estadisticasActPanel = new EstadisticasPanel();
        new EstadisticasController(estadisticasActPanel, estadisticasModel);

        EstadisticasRecursosPanel estadisticasRecPanel = new EstadisticasRecursosPanel();
        new EstadisticasRecursosController(estadisticasRecPanel, estadisticasModel);

        JTabbedPane tabsEstadisticas = new JTabbedPane();
        tabsEstadisticas.addTab("Actividades", Iconos.agenda(), estadisticasActPanel);
        tabsEstadisticas.addTab("Recursos", Iconos.recurso(), estadisticasRecPanel);

        JPanel panelEstadisticas = new JPanel(new java.awt.BorderLayout());
        panelEstadisticas.add(tabsEstadisticas, java.awt.BorderLayout.CENTER);
        paneles.add(panelEstadisticas);
        nombres.add("Estadisticas");
        iconos.add(Iconos.estadisticas());
    }
}
