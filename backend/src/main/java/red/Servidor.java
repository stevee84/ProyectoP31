package red;

import protocolo.CodigoError;
import protocolo.Respuesta;
import servicio.IFabricaServicios;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Servidor concurrente: acepta clientes y atiende cada uno en su propio hilo.
 * Atiende como máximo {@code -Dservidor.maxClientes} conexiones a la vez (100 por
 * defecto); las demás reciben un mensaje claro y se cierran. Una conexión que no
 * inicia sesión se cierra tras {@code -Dservidor.timeoutSinSesion} ms (60 s por defecto).
 */
public class Servidor {

    private static final long PAUSA_REINTENTO_MS = 200;
    private static final int TIMEOUT_RECHAZO_MS = 200;
    static final String MENSAJE_LLENO =
            "El servidor alcanzó el máximo de clientes conectados. Intente de nuevo más tarde.";

    private final int puerto;
    private final IFabricaServicios fabrica;
    private final PublicadorEventos publicador = new PublicadorEventos();
    private final List<ManejadorCliente> clientes = new CopyOnWriteArrayList<>();
    private final int maxClientes;
    private final int timeoutSinSesionMs;
    // Un hilo por cliente más el que acepta conexiones: nunca crece sin límite.
    private final ExecutorService hilos;
    private ServerSocket serverSocket;

    public Servidor(int puerto, IFabricaServicios fabrica) {
        this(puerto, fabrica, Integer.getInteger("servidor.maxClientes", 100),
                Integer.getInteger("servidor.timeoutSinSesion", 60_000));
    }

    public Servidor(int puerto, IFabricaServicios fabrica, int maxClientes, int timeoutSinSesionMs) {
        if (maxClientes < 1 || timeoutSinSesionMs < 0) {
            throw new IllegalArgumentException("maxClientes debe ser >= 1 y timeoutSinSesion >= 0.");
        }
        this.puerto = puerto;
        this.fabrica = fabrica;
        this.maxClientes = maxClientes;
        this.timeoutSinSesionMs = timeoutSinSesionMs;
        this.hilos = Executors.newFixedThreadPool(maxClientes + 1);
    }

    public void iniciar() throws IOException {
        serverSocket = new ServerSocket(puerto);
        hilos.execute(this::aceptarClientes);
    }

    private void aceptarClientes() {
        while (!serverSocket.isClosed()) {
            try {
                Socket socket = serverSocket.accept();
                if (clientes.size() >= maxClientes) {
                    rechazar(socket);
                    continue;
                }
                ManejadorCliente cliente = new ManejadorCliente(socket, fabrica, publicador, clientes::remove,
                        timeoutSinSesionMs);
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

    /** Avisa al cliente sobrante con una respuesta de error (id 0) y cierra sin crear un hilo. */
    private static void rechazar(Socket socket) {
        System.err.println("Servidor: conexión rechazada, se alcanzó el máximo de clientes.");
        try (socket) {
            socket.setSoTimeout(TIMEOUT_RECHAZO_MS);
            ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream());
            salida.writeObject(Respuesta.error(0, CodigoError.COMUNICACION, MENSAJE_LLENO));
            salida.flush();
            socket.shutdownOutput();
            // Esperar a que el cliente cierre: cerrar con datos sin leer descartaría el aviso.
            InputStream entrada = socket.getInputStream();
            byte[] descarte = new byte[512];
            // Plazo total: un cliente que envía bytes de a poco no retiene al hilo aceptador.
            long limite = System.nanoTime() + TIMEOUT_RECHAZO_MS * 1_000_000L;
            while (System.nanoTime() < limite && entrada.read(descarte) >= 0) {
                // Se descarta lo que envíe el cliente.
            }
        } catch (IOException ignorada) {
            // Cliente ya desconectado o sin respuesta: la conexión queda cerrada igual.
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
