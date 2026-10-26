package cliente;

import contrato.IReceptorEventos;
import controller.AplicacionController;
import model.FuenteEventos;
import protocolo.CodigoError;
import protocolo.Configuracion;
import protocolo.ServicioException;
import proxy.ProxyServicio;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.io.IOException;

/**
 * Punto de entrada del Frontend. Conecta el ProxyServicio con el Backend y arranca el MVC.
 * Host, puerto y timeout: -Dservidor.host=... -Dservidor.puerto=... -Dservidor.timeout=...
 * El Frontend no tiene driver, credenciales ni referencias a la base de datos.
 */
public final class MainCliente {

    private MainCliente() {
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Si no está disponible, Swing sigue con su tema por defecto.
        }

        Configuracion conf = Configuracion.getInstancia();
        ProxyServicio proxy = new ProxyServicio(conf.getTimeoutMs());

        FuenteEventos fuente = new FuenteEventos() {
            @Override
            public void agregarReceptor(IReceptorEventos receptor) {
                proxy.agregarReceptor(receptor);
            }

            @Override
            public void eliminarReceptor(IReceptorEventos receptor) {
                proxy.eliminarReceptor(receptor);
            }
        };

        Runnable asegurarConexion = () -> {
            if (proxy.estaConectado()) return;
            try {
                proxy.conectar(conf.getHost(), conf.getPuerto());
            } catch (IOException e) {
                throw new ServicioException(CodigoError.COMUNICACION, mensajeSinConexion(conf));
            }
        };

        // Se conecta al iniciar, fuera del hilo de Swing. Si falla, el login lo reintenta.
        String errorInicial = null;
        try {
            asegurarConexion.run();
        } catch (ServicioException e) {
            errorInicial = e.getMessage();
        }

        Runnable salir = () -> {
            proxy.desconectar();
            System.exit(0);
        };

        String aviso = errorInicial;
        SwingUtilities.invokeLater(() -> {
            new AplicacionController(proxy, fuente, asegurarConexion::run, salir).iniciar();
            if (aviso != null) {
                JOptionPane.showMessageDialog(null,
                        aviso + "\nSe intentara de nuevo al iniciar sesion.",
                        "Sin conexion", JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    private static String mensajeSinConexion(Configuracion conf) {
        return "No se pudo conectar con el servidor en " + conf.getHost() + ":" + conf.getPuerto() + ".";
    }
}
