package falso;

import contrato.IReceptorEventos;
import dto.EstadoReserva;
import dto.RecursoDTO;
import dto.ReservaDTO;
import dto.UsuarioDTO;
import model.FuenteEventos;
import protocolo.EventoReservaCreada;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simula las alertas que en producción entrega el ProxyServicio. Igual que el Proxy,
 * avisa a los receptores desde un hilo que NO es el de Swing, así se prueba que el
 * MVC de alertas pasa los cambios al EDT.
 *
 * <p>emitir() envía una alerta puntual (pruebas); iniciar() genera una alerta de
 * otro usuario cada cierto tiempo (demo visual mientras se cambia de pestaña).
 */
public class FuenteEventosSimulada implements FuenteEventos {

    private static final String[] ACTIVIDADES = {
            "Reunion clientes", "Capacitacion", "Cafe de cumpleanos", "Coordinacion viaje", "Revision de sprint"
    };

    private final List<IReceptorEventos> receptores = new CopyOnWriteArrayList<>();
    private final AtomicInteger siguienteId = new AtomicInteger(1000);
    private final Random azar = new Random();
    private ScheduledExecutorService temporizador;

    @Override
    public void agregarReceptor(IReceptorEventos receptor) {
        receptores.add(receptor);
    }

    @Override
    public void eliminarReceptor(IReceptorEventos receptor) {
        receptores.remove(receptor);
    }

    public int cantidadReceptores() {
        return receptores.size();
    }

    /** Entrega el evento a todos los receptores en el hilo que llama. */
    public void emitir(EventoReservaCreada evento) {
        for (IReceptorEventos receptor : receptores) {
            try {
                receptor.actualizar(evento);
            } catch (RuntimeException e) {
                // Igual que el Proxy: un receptor con errores no detiene a los demás.
            }
        }
    }

    public void emitir(ReservaDTO reserva) {
        emitir(new EventoReservaCreada(reserva, LocalDateTime.now()));
    }

    /**
     * Cada periodoSegundos emite una reserva de un autor al azar distinto de idExcluido
     * (el usuario de la sesión, que nunca recibe sus propias alertas).
     */
    public synchronized void iniciar(int periodoSegundos, List<UsuarioDTO> autores, List<RecursoDTO> recursos,
                                     String idExcluido) {
        detener();
        List<UsuarioDTO> otros = autores.stream()
                .filter(u -> !u.esAdministrador() && !u.id().equals(idExcluido))
                .toList();
        if (otros.isEmpty() || recursos.isEmpty()) {
            return;
        }
        temporizador = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread hilo = new Thread(r, "alertas-simuladas");
            hilo.setDaemon(true);
            return hilo;
        });
        temporizador.scheduleAtFixedRate(() -> emitir(reservaAlAzar(otros, recursos)),
                periodoSegundos, periodoSegundos, TimeUnit.SECONDS);
    }

    public synchronized void detener() {
        if (temporizador != null) {
            temporizador.shutdownNow();
            temporizador = null;
        }
    }

    private ReservaDTO reservaAlAzar(List<UsuarioDTO> autores, List<RecursoDTO> recursos) {
        UsuarioDTO autor = autores.get(azar.nextInt(autores.size()));
        RecursoDTO recurso = recursos.get(azar.nextInt(recursos.size()));
        LocalDate dia = LocalDate.now().plusDays(1 + azar.nextInt(5));
        int hora = 8 + azar.nextInt(8);
        return new ReservaDTO(siguienteId.getAndIncrement(), autor, List.of(recurso),
                ACTIVIDADES[azar.nextInt(ACTIVIDADES.length)],
                dia.atTime(hora, 0), dia.atTime(hora + 1, 0), EstadoReserva.ACTIVA);
    }
}
