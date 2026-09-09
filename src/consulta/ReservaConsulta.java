package consulta;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Contrato de solo lectura para consultar reservaciones en un rango de
 * fechas. Lo implementa {@link ReservaConsultaAdapter} que trabaja
 * directamente con el {@code ModeloReservaciones}.
 */
public interface ReservaConsulta {

    /**
     * @param desde inicio del rango, inclusive.
     * @param hasta fin del rango, exclusive.
     */
    List<InfoReserva> listarEnRango(LocalDateTime desde, LocalDateTime hasta);
}
