import controller.ControladorReservaciones;
import controller.UsuariosActividadesController;
import consulta.ReservaConsultaAdapter;
import model.ModeloReservaciones;
import repository.PersistenciaXml;
import view.LoginFrame;
import view.VentanaPrincipal;

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
                new PersistenciaXml(modelo, dataDir).cargarTodo();
            } else {
                modelo = new ModeloReservaciones(); // datos por defecto
            }

            PersistenciaXml persistencia = new PersistenciaXml(modelo, dataDir);

            ControladorReservaciones controlador = new ControladorReservaciones(modelo);
            ReservaConsultaAdapter reservaConsulta = new ReservaConsultaAdapter(controlador);
            UsuariosActividadesController usuariosCtrl =
                    new UsuariosActividadesController(controlador, reservaConsulta);

            // Guardar al cerrar la aplicación
            Runtime.getRuntime().addShutdownHook(new Thread(persistencia::guardarTodo));

            LoginFrame login = new LoginFrame(usuariosCtrl, empleado -> {
                new VentanaPrincipal(controlador, usuariosCtrl, empleado).setVisible(true);
            });
            login.setVisible(true);
        });
    }
}
