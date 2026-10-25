package servicio;

import dto.*;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ConsultasReservasTest {
    private final LocalDate dia = LocalDate.of(2030, 3, 11);
    private final RecursoDTO recurso = new RecursoDTO("R1", new CategoriaDTO("CAT-1", "Sala"), "Sala A");

    private ReservaDTO reserva(int id, LocalDateTime inicio, LocalDateTime fin, EstadoReserva estado) {
        return new ReservaDTO(id, new UsuarioDTO("F1", "Ana", Rol.FUNCIONARIO), List.of(recurso),
                "Clase " + id, inicio, fin, estado);
    }

    @Test
    void calendarioRespetaIntervalosParcialesContiguosYNoOcultaActividadesDeUnaMismaCelda() {
        var a = reserva(1, dia.atTime(9, 15), dia.atTime(9, 30), EstadoReserva.ACTIVA);
        var b = reserva(2, dia.atTime(9, 30), dia.atTime(10, 0), EstadoReserva.ACTIVA);
        var cancelada = reserva(3, dia.atTime(10, 0), dia.atTime(11, 0), EstadoReserva.CANCELADA);
        var matriz = ConsultasReservas.calendario(dia, List.of(recurso), List.of(a, b, cancelada));
        assertEquals(11, matriz.celdas().size());
        assertEquals("Disponible", matriz.celdas().get(1).get(0));
        assertEquals("Clase 1 - Ana; Clase 2 - Ana", matriz.celdas().get(2).get(0));
        assertEquals("Disponible", matriz.celdas().get(3).get(0));
    }

    @Test
    void agendaCompletaLunesADomingoYRepiteActividadEnCadaHoraQueSolapa() {
        var r = reserva(1, dia.atTime(9, 30), dia.atTime(11, 0), EstadoReserva.ACTIVA);
        var cancelada = reserva(2, dia.atTime(11, 0), dia.atTime(12, 0), EstadoReserva.CANCELADA);
        var agenda = ConsultasReservas.agenda(dia, List.of(r, cancelada));
        assertEquals(7, agenda.actividades().size());
        var lunes = agenda.actividades().get(DayOfWeek.MONDAY);
        assertEquals(11, lunes.size());
        assertEquals(1, lunes.get(9).size());
        assertEquals(lunes.get(9), lunes.get(10));
        assertTrue(lunes.get(11).isEmpty());
        assertEquals(List.of("Sala A"), lunes.get(9).get(0).recursos());
        assertEquals("Ana", lunes.get(9).get(0).nombreFuncionario());
        assertThrows(UnsupportedOperationException.class, () -> lunes.put(18, List.of()));
    }

    @Test
    void semanasCompletasConCerosInclusiveCuandoRangoEmpiezaUnDomingo() {
        var datos = List.of(new EstadisticaSemanaDTO(dia.plusWeeks(1), dia.plusWeeks(1).plusDays(6), 3));
        var resultado = ConsultasReservas.completarSemanas(dia.plusDays(6), dia.plusWeeks(2), datos);
        assertEquals(List.of(new EstadisticaSemanaDTO(dia, dia.plusDays(6), 0), datos.get(0),
                new EstadisticaSemanaDTO(dia.plusWeeks(2), dia.plusWeeks(2).plusDays(6), 0)), resultado);
        assertThrows(UnsupportedOperationException.class, () -> resultado.clear());
    }
}
