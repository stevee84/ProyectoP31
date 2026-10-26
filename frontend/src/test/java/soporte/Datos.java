package soporte;

import dto.CategoriaDTO;
import dto.EstadoReserva;
import dto.RecursoDTO;
import dto.ReservaDTO;
import dto.Rol;
import dto.UsuarioDTO;
import protocolo.EventoReservaCreada;

import java.time.LocalDateTime;
import java.util.List;

/** Datos de ejemplo compartidos por las pruebas del Frontend. */
public final class Datos {

    public static final UsuarioDTO ANA = new UsuarioDTO("F001", "Ana Mora", Rol.FUNCIONARIO);
    public static final UsuarioDTO LUIS = new UsuarioDTO("F002", "Luis Rojas", Rol.FUNCIONARIO);
    public static final CategoriaDTO SALAS = new CategoriaDTO("CAT-000001", "Sala de reuniones");
    public static final RecursoDTO SALA_A = new RecursoDTO("SALA-01", SALAS, "Sala A, segundo piso");
    public static final RecursoDTO SALA_B = new RecursoDTO("SALA-02", SALAS, "Sala B, primer piso");

    private Datos() {
    }

    /** Reserva de mañana de 08:00 a 10:00 con las dos salas. */
    public static ReservaDTO reserva(int id, UsuarioDTO autor, String actividad) {
        LocalDateTime inicio = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0).withNano(0);
        return new ReservaDTO(id, autor, List.of(SALA_A, SALA_B), actividad, inicio, inicio.plusHours(2),
                EstadoReserva.ACTIVA);
    }

    public static EventoReservaCreada evento(int id, UsuarioDTO autor, String actividad) {
        return new EventoReservaCreada(reserva(id, autor, actividad), LocalDateTime.now());
    }
}
