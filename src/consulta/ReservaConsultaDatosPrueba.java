package consulta;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación de {@link ReservaConsulta} con datos ficticios en memoria,
 * usada para construir y probar la agenda semanal (sección C) y las
 * estadísticas (sección D) antes de que exista el módulo real de reservas.
 * Las fechas se calculan como offsets sobre el lunes de la semana actual
 * ({@code LocalDate.now()}) para no depender de una fecha fija; incluye
 * entradas de la semana anterior y la siguiente para poder probar el conteo
 * por semana de la sección D.
 *
 * NO es la fuente real de datos: cuando exista el módulo de reservas de
 * Integrante 3, se reemplaza por {@code ReservaConsultaAdapter} — un solo
 * cambio de ensamblado, sin tocar controlador ni vistas.
 */
public class ReservaConsultaDatosPrueba implements ReservaConsulta {

    private final List<InfoReserva> datos = new ArrayList<>();

    public ReservaConsultaDatosPrueba() {
        LocalDate lunesActual = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate lunesAnterior = lunesActual.minusWeeks(1);
        LocalDate lunesSiguiente = lunesActual.plusWeeks(1);

        agregar(lunesActual, DayOfWeek.MONDAY, 8, 9, "Reunión de coordinación", "Ana Pérez", "Sala A");
        agregar(lunesActual, DayOfWeek.MONDAY, 10, 12, "Capacitación Excel", "Luis Gómez", "Laboratorio 1");
        agregar(lunesActual, DayOfWeek.TUESDAY, 9, 10, "Mantenimiento de proyector", "Ana Pérez", "Proyector 2");
        agregar(lunesActual, DayOfWeek.WEDNESDAY, 14, 16, "Taller de Java", "Carlos Rojas", "Laboratorio 2", "Proyector 2");
        agregar(lunesActual, DayOfWeek.THURSDAY, 8, 9, "Reunión de coordinación", "Ana Pérez", "Sala A");
        agregar(lunesActual, DayOfWeek.THURSDAY, 15, 17, "Videoconferencia con proveedor", "Luis Gómez", "Sala B");
        agregar(lunesActual, DayOfWeek.FRIDAY, 11, 12, "Revisión de inventario", "Carlos Rojas", "Bodega");
        agregar(lunesAnterior, DayOfWeek.WEDNESDAY, 9, 10, "Reunión de cierre de mes", "Ana Pérez", "Sala A");
        agregar(lunesSiguiente, DayOfWeek.MONDAY, 8, 10, "Capacitación de seguridad", "Luis Gómez", "Auditorio");
        agregar(lunesSiguiente, DayOfWeek.FRIDAY, 13, 14, "Entrega de equipos", "Carlos Rojas", "Bodega");
    }

    private void agregar(LocalDate lunesDeLaSemana, DayOfWeek dia, int horaInicio, int horaFin,
                          String descripcion, String funcionario, String... recursos) {
        LocalDate fecha = lunesDeLaSemana.with(dia);
        datos.add(new InfoReserva(
                fecha.atTime(horaInicio, 0),
                fecha.atTime(horaFin, 0),
                descripcion, funcionario, List.of(recursos)));
    }

    @Override
    public List<InfoReserva> listarEnRango(LocalDateTime desde, LocalDateTime hasta) {
        return datos.stream()
                .filter(r -> r.inicio().isBefore(hasta) && desde.isBefore(r.fin()))
                .sorted((a, b) -> a.inicio().compareTo(b.inicio()))
                .toList();
    }
}
