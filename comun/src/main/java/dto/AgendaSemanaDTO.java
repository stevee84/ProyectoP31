package dto;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Actividades de una semana agrupadas por día y hora, listas para la agenda semanal.
 * Siempre trae los 7 días y cada hora de HORA_INICIO a HORA_FIN - 1 (lista vacía si no hay nada);
 * una reserva aparece en cada hora que ocupa. Los mapas no conservan orden: recorrer por
 * DayOfWeek y por hora.
 */
public record AgendaSemanaDTO(
        LocalDate inicioSemana,
        Map<DayOfWeek, Map<Integer, List<InfoReservaDTO>>> actividades
) implements Serializable {

    public AgendaSemanaDTO {
        // Copia profunda e inmutable: garantiza que todo lo que viaja sea serializable.
        actividades = actividades.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, dia -> dia.getValue().entrySet().stream()
                        .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, hora -> List.copyOf(hora.getValue())))));
    }
}
