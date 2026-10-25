package repositorio;

import dto.CategoriaDTO;

import java.sql.SQLException;
import java.util.List;

/**
 * Operaciones de persistencia de categorias.
 * Las reglas de negocio pertenecen al servicio.
 */
public interface IRepositorioCategorias {

    CategoriaDTO insertar(String descripcion) throws SQLException;

    void modificar(CategoriaDTO categoria) throws SQLException;

    void eliminar(String id) throws SQLException;

    /**
     * Devuelve null si la categoria no existe.
     * El servicio decide como informar ese caso.
     */
    CategoriaDTO buscar(String id) throws SQLException;

    List<CategoriaDTO> listar() throws SQLException;
}
