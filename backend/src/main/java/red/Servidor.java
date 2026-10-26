package red;

import servicio.IFabricaServicios;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Servidor concurrente: acepta clientes y atiende cada uno en su propio hilo.
 */
public class Servidor {

    private static final long PAUSA_REINTENTO_MS = 200;

    private final int puerto;
    private final IFabricaServicios fabrica;
    private final PublicadorEventos publicador = new PublicadorEventos();
    private final List<ManejadorCliente> clientes = new CopyOnWriteArrayList<>();
    private final ExecutorService hilos = Executors.newCachedThreadPool();
    private ServerSocket serverSocket;

    public Servidor(int puerto, IFabricaServicios fabrica) {
        this.puerto = puerto;
        this.fabrica = fabrica;
    }

    public void iniciar() throws IOException {
        serverSocket = new ServerSocket(puerto);
        hilos.execute(this::aceptarClientes);
    }

    private void aceptarClientes() {
        while (!serverSocket.isClosed()) {
            try {
                Socket socket = serverSocket.accept();
                ManejadorCliente cliente = new ManejadorCliente(socket, fabrica, publicador, clientes::remove);
                clientes.add(cliente);
                hilos.execute(cliente);
            } catch (IOException e) {
                if (serverSocket.isClosed()) {
                    return; // accept se interrumpe al detener el servidor.
                }
                System.err.println("Servidor: error al aceptar un cliente: " + e);
                try {
                    Thread.sleep(PAUSA_REINTENTO_MS);
                } catch (InterruptedException interrumpido) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    /** Puerto real en uso; útil cuando se inicia con 0 para tomar uno libre. */
    public int getPuerto() {
        return serverSocket.getLocalPort();
    }

    public void detener() {
        try {
            serverSocket.close();
        } catch (IOException ignorada) {
            // Ya estaba cerrado.
        }
        clientes.forEach(ManejadorCliente::cerrar);
        hilos.shutdownNow();
    }
}
