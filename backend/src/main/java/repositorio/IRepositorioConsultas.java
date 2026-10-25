package repositorio;

import dto.EstadisticaCategoriaDTO;
import dto.EstadisticaSemanaDTO;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface IRepositorioConsultas {
    List<EstadisticaCategoriaDTO> contarPorCategoria(LocalDate desde, LocalDate hasta) throws SQLException;
    List<EstadisticaSemanaDTO> contarPorSemana(LocalDate desde, LocalDate hasta) throws SQLException;
}
