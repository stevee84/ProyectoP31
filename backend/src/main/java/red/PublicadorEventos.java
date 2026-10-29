package red;

import protocolo.EventoReservaCreada;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sujeto del patrón Observer: difunde cada reserva confirmada a los demás
 * usuarios autenticados. Un observador con fallas se retira sin afectar a los otros.
 */
public class PublicadorEventos {

    private static final Logger LOG = Logger.getLogger(PublicadorEventos.class.getName());

    private final List<IObservadorEventos> observadores = new CopyOnWriteArrayList<>();

    public void agregar(IObservadorEventos observador) {
        observadores.add(observador);
    }

    public void eliminar(IObservadorEventos observador) {
        observadores.remove(observador);
    }

    public void notificar(EventoReservaCreada evento, String idAutor) {
        for (IObservadorEventos observador : observadores) {
            String idUsuario = observador.getIdUsuario();
            if (idUsuario == null || idUsuario.equals(idAutor)) {
                continue;
            }
            try {
                observador.actualizar(evento);
            } catch (RuntimeException e) {
                LOG.log(Level.WARNING, "Se retira el observador del usuario " + idUsuario + " porque falló al notificar", e);
                eliminar(observador);
            }
        }
    }
}
