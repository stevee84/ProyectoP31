package red;

import falso.FabricaServiciosFalsa;
import protocolo.Configuracion;

import java.io.IOException;

public class MainServidor {

    public static void main(String[] args) throws IOException {
        int puerto = Configuracion.getInstancia().getPuerto();
        // Se reemplaza por la fábrica Oracle del Integrante 2 al integrar.
        Servidor servidor = new Servidor(puerto, new FabricaServiciosFalsa());
        servidor.iniciar();
        Runtime.getRuntime().addShutdownHook(new Thread(servidor::detener));
        System.out.println("Servidor de reservas escuchando en el puerto " + servidor.getPuerto());
    }
}
