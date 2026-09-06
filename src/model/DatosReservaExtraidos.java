package model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Datos que el extractor de IA logró identificar en la frase del usuario.

 * A diferencia de {@link SolicitudReserva}, ACÁ los campos pueden venir nulos o
 * incompletos a propósito: la IA puede entender la actividad pero no la hora, por
 * ejemplo. Por eso este objeto no valida nada al construirse — solo empaqueta lo
 * que se pudo extraer, para que el funcionario complete el resto a mano en el
 * formulario.
 */

public record DatosReservaExtraidos(
        String descripcionActividad,  // null si no se detectó
        LocalDate fecha,              // null si no se detectó
        LocalTime horaInicio,         // null si no se detectó
        LocalTime horaFin,            // null si no se detectó
        List<String> idsCategorias    // nunca null; vacía si no se detectó ninguna
) {
    public DatosReservaExtraidos {
        idsCategorias = idsCategorias == null ? List.of() : List.copyOf(idsCategorias);
    }
}
