package integracion;

import contrato.IReceptorEventos;
import controller.AlertasController;
import dto.CategoriaDTO;
import model.AlertasModel;
import model.FuenteEventos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import protocolo.Peticion;
import protocolo.Respuesta;
import proxy.ProxyServicio;
import soporte.VistaAlertasFalsa;

import javax.swing.SwingUtilities;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static soporte.Datos.ANA;
import static soporte.Datos.LUIS;
import static soporte.Datos.evento;

/**
 * MVC de alertas conectado al ProxyServicio real. El servidor es un socket mínimo
 * escrito en la prueba, para decidir exactamente cuándo llega cada evento.
 */
class AlertasProxyIT {

    private ServerSocket servidor;
    private ProxyServicio proxy;
    /** Mensajes que el servidor de prueba debe enviar sin que el cliente los pida. */
    private final BlockingQueue<Object> aEnviar = new LinkedBlockingQueue<>();

    @AfterEach
    void cerrar() throws Exception {
        if (proxy != null) proxy.desconectar();
        if (servidor != null) servidor.close();
    }

    private interface Script {
        void ejecutar(ObjectInputStream entrada, ObjectOutputStream salida) throws Exception;
    }

    private void iniciarServidor(Script script) throws Exception {
        servidor = new ServerSocket(0);
        Thread hilo = new Thread(() -> {
            try (Socket socket = servidor.accept()) {
                ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream());
                salida.flush();
                ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());
                script.ejecutar(entrada, salida);
            } catch (Exception ignorada) {
                // Fin de la prueba.
            }
        });
        hilo.setDaemon(true);
        hilo.start();
        proxy = new ProxyServicio(3000);
        proxy.conectar("localhost", servidor.getLocalPort());
    }

    /** El mismo adaptador que arma MainCliente. */
    private FuenteEventos fuenteDelProxy() {
        return new FuenteEventos() {
            @Override public void agregarReceptor(IReceptorEventos r) { proxy.agregarReceptor(r); }
            @Override public void eliminarReceptor(IReceptorEventos r) { proxy.eliminarReceptor(r); }
        };
    }

    /** Espera a que el EDT procese lo pendiente y devuelve cuántas alertas hay. */
    private static int alertasEnEdt(AlertasModel modelo) throws Exception {
        int[] cantidad = new int[1];
        SwingUtilities.invokeAndWait(() -> cantidad[0] = modelo.cantidad());
        return cantidad[0];
    }

    private static void esperarAlertas(AlertasModel modelo, int esperadas) throws Exception {
        long limite = System.currentTimeMillis() + 3000;
        while (alertasEnEdt(modelo) < esperadas && System.currentTimeMillis() < limite) {
            Thread.sleep(20);
        }
    }

    @Test
    void alertaQueLlegaDuranteUnaPeticionNoMezclaLaRespuesta() throws Exception {
        iniciarServidor((entrada, salida) -> {
            Peticion peticion = (Peticion) entrada.readObject();
            salida.writeObject(evento(1, LUIS, "Reunion clientes"));
            salida.flush();
            Thread.sleep(300); // la interfaz sigue esperando la respuesta mientras llega la alerta
            salida.writeObject(Respuesta.exito(peticion.idPeticion(), List.of(new CategoriaDTO("C1", "Salas"))));
            salida.flush();
            Thread.sleep(1000);
        });
        AlertasModel modelo = new AlertasModel();
        AlertasController controlador = new AlertasController(modelo, new VistaAlertasFalsa(),
                fuenteDelProxy(), ANA.id());
        SwingUtilities.invokeAndWait(controlador::iniciar);

        CompletableFuture<List<CategoriaDTO>> respuesta = CompletableFuture.supplyAsync(proxy::listarCategorias);
        esperarAlertas(modelo, 1);

        assertEquals(1, alertasEnEdt(modelo), "La alerta aparece antes de que termine la petición");
        assertFalse(respuesta.isDone());
        assertEquals("Salas", respuesta.get(3, TimeUnit.SECONDS).get(0).descripcion());
    }

    @Test
    void cicloDeSesionConElProxyReal() throws Exception {
        CountDownLatch listo = new CountDownLatch(1);
        iniciarServidor((entrada, salida) -> {
            listo.await();
            while (true) {
                Object mensaje = aEnviar.poll(2, TimeUnit.SECONDS);
                if (mensaje == null) return;
                salida.writeObject(mensaje);
                salida.flush();
            }
        });
        AlertasModel modelo = new AlertasModel();
        AlertasController controlador = new AlertasController(modelo, new VistaAlertasFalsa(),
                fuenteDelProxy(), ANA.id());
        SwingUtilities.invokeAndWait(controlador::iniciar);
        listo.countDown();

        aEnviar.add(evento(1, ANA, "Mi propia reserva"));
        aEnviar.add(evento(2, LUIS, "Reserva de Luis"));
        esperarAlertas(modelo, 1);
        Thread.sleep(200);
        assertEquals(1, alertasEnEdt(modelo), "Solo la alerta de otro usuario");

        SwingUtilities.invokeAndWait(controlador::detener);
        aEnviar.add(evento(3, LUIS, "Despues de cerrar sesion"));
        Thread.sleep(400);

        assertEquals(0, alertasEnEdt(modelo), "Al cerrar sesión la lista queda vacía y no se agregan más");
    }
}
