package model;

import contrato.IReceptorEventos;

/**
 * Origen de las alertas asincrónicas. En ejecución es el ProxyServicio; en las
 * pruebas, una fuente simulada. Así el MVC de alertas no depende del socket.
 */
public interface FuenteEventos {

    void agregarReceptor(IReceptorEventos receptor);

    void eliminarReceptor(IReceptorEventos receptor);
}
