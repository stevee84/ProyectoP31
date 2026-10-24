package red;

import protocolo.EventoReservaCreada;

/**
 * Cliente conectado que puede recibir alertas.
 */
public interface IObservadorEventos {

    /** Usuario autenticado en esta conexión, o null si todavía no inició sesión. */
    String getIdUsuario();

    /** Debe volver de inmediato: un cliente lento no puede frenar a los demás. */
    void actualizar(EventoReservaCreada evento);
}
