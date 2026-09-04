package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/*
 * Implementación de ReservaConsulta respaldada por ModeloReservaciones.
 * Traduce Reservacion (objeto de dominio, mutable, con lógica de negocio)
 * a ReservaInfo (DTO inmutable, de solo lectura) antes de devolverlo a
 * otros módulos.
 */


public class ReservaConsultaImpl implements ReservaConsulta {
    private final ModeloReservaciones modelo;

    public ReservaConsultaImpl(ModeloReservaciones modelo) {
        this.modelo = Objects.requireNonNull(modelo, "El modelo de reservaciones es obligatorio");
    }

    @Override
    public List<ReservaInfo> buscarActivasEnFecha(LocalDate fecha) {
        return modelo.listarReservacionesEnFecha(fecha).stream()
                .filter(Reservacion::esActiva)
                .map(ReservaConsultaImpl::aInfo)
                .toList();
    }

    @Override
    public List<ReservaInfo> buscarActivasEnRango(LocalDateTime desde, LocalDateTime hasta) {
        return modelo.listarReservacionesEnRango(desde, hasta).stream()
                .filter(Reservacion::esActiva)
                .map(ReservaConsultaImpl::aInfo)
                .toList();
    }

    private static ReservaInfo aInfo(Reservacion r) {
        List<ReservaInfo.RecursoInfo> recursos = r.getRecursos().stream()
                .map(rec -> new ReservaInfo.RecursoInfo(
                        rec.getCodigo(), rec.getCategoria().getId(), rec.getDescripcion()))
                .toList();
        return new ReservaInfo(
                r.getId(),
                r.getEmpleado().getId(),
                r.getEmpleado().getName(),
                r.getDescripcionActividad(),
                r.getInicio(),
                r.getFin(),
                r.getEstado(),
                recursos
        );
    }
}
