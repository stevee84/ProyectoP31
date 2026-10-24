package contrato;

import protocolo.EventoReservaCreada;

/**
 * Observador de alertas del lado del cliente. Se invoca desde el hilo de red,
 * por eso quien actualice Swing debe pasar por SwingUtilities.invokeLater.
 */
public interface IReceptorEventos {

    void actualizar(EventoReservaCreada evento);
}
