package repositorio;

import dto.RecursoDTO;
import dto.ReservaDTO;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public interface IRepositorioReservas {
    int insertar(String autor, String descripcion, LocalDateTime inicio, LocalDateTime fin) throws SQLException;
    void asignar(int reserva, String recurso) throws SQLException;
    void cancelar(int reserva, String autor) throws SQLException;
    ReservaDTO buscar(int id) throws SQLException;
    List<ReservaDTO> listarUsuario(String autor) throws SQLException;
    List<ReservaDTO> listarEnRango(LocalDateTime desde, LocalDateTime hasta) throws SQLException;
    List<RecursoDTO> recursos(int reserva) throws SQLException;
}
