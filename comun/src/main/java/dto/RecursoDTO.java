package dto;

import java.io.Serializable;

public record RecursoDTO(String codigo, CategoriaDTO categoria, String descripcion) implements Serializable {
}
