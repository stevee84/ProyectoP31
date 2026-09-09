package service;

import consulta.InfoReserva;
import consulta.ReservaConsulta;
import exception.ValidacionException;
import model.CategoriaRecurso;
import model.ModeloReservaciones;
import model.Recurso;
import model.Reservacion;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Estadísticas y agenda semanal. Usa {@link ReservaConsulta} (desacoplado)
 * para la agenda/estadísticas de actividades, y {@link ModeloReservaciones}
 * directamente para las estadísticas de recursos.
 */
public class EstadisticasService {

    /** Primera hora del día que se muestra en la agenda semanal (inclusive). */
    public static final int HORA_INICIO = 7;
    /** Hora límite de la agenda semanal (exclusive, última franja empieza en HORA_FIN - 1). */
    public static final int HORA_FIN = 18;

    private final ModeloReservaciones modelo;
    private final ReservaConsulta reservaConsulta;

    public EstadisticasService(ModeloReservaciones modelo, ReservaConsulta reservaConsulta) {
        this.modelo = modelo;
        this.reservaConsulta = reservaConsulta;
    }

    public List<CategoriaRecurso> listarCategorias() {
        return modelo.listarCategorias();
    }

    /**
     * Arma la agenda de la semana (lunes a domingo) que contiene
     * {@code fechaEnLaSemana}, agrupada por día y por hora dentro del rango
     * {@link #HORA_INICIO}-{@link #HORA_FIN}. Una reservación que cubre más
     * de una hora aparece en cada franja que ocupa.
     */
    public Map<DayOfWeek, Map<Integer, List<InfoReserva>>> obtenerMatrizSemana(LocalDate fechaEnLaSemana) {
        LocalDate lunes = fechaEnLaSemana.with(DayOfWeek.MONDAY);
        LocalDateTime desde = lunes.atStartOfDay();
        LocalDateTime hasta = lunes.plusDays(7).atStartOfDay();

        Map<DayOfWeek, Map<Integer, List<InfoReserva>>> matriz = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek dia : DayOfWeek.values()) {
            Map<Integer, List<InfoReserva>> horas = new TreeMap<>();
            for (int hora = HORA_INICIO; hora < HORA_FIN; hora++) {
                horas.put(hora, new ArrayList<>());
            }
            matriz.put(dia, horas);
        }

        LocalDate domingo = lunes.plusDays(6);
        for (InfoReserva reserva : reservaConsulta.listarEnRango(desde, hasta)) {
            LocalDate primerDia = reserva.inicio().toLocalDate().isBefore(lunes) ? lunes : reserva.inicio().toLocalDate();
            LocalDate ultimoDia = reserva.fin().toLocalDate().isAfter(domingo) ? domingo : reserva.fin().toLocalDate();

            for (LocalDate fecha = primerDia; !fecha.isAfter(ultimoDia); fecha = fecha.plusDays(1)) {
                LocalDateTime inicioDelDia = fecha.atStartOfDay();
                LocalDateTime finDelDia = fecha.plusDays(1).atStartOfDay();
                LocalDateTime inicioEfectivo = reserva.inicio().isAfter(inicioDelDia) ? reserva.inicio() : inicioDelDia;
                LocalDateTime finEfectivo = reserva.fin().isBefore(finDelDia) ? reserva.fin() : finDelDia;

                int horaInicioReserva = inicioEfectivo.getHour();
                int horaFinReserva = finEfectivo.getMinute() == 0 ? finEfectivo.getHour() : finEfectivo.getHour() + 1;

                int desdeHora = Math.max(HORA_INICIO, horaInicioReserva);
                int hastaHora = Math.min(HORA_FIN, horaFinReserva);
                for (int hora = desdeHora; hora < hastaHora; hora++) {
                    matriz.get(fecha.getDayOfWeek()).get(hora).add(reserva);
                }
            }
        }
        return matriz;
    }

    /**
     * Cuenta cuántas reservaciones caen en cada semana (lunes a domingo,
     * mismo criterio que {@link #obtenerMatrizSemana}) dentro del rango
     * [desde, hasta]. Ambas fechas se normalizan a la semana que las
     * contiene, así que el primer y el último resultado siempre son semanas
     * completas.
     */
    public List<EstadisticaSemana> contarPorSemana(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw new ValidacionException("Debe indicar ambas fechas.");
        }
        if (desde.isAfter(hasta)) {
            throw new ValidacionException("La fecha 'desde' no puede ser posterior a 'hasta'.");
        }

        List<EstadisticaSemana> resultado = new ArrayList<>();
        LocalDate lunes = desde.with(DayOfWeek.MONDAY);
        LocalDate ultimoLunes = hasta.with(DayOfWeek.MONDAY);

        List<InfoReserva> todas = reservaConsulta.listarEnRango(lunes.atStartOfDay(), ultimoLunes.plusDays(7).atStartOfDay());

        while (!lunes.isAfter(ultimoLunes)) {
            LocalDate domingo = lunes.plusDays(6);
            LocalDateTime inicioSemana = lunes.atStartOfDay();
            LocalDateTime finSemana = lunes.plusDays(7).atStartOfDay();
            long cantidad = todas.stream()
                    .filter(r -> r.inicio().isBefore(finSemana) && inicioSemana.isBefore(r.fin()))
                    .count();
            resultado.add(new EstadisticaSemana(lunes, domingo, (int) cantidad));
            lunes = lunes.plusWeeks(1);
        }
        return resultado;
    }

    public int contarReservasCategoria(CategoriaRecurso categoria, LocalDate desde, LocalDate hasta) {
        if (categoria == null) {
            throw new ValidacionException("La categoría es obligatoria.");
        }
        if (desde == null || hasta == null) {
            throw new ValidacionException("Las fechas son obligatorias.");
        }
        if (desde.isAfter(hasta)) {
            throw new ValidacionException("La fecha desde no puede ser posterior a la fecha hasta.");
        }

        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin = hasta.plusDays(1).atStartOfDay();

        int cantidad = 0;
        for (Reservacion reservacion : modelo.listarReservacionesEnRango(inicio, fin)) {
            if (reservacion.esActiva()) {
                for (Recurso recurso : reservacion.getRecursos()) {
                    if (recurso.getCategoria().equals(categoria)) {
                        cantidad++;
                    }
                }
            }
        }
        return cantidad;
    }

    public record EstadisticaSemana(LocalDate inicioSemana, LocalDate finSemana, int cantidad) {
    }
}
