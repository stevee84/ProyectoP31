package model;

import exception.DisponibilidadException;
import exception.RecursoEnUsoException;
import exception.ValidacionException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ModeloReservaciones {

    private final Map<String, Empleado> empleados = new LinkedHashMap<>();
    private final Map<String, CategoriaRecurso> categorias = new LinkedHashMap<>();
    private final Map<String, Recurso> recursos = new LinkedHashMap<>();
    private final List<Reservacion> reservaciones = new ArrayList<>();
    private int siguienteIdReservacion = 1;
    private int siguienteIdCategoria = 1;

    public ModeloReservaciones() {
        inicializarEmpleados();
        inicializarCategorias();
        inicializarRecursosYReservas();
    }

    /**
     * Constructor para persistencia: crea un modelo vacío sin datos iniciales.
     * Usar {@code true} para indicar que se cargará desde XML.
     */
    public ModeloReservaciones(boolean vacio) {
        // No inicializa datos por defecto
    }

    private void inicializarEmpleados() {
        registrarEmpleado(new Administrador("Administrador", "ADMIN"));
        registrarEmpleado(new Funcionario("Steve Moya", "SMOYA", "8888-1111"));
        registrarEmpleado(new Funcionario("Josua Pérez", "JPEREZ", "8888-2222"));
        registrarEmpleado(new Funcionario("Scott Ramírez", "SRAMIREZ", "8888-3333"));
        registrarEmpleado(new Funcionario("María López", "MLOPEZ", "8888-4444"));
        registrarEmpleado(new Funcionario("Carlos Ureña", "CURENA", "8888-5555"));
    }

    private void inicializarCategorias() {
        registrarCategoria("Sala para 10 personas");   // CAT-000001
        registrarCategoria("Laptop Windows");           // CAT-000002
        registrarCategoria("Sala de juntas");           // CAT-000003
        registrarCategoria("Proyector");                // CAT-000004
        registrarCategoria("Laboratorio de cómputo");   // CAT-000005
    }

    private void inicializarRecursosYReservas() {
        // --- Recursos ---
        registrarRecurso("SALA-101", "CAT-000001", "Sala 101 - Edificio A");
        registrarRecurso("SALA-102", "CAT-000001", "Sala 102 - Edificio A");
        registrarRecurso("SALA-201", "CAT-000001", "Sala 201 - Edificio B");
        registrarRecurso("LAP-001", "CAT-000002", "Laptop Dell Latitude #1");
        registrarRecurso("LAP-002", "CAT-000002", "Laptop Dell Latitude #2");
        registrarRecurso("LAP-003", "CAT-000002", "Laptop HP ProBook #3");
        registrarRecurso("JUNTAS-A", "CAT-000003", "Sala de juntas - Piso 1");
        registrarRecurso("JUNTAS-B", "CAT-000003", "Sala de juntas - Piso 2");
        registrarRecurso("PROY-01", "CAT-000004", "Proyector Epson #1");
        registrarRecurso("PROY-02", "CAT-000004", "Proyector Epson #2");
        registrarRecurso("LAB-A", "CAT-000005", "Laboratorio A - 30 PCs");
        registrarRecurso("LAB-B", "CAT-000005", "Laboratorio B - 25 PCs");

        // --- Reservaciones de ejemplo (futuras para que sean válidas) ---
        LocalDate manana = LocalDate.now().plusDays(1);
        LocalDate pasado = LocalDate.now().plusDays(2);
        LocalDate enTres = LocalDate.now().plusDays(3);

        Empleado steve = buscarEmpleado("SMOYA");
        Empleado josua = buscarEmpleado("JPEREZ");
        Empleado scott = buscarEmpleado("SRAMIREZ");
        Empleado maria = buscarEmpleado("MLOPEZ");

        // Steve reserva Sala 101 mañana 8-10am
        crearReservaQuemada(steve, List.of(buscarRecurso("SALA-101")),
                "Reunión de proyecto EIF206",
                manana.atTime(8, 0), manana.atTime(10, 0));

        // Steve reserva Laptop mañana 10-12
        crearReservaQuemada(steve, List.of(buscarRecurso("LAP-001")),
                "Desarrollo de módulo de reservas",
                manana.atTime(10, 0), manana.atTime(12, 0));

        // Josua reserva Sala de juntas pasado mañana 9-11
        crearReservaQuemada(josua, List.of(buscarRecurso("JUNTAS-A")),
                "Presentación avance del proyecto",
                pasado.atTime(9, 0), pasado.atTime(11, 0));

        // Josua reserva Lab A pasado mañana 13-15
        crearReservaQuemada(josua, List.of(buscarRecurso("LAB-A")),
                "Práctica de laboratorio",
                pasado.atTime(13, 0), pasado.atTime(15, 0));

        // Scott reserva Proyector + Sala 201 en 3 días 14-16
        crearReservaQuemada(scott, List.of(buscarRecurso("PROY-01"), buscarRecurso("SALA-201")),
                "Capacitación sobre API REST",
                enTres.atTime(14, 0), enTres.atTime(16, 0));

        // María reserva Lab B mañana 8-10
        crearReservaQuemada(maria, List.of(buscarRecurso("LAB-B")),
                "Taller de programación Java",
                manana.atTime(8, 0), manana.atTime(10, 0));

        // María reserva Sala 102 en 3 días 10-12
        crearReservaQuemada(maria, List.of(buscarRecurso("SALA-102")),
                "Reunión con tutor",
                enTres.atTime(10, 0), enTres.atTime(12, 0));
    }

    private void crearReservaQuemada(Empleado empleado, List<Recurso> recursos,
                                      String descripcion, LocalDateTime inicio, LocalDateTime fin) {
        Reservacion r = new Reservacion(siguienteIdReservacion++, empleado, recursos, descripcion, inicio, fin);
        reservaciones.add(r);
    }

    public CategoriaRecurso registrarCategoria(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            throw new ValidacionException("La descripción de la categoría es obligatoria.");
        }
        CategoriaRecurso categoria = new CategoriaRecurso(generarIdCategoria(), descripcion);
        categorias.put(categoria.getId(), categoria);
        return categoria;
    }

    private String generarIdCategoria() {
        return String.format("CAT-%06d", siguienteIdCategoria++);
    }

    public boolean actualizarCategoria(String id, String descripcion) {
        CategoriaRecurso categoria = buscarCategoria(id);
        if (categoria == null) {
            return false;
        }
        categoria.actualizarDatos(id, descripcion);
        return true;
    }

    public boolean eliminarCategoria(String id) {
        CategoriaRecurso categoria = buscarCategoria(id);
        if (categoria == null) {
            return false;
        }
        boolean enUso = recursos.values().stream()
                .anyMatch(r -> r.getCategoria().equals(categoria));
        if (enUso) {
            throw new RecursoEnUsoException("No se puede eliminar la categoría porque tiene recursos asociados.");
        }
        return categorias.remove(id) != null;
    }

    public CategoriaRecurso buscarCategoria(String id) {
        return categorias.get(id);
    }

    public List<CategoriaRecurso> listarCategorias() {
        List<CategoriaRecurso> lista = new ArrayList<>(categorias.values());
        Collections.sort(lista);
        return lista;
    }

    public List<CategoriaRecurso> buscarCategoriasPorDescripcion(String texto) {
        String termino = texto == null ? "" : texto.toLowerCase();
        return categorias.values().stream()
                .filter(c -> c.getDescripcion().toLowerCase().contains(termino))
                .sorted()
                .collect(Collectors.toList());
    }

    public boolean registrarRecurso(String codigo, String idCategoria, String descripcion) {
        CategoriaRecurso categoria = buscarOCategoriaInvalida(idCategoria);
        if (codigo == null || codigo.isBlank()) {
            throw new ValidacionException("El código del recurso es obligatorio.");
        }
        if (recursos.containsKey(codigo.trim())) {
            return false;
        }
        recursos.put(codigo.trim(), new Recurso(codigo, categoria, descripcion));
        return true;
    }

    public boolean actualizarRecurso(String codigo, String idCategoria, String descripcion) {
        Recurso recurso = buscarRecurso(codigo);
        if (recurso == null) {
            return false;
        }
        CategoriaRecurso categoria = buscarOCategoriaInvalida(idCategoria);
        recurso.actualizarDatos(codigo, categoria, descripcion);
        return true;
    }

    public boolean eliminarRecurso(String codigo) {
        Recurso recurso = buscarRecurso(codigo);
        if (recurso == null) {
            return false;
        }
        boolean enUso = reservaciones.stream()
                .anyMatch(r -> r.esActiva() && r.incluyeRecurso(recurso));
        if (enUso) {
            throw new RecursoEnUsoException("No se puede eliminar el recurso porque tiene reservaciones activas.");
        }
        return recursos.remove(codigo) != null;
    }

    public Recurso buscarRecurso(String codigo) {
        return recursos.get(codigo);
    }

    public List<Recurso> listarRecursos() {
        List<Recurso> lista = new ArrayList<>(recursos.values());
        Collections.sort(lista);
        return lista;
    }

    public List<Recurso> listarRecursosPorCategoria(String idCategoria) {
        CategoriaRecurso categoria = buscarOCategoriaInvalida(idCategoria);
        return recursos.values().stream()
                .filter(r -> r.getCategoria().equals(categoria))
                .sorted()
                .collect(Collectors.toList());
    }

    public boolean registrarEmpleado(Empleado empleado) {
        if (empleado == null) {
            throw new ValidacionException("El empleado no puede ser nulo.");
        }
        if (empleados.containsKey(empleado.getId())) {
            return false;
        }
        empleados.put(empleado.getId(), empleado);
        return true;
    }

    public boolean actualizarEmpleado(Empleado empleado) {
        if (empleado == null) {
            throw new ValidacionException("El empleado no puede ser nulo.");
        }
        if (!empleados.containsKey(empleado.getId())) {
            return false;
        }
        empleados.put(empleado.getId(), empleado);
        return true;
    }

    public boolean eliminarEmpleado(String id) {
        Empleado empleado = buscarEmpleado(id);
        if (empleado == null) {
            return false;
        }
        boolean enUso = reservaciones.stream()
                .anyMatch(r -> r.esActiva() && r.getEmpleado().equals(empleado));
        if (enUso) {
            throw new RecursoEnUsoException("No se puede eliminar el empleado porque tiene reservaciones activas.");
        }
        return empleados.remove(id) != null;
    }

    public Empleado buscarEmpleado(String id) {
        return empleados.get(id);
    }

    public List<Empleado> listarEmpleados() {
        List<Empleado> lista = new ArrayList<>(empleados.values());
        Collections.sort(lista);
        return lista;
    }

    public List<Funcionario> listarFuncionarios() {
        return empleados.values().stream()
                .filter(Funcionario.class::isInstance)
                .map(Funcionario.class::cast)
                .sorted()
                .collect(Collectors.toList());
    }

    public List<Funcionario> buscarFuncionariosPorTexto(String texto) {
        String termino = texto == null ? "" : texto.toLowerCase();
        return listarFuncionarios().stream()
                .filter(f -> f.getId().toLowerCase().contains(termino)
                        || f.getName().toLowerCase().contains(termino))
                .collect(Collectors.toList());
    }

    public List<Administrador> listarAdministradores() {
        return empleados.values().stream()
                .filter(Administrador.class::isInstance)
                .map(Administrador.class::cast)
                .sorted()
                .collect(Collectors.toList());
    }

    public Empleado validarCredenciales(String id, String pass) {
        Empleado empleado = buscarEmpleado(id);
        if (empleado != null && empleado.verificarContraseña(pass)) {
            return empleado;
        }
        return null;
    }

    public ResultadoReserva reservarPorCategorias(SolicitudReserva solicitud) {
        Empleado empleado = solicitud.empleado();
        List<String> idsCategorias = solicitud.idsCategorias();
        String descripcionActividad = solicitud.descripcionActividad();
        LocalDateTime inicio = solicitud.inicio();
        LocalDateTime fin = solicitud.fin();

        // Fase 1 (solo lectura): resolver TODOS los recursos candidatos antes de mutar nada.
        List<Recurso> recursosAsignados = new ArrayList<>();
        List<CategoriaRecurso> categoriasNoDisponibles = new ArrayList<>();
        Set<String> categoriasVistas = new HashSet<>();
        for (String idCategoria : idsCategorias) {
            if (!categoriasVistas.add(idCategoria)) {
                continue; // categoría repetida en la selección: se ignora
            }
            CategoriaRecurso categoria = buscarOCategoriaInvalida(idCategoria);
            Recurso disponible = primerRecursoDisponible(categoria, inicio, fin, recursosAsignados);
            if (disponible == null) {
                categoriasNoDisponibles.add(categoria);
            } else {
                recursosAsignados.add(disponible);
            }
        }

        if (!categoriasNoDisponibles.isEmpty()) {
            return ResultadoReserva.fracaso(categoriasNoDisponibles);
        }

        // Fase 2 (mutación): recién aquí se agrega la reservación, una sola vez.
        Reservacion reservacion = new Reservacion(siguienteIdReservacion++, empleado, recursosAsignados,
                descripcionActividad, inicio, fin);
        reservaciones.add(reservacion);
        return ResultadoReserva.exito(reservacion);
    }

    private Recurso primerRecursoDisponible(CategoriaRecurso categoria, LocalDateTime inicio, LocalDateTime fin,
                                             List<Recurso> yaAsignados) {
        return listarRecursosPorCategoria(categoria.getId()).stream()
                .filter(r -> !yaAsignados.contains(r) && !estaOcupado(r, inicio, fin))
                .findFirst()
                .orElse(null);
    }

    public boolean actualizarReservacion(Reservacion reservacion) {
        if (reservacion == null) {
            throw new ValidacionException("La reservación no puede ser nula.");
        }
        int indice = reservaciones.indexOf(reservacion);
        if (indice < 0) {
            return false;
        }
        reservaciones.set(indice, reservacion);
        return true;
    }

    public boolean eliminarReservacion(int id) {
        return reservaciones.removeIf(r -> r.getId() == id);
    }

    public Reservacion buscarReservacion(int id) {
        for (Reservacion reservacion : reservaciones) {
            if (reservacion.getId() == id) {
                return reservacion;
            }
        }
        return null;
    }

    public void cancelarReservacion(int id) {
        Reservacion reservacion = buscarReservacion(id);
        if (reservacion == null) {
            throw new ValidacionException("No existe una reservación con id " + id + ".");
        }
        reservacion.cancelar();
    }

    public List<Reservacion> listarReservaciones() {
        List<Reservacion> lista = new ArrayList<>(reservaciones);
        Collections.sort(lista);
        return lista;
    }

    public List<Reservacion> listarReservacionesActivas() {
        return reservaciones.stream()
                .filter(Reservacion::esActiva)
                .sorted()
                .collect(Collectors.toList());
    }

    public List<Reservacion> listarReservacionesPorEstado(EstadoReservacion estado) {
        return reservaciones.stream()
                .filter(r -> r.getEstado() == estado)
                .sorted()
                .collect(Collectors.toList());
    }

    public List<Reservacion> listarReservacionesPorEmpleado(Empleado empleado) {
        return reservaciones.stream()
                .filter(r -> r.getEmpleado().equals(empleado))
                .sorted()
                .collect(Collectors.toList());
    }

    public List<Reservacion> listarReservacionesPorRecurso(Recurso recurso) {
        return reservaciones.stream()
                .filter(r -> r.incluyeRecurso(recurso))
                .sorted()
                .collect(Collectors.toList());
    }

    public List<Reservacion> listarReservacionesPorCategoria(String idCategoria) {
        CategoriaRecurso categoria = buscarOCategoriaInvalida(idCategoria);
        return reservaciones.stream()
                .filter(r -> r.getRecursos().stream().anyMatch(rec -> rec.getCategoria().equals(categoria)))
                .sorted()
                .collect(Collectors.toList());
    }

    public List<Reservacion> listarReservacionesEnFecha(LocalDate fecha) {
        return reservaciones.stream()
                .filter(r -> r.getInicio().toLocalDate().equals(fecha))
                .sorted()
                .collect(Collectors.toList());
    }

    public List<Reservacion> listarReservacionesEnRango(LocalDateTime inicio, LocalDateTime fin) {
        return reservaciones.stream()
                .filter(r -> r.getInicio().isBefore(fin) && inicio.isBefore(r.getFin()))
                .sorted()
                .collect(Collectors.toList());
    }

    public boolean verificarDisponibilidad(Recurso recurso, LocalDateTime inicio, LocalDateTime fin) {
        if (recurso == null) {
            throw new ValidacionException("El recurso es obligatorio.");
        }
        if (inicio == null || fin == null) {
            throw new ValidacionException("La fecha de inicio y de terminación son obligatorias.");
        }
        if (!inicio.isBefore(fin)) {
            throw new ValidacionException("La hora de inicio debe ser anterior a la de terminación.");
        }
        if (estaOcupado(recurso, inicio, fin)) {
            throw new DisponibilidadException("El recurso ya está reservado en ese horario.");
        }
        return true;
    }

    private boolean estaOcupado(Recurso recurso, LocalDateTime inicio, LocalDateTime fin) {
        return reservaciones.stream()
                .anyMatch(r -> r.seSolapa(recurso, inicio, fin));
    }

    public int contarEmpleados() {
        return empleados.size();
    }

    public int contarCategorias() {
        return categorias.size();
    }

    public int contarRecursos() {
        return recursos.size();
    }

    public int contarReservaciones() {
        return reservaciones.size();
    }

    // --- Métodos para persistencia XML ---

    public Map<String, Empleado> getEmpleados() {
        return empleados;
    }

    public Map<String, CategoriaRecurso> getCategorias() {
        return categorias;
    }

    public Map<String, Recurso> getRecursos() {
        return recursos;
    }

    public List<Reservacion> getReservaciones() {
        return reservaciones;
    }

    public int getSiguienteIdCategoria() {
        return siguienteIdCategoria;
    }

    public void setSiguienteIdCategoria(int siguiente) {
        this.siguienteIdCategoria = siguiente;
    }

    public int getSiguienteIdReservacion() {
        return siguienteIdReservacion;
    }

    public void setSiguienteIdReservacion(int siguiente) {
        this.siguienteIdReservacion = siguiente;
    }

    public void registrarEmpleadoCargado(Empleado empleado) {
        empleados.put(empleado.getId(), empleado);
    }

    public void registrarCategoriaCargada(CategoriaRecurso categoria) {
        categorias.put(categoria.getId(), categoria);
    }

    public void registrarRecursoCargado(Recurso recurso) {
        recursos.put(recurso.getCodigo(), recurso);
    }

    public void registrarReservacionCargada(Reservacion reservacion) {
        reservaciones.add(reservacion);
    }

    private CategoriaRecurso buscarOCategoriaInvalida(String idCategoria) {
        CategoriaRecurso categoria = buscarCategoria(idCategoria);
        if (categoria == null) {
            throw new ValidacionException("No existe una categoría con id " + idCategoria + ".");
        }
        return categoria;
    }
}