package repositorio;

import dto.*;
import oracle.jdbc.OracleTypes;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class RepositorioReservasOracle implements IRepositorioReservas {
    private final Connection conexion;

    public RepositorioReservasOracle(Connection conexion) {
        this.conexion = conexion;
    }

    @Override
    public int insertar(String autor, String descripcion, LocalDateTime inicio, LocalDateTime fin) throws SQLException {
        try (CallableStatement s = conexion.prepareCall("{call insertarReserva(?, ?, ?, ?, ?)}")) {
            s.setString(1, autor);
            s.setString(2, descripcion);
            s.setTimestamp(3, Timestamp.valueOf(inicio));
            s.setTimestamp(4, Timestamp.valueOf(fin));
            s.registerOutParameter(5, Types.INTEGER);
            s.execute();
            return s.getInt(5);
        }
    }

    @Override
    public void asignar(int reserva, String recurso) throws SQLException {
        try (CallableStatement s = conexion.prepareCall("{call asignarRecursoReserva(?, ?)}")) {
            s.setInt(1, reserva);
            s.setString(2, recurso);
            s.execute();
        }
    }

    @Override
    public void cancelar(int reserva, String autor) throws SQLException {
        try (CallableStatement s = conexion.prepareCall("{call cancelarReserva(?, ?)}")) {
            s.setInt(1, reserva);
            s.setString(2, autor);
            s.execute();
        }
    }

    @Override
    public ReservaDTO buscar(int id) throws SQLException {
        List<ReservaDTO> lista;
        try (CallableStatement s = conexion.prepareCall("{? = call buscarReserva(?)}")) {
            s.registerOutParameter(1, OracleTypes.CURSOR);
            s.setInt(2, id);
            lista = leerCabeceras(s);
        }
        return lista.isEmpty() ? null : completar(lista.get(0));
    }

    @Override
    public List<ReservaDTO> listarUsuario(String autor) throws SQLException {
        List<ReservaDTO> lista;
        try (CallableStatement s = conexion.prepareCall("{? = call listarReservasUsuario(?)}")) {
            s.registerOutParameter(1, OracleTypes.CURSOR);
            s.setString(2, autor);
            lista = leerCabeceras(s);
        }
        return completar(lista);
    }

    @Override
    public List<ReservaDTO> listarEnRango(LocalDateTime desde, LocalDateTime hasta) throws SQLException {
        List<ReservaDTO> lista;
        try (CallableStatement s = conexion.prepareCall("{? = call listarReservasEnRango(?, ?)}")) {
            s.registerOutParameter(1, OracleTypes.CURSOR);
            s.setTimestamp(2, Timestamp.valueOf(desde));
            s.setTimestamp(3, Timestamp.valueOf(hasta));
            lista = leerCabeceras(s);
        }
        return completar(lista);
    }

    @Override
    public List<RecursoDTO> recursos(int reserva) throws SQLException {
        try (CallableStatement s = conexion.prepareCall("{? = call listarRecursosReserva(?)}")) {
            s.registerOutParameter(1, OracleTypes.CURSOR);
            s.setInt(2, reserva);
            return RepositorioRecursosOracle.ejecutarLista(s);
        }
    }

    private List<ReservaDTO> leerCabeceras(CallableStatement s) throws SQLException {
        List<ReservaDTO> lista = new ArrayList<>();
        s.execute();
        try (ResultSet r = (ResultSet) s.getObject(1)) {
            while (r.next()) {
                lista.add(new ReservaDTO(r.getInt("ID"),
                        new UsuarioDTO(r.getString("ID_USUARIO"), r.getString("NOMBRE_USUARIO"),
                                Rol.valueOf(r.getString("ROL"))), List.of(),
                        r.getString("DESCRIPCION_ACTIVIDAD"), r.getTimestamp("INICIO").toLocalDateTime(),
                        r.getTimestamp("FIN").toLocalDateTime(), EstadoReserva.valueOf(r.getString("ESTADO"))));
            }
        }
        return lista;
    }

    private ReservaDTO completar(ReservaDTO r) throws SQLException {
        return new ReservaDTO(r.id(), r.autor(), recursos(r.id()), r.descripcionActividad(),
                r.inicio(), r.fin(), r.estado());
    }

    private List<ReservaDTO> completar(List<ReservaDTO> cabeceras) throws SQLException {
        List<ReservaDTO> lista = new ArrayList<>();
        for (ReservaDTO r : cabeceras) lista.add(completar(r));
        return List.copyOf(lista);
    }
}
