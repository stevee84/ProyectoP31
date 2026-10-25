package repositorio;

import dto.CategoriaDTO;
import oracle.jdbc.OracleTypes;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Usa la conexion recibida para consultar Oracle.
 *
 * Cierra sus sentencias y cursores, pero no cierra la conexion
 * ni ejecuta commit o rollback.
 */
public final class RepositorioCategoriasOracle
        implements IRepositorioCategorias {

    private final Connection conexion;

    public RepositorioCategoriasOracle(Connection conexion) {
        this.conexion = conexion;
    }

    @Override
    public CategoriaDTO insertar(String descripcion) throws SQLException {
        try (CallableStatement sentencia = conexion.prepareCall(
                "{call insertarCategoria(?, ?)}")) {

            sentencia.setString(1, descripcion);
            sentencia.registerOutParameter(2, Types.VARCHAR);

            sentencia.execute();

            String id = sentencia.getString(2);

            return new CategoriaDTO(id, descripcion.trim());
        }
    }

    @Override
    public void modificar(CategoriaDTO categoria) throws SQLException {
        try (CallableStatement sentencia = conexion.prepareCall(
                "{call modificarCategoria(?, ?)}")) {

            sentencia.setString(1, categoria.id());
            sentencia.setString(2, categoria.descripcion());

            sentencia.execute();
        }
    }

    @Override
    public void eliminar(String id) throws SQLException {
        try (CallableStatement sentencia = conexion.prepareCall(
                "{call eliminarCategoria(?)}")) {

            sentencia.setString(1, id);
            sentencia.execute();
        }
    }

    @Override
    public CategoriaDTO buscar(String id) throws SQLException {
        try (CallableStatement sentencia = conexion.prepareCall(
                "{? = call buscarCategoria(?)}")) {

            sentencia.registerOutParameter(1, OracleTypes.CURSOR);
            sentencia.setString(2, id);

            sentencia.execute();

            try (ResultSet resultado =
                         (ResultSet) sentencia.getObject(1)) {

                if (resultado.next()) {
                    return leer(resultado);
                }

                return null;
            }
        }
    }

    @Override
    public List<CategoriaDTO> listar() throws SQLException {
        List<CategoriaDTO> categorias = new ArrayList<>();

        try (CallableStatement sentencia = conexion.prepareCall(
                "{? = call listarCategorias()}")) {

            sentencia.registerOutParameter(1, OracleTypes.CURSOR);
            sentencia.execute();

            try (ResultSet resultado =
                         (ResultSet) sentencia.getObject(1)) {

                while (resultado.next()) {
                    categorias.add(leer(resultado));
                }
            }
        }

        return List.copyOf(categorias);
    }

    private CategoriaDTO leer(ResultSet resultado) throws SQLException {
        return new CategoriaDTO(
                resultado.getString("ID"),
                resultado.getString("DESCRIPCION")
        );
    }
}
