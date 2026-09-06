package model;

import java.util.List;

public interface ExtractorReservaIA {
    ResultadoExtraccionIA extraer(String frase, List<CategoriaRecurso> categoriasDisponibles);
}
