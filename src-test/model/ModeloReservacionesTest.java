package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ModeloReservacionesTest {

    private ModeloReservaciones modelo;
    private Funcionario juan;
    private CategoriaRecurso salas;
    private CategoriaRecurso laptops;
    private CategoriaRecurso proyectores;

    @BeforeEach
    void setUp() {
        modelo = new ModeloReservaciones();
        juan = new Funcionario("Juan Perez", "111", "3323");
        modelo.registrarEmpleado(juan);

        // ModeloReservaciones ya trae 3 categorías por defecto en su constructor
        // (Sala para 10 personas, Laptop Windows, Sala de juntas); las reutilizamos.
        salas = modelo.listarCategorias().stream()
                .filter(c -> c.getDescripcion().equals("Sala para 10 personas"))
                .findFirst().orElseThrow();
        laptops = modelo.listarCategorias().stream()
                .filter(c -> c.getDescripcion().equals("Laptop Windows"))
                .findFirst().orElseThrow();
        proyectores = modelo.registrarCategoria("Proyector");

        // "salas" y "laptops" SÍ tienen recursos; "proyectores" NO tiene ninguno registrado,
        // así que cualquier solicitud que la incluya debe fallar por esa categoría.
        modelo.registrarRecurso("SALA-1", salas.getId(), "Sala 1 primer piso");
        modelo.registrarRecurso("LAP-1", laptops.getId(), "Laptop #238715");
    }

    @Test
    void reservaExitosaCuandoTodasLasCategoriasTienenRecursoLibre() {
        LocalDateTime inicio = LocalDateTime.of(2026, 9, 1, 9, 0);
        LocalDateTime fin = LocalDateTime.of(2026, 9, 1, 10, 0);

        SolicitudReserva solicitud = new SolicitudReserva(
                juan, List.of(salas.getId(), laptops.getId()), "Reunion de trabajo", inicio, fin);

        ResultadoReserva resultado = modelo.reservarPorCategorias(solicitud);

        assertTrue(resultado.esExito());
        assertEquals(2, resultado.getReservacion().getRecursos().size());
        assertTrue(resultado.getCategoriasNoDisponibles().isEmpty());
    }

    @Test
    void reservaFallidaReportaTodasLasCategoriasSinDisponibilidad_noSoloLaPrimera() {
        LocalDateTime inicio = LocalDateTime.of(2026, 9, 1, 9, 0);
        LocalDateTime fin = LocalDateTime.of(2026, 9, 1, 10, 0);

        // "proyectores" no tiene recursos y "salas" la vamos a dejar ocupada a propósito
        // reservándola primero. Así DOS de las tres categorías solicitadas van a fallar.
        modelo.reservarPorCategorias(new SolicitudReserva(
                juan, List.of(salas.getId()), "Reunion previa", inicio, fin));

        SolicitudReserva segundaSolicitud = new SolicitudReserva(
                juan, List.of(salas.getId(), laptops.getId(), proyectores.getId()),
                "Sesion de junta", inicio, fin);

        ResultadoReserva resultado = modelo.reservarPorCategorias(segundaSolicitud);

        assertFalse(resultado.esExito());
        assertEquals(2, resultado.getCategoriasNoDisponibles().size(),
                "Debe reportar AMBAS categorías fallidas (salas y proyectores), no detenerse en la primera");
        assertTrue(resultado.getCategoriasNoDisponibles().contains(salas));
        assertTrue(resultado.getCategoriasNoDisponibles().contains(proyectores));
        // laptops SÍ tenía disponibilidad, así que no debe aparecer como fallida
        assertFalse(resultado.getCategoriasNoDisponibles().contains(laptops));
    }

    @Test
    void reservaFallidaNoDejaNingunRecursoAsignado_todoNada() {
        LocalDateTime inicio = LocalDateTime.of(2026, 9, 1, 9, 0);
        LocalDateTime fin = LocalDateTime.of(2026, 9, 1, 10, 0);

        int reservacionesAntes = modelo.contarReservaciones();

        SolicitudReserva solicitud = new SolicitudReserva(
                juan, List.of(laptops.getId(), proyectores.getId()), "Charla tecnica", inicio, fin);

        ResultadoReserva resultado = modelo.reservarPorCategorias(solicitud);

        assertFalse(resultado.esExito());
        // La laptop SÍ estaba libre, pero como "proyectores" falló, no debe haberse
        // creado ninguna reservación -> el laptop sigue disponible para otra solicitud.
        assertEquals(reservacionesAntes, modelo.contarReservaciones());

        SolicitudReserva otraSolicitud = new SolicitudReserva(
                juan, List.of(laptops.getId()), "Otra actividad", inicio, fin);
        ResultadoReserva otroResultado = modelo.reservarPorCategorias(otraSolicitud);
        assertTrue(otroResultado.esExito(), "El laptop debía seguir libre tras el fracaso anterior");
    }

    @Test
    void getReservacionLanzaExcepcionSiElResultadoFueFracaso() {
        LocalDateTime inicio = LocalDateTime.of(2026, 9, 1, 9, 0);
        LocalDateTime fin = LocalDateTime.of(2026, 9, 1, 10, 0);

        ResultadoReserva resultado = modelo.reservarPorCategorias(new SolicitudReserva(
                juan, List.of(proyectores.getId()), "Actividad sin proyector disponible", inicio, fin));

        assertFalse(resultado.esExito());
        assertThrows(IllegalStateException.class, resultado::getReservacion);
    }
}
