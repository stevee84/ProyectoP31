package repositorio;

import dto.*;
import oracle.jdbc.OracleTypes;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** No cierra ni confirma la conexión recibida. */
public final class RepositorioFuncionariosOracle implements IRepositorioFuncionarios {
    private final Connection conexion;

    public RepositorioFuncionariosOracle(Connection conexion) {
        this.conexion = conexion;
    }

    @Override
    public void insertar(FuncionarioDTO f) throws SQLException {
        try (CallableStatement s = conexion.prepareCall("{call insertarFuncionario(?, ?, ?)}")) {
            s.setString(1, f.id());
            s.setString(2, f.nombre());
            s.setString(3, f.telefono());
            s.execute();
        }
    }

    @Override
    public void modificar(FuncionarioDTO f) throws SQLException {
        try (CallableStatement s = conexion.prepareCall("{call modificarFuncionario(?, ?, ?)}")) {
            s.setString(1, f.id());
            s.setString(2, f.nombre());
            s.setString(3, f.telefono());
            s.execute();
        }
    }

    @Override
    public void eliminar(String id) throws SQLException {
        try (CallableStatement s = conexion.prepareCall("{call eliminarFuncionario(?)}")) {
            s.setString(1, id);
            s.execute();
        }
    }

    @Override
    public FuncionarioDTO buscar(String id) throws SQLException {
        try (CallableStatement s = conexion.prepareCall("{? = call buscarFuncionario(?)}")) {
            s.registerOutParameter(1, OracleTypes.CURSOR);
            s.setString(2, id);
            s.execute();
            try (ResultSet r = (ResultSet) s.getObject(1)) {
                return r.next() ? leer(r) : null;
            }
        }
    }

    @Override
    public List<FuncionarioDTO> listar() throws SQLException {
        List<FuncionarioDTO> lista = new ArrayList<>();
        try (CallableStatement s = conexion.prepareCall("{? = call listarFuncionarios()}")) {
            s.registerOutParameter(1, OracleTypes.CURSOR);
            s.execute();
            try (ResultSet r = (ResultSet) s.getObject(1)) {
                while (r.next()) lista.add(leer(r));
            }
        }
        return List.copyOf(lista);
    }

    @Override
    public ResultadoSesionDTO autenticar(String id, String clave) throws SQLException {
        try (CallableStatement s = conexion.prepareCall("{? = call validarUsuario(?, ?)}")) {
            s.registerOutParameter(1, OracleTypes.CURSOR);
            s.setString(2, id);
            s.setString(3, clave);
            s.execute();
            try (ResultSet r = (ResultSet) s.getObject(1)) {
                if (!r.next()) return null;
                return new ResultadoSesionDTO(new UsuarioDTO(r.getString("ID"), r.getString("NOMBRE"),
                        Rol.valueOf(r.getString("ROL"))), r.getInt("PRIMER_INGRESO") == 1);
            }
        }
    }

    @Override
    public void cambiarClave(String id, String clave) throws SQLException {
        try (CallableStatement s = conexion.prepareCall("{call cambiarClaveUsuario(?, ?)}")) {
            s.setString(1, id);
            s.setString(2, clave);
            s.execute();
        }
    }

    private static FuncionarioDTO leer(ResultSet r) throws SQLException {
        return new FuncionarioDTO(r.getString("ID"), r.getString("NOMBRE"), r.getString("TELEFONO"));
    }
}
