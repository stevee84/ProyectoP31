package red;

import dto.UsuarioDTO;
import protocolo.CodigoError;
import protocolo.Configuracion;
import protocolo.EventoReservaCreada;
import protocolo.Mensaje;
import protocolo.Peticion;
import protocolo.Respuesta;
import servicio.ContextoSesion;
import servicio.IFabricaServicios;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Atiende una conexión en su propio hilo: lee peticiones y responde. Las alertas
 * salen por un hilo aparte de esta misma conexión, así un cliente que no lee
 * nunca frena al autor de la reserva ni a los demás clientes.
 */
public class ManejadorCliente implements Runnable, IObservadorEventos {

    private static final int TIMEOUT_SALUDO_MS = 10_000;

    private final Socket socket;
    private final ContextoSesion contexto = new ContextoSesion();
    private final Despachador despachador;
    private final PublicadorEventos publicador;
    private final Consumer<ManejadorCliente> alTerminar;
    private final ExecutorService eventos = Executors.newSingleThreadExecutor();
    private ObjectOutputStream salida;

    public ManejadorCliente(Socket socket, IFabricaServicios fabrica, PublicadorEventos publicador,
                            Consumer<ManejadorCliente> alTerminar) {
        this.socket = socket;
        this.publicador = publicador;
        this.alTerminar = alTerminar;
        this.despachador = new Despachador(fabrica.crear(contexto), contexto, publicador);
    }

    @Override
    public void run() {
        try {
            // La salida se crea y vacía antes que la entrada: el ObjectInputStream del
            // otro extremo bloquea hasta recibir el encabezado del stream.
            salida = new ObjectOutputStream(socket.getOutputStream());
            salida.flush();
            socket.setSoTimeout(TIMEOUT_SALUDO_MS);
            ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());
            socket.setSoTimeout(0);
            entrada.setObjectInputFilter(Configuracion.FILTRO_MENSAJES);
            publicador.agregar(this);

            while (!socket.isClosed()) {
                if (!(entrada.readObject() instanceof Peticion peticion)) {
                    break;
                }
                responder(despachador.atender(peticion));
            }
        } catch (IOException | ClassNotFoundException | RuntimeException e) {
            // Cliente desconectado, mensaje ilegible o rechazado por el filtro: se cierra solo esta conexión.
        } finally {
            publicador.eliminar(this);
            cerrar();
            alTerminar.accept(this);
        }
    }

    @Override
    public String getIdUsuario() {
        UsuarioDTO usuario = contexto.getUsuario();
        return usuario == null ? null : usuario.id();
    }

    @Override
    public void actualizar(EventoReservaCreada evento) {
        eventos.execute(() -> {
            try {
                enviar(evento);
            } catch (IOException e) {
                cerrar();
            }
        });
    }

    private void responder(Respuesta respuesta) throws IOException {
        if (!esSerializable(respuesta)) {
            respuesta = Respuesta.error(respuesta.idPeticion(), CodigoError.INTERNO,
                    "El servidor produjo una respuesta inválida.");
        }
        enviar(respuesta);
    }

    /** Una respuesta que falla a medio escribir dejaría el stream inservible: se prueba antes. */
    private static boolean esSerializable(Respuesta respuesta) {
        try (ObjectOutputStream prueba = new ObjectOutputStream(OutputStream.nullOutputStream())) {
            prueba.writeObject(respuesta);
            return true;
        } catch (IOException e) {
            System.err.println("Respuesta no serializable: " + e);
            return false;
        }
    }

    private synchronized void enviar(Mensaje mensaje) throws IOException {
        salida.writeObject(mensaje);
        // reset: que el siguiente envío no reutilice una copia vieja del mismo objeto.
        salida.reset();
        salida.flush();
    }

    public void cerrar() {
        eventos.shutdownNow();
        try {
            socket.close();
        } catch (IOException ignorada) {
            // Ya estaba cerrado.
        }
    }
}
