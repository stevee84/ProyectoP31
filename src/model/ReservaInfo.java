package model;

import java.time.LocalDateTime;
import java.util.List;

/*
        Vista unicamente de lectura de una reserva, de donde los otros modulos accesaran a la informacion
        sin depender de la clase Reservacion completa y menos de sus operaciones de mutacion...
 */

public record ReservaInfo(
    int id,
    String idFuncionario,
    String nombreFuncionario,
    String descripcionActividad,
    LocalDateTime inicio,
    LocalDateTime fin,
    EstadoReservacion estado,
    List<RecursoInfo> recursos
) {
    public ReservaInfo {
        recursos = List.copyOf(recursos);
    }

    //  Informacion minima de un recurso asignado, para no exponer la clase Recurso completa
    public record RecursoInfo(
            String codigoRecurso, String idCategoria, String descripcionRecurso
    ) {}
}
