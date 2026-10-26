package model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Lista local de alertas de la sesión actual (Observer: la vista se suscribe).
 * Debe modificarse solo desde el hilo de Swing; AlertasController se encarga de eso.
 * Descartar una alerta solo la quita de esta lista: no se envía nada al Backend.
 */
public class AlertasModel {

    /** Se notifica después de cada cambio en la lista. */
    @FunctionalInterface
    public interface Observador {
        void alertasCambiaron(List<Alerta> alertas);
    }

    private final List<Alerta> alertas = new ArrayList<>();
    private final List<Observador> observadores = new CopyOnWriteArrayList<>();

    public void suscribir(Observador o) {
        observadores.add(o);
        o.alertasCambiaron(getAlertas());
    }

    public void desuscribir(Observador o) {
        observadores.remove(o);
    }

    /** Las más recientes quedan arriba. Ignora la misma reserva repetida. */
    public void agregar(Alerta alerta) {
        boolean repetida = alertas.stream().anyMatch(a -> a.reserva().id() == alerta.reserva().id());
        if (repetida) {
            return;
        }
        alertas.add(0, alerta);
        notificar();
    }

    public Alerta get(int indice) {
        return alertas.get(indice);
    }

    public boolean descartar(Alerta alerta) {
        boolean quitada = alertas.remove(alerta);
        if (quitada) {
            notificar();
        }
        return quitada;
    }

    /** Al cerrar sesión: la próxima sesión empieza con la lista vacía. */
    public void limpiar() {
        alertas.clear();
        notificar();
    }

    public List<Alerta> getAlertas() {
        return List.copyOf(alertas);
    }

    public int cantidad() {
        return alertas.size();
    }

    private void notificar() {
        List<Alerta> copia = getAlertas();
        observadores.forEach(o -> o.alertasCambiaron(copia));
    }
}
