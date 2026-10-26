package controller;

import falso.FuenteEventosSimulada;
import model.AlertasModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import soporte.VistaAlertasFalsa;

import javax.swing.SwingUtilities;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static soporte.Datos.ANA;
import static soporte.Datos.LUIS;
import static soporte.Datos.evento;

/**
 * AlertasController con una vista simulada (sin ventanas) y un "hilo de interfaz"
 * controlado por la prueba: así se comprueba que el modelo solo cambia allí.
 */
class AlertasControllerTest {

    private final Queue<Runnable> pendientesInterfaz = new ArrayDeque<>();
    private AlertasModel modelo;
    private VistaAlertasFalsa vista;
    private FuenteEventosSimulada fuente;
    private AlertasController controlador;

    @BeforeEach
    void preparar() {
        modelo = new AlertasModel();
        vista = new VistaAlertasFalsa();
        fuente = new FuenteEventosSimulada();
        controlador = new AlertasController(modelo, vista, fuente, ANA.id(), pendientesInterfaz::add);
        controlador.iniciar();
    }

    private void correrHiloInterfaz() {
        Runnable r;
        while ((r = pendientesInterfaz.poll()) != null) {
            r.run();
        }
    }

    @Test
    void iniciarRegistraElReceptorYMuestraListaVacia() {
        assertEquals(1, fuente.cantidadReceptores());
        assertTrue(vista.mostradas.isEmpty());
    }

    @Test
    void alertaDeOtroUsuarioSeAgregaEnElHiloDeInterfaz() {
        fuente.emitir(evento(1, LUIS, "Reunion clientes"));

        assertEquals(0, modelo.cantidad(), "No debe tocar el modelo desde el hilo de red");
        correrHiloInterfaz();

        assertEquals(1, modelo.cantidad());
        assertEquals("Luis Rojas - Reunion clientes", vista.mostradas.get(0).titulo());
    }

    @Test
    void elAutorNoRecibeSuPropiaAlerta() {
        fuente.emitir(evento(1, ANA, "Mi reunion"));
        correrHiloInterfaz();

        assertEquals(0, modelo.cantidad());
    }

    @Test
    void verMuestraElDetalleSinQuitarLaAlerta() {
        fuente.emitir(evento(1, LUIS, "Reunion clientes"));
        correrHiloInterfaz();

        vista.onVer.accept(0);

        assertNotNull(vista.detalle);
        assertEquals("Luis Rojas", vista.detalle.encargado());
        assertEquals(1, modelo.cantidad());
    }

    @Test
    void rechazarElDescarteMantieneLaAlerta() {
        fuente.emitir(evento(1, LUIS, "Reunion clientes"));
        correrHiloInterfaz();
        vista.respuestaConfirmacion = false;

        vista.onDescartar.accept(0);

        assertEquals(1, vista.confirmaciones);
        assertEquals(1, modelo.cantidad());
    }

    @Test
    void aceptarElDescarteQuitaSoloEsaAlerta() {
        fuente.emitir(evento(1, LUIS, "Primera"));
        fuente.emitir(evento(2, LUIS, "Segunda"));
        correrHiloInterfaz();
        vista.respuestaConfirmacion = true;

        vista.onDescartar.accept(0); // arriba está la más reciente: "Segunda"

        assertEquals(1, modelo.cantidad());
        assertEquals("Primera", modelo.get(0).actividad());
        assertEquals(1, vista.mostradas.size());
    }

    @Test
    void indicesFueraDeRangoSeIgnoran() {
        vista.onVer.accept(5);
        vista.onDescartar.accept(-1);

        assertNull(vista.detalle);
        assertEquals(0, vista.confirmaciones);
    }

    @Test
    void detenerLimpiaLaListaYDejaDeEscuchar() {
        fuente.emitir(evento(1, LUIS, "Reunion clientes"));
        correrHiloInterfaz();

        controlador.detener();
        correrHiloInterfaz();
        fuente.emitir(evento(2, LUIS, "Despues de salir"));
        correrHiloInterfaz();

        assertEquals(0, fuente.cantidadReceptores());
        assertEquals(0, modelo.cantidad());
    }

    @Test
    void alertaEnCaminoAlCerrarSesionNoAparece() {
        // El evento ya pasó el chequeo en el hilo de red, pero llega al EDT después de detener().
        fuente.emitir(evento(1, LUIS, "Tarde"));
        controlador.detener();
        correrHiloInterfaz();

        assertEquals(0, modelo.cantidad());
    }

    @Test
    void nuevaSesionEmpiezaConListaVacia() {
        fuente.emitir(evento(1, LUIS, "De la sesion anterior"));
        correrHiloInterfaz();
        controlador.detener();
        correrHiloInterfaz();

        VistaAlertasFalsa vista2 = new VistaAlertasFalsa();
        new AlertasController(modelo, vista2, fuente, LUIS.id(), pendientesInterfaz::add).iniciar();

        assertTrue(vista2.mostradas.isEmpty());
        assertEquals(1, fuente.cantidadReceptores());
    }

    @Test
    void conElConstructorNormalLosCambiosOcurrenEnElEDT() throws Exception {
        AlertasModel modeloEdt = new AlertasModel();
        AtomicBoolean enEdt = new AtomicBoolean(false);
        CountDownLatch llego = new CountDownLatch(1);
        FuenteEventosSimulada fuenteEdt = new FuenteEventosSimulada();
        AlertasController c = new AlertasController(modeloEdt, new VistaAlertasFalsa(), fuenteEdt, ANA.id());
        SwingUtilities.invokeAndWait(c::iniciar);
        modeloEdt.suscribir(alertas -> {
            if (!alertas.isEmpty()) {
                enEdt.set(SwingUtilities.isEventDispatchThread());
                llego.countDown();
            }
        });

        Thread red = new Thread(() -> fuenteEdt.emitir(evento(1, LUIS, "Desde la red")));
        red.start();

        assertTrue(llego.await(3, TimeUnit.SECONDS));
        assertTrue(enEdt.get(), "La alerta debe agregarse en el Event Dispatch Thread");
    }

    @Test
    void variasAlertasSeguidasLleganTodas() {
        List<Integer> ids = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            fuente.emitir(evento(i, LUIS, "Actividad " + i));
            ids.add(i);
        }
        correrHiloInterfaz();

        assertEquals(ids.size(), modelo.cantidad());
    }
}
