package consulta;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Contrato de solo lectura para consultar reservaciones en un rango de
 * fechas. Lo implementa {@link ReservaConsultaDatosPrueba} (datos ficticios,
 * mientras se desarrollan las secciones C y D) y, más adelante, un
 * {@code ReservaConsultaAdapter} que envuelva el {@code ControladorReservaciones}
 * real — cambio de una sola línea de ensamblado, sin tocar el controlador ni
 * las vistas de agenda/estadísticas.
 */
public interface ReservaConsulta {

    /**
     * @param desde inicio del rango, inclusive.
     * @param hasta fin del rango, exclusive.
     */
    List<InfoReserva> listarEnRango(LocalDateTime desde, LocalDateTime hasta);
}
