package contrato;

import dto.AgendaSemanaDTO;
import dto.CategoriaDTO;
import dto.EstadisticaSemanaDTO;
import dto.FuncionarioDTO;
import dto.MatrizCalendarioDTO;
import dto.RecursoDTO;
import dto.ReservaDTO;
import dto.ResultadoExtraccionDTO;
import dto.ResultadoReservaDTO;
import dto.ResultadoSesionDTO;
import protocolo.ServicioException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Contrato completo de la aplicación. Lo implementan ProxyServicio en el Frontend
 * y el servicio real del Backend. El autor de cada operación lo decide el servidor
 * a partir de la sesión de la conexión, nunca el cliente.
 *
 * <p>Errores: toda falla se informa con {@link ServicioException} y su {@link protocolo.CodigoError}:
 * <ul>
 *   <li>VALIDACION: datos inválidos, duplicados o inexistentes.</li>
 *   <li>SESION: credenciales incorrectas o sesión no iniciada.</li>
 *   <li>PERMISO: el rol no permite la operación o la reserva es de otro usuario.</li>
 *   <li>RECURSO_EN_USO: borrar algo que tiene recursos o reservas asociados.</li>
 *   <li>DISPONIBILIDAD: horario no disponible (fuera del caso normal de ResultadoReservaDTO.fracaso).</li>
 *   <li>PERSISTENCIA: Oracle falló y la operación se revirtió.</li>
 *   <li>COMUNICACION: la red falló (solo lo genera el Proxy).</li>
 * </ul>
 * Las operaciones sin retorno devuelven normalmente si tuvieron éxito.
 */
public interface IServicioAplicacion {

    int HORA_INICIO = 7;
    int HORA_FIN = 18;

    // ── Sesión ───────────────────────────────────────────────────────
    /** Nunca devuelve null: con credenciales incorrectas lanza SESION. */
    ResultadoSesionDTO iniciarSesion(String id, String clave) throws ServicioException;

    void cerrarSesion() throws ServicioException;

    void cambiarClave(String nuevaClave) throws ServicioException;

    // ── Funcionarios (administrador) ─────────────────────────────────
    /** Crea el usuario con rol FUNCIONARIO, clave inicial igual a su id y primer ingreso pendiente. */
    void registrarFuncionario(FuncionarioDTO funcionario) throws ServicioException;

    void actualizarFuncionario(FuncionarioDTO funcionario) throws ServicioException;

    void eliminarFuncionario(String id) throws ServicioException;

    FuncionarioDTO buscarFuncionario(String id) throws ServicioException;

    List<FuncionarioDTO> listarFuncionarios() throws ServicioException;

    // ── Categorías (administrador) ───────────────────────────────────
    CategoriaDTO registrarCategoria(String descripcion) throws ServicioException;

    void actualizarCategoria(CategoriaDTO categoria) throws ServicioException;

    void eliminarCategoria(String id) throws ServicioException;

    CategoriaDTO buscarCategoria(String id) throws ServicioException;

    List<CategoriaDTO> listarCategorias() throws ServicioException;

    // ── Recursos (administrador) ─────────────────────────────────────
    void registrarRecurso(String codigo, String idCategoria, String descripcion) throws ServicioException;

    void actualizarRecurso(String codigo, String idCategoria, String descripcion) throws ServicioException;

    void eliminarRecurso(String codigo) throws ServicioException;

    RecursoDTO buscarRecurso(String codigo) throws ServicioException;

    List<RecursoDTO> listarRecursos() throws ServicioException;

    List<RecursoDTO> listarRecursosPorCategoria(String idCategoria) throws ServicioException;

    // ── Reservas (funcionario) ───────────────────────────────────────
    ResultadoReservaDTO crearReservacion(List<String> idsCategorias, String descripcionActividad,
                                         LocalDateTime inicio, LocalDateTime fin) throws ServicioException;

    void cancelarReservacion(int id) throws ServicioException;

    List<ReservaDTO> listarReservacionesSesion() throws ServicioException;

    ResultadoExtraccionDTO extraerDatosDesdeFrase(String frase) throws ServicioException;

    // ── Consultas ────────────────────────────────────────────────────
    MatrizCalendarioDTO obtenerCalendarizacion(LocalDate fecha, String idCategoria) throws ServicioException;

    AgendaSemanaDTO obtenerAgendaSemana(LocalDate dia) throws ServicioException;

    List<EstadisticaSemanaDTO> contarPorSemana(LocalDate desde, LocalDate hasta) throws ServicioException;

    int contarReservasCategoria(String idCategoria, LocalDate desde, LocalDate hasta) throws ServicioException;
}
