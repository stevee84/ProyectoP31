package consulta;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de solo lectura para mostrar una reservación en la agenda semanal y
 * las estadísticas, sin depender directamente de {@code model.Reservacion}.
 * Esto permite construir y probar las secciones C y D con datos ficticios
 * ({@link ReservaConsultaDatosPrueba}) mientras no existe todavía el módulo
 * real de reservas, y más adelante conectar los datos reales con un
 * {@code ReservaConsultaAdapter} sin tocar el controlador ni las vistas.
 *
 * {@code recursos} es una lista (no un solo texto) para poder filtrar la
 * agenda por recurso más adelante sin cambiar el DTO otra vez.
 */
public record InfoReserva(
        LocalDateTime inicio,
        LocalDateTime fin,
        String descripcionActividad,
        String nombreFuncionario,
        List<String> recursos) {

    // Constructor compacto: copia la lista a una versión inmutable para que
    // "solo lectura" sea una garantía real y no solo una intención escrita
    // en el javadoc (protege incluso si quien construye el record pasa una
    // lista mutable, como hará más adelante un adaptador con datos reales).
    public InfoReserva {
        recursos = List.copyOf(recursos);
    }
}
