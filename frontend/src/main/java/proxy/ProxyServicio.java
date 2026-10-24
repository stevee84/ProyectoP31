package proxy;

import contrato.IReceptorEventos;
import contrato.IServicioAplicacion;
import dto.AgendaSemanaDTO;
import dto.CategoriaDTO;
import dto.EstadisticaCategoriaDTO;
import dto.EstadisticaSemanaDTO;
import dto.FuncionarioDTO;
import dto.MatrizCalendarioDTO;
import dto.RecursoDTO;
import dto.ReservaDTO;
import dto.ResultadoExtraccionDTO;
import dto.ResultadoReservaDTO;
import dto.ResultadoSesionDTO;
import protocolo.CodigoError;
import protocolo.Configuracion;
import protocolo.EventoReservaCreada;
import protocolo.Operacion;
import protocolo.Peticion;
import protocolo.Respuesta;
import protocolo.ServicioException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Proxy remoto: representa localmente al servicio del Backend. Implementa el mismo
 * contrato que el servicio real, pero cada llamada viaja por el socket. Un único
 * hilo lector separa las respuestas (por idPeticion) de las alertas que llegan en
 * cualquier momento.
 * No conoce Swing; las llamadas son bloqueantes y deben hacerse fuera del EDT.
 */
public class ProxyServicio implements IServicioAplicacion {

    private final AtomicLong secuencia = new AtomicLong();
    private final Map<Long, CompletableFuture<Respuesta>> pendientes = new ConcurrentHashMap<>();
    private final List<IReceptorEventos> receptores = new CopyOnWriteArrayList<>();
    private final Object candadoEscritura = new Object();
    private final int timeoutMs;
    private volatile Socket socket;
    private volatile ObjectOutputStream salida;
    private volatile boolean conectado;

    public ProxyServicio(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public synchronized void conectar(String host, int puerto) throws IOException {
        desconectar();
        Socket nuevo = new Socket(host, puerto);
        ObjectOutputStream nuevaSalida = new ObjectOutputStream(nuevo.getOutputStream());
        nuevaSalida.flush();
        ObjectInputStream entrada = new ObjectInputStream(nuevo.getInputStream());
        entrada.setObjectInputFilter(Configuracion.FILTRO_MENSAJES);
        socket = nuevo;
        salida = nuevaSalida;
        conectado = true;

        Thread lector = new Thread(() -> leer(nuevo, entrada), "proxy-lector");
        lector.setDaemon(true);
        lector.start();
    }

    public synchronized void desconectar() {
        conectado = false;
        cerrar(socket);
    }

    public boolean estaConectado() {
        return conectado;
    }

    public void agregarReceptor(IReceptorEventos receptor) {
        receptores.add(receptor);
    }

    public void eliminarReceptor(IReceptorEventos receptor) {
        receptores.remove(receptor);
    }

    // ── Comunicación ─────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private <T> T enviar(Operacion operacion, Object... datos) {
        if (!conectado) {
            throw new ServicioException(CodigoError.COMUNICACION, "No hay conexión con el servidor.");
        }
        long id = secuencia.incrementAndGet();
        CompletableFuture<Respuesta> futura = new CompletableFuture<>();
        pendientes.put(id, futura);
        try {
            // La conexión pudo caer entre el chequeo anterior y el registro de la solicitud.
            if (!conectado) {
                throw new ServicioException(CodigoError.COMUNICACION, "No hay conexión con el servidor.");
            }
            synchronized (candadoEscritura) {
                salida.writeObject(new Peticion(id, operacion, datos));
                salida.reset();
                salida.flush();
            }
            Respuesta respuesta = futura.get(timeoutMs, TimeUnit.MILLISECONDS);
            if (!respuesta.exito()) {
                throw new ServicioException(respuesta.codigoError(), respuesta.mensaje());
            }
            return (T) respuesta.resultado();
        } catch (IOException e) {
            throw new ServicioException(CodigoError.COMUNICACION, "No se pudo enviar la solicitud al servidor.");
        } catch (TimeoutException e) {
            throw new ServicioException(CodigoError.COMUNICACION, "El servidor no respondió a tiempo.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServicioException(CodigoError.COMUNICACION, "La solicitud fue interrumpida.");
        } catch (ExecutionException e) {
            throw new ServicioException(CodigoError.INTERNO, "Error inesperado al procesar la respuesta.");
        } finally {
            pendientes.remove(id);
        }
    }

    private void leer(Socket propio, ObjectInputStream entrada) {
        try {
            while (!propio.isClosed()) {
                Object recibido = entrada.readObject();
                if (recibido instanceof Respuesta respuesta) {
                    CompletableFuture<Respuesta> futura = pendientes.get(respuesta.idPeticion());
                    if (futura != null) {
                        futura.complete(respuesta);
                    }
                } else if (recibido instanceof EventoReservaCreada evento) {
                    notificar(evento);
                }
            }
        } catch (IOException | ClassNotFoundException | RuntimeException e) {
            // Conexión cerrada por el servidor, por desconectar() o mensaje rechazado por el filtro.
        } finally {
            cerrar(propio);
            // Un lector viejo no debe invalidar una conexión nueva hecha con conectar().
            if (propio == socket) {
                conectado = false;
                pendientes.forEach((id, futura) -> futura.complete(
                        Respuesta.error(id, CodigoError.COMUNICACION, "Se perdió la conexión con el servidor.")));
            }
        }
    }

    private static void cerrar(Socket s) {
        try {
            if (s != null) {
                s.close();
            }
        } catch (IOException ignorada) {
            // Ya estaba cerrado.
        }
    }

    private void notificar(EventoReservaCreada evento) {
        for (IReceptorEventos receptor : receptores) {
            try {
                receptor.actualizar(evento);
            } catch (RuntimeException e) {
                // Un receptor con errores no debe detener la lectura del socket.
            }
        }
    }

    // ── Sesión ───────────────────────────────────────────────────────

    @Override
    public ResultadoSesionDTO iniciarSesion(String id, String clave) {
        return enviar(Operacion.INICIAR_SESION, id, clave);
    }

    @Override
    public void cerrarSesion() {
        enviar(Operacion.CERRAR_SESION);
    }

    @Override
    public void cambiarClave(String nuevaClave) {
        enviar(Operacion.CAMBIAR_CLAVE, nuevaClave);
    }

    // ── Funcionarios ─────────────────────────────────────────────────

    @Override
    public void registrarFuncionario(FuncionarioDTO funcionario) {
        enviar(Operacion.REGISTRAR_FUNCIONARIO, funcionario);
    }

    @Override
    public void actualizarFuncionario(FuncionarioDTO funcionario) {
        enviar(Operacion.ACTUALIZAR_FUNCIONARIO, funcionario);
    }

    @Override
    public void eliminarFuncionario(String id) {
        enviar(Operacion.ELIMINAR_FUNCIONARIO, id);
    }

    @Override
    public FuncionarioDTO buscarFuncionario(String id) {
        return enviar(Operacion.BUSCAR_FUNCIONARIO, id);
    }

    @Override
    public List<FuncionarioDTO> listarFuncionarios() {
        return enviar(Operacion.LISTAR_FUNCIONARIOS);
    }

    // ── Categorías ───────────────────────────────────────────────────

    @Override
    public CategoriaDTO registrarCategoria(String descripcion) {
        return enviar(Operacion.REGISTRAR_CATEGORIA, descripcion);
    }

    @Override
    public void actualizarCategoria(CategoriaDTO categoria) {
        enviar(Operacion.ACTUALIZAR_CATEGORIA, categoria);
    }

    @Override
    public void eliminarCategoria(String id) {
        enviar(Operacion.ELIMINAR_CATEGORIA, id);
    }

    @Override
    public CategoriaDTO buscarCategoria(String id) {
        return enviar(Operacion.BUSCAR_CATEGORIA, id);
    }

    @Override
    public List<CategoriaDTO> listarCategorias() {
        return enviar(Operacion.LISTAR_CATEGORIAS);
    }

    // ── Recursos ─────────────────────────────────────────────────────

    @Override
    public void registrarRecurso(String codigo, String idCategoria, String descripcion) {
        enviar(Operacion.REGISTRAR_RECURSO, codigo, idCategoria, descripcion);
    }

    @Override
    public void actualizarRecurso(String codigo, String idCategoria, String descripcion) {
        enviar(Operacion.ACTUALIZAR_RECURSO, codigo, idCategoria, descripcion);
    }

    @Override
    public void eliminarRecurso(String codigo) {
        enviar(Operacion.ELIMINAR_RECURSO, codigo);
    }

    @Override
    public RecursoDTO buscarRecurso(String codigo) {
        return enviar(Operacion.BUSCAR_RECURSO, codigo);
    }

    @Override
    public List<RecursoDTO> listarRecursos() {
        return enviar(Operacion.LISTAR_RECURSOS);
    }

    @Override
    public List<RecursoDTO> listarRecursosPorCategoria(String idCategoria) {
        return enviar(Operacion.LISTAR_RECURSOS_POR_CATEGORIA, idCategoria);
    }

    // ── Reservas ─────────────────────────────────────────────────────

    /** Sin reintento automático: si la respuesta se pierde, la reserva podría ya existir. */
    @Override
    public ResultadoReservaDTO crearReservacion(List<String> idsCategorias, String descripcionActividad,
                                                LocalDateTime inicio, LocalDateTime fin) {
        // Copia serializable; las reglas de negocio las valida el servidor.
        List<String> categorias = idsCategorias == null ? List.of() : new ArrayList<>(idsCategorias);
        return enviar(Operacion.CREAR_RESERVACION, categorias, descripcionActividad, inicio, fin);
    }

    @Override
    public void cancelarReservacion(int id) {
        enviar(Operacion.CANCELAR_RESERVACION, id);
    }

    @Override
    public List<ReservaDTO> listarReservacionesSesion() {
        return enviar(Operacion.LISTAR_RESERVACIONES_SESION);
    }

    @Override
    public ResultadoExtraccionDTO extraerDatosDesdeFrase(String frase) {
        return enviar(Operacion.EXTRAER_DATOS_IA, frase);
    }

    // ── Consultas ────────────────────────────────────────────────────

    @Override
    public MatrizCalendarioDTO obtenerCalendarizacion(LocalDate fecha, String idCategoria) {
        return enviar(Operacion.OBTENER_CALENDARIZACION, fecha, idCategoria);
    }

    @Override
    public AgendaSemanaDTO obtenerAgendaSemana(LocalDate dia) {
        return enviar(Operacion.OBTENER_AGENDA_SEMANA, dia);
    }

    @Override
    public List<EstadisticaSemanaDTO> contarPorSemana(LocalDate desde, LocalDate hasta) {
        return enviar(Operacion.CONTAR_POR_SEMANA, desde, hasta);
    }

    @Override
    public int contarReservasCategoria(String idCategoria, LocalDate desde, LocalDate hasta) {
        return this.<Integer>enviar(Operacion.CONTAR_RESERVAS_CATEGORIA, idCategoria, desde, hasta);
    }

    @Override
    public List<EstadisticaCategoriaDTO> contarPorCategoria(LocalDate desde, LocalDate hasta) {
        return enviar(Operacion.CONTAR_POR_CATEGORIA, desde, hasta);
    }
}
