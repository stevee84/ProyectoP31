package controller;

import contrato.IReceptorEventos;
import dto.ReservaDTO;
import model.Alerta;
import model.AlertasModel;
import model.FuenteEventos;
import protocolo.EventoReservaCreada;
import view.IVistaAlertas;

import javax.swing.SwingUtilities;
import java.time.LocalDateTime;
import java.util.concurrent.Executor;

/**
 * Recibe las alertas asincrónicas del Backend y las muestra en el área de alertas.
 *
 * <p>actualizar() llega desde el hilo lector del Proxy, nunca desde Swing; por eso
 * todo cambio del modelo se pasa al hilo de Swing con invokeLater. Ver y descartar
 * son acciones locales: descartar solo quita la alerta de la lista.
 */
public class AlertasController implements IReceptorEventos {

    private final AlertasModel modelo;
    private final IVistaAlertas vista;
    private final FuenteEventos fuente;
    private final String idUsuarioSesion;
    private final Executor hiloInterfaz;
    private final AlertasModel.Observador observador;
    /** Evita que una alerta que ya venía en camino aparezca después de cerrar sesión. */
    private volatile boolean activo;

    public AlertasController(AlertasModel modelo, IVistaAlertas vista, FuenteEventos fuente, String idUsuarioSesion) {
        this(modelo, vista, fuente, idUsuarioSesion, SwingUtilities::invokeLater);
    }

    /** hiloInterfaz se reemplaza en las pruebas para ejecutar de forma directa. */
    public AlertasController(AlertasModel modelo, IVistaAlertas vista, FuenteEventos fuente,
                             String idUsuarioSesion, Executor hiloInterfaz) {
        this.modelo = modelo;
        this.vista = vista;
        this.fuente = fuente;
        this.idUsuarioSesion = idUsuarioSesion;
        this.hiloInterfaz = hiloInterfaz;
        this.observador = vista::mostrarAlertas;

        vista.setOnVer(this::ver);
        vista.setOnDescartar(this::descartar);
    }

    /** Empieza a escuchar alertas; se llama al abrir la ventana principal. */
    public void iniciar() {
        activo = true;
        modelo.suscribir(observador);
        fuente.agregarReceptor(this);
    }

    /** Al cerrar sesión: deja de escuchar y vacía la lista para la próxima sesión. */
    public void detener() {
        activo = false;
        fuente.eliminarReceptor(this);
        hiloInterfaz.execute(() -> {
            modelo.limpiar();
            modelo.desuscribir(observador);
        });
    }

    @Override
    public void actualizar(EventoReservaCreada evento) {
        if (!activo || evento == null || evento.reserva() == null) {
            return;
        }
        ReservaDTO reserva = evento.reserva();
        // El servidor ya excluye al autor; se revisa también aquí por si el mismo
        // usuario tiene otra ventana abierta.
        if (reserva.autor() != null && reserva.autor().id().equals(idUsuarioSesion)) {
            return;
        }
        LocalDateTime recibida = evento.emitido() != null ? evento.emitido() : LocalDateTime.now();
        hiloInterfaz.execute(() -> {
            if (activo) modelo.agregar(new Alerta(reserva, recibida));
        });
    }

    private void ver(int indice) {
        if (indice < 0 || indice >= modelo.cantidad()) return;
        vista.mostrarDetalle(modelo.get(indice));
    }

    private void descartar(int indice) {
        if (indice < 0 || indice >= modelo.cantidad()) return;
        Alerta alerta = modelo.get(indice);
        if (vista.confirmarDescarte(alerta)) {
            modelo.descartar(alerta);
        }
    }
}
