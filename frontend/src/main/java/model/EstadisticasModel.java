package model;

import contrato.IServicioAplicacion;
import dto.AgendaSemanaDTO;
import dto.EstadisticaCategoriaDTO;
import dto.EstadisticaSemanaDTO;
import dto.InfoReservaDTO;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Agenda semanal y estadísticas: consultas agregadas que ya calcula el Backend. */
public class EstadisticasModel {

    private static final String[] DIAS = {"Lunes", "Martes", "Miercoles", "Jueves", "Viernes", "Sabado", "Domingo"};
    private static final DateTimeFormatter DIA_MES = DateTimeFormatter.ofPattern("dd/MM");

    private final IServicioAplicacion servicio;

    public EstadisticasModel(IServicioAplicacion servicio) {
        this.servicio = servicio;
    }

    public AgendaSemanaDTO obtenerAgenda(LocalDate dia) {
        return servicio.obtenerAgendaSemana(dia);
    }

    public List<EstadisticaSemanaDTO> contarPorSemana(LocalDate desde, LocalDate hasta) {
        return servicio.contarPorSemana(desde, hasta);
    }

    public List<EstadisticaCategoriaDTO> contarPorCategoria(LocalDate desde, LocalDate hasta) {
        return servicio.contarPorCategoria(desde, hasta);
    }

    // ── Conversión para la vista ─────────────────────────────────────

    /** Filas = horas de HORA_INICIO a HORA_FIN - 1; columnas = Hora + lunes a domingo. */
    public static TablaDatos agendaATabla(AgendaSemanaDTO agenda) {
        String[] columnas = new String[DIAS.length + 1];
        columnas[0] = "Hora";
        System.arraycopy(DIAS, 0, columnas, 1, DIAS.length);

        int horas = IServicioAplicacion.HORA_FIN - IServicioAplicacion.HORA_INICIO;
        Object[][] filas = new Object[horas][columnas.length];
        for (int h = 0; h < horas; h++) {
            int hora = IServicioAplicacion.HORA_INICIO + h;
            filas[h][0] = String.format("%02d:00", hora);
            for (int d = 0; d < DIAS.length; d++) {
                Map<Integer, List<InfoReservaDTO>> porHora =
                        agenda.actividades().getOrDefault(DayOfWeek.of(d + 1), Map.of());
                List<InfoReservaDTO> lista = porHora.getOrDefault(hora, List.of());
                filas[h][d + 1] = lista.stream()
                        .map(r -> r.descripcionActividad() + " (" + r.nombreFuncionario() + ")")
                        .collect(Collectors.joining(", "));
            }
        }
        return new TablaDatos(columnas, filas);
    }

    public static String etiquetaSemana(EstadisticaSemanaDTO s) {
        return DIA_MES.format(s.inicioSemana()) + " - " + DIA_MES.format(s.finSemana());
    }
}
