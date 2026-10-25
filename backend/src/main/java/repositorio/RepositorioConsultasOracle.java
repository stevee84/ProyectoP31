package repositorio;

import dto.*;
import oracle.jdbc.OracleTypes;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class RepositorioConsultasOracle implements IRepositorioConsultas {
    private final Connection conexion;

    public RepositorioConsultasOracle(Connection conexion) {
        this.conexion = conexion;
    }

    @Override
    public List<EstadisticaCategoriaDTO> contarPorCategoria(LocalDate desde, LocalDate hasta) throws SQLException {
        List<EstadisticaCategoriaDTO> lista = new ArrayList<>();
        try (CallableStatement s = conexion.prepareCall("{? = call contarPorCategoria(?, ?)}")) {
            s.registerOutParameter(1, OracleTypes.CURSOR);
            s.setDate(2, Date.valueOf(desde));
            s.setDate(3, Date.valueOf(hasta));
            s.execute();
            try (ResultSet r = (ResultSet) s.getObject(1)) {
                while (r.next()) lista.add(new EstadisticaCategoriaDTO(
                        new CategoriaDTO(r.getString("ID_CATEGORIA"), r.getString("DESCRIPCION_CATEGORIA")),
                        r.getInt("CANTIDAD")));
            }
        }
        return List.copyOf(lista);
    }

    @Override
    public List<EstadisticaSemanaDTO> contarPorSemana(LocalDate desde, LocalDate hasta) throws SQLException {
        List<EstadisticaSemanaDTO> lista = new ArrayList<>();
        try (CallableStatement s = conexion.prepareCall("{? = call contarPorSemana(?, ?)}")) {
            s.registerOutParameter(1, OracleTypes.CURSOR);
            s.setDate(2, Date.valueOf(desde));
            s.setDate(3, Date.valueOf(hasta));
            s.execute();
            try (ResultSet r = (ResultSet) s.getObject(1)) {
                while (r.next()) lista.add(new EstadisticaSemanaDTO(r.getDate("INICIO_SEMANA").toLocalDate(),
                        r.getDate("FIN_SEMANA").toLocalDate(), r.getInt("CANTIDAD")));
            }
        }
        return List.copyOf(lista);
    }
}
