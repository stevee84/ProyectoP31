package repositorio;

import dto.CategoriaDTO;
import dto.RecursoDTO;
import oracle.jdbc.OracleTypes;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class RepositorioRecursosOracle implements IRepositorioRecursos {
    private final Connection conexion;

    public RepositorioRecursosOracle(Connection conexion) {
        this.conexion = conexion;
    }

    @Override
    public void insertar(String codigo, String categoria, String descripcion) throws SQLException {
        try (CallableStatement s = conexion.prepareCall("{call insertarRecurso(?, ?, ?)}")) {
            s.setString(1, codigo);
            s.setString(2, categoria);
            s.setString(3, descripcion);
            s.execute();
        }
    }

    @Override
    public void modificar(String codigo, String categoria, String descripcion) throws SQLException {
        try (CallableStatement s = conexion.prepareCall("{call modificarRecurso(?, ?, ?)}")) {
            s.setString(1, codigo);
            s.setString(2, categoria);
            s.setString(3, descripcion);
            s.execute();
        }
    }

    @Override
    public void eliminar(String codigo) throws SQLException {
        try (CallableStatement s = conexion.prepareCall("{call eliminarRecurso(?)}")) {
            s.setString(1, codigo);
            s.execute();
        }
    }

    @Override
    public RecursoDTO buscar(String codigo) throws SQLException {
        try (CallableStatement s = conexion.prepareCall("{? = call buscarRecurso(?)}")) {
            s.registerOutParameter(1, OracleTypes.CURSOR);
            s.setString(2, codigo);
            s.execute();
            try (ResultSet r = (ResultSet) s.getObject(1)) {
                return r.next() ? leer(r) : null;
            }
        }
    }

    @Override
    public List<RecursoDTO> listar() throws SQLException {
        try (CallableStatement s = conexion.prepareCall("{? = call listarRecursos()}")) {
            s.registerOutParameter(1, OracleTypes.CURSOR);
            return ejecutarLista(s);
        }
    }

    @Override
    public List<RecursoDTO> listarPorCategoria(String categoria) throws SQLException {
        try (CallableStatement s = conexion.prepareCall("{? = call listarRecursosPorCategoria(?)}")) {
            s.registerOutParameter(1, OracleTypes.CURSOR);
            s.setString(2, categoria);
            return ejecutarLista(s);
        }
    }

    @Override
    public List<RecursoDTO> disponibles(String categoria, LocalDateTime inicio, LocalDateTime fin) throws SQLException {
        try (CallableStatement s = conexion.prepareCall("{? = call listarRecursosDisponibles(?, ?, ?)}")) {
            s.registerOutParameter(1, OracleTypes.CURSOR);
            s.setString(2, categoria);
            s.setTimestamp(3, Timestamp.valueOf(inicio));
            s.setTimestamp(4, Timestamp.valueOf(fin));
            return ejecutarLista(s);
        }
    }

    static List<RecursoDTO> ejecutarLista(CallableStatement s) throws SQLException {
        List<RecursoDTO> lista = new ArrayList<>();
        s.execute();
        try (ResultSet r = (ResultSet) s.getObject(1)) {
            while (r.next()) lista.add(leer(r));
        }
        return List.copyOf(lista);
    }

    static RecursoDTO leer(ResultSet r) throws SQLException {
        return new RecursoDTO(r.getString("CODIGO"),
                new CategoriaDTO(r.getString("ID_CATEGORIA"), r.getString("DESCRIPCION_CATEGORIA")),
                r.getString("DESCRIPCION"));
    }
}
