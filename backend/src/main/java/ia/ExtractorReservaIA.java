package ia;

import dto.CategoriaDTO;
import dto.ResultadoExtraccionDTO;
import java.util.List;

/** Extrae datos incompletos; nunca guarda ni confirma una reserva. */
@FunctionalInterface
public interface ExtractorReservaIA {
    ResultadoExtraccionDTO extraer(String frase, List<CategoriaDTO> categorias);
}
