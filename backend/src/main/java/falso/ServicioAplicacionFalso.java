package falso;

import contrato.IServicioAplicacion;
import dto.AgendaSemanaDTO;
import dto.CategoriaDTO;
import dto.EstadisticaSemanaDTO;
import dto.EstadoReserva;
import dto.FuncionarioDTO;
import dto.InfoReservaDTO;
import dto.MatrizCalendarioDTO;
import dto.RecursoDTO;
import dto.ReservaDTO;
import dto.ResultadoExtraccionDTO;
import dto.ResultadoReservaDTO;
import dto.ResultadoSesionDTO;
import dto.Rol;
import dto.UsuarioDTO;
import protocolo.CodigoError;
import protocolo.ServicioException;
import servicio.ContextoSesion;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Implementación en memoria del contrato, solo para pruebas de red y demostraciones.
 * Aplica las reglas mínimas (roles, disponibilidad, cancelar solo lo propio) para
 * que los escenarios del enunciado se puedan probar sin Oracle.
 */
public class ServicioAplicacionFalso implements IServicioAplicacion {

    private final DatosFalsos datos;
    private final ContextoSesion contexto;

    ServicioAplicacionFalso(DatosFalsos datos, ContextoSesion contexto) {
        this.datos = datos;
        this.contexto = contexto;
    }

    // ── Sesión ───────────────────────────────────────────────────────

    @Override
    public ResultadoSesionDTO iniciarSesion(String id, String clave) {
        synchronized (datos) {
            UsuarioDTO usuario = datos.usuarios.get(id);
            if (usuario == null || !datos.claves.get(id).equals(clave)) {
                throw new ServicioException(CodigoError.SESION, "Usuario o contraseña incorrectos.");
            }
            return new ResultadoSesionDTO(usuario, false);
        }
    }

    @Override
    public void cerrarSesion() {
    }

    @Override
    public void cambiarClave(String nuevaClave) {
        synchronized (datos) {
            datos.claves.put(contexto.getUsuario().id(), nuevaClave);
        }
    }

    // ── Funcionarios ─────────────────────────────────────────────────

    @Override
    public void registrarFuncionario(FuncionarioDTO funcionario) {
        exigirAdministrador();
        synchronized (datos) {
            if (datos.usuarios.containsKey(funcionario.id())) {
                throw new ServicioException(CodigoError.VALIDACION, "Identificación duplicada.");
            }
            datos.usuarios.put(funcionario.id(), new UsuarioDTO(funcionario.id(), funcionario.nombre(), Rol.FUNCIONARIO));
            datos.claves.put(funcionario.id(), funcionario.id());
            datos.funcionarios.put(funcionario.id(), funcionario);
        }
    }

    @Override
    public void actualizarFuncionario(FuncionarioDTO funcionario) {
        exigirAdministrador();
        synchronized (datos) {
            existente(datos.funcionarios, funcionario.id(), "funcionario");
            datos.funcionarios.put(funcionario.id(), funcionario);
        }
    }

    @Override
    public void eliminarFuncionario(String id) {
        exigirAdministrador();
        synchronized (datos) {
            existente(datos.funcionarios, id, "funcionario");
            datos.funcionarios.remove(id);
            datos.usuarios.remove(id);
        }
    }

    @Override
    public FuncionarioDTO buscarFuncionario(String id) {
        synchronized (datos) {
            return datos.funcionarios.get(id);
        }
    }

    @Override
    public List<FuncionarioDTO> listarFuncionarios() {
        synchronized (datos) {
            return List.copyOf(datos.funcionarios.values());
        }
    }

    // ── Categorías ───────────────────────────────────────────────────

    @Override
    public CategoriaDTO registrarCategoria(String descripcion) {
        exigirAdministrador();
        synchronized (datos) {
            return datos.nuevaCategoria(descripcion);
        }
    }

    @Override
    public void actualizarCategoria(CategoriaDTO categoria) {
        exigirAdministrador();
        synchronized (datos) {
            existente(datos.categorias, categoria.id(), "categoría");
            datos.categorias.put(categoria.id(), categoria);
        }
    }

    @Override
    public void eliminarCategoria(String id) {
        exigirAdministrador();
        synchronized (datos) {
            existente(datos.categorias, id, "categoría");
            if (datos.recursos.values().stream().anyMatch(r -> r.categoria().id().equals(id))) {
                throw new ServicioException(CodigoError.RECURSO_EN_USO, "La categoría tiene recursos asociados.");
            }
            datos.categorias.remove(id);
        }
    }

    @Override
    public CategoriaDTO buscarCategoria(String id) {
        synchronized (datos) {
            return datos.categorias.get(id);
        }
    }

    @Override
    public List<CategoriaDTO> listarCategorias() {
        synchronized (datos) {
            return List.copyOf(datos.categorias.values());
        }
    }

    // ── Recursos ─────────────────────────────────────────────────────

    @Override
    public void registrarRecurso(String codigo, String idCategoria, String descripcion) {
        exigirAdministrador();
        synchronized (datos) {
            if (datos.recursos.containsKey(codigo)) {
                throw new ServicioException(CodigoError.VALIDACION, "Código de recurso duplicado.");
            }
            CategoriaDTO categoria = existente(datos.categorias, idCategoria, "categoría");
            datos.recursos.put(codigo, new RecursoDTO(codigo, categoria, descripcion));
        }
    }

    @Override
    public void actualizarRecurso(String codigo, String idCategoria, String descripcion) {
        exigirAdministrador();
        synchronized (datos) {
            existente(datos.recursos, codigo, "recurso");
            CategoriaDTO categoria = existente(datos.categorias, idCategoria, "categoría");
            datos.recursos.put(codigo, new RecursoDTO(codigo, categoria, descripcion));
        }
    }

    @Override
    public void eliminarRecurso(String codigo) {
        exigirAdministrador();
        synchronized (datos) {
            existente(datos.recursos, codigo, "recurso");
            datos.recursos.remove(codigo);
        }
    }

    @Override
    public RecursoDTO buscarRecurso(String codigo) {
        synchronized (datos) {
            return datos.recursos.get(codigo);
        }
    }

    @Override
    public List<RecursoDTO> listarRecursos() {
        synchronized (datos) {
            return List.copyOf(datos.recursos.values());
        }
    }

    @Override
    public List<RecursoDTO> listarRecursosPorCategoria(String idCategoria) {
        synchronized (datos) {
            return datos.recursos.values().stream()
                    .filter(r -> r.categoria().id().equals(idCategoria))
                    .toList();
        }
    }

    // ── Reservas ─────────────────────────────────────────────────────

    @Override
    public ResultadoReservaDTO crearReservacion(List<String> idsCategorias, String descripcionActividad,
                                                LocalDateTime inicio, LocalDateTime fin) {
        exigirFuncionario();
        // Consultar, asignar y guardar bajo el mismo bloqueo: dos clientes no pueden
        // quedarse con el último recurso del mismo horario.
        synchronized (datos) {
            List<RecursoDTO> asignados = new ArrayList<>();
            List<CategoriaDTO> noDisponibles = new ArrayList<>();
            for (String idCategoria : idsCategorias) {
                RecursoDTO libre = listarRecursosPorCategoria(idCategoria).stream()
                        .filter(r -> !asignados.contains(r) && !estaOcupado(r, inicio, fin))
                        .findFirst()
                        .orElse(null);
                if (libre == null) {
                    noDisponibles.add(existente(datos.categorias, idCategoria, "categoría"));
                } else {
                    asignados.add(libre);
                }
            }
            if (!noDisponibles.isEmpty()) {
                return ResultadoReservaDTO.fracaso(noDisponibles);
            }
            ReservaDTO reserva = new ReservaDTO(datos.siguienteReserva++, contexto.getUsuario(), asignados,
                    descripcionActividad, inicio, fin, EstadoReserva.ACTIVA);
            datos.reservas.add(reserva);
            return ResultadoReservaDTO.exito(reserva);
        }
    }

    @Override
    public void cancelarReservacion(int id) {
        exigirFuncionario();
        synchronized (datos) {
            for (int i = 0; i < datos.reservas.size(); i++) {
                ReservaDTO r = datos.reservas.get(i);
                if (r.id() == id) {
                    if (!r.autor().id().equals(contexto.getUsuario().id())) {
                        throw new ServicioException(CodigoError.PERMISO, "Solo puede cancelar sus propias reservas.");
                    }
                    datos.reservas.set(i, new ReservaDTO(r.id(), r.autor(), r.recursos(),
                            r.descripcionActividad(), r.inicio(), r.fin(), EstadoReserva.CANCELADA));
                    return;
                }
            }
            throw new ServicioException(CodigoError.VALIDACION, "No existe la reservación " + id + ".");
        }
    }

    @Override
    public List<ReservaDTO> listarReservacionesSesion() {
        synchronized (datos) {
            String idUsuario = contexto.getUsuario().id();
            return datos.reservas.stream().filter(r -> r.autor().id().equals(idUsuario)).toList();
        }
    }

    @Override
    public ResultadoExtraccionDTO extraerDatosDesdeFrase(String frase) {
        return ResultadoExtraccionDTO.fallo("La extracción con IA no está disponible en el servidor de prueba.");
    }

    // ── Consultas ────────────────────────────────────────────────────

    @Override
    public MatrizCalendarioDTO obtenerCalendarizacion(LocalDate fecha, String idCategoria) {
        synchronized (datos) {
            List<RecursoDTO> recursos = listarRecursosPorCategoria(idCategoria);
            List<List<String>> celdas = new ArrayList<>();
            for (int hora = HORA_INICIO; hora < HORA_FIN; hora++) {
                LocalDateTime inicioHora = fecha.atTime(hora, 0);
                List<String> fila = new ArrayList<>();
                for (RecursoDTO recurso : recursos) {
                    fila.add(datos.reservas.stream()
                            .filter(r -> activaConRecurso(r, recurso, inicioHora, inicioHora.plusHours(1)))
                            .map(r -> r.descripcionActividad() + " - " + r.autor().nombre())
                            .findFirst()
                            .orElse("Disponible"));
                }
                celdas.add(fila);
            }
            return new MatrizCalendarioDTO(fecha, recursos, celdas);
        }
    }

    @Override
    public AgendaSemanaDTO obtenerAgendaSemana(LocalDate dia) {
        LocalDate lunes = dia.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        Map<DayOfWeek, Map<Integer, List<InfoReservaDTO>>> actividades = new EnumMap<>(DayOfWeek.class);
        synchronized (datos) {
            for (ReservaDTO r : datos.reservas) {
                LocalDate fecha = r.inicio().toLocalDate();
                if (r.estado() == EstadoReserva.ACTIVA && !fecha.isBefore(lunes) && fecha.isBefore(lunes.plusDays(7))) {
                    actividades.computeIfAbsent(fecha.getDayOfWeek(), d -> new TreeMap<>())
                            .computeIfAbsent(r.inicio().getHour(), h -> new ArrayList<>())
                            .add(new InfoReservaDTO(r.inicio(), r.fin(), r.descripcionActividad(), r.autor().nombre(),
                                    r.recursos().stream().map(RecursoDTO::descripcion).toList()));
                }
            }
        }
        return new AgendaSemanaDTO(lunes, actividades);
    }

    @Override
    public List<EstadisticaSemanaDTO> contarPorSemana(LocalDate desde, LocalDate hasta) {
        List<EstadisticaSemanaDTO> semanas = new ArrayList<>();
        synchronized (datos) {
            for (LocalDate lunes = desde.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                 !lunes.isAfter(hasta); lunes = lunes.plusWeeks(1)) {
                LocalDate inicio = lunes;
                int cantidad = (int) datos.reservas.stream()
                        .map(r -> r.inicio().toLocalDate())
                        .filter(f -> !f.isBefore(inicio) && f.isBefore(inicio.plusDays(7)))
                        .count();
                semanas.add(new EstadisticaSemanaDTO(inicio, inicio.plusDays(6), cantidad));
            }
        }
        return semanas;
    }

    @Override
    public int contarReservasCategoria(String idCategoria, LocalDate desde, LocalDate hasta) {
        synchronized (datos) {
            return (int) datos.reservas.stream()
                    .filter(r -> !r.inicio().toLocalDate().isBefore(desde) && !r.inicio().toLocalDate().isAfter(hasta))
                    .filter(r -> r.recursos().stream().anyMatch(rec -> rec.categoria().id().equals(idCategoria)))
                    .count();
        }
    }

    // ── Apoyo ────────────────────────────────────────────────────────

    private void exigirAdministrador() {
        if (!contexto.esAdministrador()) {
            throw new ServicioException(CodigoError.PERMISO, "Solo el administrador puede realizar esta acción.");
        }
    }

    private void exigirFuncionario() {
        if (contexto.esAdministrador()) {
            throw new ServicioException(CodigoError.PERMISO, "Solo los funcionarios pueden gestionar reservas.");
        }
    }

    private boolean estaOcupado(RecursoDTO recurso, LocalDateTime inicio, LocalDateTime fin) {
        return datos.reservas.stream().anyMatch(r -> activaConRecurso(r, recurso, inicio, fin));
    }

    private static boolean activaConRecurso(ReservaDTO r, RecursoDTO recurso, LocalDateTime inicio, LocalDateTime fin) {
        return r.estado() == EstadoReserva.ACTIVA
                && r.recursos().contains(recurso)
                && r.inicio().isBefore(fin)
                && inicio.isBefore(r.fin());
    }

    private static <T> T existente(Map<String, T> mapa, String id, String tipo) {
        T valor = mapa.get(id);
        if (valor == null) {
            throw new ServicioException(CodigoError.VALIDACION, "No se encontró " + tipo + " con id " + id + ".");
        }
        return valor;
    }
}
