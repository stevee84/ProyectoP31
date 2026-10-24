package protocolo;

import dto.ReservaDTO;

import java.time.LocalDateTime;

/**
 * Aviso asincrónico que el servidor envía a los demás usuarios conectados
 * cuando una reserva queda confirmada. emitido indica cuándo se generó la alerta.
 */
public record EventoReservaCreada(ReservaDTO reserva, LocalDateTime emitido) implements Mensaje {
}
