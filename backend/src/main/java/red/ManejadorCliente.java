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
import java.net.SocketTimeoutException;
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
    /** Una conexión sin sesión no puede ocupar un hilo para siempre (0 = sin límite). */
    private final int timeoutSinSesionMs;
    private ObjectOutputStream salida;

    public ManejadorCliente(Socket socket, IFabricaServicios fabrica, PublicadorEventos publicador,
                            Consumer<ManejadorCliente> alTerminar, int timeoutSinSesionMs) {
        this.timeoutSinSesionMs = timeoutSinSesionMs;
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
            entrada.setObjectInputFilter(Configuracion.FILTRO_MENSAJES);
            publicador.agregar(this);

            // Plazo total sin sesión: no se reinicia con cada petición, para que un cliente
            // que nunca inicia sesión no ocupe un cupo indefinidamente.
            long limiteSinSesion = 0;
            while (!socket.isClosed()) {
                if (contexto.estaAutenticado() || timeoutSinSesionMs == 0) {
                    limiteSinSesion = 0;
                    socket.setSoTimeout(0);
                } else {
                    long ahora = System.currentTimeMillis();
                    if (limiteSinSesion == 0) {
                        limiteSinSesion = ahora + timeoutSinSesionMs;
                    }
                    if (ahora >= limiteSinSesion) {
                        throw new SocketTimeoutException();
                    }
                    socket.setSoTimeout((int) (limiteSinSesion - ahora));
                }
                if (!(entrada.readObject() instanceof Peticion peticion)) {
                    break;
                }
                responder(despachador.atender(peticion));
            }
        } catch (SocketTimeoutException e) {
            System.err.println("Servidor: se cerró una conexión que no inició sesión en "
                    + timeoutSinSesionMs + " ms.");
            avisarCierre("Se cerró la conexión por inactividad: no se inició sesión a tiempo.");
        } catch (IOException | ClassNotFoundException | RuntimeException e) {
            // Cliente desconectado, mensaje ilegible o rechazado por el filtro: se cierra solo esta conexión.
            if (!(e instanceof java.io.EOFException) && !socket.isClosed()) {
                System.err.println("Servidor: se cerró la conexión de un cliente por un error: " + e);
            }
        } finally {
            publicador.eliminar(this);
            cerrar();
            alTerminar.accept(this);
        }
    }

    @Override
    public String getIdUsuario() {
        // Con el primer ingreso pendiente tampoco recibe alertas.
        UsuarioDTO usuario = contexto.getUsuario();
        return usuario == null || contexto.requiereCambioClave() ? null : usuario.id();
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

    /** Respuesta de error con id 0: el Proxy la usa como motivo del cierre. */
    private void avisarCierre(String mensaje) {
        try {
            enviar(Respuesta.error(0, CodigoError.COMUNICACION, mensaje));
        } catch (IOException ignorada) {
            // El cliente ya no está.
        }
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
