package model;

import contrato.IServicioAplicacion;
import dto.AgendaSemanaDTO;
import dto.CategoriaDTO;
import dto.EstadisticaSemanaDTO;
import dto.InfoReservaDTO;
import dto.MatrizCalendarioDTO;
import dto.ResultadoReservaDTO;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static soporte.Datos.ANA;
import static soporte.Datos.SALA_A;
import static soporte.Datos.SALA_B;
import static soporte.Datos.reserva;

/** Los modelos convierten los DTO del servidor en filas para JTable, sin Swing. */
class ConversionTablasTest {

    private static final int HORAS = IServicioAplicacion.HORA_FIN - IServicioAplicacion.HORA_INICIO;

    @Test
    void reservasATabla() {
        TablaDatos tabla = ReservaModel.aTabla(List.of(reserva(5, ANA, "Reunion")));

        assertArrayEquals(ReservaModel.COLUMNAS, tabla.columnas());
        assertEquals(1, tabla.cantidadFilas());
        assertEquals(5, tabla.valor(0, 0));
        assertEquals("Reunion", tabla.valor(0, 1));
        assertEquals("08:00 - 10:00", tabla.valor(0, 3));
        assertEquals("Sala A, segundo piso, Sala B, primer piso", tabla.valor(0, 4));
        assertEquals("ACTIVA", tabla.valor(0, 5));
    }

    @Test
    void mensajeSinDisponibilidadNombraLasCategorias() {
        ResultadoReservaDTO fracaso = ResultadoReservaDTO.fracaso(List.of(
                new CategoriaDTO("C1", "Proyector"), new CategoriaDTO("C2", "Laptop")));

        assertEquals("No hay disponibilidad en: Proyector, Laptop", ReservaModel.mensajeSinDisponibilidad(fracaso));
        assertEquals("No hay recursos disponibles en ese horario.",
                ReservaModel.mensajeSinDisponibilidad(ResultadoReservaDTO.fracaso(List.of())));
    }

    @Test
    void matrizCalendarioATabla() {
        List<List<String>> celdas = new ArrayList<>();
        for (int h = 0; h < HORAS; h++) {
            celdas.add(List.of(h == 1 ? "Reunion - Ana Mora" : "Disponible", "Disponible"));
        }
        MatrizCalendarioDTO matriz = new MatrizCalendarioDTO(LocalDate.now(), List.of(SALA_A, SALA_B), celdas);

        TablaDatos tabla = CalendarizacionModel.aTabla(matriz);

        assertArrayEquals(new String[]{"Hora", "Sala A, segundo piso", "Sala B, primer piso"}, tabla.columnas());
        assertEquals(HORAS, tabla.cantidadFilas());
        assertEquals("07:00", tabla.valor(0, 0));
        assertEquals("Reunion - Ana Mora", tabla.valor(1, 1));
        assertEquals("Disponible", tabla.valor(1, 2));
    }

    @Test
    void matrizIncompletaNoRevienta() {
        MatrizCalendarioDTO matriz = new MatrizCalendarioDTO(LocalDate.now(), List.of(SALA_A), List.of());

        TablaDatos tabla = CalendarizacionModel.aTabla(matriz);

        assertEquals(HORAS, tabla.cantidadFilas());
        assertEquals("", tabla.valor(0, 1));
    }

    @Test
    void agendaATablaUbicaCadaActividadEnSuDiaYHora() {
        LocalDateTime inicio = LocalDate.of(2026, 10, 27).atTime(9, 0); // martes
        InfoReservaDTO info = new InfoReservaDTO(inicio, inicio.plusHours(1), "Reunion", "Ana Mora", List.of("Sala A"));
        AgendaSemanaDTO agenda = new AgendaSemanaDTO(LocalDate.of(2026, 10, 26),
                Map.of(DayOfWeek.TUESDAY, Map.of(9, List.of(info))));

        TablaDatos tabla = EstadisticasModel.agendaATabla(agenda);

        assertEquals(8, tabla.columnas().length);
        assertEquals("Martes", tabla.columnas()[2]);
        int filaNueve = 9 - IServicioAplicacion.HORA_INICIO;
        assertEquals("09:00", tabla.valor(filaNueve, 0));
        assertEquals("Reunion (Ana Mora)", tabla.valor(filaNueve, 2));
        assertEquals("", tabla.valor(filaNueve, 1), "El lunes queda vacío");
    }

    @Test
    void etiquetaDeSemana() {
        EstadisticaSemanaDTO s = new EstadisticaSemanaDTO(LocalDate.of(2026, 10, 26), LocalDate.of(2026, 11, 1), 3);

        assertEquals("26/10 - 01/11", EstadisticasModel.etiquetaSemana(s));
    }

    @Test
    void tablaDatosEsUnaCopiaDefensiva() {
        Object[][] filas = {{"a"}};
        TablaDatos tabla = new TablaDatos(new String[]{"Col"}, filas);

        filas[0][0] = "cambiado";

        assertEquals("a", tabla.valor(0, 0));
    }
}
