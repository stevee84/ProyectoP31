package falso;

import contrato.IServicioAplicacion;
import servicio.ContextoSesion;
import servicio.IFabricaServicios;

/**
 * Crea servicios en memoria que comparten los mismos datos, para probar
 * el servidor y el Proxy sin Oracle.
 */
public class FabricaServiciosFalsa implements IFabricaServicios {

    final DatosFalsos datos = new DatosFalsos();

    @Override
    public IServicioAplicacion crear(ContextoSesion contexto) {
        return new ServicioAplicacionFalso(datos, contexto);
    }
}
