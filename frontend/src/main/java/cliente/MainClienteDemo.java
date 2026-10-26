package cliente;

import controller.AplicacionController;
import dto.ResultadoSesionDTO;
import falso.FuenteEventosSimulada;
import falso.ServicioClienteFalso;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Arranca el Frontend completo SIN servidor, con ServicioClienteFalso y alertas
 * simuladas. Sirve para revisar visualmente las pantallas de ambos roles y el área
 * de alertas mientras se cambia de pestaña. No reemplaza a MainCliente.
 *
 * <p>Usuarios (clave = id): ADMIN, F001, F002, F003.
 * Periodo de alertas en segundos: -Ddemo.alertas=8 (por defecto).
 */
public final class MainClienteDemo {

    private MainClienteDemo() {
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Si no está disponible, Swing sigue con su tema por defecto.
        }

        int periodo = Integer.getInteger("demo.alertas", 8);
        FuenteEventosSimulada fuente = new FuenteEventosSimulada();

        // Las alertas simuladas empiezan al iniciar sesión y excluyen al usuario de la sesión,
        // igual que el servidor real; al cerrar sesión se detienen.
        ServicioClienteFalso servicio = new ServicioClienteFalso() {
            @Override
            public synchronized ResultadoSesionDTO iniciarSesion(String id, String clave) {
                ResultadoSesionDTO resultado = super.iniciarSesion(id, clave);
                fuente.iniciar(periodo, usuariosDeEjemplo(), recursosDeEjemplo(), resultado.usuario().id());
                return resultado;
            }

            @Override
            public synchronized void cerrarSesion() {
                fuente.detener();
                super.cerrarSesion();
            }
        };

        Runnable salir = () -> {
            fuente.detener();
            System.exit(0);
        };

        SwingUtilities.invokeLater(() -> new AplicacionController(servicio, fuente, () -> { }, salir).iniciar());
    }
}
