package falso;

import contrato.IServicioAplicacion;
import contrato.Reglas;
import dto.AgendaSemanaDTO;
import dto.CategoriaDTO;
import dto.EstadisticaCategoriaDTO;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Implementación en memoria del contrato para trabajar el Frontend sin servidor:
 * pruebas del MVC y revisión visual de las pantallas (MainClienteDemo).
 *
 * <p>Representa una sola conexión: guarda su propio usuario de sesión, igual que el
 * ContextoSesion del Backend. Aplica las mismas reglas documentadas en
 * IServicioAplicacion para que los mensajes de error se vean como en producción.
 * Usuarios de ejemplo (clave = id): ADMIN, F001, F002, F003; los mismos del
 * servicio falso del Backend.
 */
public class ServicioClienteFalso implements IServicioAplicacion {

    private final Map<String, UsuarioDTO> usuarios = new LinkedHashMap<>();
    private final Map<String, String> claves = new LinkedHashMap<>();
    private final Set<String> primerIngreso = new HashSet<>();
    private final Map<String, FuncionarioDTO> funcionarios = new LinkedHashMap<>();
    private final Map<String, CategoriaDTO> categorias = new LinkedHashMap<>();
    private final Map<String, RecursoDTO> recursos = new LinkedHashMap<>();
    private final List<ReservaDTO> reservas = new ArrayList<>();
    private int siguienteReserva = 1;
    private int siguienteCategoria = 1;

    private UsuarioDTO usuarioSesion;
    /** Cantidad de llamadas recibidas: las pruebas lo usan para comprobar que no se pide por celda. */
    private int llamadas;

    public ServicioClienteFalso() {
        agregarUsuario(new UsuarioDTO("ADMIN", "Administrador", Rol.ADMINISTRADOR), null);
        agregarUsuario(new UsuarioDTO("F001", "Ana Mora", Rol.FUNCIONARIO), "8888-0001");
        agregarUsuario(new UsuarioDTO("F002", "Luis Rojas", Rol.FUNCIONARIO), "8888-0002");
        agregarUsuario(new UsuarioDTO("F003", "Carla Vargas", Rol.FUNCIONARIO), "8888-0003");

        CategoriaDTO sala = nuevaCategoria("Sala de reuniones");
        CategoriaDTO laptop = nuevaCategoria("Laptop Windows");
        CategoriaDTO proyector = nuevaCategoria("Proyector");
        agregarRecurso("SALA-01", sala, "Sala A, segundo piso");
        agregarRecurso("SALA-02", sala, "Sala B, primer piso");
        agregarRecurso("LAP-01", laptop, "Laptop Dell 14\"");
        agregarRecurso("LAP-02", laptop, "Laptop HP 15\"");
        agregarRecurso("PROY-01", proyector, "Proyector Epson");

        // Reservas de otros usuarios para que calendario, agenda y estadísticas tengan datos.
        LocalDate manana = LocalDate.now().plusDays(1);
        reservaInicial("F002", "Reunion clientes", manana.atTime(8, 0), manana.atTime(10, 0), "SALA-01", "LAP-01");
        reservaInicial("F003", "Capacitacion", manana.atTime(13, 0), manana.atTime(15, 0), "SALA-02", "PROY-01");
        reservaInicial("F002", "Cafe de cumpleanos", manana.plusDays(2).atTime(15, 0),
                manana.plusDays(2).atTime(16, 0), "SALA-01");
    }

    // ── Datos de ejemplo ─────────────────────────────────────────────

    private void agregarUsuario(UsuarioDTO usuario, String telefono) {
        usuarios.put(usuario.id(), usuario);
        claves.put(usuario.id(), usuario.id());
        if (telefono != null) {
            funcionarios.put(usuario.id(), new FuncionarioDTO(usuario.id(), usuario.nombre(), telefono));
        }
    }

    private CategoriaDTO nuevaCategoria(String descripcion) {
        CategoriaDTO categoria = new CategoriaDTO(String.format("CAT-%06d", siguienteCategoria++), descripcion);
        categorias.put(categoria.id(), categoria);
        return categoria;
    }

    private void agregarRecurso(String codigo, CategoriaDTO categoria, String descripcion) {
        recursos.put(codigo, new RecursoDTO(codigo, categoria, descripcion));
    }

    private void reservaInicial(String idAutor, String actividad, LocalDateTime inicio, LocalDateTime fin,
                                String... codigos) {
        List<RecursoDTO> asignados = new ArrayList<>();
        for (String codigo : codigos) {
            asignados.add(recursos.get(codigo));
        }
        reservas.add(new ReservaDTO(siguienteReserva++, usuarios.get(idAutor), asignados, actividad,
                inicio, fin, EstadoReserva.ACTIVA));
    }

    /** Recursos de ejemplo, para que la fuente simulada arme alertas coherentes. */
    public synchronized List<RecursoDTO> recursosDeEjemplo() {
        return List.copyOf(recursos.values());
    }

    /** Usuarios de ejemplo (sin claves), para que la fuente simulada elija autores. */
    public synchronized List<UsuarioDTO> usuariosDeEjemplo() {
        return List.copyOf(usuarios.values());
    }

    public synchronized int getLlamadas() {
        return llamadas;
    }

    // ── Sesión ───────────────────────────────────────────────────────

    @Override
    public synchronized ResultadoSesionDTO iniciarSesion(String id, String clave) {
        llamadas++;
        UsuarioDTO usuario = usuarios.get(id);
        if (usuario == null || !claves.get(id).equals(clave)) {
            throw new ServicioException(CodigoError.SESION, "Usuario o contraseña incorrectos.");
        }
        usuarioSesion = usuario;
        return new ResultadoSesionDTO(usuario, primerIngreso.contains(id));
    }

    @Override
    public synchronized void cerrarSesion() {
        sesionSinRestriccion();
        usuarioSesion = null;
    }

    @Override
    public synchronized void cambiarClave(String nuevaClave) {
        String id = sesionSinRestriccion().id();
        if (nuevaClave == null || nuevaClave.isBlank()) {
            throw new ServicioException(CodigoError.VALIDACION, "La nueva contraseña es obligatoria.");
        }
        if (Reglas.claveIgualId(nuevaClave, id)) {
            throw new ServicioException(CodigoError.VALIDACION, Reglas.MENSAJE_CLAVE_IGUAL_ID);
        }
        if (nuevaClave.equals(claves.get(id))) {
            throw new ServicioException(CodigoError.VALIDACION, Reglas.MENSAJE_CLAVE_IGUAL_ACTUAL);
        }
        claves.put(id, nuevaClave);
        primerIngreso.remove(id);
    }

    // ── Funcionarios ─────────────────────────────────────────────────

    @Override
    public synchronized void registrarFuncionario(FuncionarioDTO funcionario) {
        exigirAdministrador();
        validarTelefono(funcionario);
        if (usuarios.containsKey(funcionario.id())) {
            throw new ServicioException(CodigoError.VALIDACION, "Identificación duplicada.");
        }
        usuarios.put(funcionario.id(), new UsuarioDTO(funcionario.id(), funcionario.nombre(), Rol.FUNCIONARIO));
        claves.put(funcionario.id(), funcionario.id());
        primerIngreso.add(funcionario.id());
        funcionarios.put(funcionario.id(), funcionario);
    }

    @Override
    public synchronized void actualizarFuncionario(FuncionarioDTO funcionario) {
        exigirAdministrador();
        validarTelefono(funcionario);
        existente(funcionarios, funcionario.id(), "funcionario");
        funcionarios.put(funcionario.id(), funcionario);
        usuarios.put(funcionario.id(), new UsuarioDTO(funcionario.id(), funcionario.nombre(), Rol.FUNCIONARIO));
    }

    @Override
    public synchronized void eliminarFuncionario(String id) {
        exigirAdministrador();
        existente(funcionarios, id, "funcionario");
        if (reservas.stream().anyMatch(r -> r.autor().id().equals(id))) {
            throw new ServicioException(CodigoError.RECURSO_EN_USO, "El funcionario tiene reservas registradas.");
        }
        funcionarios.remove(id);
        usuarios.remove(id);
        claves.remove(id);
    }

    @Override
    public synchronized FuncionarioDTO buscarFuncionario(String id) {
        exigirAdministrador();
        return existente(funcionarios, id, "funcionario");
    }

    @Override
    public synchronized List<FuncionarioDTO> listarFuncionarios() {
        exigirAdministrador();
        return List.copyOf(funcionarios.values());
    }

    // ── Categorías ───────────────────────────────────────────────────

    @Override
    public synchronized CategoriaDTO registrarCategoria(String descripcion) {
        exigirAdministrador();
        if (descripcion == null || descripcion.isBlank()) {
            throw new ServicioException(CodigoError.VALIDACION, "La descripción es obligatoria.");
        }
        if (categorias.values().stream().anyMatch(c -> c.descripcion().trim().equalsIgnoreCase(descripcion.trim()))) {
            throw new ServicioException(CodigoError.VALIDACION, Reglas.MENSAJE_CATEGORIA_DUPLICADA);
        }
        return nuevaCategoria(descripcion);
    }

    @Override
    public synchronized void actualizarCategoria(CategoriaDTO categoria) {
        exigirAdministrador();
        existente(categorias, categoria.id(), "categoría");
        if (categorias.values().stream().anyMatch(c -> categoria.descripcion() != null && !c.id().equals(categoria.id())
                && c.descripcion().trim().equalsIgnoreCase(categoria.descripcion().trim()))) {
            throw new ServicioException(CodigoError.VALIDACION, Reglas.MENSAJE_CATEGORIA_DUPLICADA);
        }
        categorias.put(categoria.id(), categoria);
    }

    @Override
    public synchronized void eliminarCategoria(String id) {
        exigirAdministrador();
        existente(categorias, id, "categoría");
        if (recursos.values().stream().anyMatch(r -> r.categoria().id().equals(id))) {
            throw new ServicioException(CodigoError.RECURSO_EN_USO, "La categoría tiene recursos asociados.");
        }
        categorias.remove(id);
    }

    @Override
    public synchronized CategoriaDTO buscarCategoria(String id) {
        sesion();
        return existente(categorias, id, "categoría");
    }

    @Override
    public synchronized List<CategoriaDTO> listarCategorias() {
        sesion();
        return List.copyOf(categorias.values());
    }

    // ── Recursos ─────────────────────────────────────────────────────

    @Override
    public synchronized void registrarRecurso(String codigo, String idCategoria, String descripcion) {
        exigirAdministrador();
        if (recursos.containsKey(codigo)) {
            throw new ServicioException(CodigoError.VALIDACION, "Código de recurso duplicado.");
        }
        recursos.put(codigo, new RecursoDTO(codigo, existente(categorias, idCategoria, "categoría"), descripcion));
    }

    @Override
    public synchronized void actualizarRecurso(String codigo, String idCategoria, String descripcion) {
        exigirAdministrador();
        existente(recursos, codigo, "recurso");
        recursos.put(codigo, new RecursoDTO(codigo, existente(categorias, idCategoria, "categoría"), descripcion));
    }

    @Override
    public synchronized void eliminarRecurso(String codigo) {
        exigirAdministrador();
        existente(recursos, codigo, "recurso");
        if (reservas.stream().anyMatch(r -> r.estado() == EstadoReserva.ACTIVA
                && r.recursos().stream().anyMatch(x -> x.codigo().equals(codigo)))) {
            throw new ServicioException(CodigoError.RECURSO_EN_USO, "El recurso tiene reservas activas.");
        }
        recursos.remove(codigo);
    }

    @Override
    public synchronized RecursoDTO buscarRecurso(String codigo) {
        sesion();
        return existente(recursos, codigo, "recurso");
    }

    @Override
    public synchronized List<RecursoDTO> listarRecursos() {
        sesion();
        return List.copyOf(recursos.values());
    }

    @Override
    public synchronized List<RecursoDTO> listarRecursosPorCategoria(String idCategoria) {
        sesion();
        existente(categorias, idCategoria, "categoría");
        return recursos.values().stream().filter(r -> r.categoria().id().equals(idCategoria)).toList();
    }

    // ── Reservas ─────────────────────────────────────────────────────

    @Override
    public synchronized ResultadoReservaDTO crearReservacion(List<String> idsCategorias, String descripcionActividad,
                                                             LocalDateTime inicio, LocalDateTime fin) {
        exigirFuncionario();
        validarReservacion(idsCategorias, descripcionActividad, inicio, fin);
        List<RecursoDTO> asignados = new ArrayList<>();
        List<CategoriaDTO> noDisponibles = new ArrayList<>();
        for (String idCategoria : idsCategorias) {
            RecursoDTO libre = listarRecursosPorCategoria(idCategoria).stream()
                    .filter(r -> !asignados.contains(r) && !estaOcupado(r, inicio, fin))
                    .findFirst()
                    .orElse(null);
            if (libre == null) {
                noDisponibles.add(categorias.get(idCategoria));
            } else {
                asignados.add(libre);
            }
        }
        if (!noDisponibles.isEmpty()) {
            return ResultadoReservaDTO.fracaso(noDisponibles);
        }
        ReservaDTO reserva = new ReservaDTO(siguienteReserva++, usuarioSesion, asignados, descripcionActividad,
                inicio, fin, EstadoReserva.ACTIVA);
        reservas.add(reserva);
        return ResultadoReservaDTO.exito(reserva);
    }

    @Override
    public synchronized void cancelarReservacion(int id) {
        exigirFuncionario();
        for (int i = 0; i < reservas.size(); i++) {
            ReservaDTO r = reservas.get(i);
            if (r.id() != id) {
                continue;
            }
            if (!r.autor().id().equals(usuarioSesion.id())) {
                throw new ServicioException(CodigoError.PERMISO, "Solo puede cancelar sus propias reservas.");
            }
            if (r.estado() != EstadoReserva.ACTIVA) {
                throw new ServicioException(CodigoError.VALIDACION, "La reservación ya está cancelada.");
            }
            if (!r.inicio().isAfter(LocalDateTime.now())) {
                throw new ServicioException(CodigoError.VALIDACION,
                        "Solo se pueden cancelar reservaciones que aún no han iniciado.");
            }
            reservas.set(i, new ReservaDTO(r.id(), r.autor(), r.recursos(), r.descripcionActividad(),
                    r.inicio(), r.fin(), EstadoReserva.CANCELADA));
            return;
        }
        throw new ServicioException(CodigoError.VALIDACION, "No existe la reservación " + id + ".");
    }

    @Override
    public synchronized List<ReservaDTO> listarReservacionesSesion() {
        String id = sesion().id();
        return reservas.stream().filter(r -> r.autor().id().equals(id)).toList();
    }

    /**
     * Extractor falso: no llama a ninguna IA. Usa la frase como actividad y propone
     * mañana de 9:00 a 10:00 con la primera categoría, para probar el llenado del formulario.
     */
    @Override
    public synchronized ResultadoExtraccionDTO extraerDatosDesdeFrase(String frase) {
        exigirFuncionario();
        if (frase == null || frase.isBlank()) {
            return ResultadoExtraccionDTO.fallo("Escriba una frase para extraer datos.");
        }
        List<String> ids = categorias.isEmpty() ? List.of() : List.of(categorias.keySet().iterator().next());
        return new ResultadoExtraccionDTO(true, frase.trim(), LocalDate.now().plusDays(1),
                LocalTime.of(9, 0), LocalTime.of(10, 0), ids, null);
    }

    // ── Consultas ────────────────────────────────────────────────────

    @Override
    public synchronized MatrizCalendarioDTO obtenerCalendarizacion(LocalDate fecha, String idCategoria) {
        List<RecursoDTO> lista = listarRecursosPorCategoria(idCategoria);
        List<List<String>> celdas = new ArrayList<>();
        for (int hora = HORA_INICIO; hora < HORA_FIN; hora++) {
            LocalDateTime inicioHora = fecha.atTime(hora, 0);
            List<String> fila = new ArrayList<>();
            for (RecursoDTO recurso : lista) {
                fila.add(reservas.stream()
                        .filter(r -> r.estado() == EstadoReserva.ACTIVA && r.recursos().contains(recurso)
                                && seSolapan(r.inicio(), r.fin(), inicioHora, inicioHora.plusHours(1)))
                        .map(r -> r.descripcionActividad() + " - " + r.autor().nombre())
                        .reduce((a, b) -> a + "; " + b)
                        .orElse("Disponible"));
            }
            celdas.add(fila);
        }
        return new MatrizCalendarioDTO(fecha, lista, celdas);
    }

    @Override
    public synchronized AgendaSemanaDTO obtenerAgendaSemana(LocalDate dia) {
        sesion();
        LocalDate lunes = dia.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        Map<DayOfWeek, Map<Integer, List<InfoReservaDTO>>> actividades = new EnumMap<>(DayOfWeek.class);
        for (LocalDate fecha = lunes; fecha.isBefore(lunes.plusDays(7)); fecha = fecha.plusDays(1)) {
            Map<Integer, List<InfoReservaDTO>> horas = new TreeMap<>();
            for (int hora = HORA_INICIO; hora < HORA_FIN; hora++) {
                LocalDateTime inicioHora = fecha.atTime(hora, 0);
                horas.put(hora, reservas.stream()
                        .filter(r -> r.estado() == EstadoReserva.ACTIVA
                                && seSolapan(r.inicio(), r.fin(), inicioHora, inicioHora.plusHours(1)))
                        .map(r -> new InfoReservaDTO(r.inicio(), r.fin(), r.descripcionActividad(),
                                r.autor().nombre(), r.recursos().stream().map(RecursoDTO::descripcion).toList()))
                        .toList());
            }
            actividades.put(fecha.getDayOfWeek(), horas);
        }
        return new AgendaSemanaDTO(lunes, actividades);
    }

    @Override
    public synchronized List<EstadisticaSemanaDTO> contarPorSemana(LocalDate desde, LocalDate hasta) {
        sesion();
        validarRango(desde, hasta);
        List<EstadisticaSemanaDTO> semanas = new ArrayList<>();
        for (LocalDate lunes = desde.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
             !lunes.isAfter(hasta); lunes = lunes.plusWeeks(1)) {
            LocalDate inicio = lunes;
            int cantidad = (int) reservas.stream()
                    .filter(r -> r.estado() == EstadoReserva.ACTIVA)
                    .map(r -> r.inicio().toLocalDate())
                    .filter(f -> !f.isBefore(inicio) && f.isBefore(inicio.plusDays(7)))
                    .count();
            semanas.add(new EstadisticaSemanaDTO(inicio, inicio.plusDays(6), cantidad));
        }
        return semanas;
    }

    @Override
    public synchronized int contarReservasCategoria(String idCategoria, LocalDate desde, LocalDate hasta) {
        sesion();
        validarRango(desde, hasta);
        return contar(idCategoria, desde, hasta);
    }

    @Override
    public synchronized List<EstadisticaCategoriaDTO> contarPorCategoria(LocalDate desde, LocalDate hasta) {
        sesion();
        validarRango(desde, hasta);
        return categorias.values().stream()
                .map(c -> new EstadisticaCategoriaDTO(c, contar(c.id(), desde, hasta)))
                .toList();
    }

    // ── Reglas auxiliares ────────────────────────────────────────────

    private int contar(String idCategoria, LocalDate desde, LocalDate hasta) {
        return (int) reservas.stream()
                .filter(r -> r.estado() == EstadoReserva.ACTIVA)
                .filter(r -> !r.inicio().toLocalDate().isBefore(desde) && !r.inicio().toLocalDate().isAfter(hasta))
                .flatMap(r -> r.recursos().stream())
                .filter(x -> x.categoria().id().equals(idCategoria))
                .count();
    }

    private boolean estaOcupado(RecursoDTO recurso, LocalDateTime inicio, LocalDateTime fin) {
        return reservas.stream().anyMatch(r -> r.estado() == EstadoReserva.ACTIVA
                && r.recursos().contains(recurso) && seSolapan(r.inicio(), r.fin(), inicio, fin));
    }

    private static boolean seSolapan(LocalDateTime i1, LocalDateTime f1, LocalDateTime i2, LocalDateTime f2) {
        return i1.isBefore(f2) && i2.isBefore(f1);
    }

    private static void validarReservacion(List<String> idsCategorias, String descripcion,
                                           LocalDateTime inicio, LocalDateTime fin) {
        if (idsCategorias == null || idsCategorias.isEmpty()) {
            throw new ServicioException(CodigoError.VALIDACION, "Seleccione al menos una categoría.");
        }
        if (descripcion == null || descripcion.isBlank()) {
            throw new ServicioException(CodigoError.VALIDACION, "La descripción de la actividad es obligatoria.");
        }
        if (inicio == null || fin == null) {
            throw new ServicioException(CodigoError.VALIDACION, "La fecha y las horas son obligatorias.");
        }
        if (!fin.isAfter(inicio)) {
            throw new ServicioException(CodigoError.VALIDACION, "La hora de fin debe ser posterior a la de inicio.");
        }
        if (inicio.isBefore(LocalDateTime.now())) {
            throw new ServicioException(CodigoError.VALIDACION, "No se pueden reservar horarios en el pasado.");
        }
        if (!inicio.toLocalDate().equals(fin.toLocalDate()) || inicio.getHour() < HORA_INICIO
                || fin.isAfter(fin.toLocalDate().atTime(HORA_FIN, 0))) {
            throw new ServicioException(CodigoError.VALIDACION,
                    "El horario debe estar entre las " + HORA_INICIO + ":00 y las " + HORA_FIN + ":00 del mismo día.");
        }
    }

    private static void validarRango(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null || desde.isAfter(hasta)) {
            throw new ServicioException(CodigoError.VALIDACION, "El rango de fechas no es válido.");
        }
    }

    /** Igual que el Despachador: con el primer ingreso pendiente solo se cambia la clave o se sale. */
    private UsuarioDTO sesion() {
        UsuarioDTO usuario = sesionSinRestriccion();
        if (primerIngreso.contains(usuario.id())) {
            throw new ServicioException(CodigoError.SESION, Reglas.MENSAJE_CAMBIO_CLAVE_PENDIENTE);
        }
        return usuario;
    }

    private UsuarioDTO sesionSinRestriccion() {
        llamadas++;
        if (usuarioSesion == null) {
            throw new ServicioException(CodigoError.SESION, "Debe iniciar sesión.");
        }
        return usuarioSesion;
    }

    private static void validarTelefono(FuncionarioDTO funcionario) {
        if (funcionario == null || !Reglas.telefonoValido(funcionario.telefono())) {
            throw new ServicioException(CodigoError.VALIDACION, Reglas.MENSAJE_TELEFONO);
        }
    }

    private void exigirAdministrador() {
        if (!sesion().esAdministrador()) {
            throw new ServicioException(CodigoError.PERMISO, "Operación permitida solo para el administrador.");
        }
    }

    private void exigirFuncionario() {
        if (sesion().esAdministrador()) {
            throw new ServicioException(CodigoError.PERMISO, "Operación permitida solo para funcionarios.");
        }
    }

    private static <T> T existente(Map<String, T> mapa, String id, String nombre) {
        T valor = id == null ? null : mapa.get(id);
        if (valor == null) {
            throw new ServicioException(CodigoError.VALIDACION, "No existe " + nombre + " con identificador " + id + ".");
        }
        return valor;
    }
}
