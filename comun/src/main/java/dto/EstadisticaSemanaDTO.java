package dto;

import java.io.Serializable;
import java.time.LocalDate;

public record EstadisticaSemanaDTO(LocalDate inicioSemana, LocalDate finSemana, int cantidad) implements Serializable {
}
