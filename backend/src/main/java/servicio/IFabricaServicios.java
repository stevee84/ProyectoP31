package servicio;

import contrato.IServicioAplicacion;

/**
 * Abstract Factory: el servidor pide un servicio por cada conexión sin saber
 * si detrás hay datos en memoria (pruebas) u Oracle (Integrante 2).
 * El servicio creado recibe el contexto de su conexión y solo lo lee.
 */
public interface IFabricaServicios {

    IServicioAplicacion crear(ContextoSesion contexto);
}
