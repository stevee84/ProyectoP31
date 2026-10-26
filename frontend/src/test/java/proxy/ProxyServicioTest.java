package proxy;

import dto.CategoriaDTO;
import dto.EstadoReserva;
import dto.ReservaDTO;
import dto.Rol;
import dto.UsuarioDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import protocolo.CodigoError;
import protocolo.EventoReservaCreada;
import protocolo.Peticion;
import protocolo.Respuesta;
import protocolo.ServicioException;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba el Proxy contra un servidor mínimo escrito en la prueba, para controlar
 * exactamente el orden de lo que llega por el socket.
 */
class ProxyServicioTest {

    private ServerSocket servidor;
    private ProxyServicio proxy;

    @AfterEach
    void cerrar() throws Exception {
        if (proxy != null) proxy.desconectar();
        if (servidor != null) servidor.close();
    }

    /** Servidor de un solo cliente; el script decide qué responder y cuándo. */
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
    }

    private void conectarProxy(int timeoutMs) throws Exception {
        proxy = new ProxyServicio(timeoutMs);
        proxy.conectar("localhost", servidor.getLocalPort());
    }

    private interface Script {
        void ejecutar(ObjectInputStream entrada, ObjectOutputStream salida) throws Exception;
    }

    @Test
    void respuestasDesordenadasLleganASuSolicitante() throws Exception {
        iniciarServidor((entrada, salida) -> {
            Peticion primera = (Peticion) entrada.readObject();
            Peticion segunda = (Peticion) entrada.readObject();
            salida.writeObject(Respuesta.exito(segunda.idPeticion(), new CategoriaDTO("C2", "segunda")));
            salida.writeObject(Respuesta.exito(primera.idPeticion(), new CategoriaDTO("C1", "primera")));
            salida.flush();
            Thread.sleep(1000);
        });
        conectarProxy(3000);

        CompletableFuture<CategoriaDTO> a = CompletableFuture.supplyAsync(() -> proxy.buscarCategoria("C1"));
        Thread.sleep(200);
        CompletableFuture<CategoriaDTO> b = CompletableFuture.supplyAsync(() -> proxy.buscarCategoria("C2"));

        assertEquals("primera", a.get(3, TimeUnit.SECONDS).descripcion());
        assertEquals("segunda", b.get(3, TimeUnit.SECONDS).descripcion());
    }

    @Test
    void eventoIntercaladoNoRompeLaRespuesta() throws Exception {
        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        ReservaDTO reserva = new ReservaDTO(9, new UsuarioDTO("F002", "Luis", Rol.FUNCIONARIO), List.of(),
                "Reunión", inicio, inicio.plusHours(1), EstadoReserva.ACTIVA);
        iniciarServidor((entrada, salida) -> {
            Peticion peticion = (Peticion) entrada.readObject();
            salida.writeObject(new EventoReservaCreada(reserva, LocalDateTime.now()));
            salida.writeObject(Respuesta.exito(peticion.idPeticion(), List.of()));
            salida.flush();
            Thread.sleep(1000);
        });
        conectarProxy(3000);
        BlockingQueue<EventoReservaCreada> recibidos = new LinkedBlockingQueue<>();
        proxy.agregarReceptor(recibidos::add);

        assertEquals(List.of(), proxy.listarCategorias());
        assertEquals(reserva, recibidos.poll(2, TimeUnit.SECONDS).reserva());
    }

    @Test
    void errorDelServidorSeConvierteEnExcepcion() throws Exception {
        iniciarServidor((entrada, salida) -> {
            Peticion peticion = (Peticion) entrada.readObject();
            salida.writeObject(Respuesta.error(peticion.idPeticion(), CodigoError.PERMISO, "Sin permiso"));
            salida.flush();
            Thread.sleep(1000);
        });
        conectarProxy(3000);

        ServicioException e = assertThrows(ServicioException.class, () -> proxy.eliminarCategoria("C1"));
        assertEquals(CodigoError.PERMISO, e.getCodigo());
        assertEquals("Sin permiso", e.getMessage());
    }

    @Test
    void servidorQueNoRespondeProvocaTimeout() throws Exception {
        iniciarServidor((entrada, salida) -> {
            entrada.readObject();
            Thread.sleep(3000);
        });
        conectarProxy(300);

        ServicioException e = assertThrows(ServicioException.class, () -> proxy.listarCategorias());
        assertEquals(CodigoError.COMUNICACION, e.getCodigo());
    }

    @Test
    void caidaDelServidorLiberaLaSolicitudPendiente() throws Exception {
        iniciarServidor((entrada, salida) -> entrada.readObject());
        conectarProxy(5000);

        long antes = System.currentTimeMillis();
        ServicioException e = assertThrows(ServicioException.class, () -> proxy.listarCategorias());

        assertEquals(CodigoError.COMUNICACION, e.getCodigo());
        assertTrue(System.currentTimeMillis() - antes < 4000, "No debe esperar al timeout completo");
        assertFalse(proxy.estaConectado());
    }

    @Test
    void muchasPeticionesPorLaMismaConexionSuperanUnMegaSinDesconectar() throws Exception {
        // El filtro de deserialización no debe limitar los bytes acumulados de toda la sesión.
        String relleno = "x".repeat(2000);
        int total = 1500; // ~3 MB en cada sentido
        iniciarServidor((entrada, salida) -> {
            for (int i = 0; i < total; i++) {
                Peticion peticion = (Peticion) entrada.readObject();
                salida.writeObject(Respuesta.exito(peticion.idPeticion(), new CategoriaDTO("C" + i, relleno)));
                salida.reset();
                salida.flush();
            }
            Thread.sleep(1000);
        });
        conectarProxy(3000);

        for (int i = 0; i < total; i++) {
            assertEquals(relleno, proxy.buscarCategoria(relleno).descripcion(), "Petición " + i);
        }
        assertTrue(proxy.estaConectado());
    }

    @Test
    void servidorQueAceptaPeroNoEnviaNadaHaceFallarConectarATiempo() throws Exception {
        servidor = new ServerSocket(0);
        Thread hilo = new Thread(() -> {
            try (Socket mudo = servidor.accept()) {
                Thread.sleep(5000);
            } catch (Exception ignorada) {
                // Fin de la prueba.
            }
        });
        hilo.setDaemon(true);
        hilo.start();
        proxy = new ProxyServicio(500);

        long antes = System.currentTimeMillis();
        assertThrows(java.io.IOException.class, () -> proxy.conectar("localhost", servidor.getLocalPort()));

        assertTrue(System.currentTimeMillis() - antes < 3000, "conectar no debe colgarse");
        assertFalse(proxy.estaConectado());
    }
}
