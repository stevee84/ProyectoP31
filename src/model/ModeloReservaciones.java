package model;

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
    }

    private void inicializarEmpleados() {
        registrarEmpleado(new Administrador("Administrador", "ADMIN"));
    }

    private void inicializarCategorias() {
        registrarCategoria("Sala para 10 personas");
        registrarCategoria("Laptop Windows");
        registrarCategoria("Sala de juntas");
    }

    public CategoriaRecurso registrarCategoria(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripción de la categoría es obligatoria.");
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
            throw new IllegalStateException("No se puede eliminar la categoría porque tiene recursos asociados.");
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
            throw new IllegalArgumentException("El código del recurso es obligatorio.");
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
            throw new IllegalArgumentException("El empleado no puede ser nulo.");
        }
        if (empleados.containsKey(empleado.getId())) {
            return false;
        }
        empleados.put(empleado.getId(), empleado);
        return true;
    }

    public boolean actualizarEmpleado(Empleado empleado) {
        if (empleado == null) {
            throw new IllegalArgumentException("El empleado no puede ser nulo.");
        }
        if (!empleados.containsKey(empleado.getId())) {
            return false;
        }
        empleados.put(empleado.getId(), empleado);
        return true;
    }

    public boolean eliminarEmpleado(String id) {
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

    public Reservacion reservarPorCategorias(Empleado empleado, List<String> idsCategorias,
                                              String descripcionActividad,
                                              LocalDateTime inicio, LocalDateTime fin) {
        if (empleado == null) {
            throw new IllegalArgumentException("El empleado es obligatorio.");
        }
        if (idsCategorias == null || idsCategorias.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos una categoría.");
        }
        if (inicio == null || fin == null) {
            throw new IllegalArgumentException("La fecha de inicio y de terminación son obligatorias.");
        }
        if (!inicio.isBefore(fin)) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior a la de terminación.");
        }

        // Fase 1 (solo lectura): resolver TODOS los recursos candidatos antes de mutar nada.
        // Si cualquier categoría se queda sin recurso libre, se lanza la excepción aquí y
        // "reservaciones" queda exactamente como estaba -> operación todo o nada.
        List<Recurso> recursosAsignados = new ArrayList<>();
        Set<String> categoriasVistas = new HashSet<>();
        for (String idCategoria : idsCategorias) {
            if (!categoriasVistas.add(idCategoria)) {
                continue; // categoría repetida en la selección: se ignora
            }
            CategoriaRecurso categoria = buscarOCategoriaInvalida(idCategoria);
            Recurso disponible = primerRecursoDisponible(categoria, inicio, fin, recursosAsignados);
            if (disponible == null) {
                throw new IllegalArgumentException(
                        "No hay recursos disponibles en la categoría '" + categoria.getDescripcion()
                                + "' para el horario solicitado.");
            }
            recursosAsignados.add(disponible);
        }

        // Fase 2 (mutación): recién aquí se agrega la reservación, una sola vez.
        Reservacion reservacion = new Reservacion(siguienteIdReservacion++, empleado, recursosAsignados,
                descripcionActividad, inicio, fin);
        reservaciones.add(reservacion);
        return reservacion;
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
            throw new IllegalArgumentException("La reservación no puede ser nula.");
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
            throw new IllegalArgumentException("No existe una reservación con id " + id + ".");
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
            throw new IllegalArgumentException("El recurso es obligatorio.");
        }
        if (inicio == null || fin == null) {
            throw new IllegalArgumentException("La fecha de inicio y de terminación son obligatorias.");
        }
        if (!inicio.isBefore(fin)) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior a la de terminación.");
        }
        if (estaOcupado(recurso, inicio, fin)) {
            throw new IllegalArgumentException("El recurso ya está reservado en ese horario.");
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

    private CategoriaRecurso buscarOCategoriaInvalida(String idCategoria) {
        CategoriaRecurso categoria = buscarCategoria(idCategoria);
        if (categoria == null) {
            throw new IllegalArgumentException("No existe una categoría con id " + idCategoria + ".");
        }
        return categoria;
    }
}