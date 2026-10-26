package soporte;

import model.Alerta;
import view.IVistaAlertas;

import java.util.List;
import java.util.function.IntConsumer;

/** Vista de alertas sin Swing: guarda lo que el controlador le pide y responde la confirmación. */
public class VistaAlertasFalsa implements IVistaAlertas {

    public volatile List<Alerta> mostradas = List.of();
    public Alerta detalle;
    public boolean respuestaConfirmacion;
    public int confirmaciones;
    public IntConsumer onVer;
    public IntConsumer onDescartar;

    @Override public void mostrarAlertas(List<Alerta> alertas) { mostradas = alertas; }
    @Override public void setOnVer(IntConsumer cb) { onVer = cb; }
    @Override public void setOnDescartar(IntConsumer cb) { onDescartar = cb; }
    @Override public void mostrarDetalle(Alerta alerta) { detalle = alerta; }

    @Override
    public boolean confirmarDescarte(Alerta alerta) {
        confirmaciones++;
        return respuestaConfirmacion;
    }
}
