package consulta;

import model.ModeloReservaciones;
import model.Recurso;
import model.Reservacion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación de {@link ReservaConsulta} que obtiene datos reales del
 * {@link ModeloReservaciones}, convirtiendo cada {@link Reservacion}
 * en un {@link InfoReserva} de solo lectura.
 */
public class ReservaConsultaAdapter implements ReservaConsulta {

    private final ModeloReservaciones modelo;

    public ReservaConsultaAdapter(ModeloReservaciones modelo) {
        this.modelo = modelo;
    }

    @Override
    public List<InfoReserva> listarEnRango(LocalDateTime desde, LocalDateTime hasta) {
        return modelo.listarReservacionesEnRango(desde, hasta).stream()
                .filter(Reservacion::esActiva)
                .map(ReservaConsultaAdapter::toInfoReserva)
                .sorted((a, b) -> a.inicio().compareTo(b.inicio()))
                .collect(Collectors.toList());
    }

    private static InfoReserva toInfoReserva(Reservacion r) {
        List<String> nombresRecursos = r.getRecursos().stream()
                .map(Recurso::getDescripcion)
                .toList();
        return new InfoReserva(
                r.getInicio(),
                r.getFin(),
                r.getDescripcionActividad(),
                r.getEmpleado().getName(),
                nombresRecursos);
    }
}
