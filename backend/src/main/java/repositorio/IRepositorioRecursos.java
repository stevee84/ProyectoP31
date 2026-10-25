package repositorio;

import dto.RecursoDTO;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public interface IRepositorioRecursos {
    void insertar(String codigo, String categoria, String descripcion) throws SQLException;
    void modificar(String codigo, String categoria, String descripcion) throws SQLException;
    void eliminar(String codigo) throws SQLException;
    RecursoDTO buscar(String codigo) throws SQLException;
    List<RecursoDTO> listar() throws SQLException;
    List<RecursoDTO> listarPorCategoria(String categoria) throws SQLException;
    List<RecursoDTO> disponibles(String categoria, LocalDateTime inicio, LocalDateTime fin) throws SQLException;
}
