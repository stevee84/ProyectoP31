package repositorio;

import dto.FuncionarioDTO;
import dto.ResultadoSesionDTO;
import java.sql.SQLException;
import java.util.List;

public interface IRepositorioFuncionarios {
    void insertar(FuncionarioDTO funcionario) throws SQLException;
    void modificar(FuncionarioDTO funcionario) throws SQLException;
    void eliminar(String id) throws SQLException;
    FuncionarioDTO buscar(String id) throws SQLException;
    List<FuncionarioDTO> listar() throws SQLException;
    /** null significa credenciales incorrectas; un fallo JDBC lanza SQLException. */
    ResultadoSesionDTO autenticar(String id, String clave) throws SQLException;
    void cambiarClave(String id, String clave) throws SQLException;
}
