import controller.ControladorReservaciones;
import controller.UsuariosActividadesController;
import consulta.ReservaConsultaAdapter;
import model.ModeloReservaciones;
import repository.PersistenciaXml;
import view.LoginFrame;
import view.VentanaPrincipal;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.File;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Intentar cargar datos desde XML
            String dataDir = "data";
            File dataDirFile = new File(dataDir);
            String[] archivos = dataDirFile.list();
            boolean existenDatos = dataDirFile.exists()
                    && archivos != null
                    && archivos.length > 0;

            ModeloReservaciones modelo;
            if (existenDatos) {
                modelo = new ModeloReservaciones(true); // vacío, se carga desde XML
                // BUG-02: Proteger contra XML corrupto
                try {
                    new PersistenciaXml(modelo, dataDir).cargarTodo();
                } catch (Exception e) {
                    int opcion = JOptionPane.showConfirmDialog(null,
                            "Error al cargar los datos guardados:\n" + e.getMessage()
                                    + "\n\n¿Desea iniciar con datos por defecto?",
                            "Error de carga", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
                    if (opcion == JOptionPane.YES_OPTION) {
                        modelo = new ModeloReservaciones(); // datos por defecto
                    } else {
                        System.exit(0);
                        return;
                    }
                }
            } else {
                modelo = new ModeloReservaciones(); // datos por defecto
            }

            PersistenciaXml persistencia = new PersistenciaXml(modelo, dataDir);

            // BUG-03/04: Pasar persistencia al controlador para guardar después de cada mutación
            ControladorReservaciones controlador = new ControladorReservaciones(modelo, persistencia);
            ReservaConsultaAdapter reservaConsulta = new ReservaConsultaAdapter(controlador);
            UsuariosActividadesController usuariosCtrl =
                    new UsuariosActividadesController(controlador, reservaConsulta);

            // Guardar al cerrar la aplicación (mantener como respaldo, con try-catch)
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    persistencia.guardarTodo();
                } catch (Exception e) {
                    System.err.println("Error al guardar datos en shutdown: " + e.getMessage());
                }
            }));

            LoginFrame login = new LoginFrame(usuariosCtrl, empleado -> {
                new VentanaPrincipal(controlador, usuariosCtrl, empleado).setVisible(true);
            });
            login.setVisible(true);
        });
    }
}
