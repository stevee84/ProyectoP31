package servicio;

import accesodatos.ConexionCompartida;
import accesodatos.ConexionOracle;
import accesodatos.ProveedorConexion;
import contrato.IServicioAplicacion;
import ia.ExtractorReservaIA;
import ia.ExtractorReservaIAGemini;
import repositorio.FabricaRepositoriosOracle;
import java.time.Clock;

/** Usar UNA fábrica por Servidor: todos sus clientes comparten el mismo bloqueo. */
public final class FabricaServiciosOracle implements IFabricaServicios {
    private final Object bloqueo = new Object();
    private final ProveedorConexion conexiones;
    private final FabricaRepositoriosOracle repositorios;
    private final ExtractorReservaIA extractor;
    private final Clock reloj;

    public FabricaServiciosOracle() {
        this(new ConexionCompartida(ConexionOracle::abrir), new FabricaRepositoriosOracle(), new ExtractorReservaIAGemini(), Clock.systemDefaultZone());
    }

    // Dependencias sustituibles desde las pruebas del paquete, no desde el cliente de red.
    FabricaServiciosOracle(ProveedorConexion conexiones, FabricaRepositoriosOracle repositorios,
                          ExtractorReservaIA extractor, Clock reloj) {
        this.conexiones = conexiones;
        this.repositorios = repositorios;
        this.extractor = extractor;
        this.reloj = reloj;
    }

    @Override
    public IServicioAplicacion crear(ContextoSesion contexto) {
        return new ServicioAplicacion(contexto, conexiones, repositorios, extractor, reloj, bloqueo);
    }
}
