package servicio;

import contrato.IServicioAplicacion;
import dto.*;
import java.time.*;
import java.util.*;

/** Construcción en memoria de las matrices, sin una consulta JDBC por celda. */
final class ConsultasReservas {
    private ConsultasReservas() { }

    static MatrizCalendarioDTO calendario(LocalDate fecha, List<RecursoDTO> recursos, List<ReservaDTO> reservas) {
        List<List<String>> celdas = new ArrayList<>();
        for (int hora = IServicioAplicacion.HORA_INICIO; hora < IServicioAplicacion.HORA_FIN; hora++) {
            LocalDateTime inicio = fecha.atTime(hora, 0);
            List<String> fila = new ArrayList<>();
            for (RecursoDTO recurso : recursos) {
                String informacion = reservas.stream()
                        .filter(r -> solapa(r, inicio, inicio.plusHours(1)))
                        .filter(r -> r.recursos().stream().anyMatch(x -> x.codigo().equals(recurso.codigo())))
                        .map(r -> r.descripcionActividad() + " - " + r.autor().nombre())
                        .reduce((a, b) -> a + "; " + b).orElse("Disponible");
                fila.add(informacion);
            }
            celdas.add(fila);
        }
        return new MatrizCalendarioDTO(fecha, recursos, celdas);
    }

    static AgendaSemanaDTO agenda(LocalDate lunes, List<ReservaDTO> reservas) {
        Map<DayOfWeek, Map<Integer, List<InfoReservaDTO>>> dias = new EnumMap<>(DayOfWeek.class);
        for (int d = 0; d < 7; d++) {
            LocalDate fecha = lunes.plusDays(d);
            Map<Integer, List<InfoReservaDTO>> horas = new TreeMap<>();
            for (int h = IServicioAplicacion.HORA_INICIO; h < IServicioAplicacion.HORA_FIN; h++) {
                LocalDateTime inicio = fecha.atTime(h, 0);
                List<InfoReservaDTO> actividades = reservas.stream()
                        .filter(r -> solapa(r, inicio, inicio.plusHours(1)))
                        .map(r -> new InfoReservaDTO(r.inicio(), r.fin(), r.descripcionActividad(), r.autor().nombre(),
                                r.recursos().stream().map(RecursoDTO::descripcion).toList())).toList();
                horas.put(h, actividades);
            }
            dias.put(fecha.getDayOfWeek(), horas);
        }
        return new AgendaSemanaDTO(lunes, dias);
    }

    static List<EstadisticaSemanaDTO> completarSemanas(LocalDate desde, LocalDate hasta,
                                                       List<EstadisticaSemanaDTO> existentes) {
        Map<LocalDate, Integer> cantidades = new HashMap<>();
        existentes.forEach(e -> cantidades.put(e.inicioSemana(), e.cantidad()));
        List<EstadisticaSemanaDTO> resultado = new ArrayList<>();
        for (LocalDate lunes = desde.with(DayOfWeek.MONDAY); !lunes.isAfter(hasta); lunes = lunes.plusWeeks(1)) {
            resultado.add(new EstadisticaSemanaDTO(lunes, lunes.plusDays(6), cantidades.getOrDefault(lunes, 0)));
        }
        return List.copyOf(resultado);
    }

    private static boolean solapa(ReservaDTO r, LocalDateTime inicio, LocalDateTime fin) {
        return r.estado() == EstadoReserva.ACTIVA && r.inicio().isBefore(fin) && inicio.isBefore(r.fin());
    }
}
