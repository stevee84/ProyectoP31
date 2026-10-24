package dto;

import java.io.Serializable;

public record EstadisticaCategoriaDTO(CategoriaDTO categoria, int cantidad) implements Serializable {
}
