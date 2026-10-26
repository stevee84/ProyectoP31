package servicio;

import accesodatos.ProveedorConexion;
import contrato.IServicioAplicacion;
import dto.*;
import ia.ExtractorReservaIA;
import protocolo.CodigoError;
import protocolo.ServicioException;
import repositorio.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.*;
import java.util.*;
import static servicio.ErroresOracle.Accion;

/** Reglas del Backend. Solo lee la sesión; el Despachador sigue siendo su dueño. */
public final class ServicioAplicacion implements IServicioAplicacion {
    private final ContextoSesion contexto;
    private final ProveedorConexion conexiones;
    private final FabricaRepositoriosOracle repositorios;
    private final ExtractorReservaIA extractor;
    private final Clock reloj;
    private final Object bloqueo;

    ServicioAplicacion(ContextoSesion contexto, ProveedorConexion conexiones, FabricaRepositoriosOracle repositorios,
                       ExtractorReservaIA extractor, Clock reloj, Object bloqueo) {
        this.contexto = Objects.requireNonNull(contexto);
        this.conexiones = Objects.requireNonNull(conexiones);
        this.repositorios = Objects.requireNonNull(repositorios);
        this.extractor = Objects.requireNonNull(extractor);
        this.reloj = Objects.requireNonNull(reloj);
        this.bloqueo = Objects.requireNonNull(bloqueo);
    }

    @Override
    public ResultadoSesionDTO iniciarSesion(String id, String clave) {
        if (id == null || id.isBlank() || id.trim().length() > 50 || clave == null || clave.isBlank() || clave.length() > 255)
            throw credencialesIncorrectas();
        ResultadoSesionDTO resultado = operar(Accion.CONSULTA, c -> repositorios.funcionarios(c).autenticar(id.trim(), clave));
        if (resultado == null) throw credencialesIncorrectas();
        return resultado;
    }

    @Override
    public void cerrarSesion() {
        sesion(); // El Despachador cerrará el contexto después de este retorno.
    }

    @Override
    public void cambiarClave(String nuevaClave) {
        UsuarioDTO usuario = sesion();
        texto(nuevaClave, 255, "La nueva clave");
        operar(Accion.ESCRITURA, c -> { repositorios.funcionarios(c).cambiarClave(usuario.id(), nuevaClave); return null; });
    }

    @Override
    public void registrarFuncionario(FuncionarioDTO funcionario) {
        administrador();
        FuncionarioDTO f = validarFuncionario(funcionario);
        operar(Accion.ESCRITURA, c -> { repositorios.funcionarios(c).insertar(f); return null; });
    }

    @Override
    public void actualizarFuncionario(FuncionarioDTO funcionario) {
        administrador();
        FuncionarioDTO f = validarFuncionario(funcionario);
        operar(Accion.ESCRITURA, c -> { repositorios.funcionarios(c).modificar(f); return null; });
    }

    @Override
    public void eliminarFuncionario(String id) {
        administrador();
        String identificacion = texto(id, 50, "La identificación");
        operar(Accion.ELIMINACION, c -> { repositorios.funcionarios(c).eliminar(identificacion); return null; });
    }

    @Override
    public FuncionarioDTO buscarFuncionario(String id) {
        administrador();
        String identificacion = texto(id, 50, "La identificación");
        return operar(Accion.CONSULTA, c -> existente(repositorios.funcionarios(c).buscar(identificacion), "funcionario"));
    }

    @Override
    public List<FuncionarioDTO> listarFuncionarios() {
        administrador();
        return operar(Accion.CONSULTA, c -> repositorios.funcionarios(c).listar());
    }

    @Override
    public CategoriaDTO registrarCategoria(String descripcion) {
        administrador();
        String detalle = texto(descripcion, 200, "La descripción");
        return operar(Accion.ESCRITURA, c -> repositorios.categorias(c).insertar(detalle));
    }

    @Override
    public void actualizarCategoria(CategoriaDTO categoria) {
        administrador();
        if (categoria == null) throw validacion("Debe indicar la categoría.");
        CategoriaDTO categoriaValida = new CategoriaDTO(texto(categoria.id(), 20, "La categoría"),
                texto(categoria.descripcion(), 200, "La descripción"));
        operar(Accion.ESCRITURA, c -> { repositorios.categorias(c).modificar(categoriaValida); return null; });
    }

    @Override
    public void eliminarCategoria(String id) {
        administrador();
        String categoria = texto(id, 20, "La categoría");
        operar(Accion.ELIMINACION, c -> { repositorios.categorias(c).eliminar(categoria); return null; });
    }

    @Override
    public CategoriaDTO buscarCategoria(String id) {
        sesion();
        String categoria = texto(id, 20, "La categoría");
        return operar(Accion.CONSULTA, c -> categoria(c, categoria));
    }

    @Override
    public List<CategoriaDTO> listarCategorias() {
        sesion();
        return operar(Accion.CONSULTA, c -> repositorios.categorias(c).listar());
    }

    @Override
    public void registrarRecurso(String codigo, String idCategoria, String descripcion) {
        guardarRecurso(codigo, idCategoria, descripcion, false);
    }

    @Override
    public void actualizarRecurso(String codigo, String idCategoria, String descripcion) {
        guardarRecurso(codigo, idCategoria, descripcion, true);
    }

    private void guardarRecurso(String codigo, String idCategoria, String descripcion, boolean modificar) {
        administrador();
        String cod = texto(codigo, 50, "El código");
        String cat = texto(idCategoria, 20, "La categoría");
        String detalle = texto(descripcion, 200, "La descripción");
        operar(Accion.ESCRITURA, c -> {
            categoria(c, cat);
            if (modificar) repositorios.recursos(c).modificar(cod, cat, detalle);
            else repositorios.recursos(c).insertar(cod, cat, detalle);
            return null;
        });
    }

    @Override
    public void eliminarRecurso(String codigo) {
        administrador();
        String cod = texto(codigo, 50, "El código");
        operar(Accion.ELIMINACION, c -> { repositorios.recursos(c).eliminar(cod); return null; });
    }

    @Override
    public RecursoDTO buscarRecurso(String codigo) {
        sesion();
        String cod = texto(codigo, 50, "El código");
        return operar(Accion.CONSULTA, c -> existente(repositorios.recursos(c).buscar(cod), "recurso"));
    }

    @Override
    public List<RecursoDTO> listarRecursos() {
        sesion();
        return operar(Accion.CONSULTA, c -> repositorios.recursos(c).listar());
    }

    @Override
    public List<RecursoDTO> listarRecursosPorCategoria(String idCategoria) {
        sesion();
        String cat = texto(idCategoria, 20, "La categoría");
        return operar(Accion.CONSULTA, c -> { categoria(c, cat); return repositorios.recursos(c).listarPorCategoria(cat); });
    }

    @Override
    public ResultadoReservaDTO crearReservacion(List<String> idsCategorias, String descripcionActividad,
                                                LocalDateTime inicio, LocalDateTime fin) {
        UsuarioDTO autor = funcionario();
        String descripcion = texto(descripcionActividad, 500, "La actividad");
        if (idsCategorias == null || idsCategorias.isEmpty()) throw validacion("Seleccione al menos una categoría.");
        List<String> categorias = idsCategorias.stream().map(id -> texto(id, 20, "La categoría")).toList();
        validarHorario(inicio, fin);
        // operar conserva el bloqueo COMPARTIDO hasta después del commit o rollback.
        return operar(Accion.RESERVA, c -> {
            Map<String, CategoriaDTO> conocidas = new HashMap<>();
            for (String id : categorias) {
                if (!conocidas.containsKey(id)) conocidas.put(id, categoria(c, id));
            }
            Map<String, List<RecursoDTO>> libres = new HashMap<>();
            for (String id : conocidas.keySet()) libres.put(id, repositorios.recursos(c).disponibles(id, inicio, fin));
            Set<String> seleccionados = new HashSet<>();
            List<RecursoDTO> asignados = new ArrayList<>();
            List<CategoriaDTO> faltantes = new ArrayList<>();
            for (String id : categorias) {
                RecursoDTO libre = libres.get(id).stream().filter(r -> !seleccionados.contains(r.codigo())).findFirst().orElse(null);
                if (libre == null) faltantes.add(conocidas.get(id));
                else { seleccionados.add(libre.codigo()); asignados.add(libre); }
            }
            // Aún no se insertó nada: falta normal, incluso con categorías repetidas.
            if (!faltantes.isEmpty()) return ResultadoReservaDTO.fracaso(faltantes);
            IRepositorioReservas reservas = repositorios.reservas(c);
            int id = reservas.insertar(autor.id(), descripcion, inicio, fin);
            // Orden global estable para los SELECT FOR UPDATE de Oracle.
            asignados.sort(Comparator.comparing(RecursoDTO::codigo));
            for (RecursoDTO recurso : asignados) reservas.asignar(id, recurso.codigo());
            return ResultadoReservaDTO.exito(existente(reservas.buscar(id), "reserva"));
        });
    }

    @Override
    public void cancelarReservacion(int id) {
        UsuarioDTO autor = funcionario();
        if (id <= 0) throw validacion("El identificador de reserva no es válido.");
        operar(Accion.CANCELACION, c -> {
            IRepositorioReservas reservas = repositorios.reservas(c);
            ReservaDTO reserva = existente(reservas.buscar(id), "reserva");
            if (!reserva.autor().id().equals(autor.id()))
                throw new ServicioException(CodigoError.PERMISO, "Solo puede cancelar sus propias reservas.");
            if (reserva.estado() != EstadoReserva.ACTIVA || !reserva.inicio().isAfter(LocalDateTime.now(reloj)))
                throw validacion("Solo se cancelan reservas activas que aún no hayan iniciado.");
            reservas.cancelar(id, autor.id()); // Revalida bajo bloqueo de fila en Oracle.
            return null;
        });
    }

    @Override
    public List<ReservaDTO> listarReservacionesSesion() {
        UsuarioDTO usuario = sesion();
        return operar(Accion.CONSULTA, c -> repositorios.reservas(c).listarUsuario(usuario.id()));
    }

    @Override
    public ResultadoExtraccionDTO extraerDatosDesdeFrase(String frase) {
        funcionario();
        String texto = texto(frase, 4000, "La frase");
        List<CategoriaDTO> categorias = operar(Accion.CONSULTA, c -> repositorios.categorias(c).listar());
        // Conexión cerrada y bloqueo liberado ANTES de usar Internet.
        ResultadoExtraccionDTO resultado = extractor.extraer(texto, categorias);
        if (resultado == null) return ResultadoExtraccionDTO.fallo("No se pudo interpretar la frase.");
        if (resultado.exito() && resultado.idsCategorias().stream()
                .anyMatch(id -> categorias.stream().noneMatch(cat -> cat.id().equals(id))))
            return ResultadoExtraccionDTO.fallo("La IA devolvió una categoría desconocida. Complete el formulario manualmente.");
        return resultado;
    }

    @Override
    public MatrizCalendarioDTO obtenerCalendarizacion(LocalDate fecha, String idCategoria) {
        sesion();
        validarFecha(fecha);
        String id = texto(idCategoria, 20, "La categoría");
        return operar(Accion.CONSULTA, c -> {
            categoria(c, id);
            List<RecursoDTO> recursos = repositorios.recursos(c).listarPorCategoria(id);
            List<ReservaDTO> reservas = repositorios.reservas(c).listarEnRango(fecha.atStartOfDay(), fecha.plusDays(1).atStartOfDay());
            return ConsultasReservas.calendario(fecha, recursos, reservas);
        });
    }

    @Override
    public AgendaSemanaDTO obtenerAgendaSemana(LocalDate dia) {
        sesion();
        validarFecha(dia);
        LocalDate lunes = dia.with(DayOfWeek.MONDAY);
        return operar(Accion.CONSULTA, c -> ConsultasReservas.agenda(lunes,
                repositorios.reservas(c).listarEnRango(lunes.atStartOfDay(), lunes.plusWeeks(1).atStartOfDay())));
    }

    @Override
    public List<EstadisticaSemanaDTO> contarPorSemana(LocalDate desde, LocalDate hasta) {
        sesion();
        validarRango(desde, hasta);
        return operar(Accion.CONSULTA, c -> ConsultasReservas.completarSemanas(desde, hasta,
                repositorios.consultas(c).contarPorSemana(desde, hasta)));
    }

    @Override
    public int contarReservasCategoria(String idCategoria, LocalDate desde, LocalDate hasta) {
        sesion();
        validarRango(desde, hasta);
        String id = texto(idCategoria, 20, "La categoría");
        return operar(Accion.CONSULTA, c -> {
            categoria(c, id);
            return repositorios.consultas(c).contarPorCategoria(desde, hasta).stream()
                    .filter(e -> e.categoria().id().equals(id)).mapToInt(EstadisticaCategoriaDTO::cantidad).findFirst().orElse(0);
        });
    }

    @Override
    public List<EstadisticaCategoriaDTO> contarPorCategoria(LocalDate desde, LocalDate hasta) {
        sesion();
        validarRango(desde, hasta);
        return operar(Accion.CONSULTA, c -> repositorios.consultas(c).contarPorCategoria(desde, hasta));
    }

    private UsuarioDTO sesion() {
        UsuarioDTO usuario = contexto.getUsuario();
        if (usuario == null) throw new ServicioException(CodigoError.SESION, "Debe iniciar sesión.");
        return usuario;
    }

    private void administrador() {
        if (!sesion().esAdministrador()) throw new ServicioException(CodigoError.PERMISO, "Solo el administrador puede realizar esta acción.");
    }

    private UsuarioDTO funcionario() {
        UsuarioDTO usuario = sesion();
        if (usuario.rol() != Rol.FUNCIONARIO) throw new ServicioException(CodigoError.PERMISO, "Solo los funcionarios pueden gestionar reservas.");
        return usuario;
    }

    private static FuncionarioDTO validarFuncionario(FuncionarioDTO f) {
        if (f == null) throw validacion("Debe indicar el funcionario.");
        return new FuncionarioDTO(texto(f.id(), 50, "La identificación"), texto(f.nombre(), 150, "El nombre"),
                texto(f.telefono(), 30, "El teléfono"));
    }

    private static String texto(String valor, int maximo, String campo) {
        if (valor == null || valor.isBlank()) throw validacion(campo + (campo.startsWith("La ") ? " es obligatoria." : " es obligatorio."));
        if (valor.length() > maximo) throw validacion(campo + " admite como máximo " + maximo + " caracteres.");
        return valor.trim();
    }

    private void validarHorario(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null || fin == null) throw validacion("Debe indicar inicio y fin.");
        validarFecha(inicio.toLocalDate());
        validarFecha(fin.toLocalDate());
        if (!fin.isAfter(inicio)) throw validacion("El fin debe ser posterior al inicio.");
        if (!inicio.toLocalDate().equals(fin.toLocalDate()) || inicio.toLocalTime().isBefore(LocalTime.of(HORA_INICIO, 0))
                || fin.toLocalTime().isAfter(LocalTime.of(HORA_FIN, 0)))
            throw validacion("La reserva debe ser de un mismo día, entre las 07:00 y las 18:00.");
        if (inicio.isBefore(LocalDateTime.now(reloj))) throw validacion("No se pueden crear reservas en el pasado.");
    }

    private static void validarFecha(LocalDate fecha) {
        if (fecha == null || fecha.getYear() < 1 || fecha.getYear() > 9998)
            throw validacion("Debe indicar una fecha válida entre los años 1 y 9998.");
    }

    private static void validarRango(LocalDate desde, LocalDate hasta) {
        validarFecha(desde);
        validarFecha(hasta);
        if (desde.isAfter(hasta)) throw validacion("La fecha inicial no puede ser posterior a la final.");
    }

    private CategoriaDTO categoria(Connection c, String id) throws SQLException {
        return existente(repositorios.categorias(c).buscar(id), "categoría");
    }

    private static <T> T existente(T valor, String tipo) {
        if (valor == null) throw validacion("No existe el registro de " + tipo + ".");
        return valor;
    }

    private static ServicioException validacion(String mensaje) { return new ServicioException(CodigoError.VALIDACION, mensaje); }
    private static ServicioException credencialesIncorrectas() { return new ServicioException(CodigoError.SESION, "Usuario o contraseña incorrectos."); }

    @FunctionalInterface
    private interface OperacionBD<T> { T ejecutar(Connection conexion) throws SQLException; }

    private <T> T operar(Accion accion, OperacionBD<T> operacion) {
        synchronized (bloqueo) {
            boolean confirmada = false;
            T resultado = null;
            // Cada operación abre y cierra su conexión. Nunca se comparte entre clientes.
            try (Connection c = conexiones.abrir()) {
                c.setAutoCommit(false);
                try {
                    resultado = operacion.ejecutar(c);
                    c.commit();
                    confirmada = true;
                } catch (SQLException | RuntimeException e) {
                    try {
                        c.rollback();
                    } catch (SQLException rollback) {
                        // Si tampoco se puede revertir, no anunciar solo un error de validación.
                        throw ErroresOracle.traducir(rollback, Accion.CONSULTA);
                    }
                    if (e instanceof SQLException sql) throw ErroresOracle.traducir(sql, accion);
                    throw e;
                }
                return resultado;
            } catch (SQLException e) {
                // No anunciar fracaso por un fallo de close DESPUÉS de un commit exitoso.
                if (confirmada) { ErroresOracle.registrar(e); return resultado; }
                throw ErroresOracle.traducir(e, Accion.CONSULTA);
            }
        }
    }
}
