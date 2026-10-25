package repositorio;

import java.sql.Connection;

/** Repositorios de una operación: todos reciben su misma conexión. */
public class FabricaRepositoriosOracle {
    public IRepositorioCategorias categorias(Connection c) { return new RepositorioCategoriasOracle(c); }
    public IRepositorioFuncionarios funcionarios(Connection c) { return new RepositorioFuncionariosOracle(c); }
    public IRepositorioRecursos recursos(Connection c) { return new RepositorioRecursosOracle(c); }
    public IRepositorioReservas reservas(Connection c) { return new RepositorioReservasOracle(c); }
    public IRepositorioConsultas consultas(Connection c) { return new RepositorioConsultasOracle(c); }
}
