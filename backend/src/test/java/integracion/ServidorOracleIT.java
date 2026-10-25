package integracion;

import dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import protocolo.*;
import proxy.ProxyServicio;
import red.Servidor;
import servicio.FabricaServiciosOracle;
import soporte.DatosOracle;
import java.util.List;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

/** Red de Steve + Proxy original + fábrica y servicio Oracle reales. */
@EnabledIfEnvironmentVariable(named = "ORACLE_IT", matches = "(?i)true")
class ServidorOracleIT {
    @Test
    @Timeout(90)
    void clientesAutenticanReservanConsultanRecibenUnaAlertaYCancelan() throws Exception {
        try (DatosOracle d = DatosOracle.crear()) {
            Servidor servidor = new Servidor(0, new FabricaServiciosOracle());
            ProxyServicio autor = new ProxyServicio(40000);
            ProxyServicio otro = new ProxyServicio(40000);
            servidor.iniciar();
            try {
                autor.conectar("localhost", servidor.getPuerto());
                otro.conectar("localhost", servidor.getPuerto());
                assertEquals(CodigoError.SESION, assertThrows(ServicioException.class, autor::listarCategorias).getCodigo());
                assertTrue(autor.iniciarSesion(d.autor, d.autor).requiereCambioClave());
                otro.iniciarSesion(d.otro, d.otro);
                BlockingQueue<EventoReservaCreada> alertas = new LinkedBlockingQueue<>();
                BlockingQueue<EventoReservaCreada> propias = new LinkedBlockingQueue<>();
                otro.agregarReceptor(alertas::add);
                autor.agregarReceptor(propias::add);
                assertTrue(autor.listarCategorias().contains(d.categoriaA));
                var r = autor.crearReservacion(List.of(d.categoriaA.id()), "Actividad sockets Oracle", d.inicio, d.inicio.plusHours(1));
                assertTrue(r.exito());
                assertEquals(1, d.cantidadReservas());
                assertEquals(List.of(r.reserva()), autor.listarReservacionesSesion());
                var evento = alertas.poll(5, TimeUnit.SECONDS);
                assertNotNull(evento);
                assertEquals(r.reserva(), evento.reserva());
                assertNull(propias.poll(200, TimeUnit.MILLISECONDS));
                assertFalse(otro.crearReservacion(List.of(d.categoriaA.id()), "Sin recursos", d.inicio, d.inicio.plusHours(1)).exito());
                assertNull(alertas.poll(200, TimeUnit.MILLISECONDS));
                assertEquals(CodigoError.PERMISO, assertThrows(ServicioException.class, () -> otro.cancelarReservacion(r.reserva().id())).getCodigo());
                assertEquals(1, autor.contarReservasCategoria(d.categoriaA.id(), d.inicio.toLocalDate(), d.inicio.toLocalDate()));
                assertFalse(autor.obtenerAgendaSemana(d.inicio.toLocalDate()).actividades().isEmpty());
                assertEquals(11, autor.obtenerCalendarizacion(d.inicio.toLocalDate(), d.categoriaA.id()).celdas().size());
                autor.cancelarReservacion(r.reserva().id());
                assertTrue(otro.crearReservacion(List.of(d.categoriaA.id()), "Disponible otra vez", d.inicio, d.inicio.plusHours(1)).exito());
                autor.cerrarSesion();
                assertEquals(CodigoError.SESION, assertThrows(ServicioException.class, autor::listarCategorias).getCodigo());
            } finally {
                autor.desconectar();
                otro.desconectar();
                servidor.detener();
            }
        }
    }
}
