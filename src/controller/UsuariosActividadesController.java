package controller;

import consulta.InfoReserva;
import consulta.ReservaConsulta;
import model.Empleado;
import model.Funcionario;
import model.Reservacion;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Controlador único del módulo de Integrante 1 (Usuarios, funcionarios y
 * actividades: login/cambio de clave, mantenimiento de funcionarios,
 * programación semanal y estadísticas). Delega en ControladorReservaciones
 * (compartido) para lo de usuarios/funcionarios y en {@link ReservaConsulta}
 * (desacoplado, ver paquete {@code consulta}) para agenda y estadísticas,
 * agregando las validaciones propias del módulo sin modificar ninguno de
 * los dos.
 */
public class UsuariosActividadesController {

    /** Primera hora del día que se muestra en la agenda semanal (inclusive). */
    public static final int HORA_INICIO = 7;
    /** Hora límite de la agenda semanal (exclusive, última franja empieza en HORA_FIN - 1). */
    public static final int HORA_FIN = 18;

    private final ControladorReservaciones controlador;
    private final ReservaConsulta reservaConsulta;

    public UsuariosActividadesController(ControladorReservaciones controlador, ReservaConsulta reservaConsulta) {
        this.controlador = controlador;
        this.reservaConsulta = reservaConsulta;
    }

    // ---- Login y cambio de clave ----

    public ControladorReservaciones.ResultadoSesion iniciarSesion(String id, String pass) {
        return controlador.iniciarSesion(id, pass);
    }

    public void cambiarContrasena(String nueva) {
        controlador.cambiarContraseña(nueva);
    }

    public void cerrarSesion() {
        controlador.cerrarSesion();
    }

    public Empleado getSesionActual() {
        return controlador.getSesionActual();
    }

    // ---- Mantenimiento de funcionarios ----

    public boolean agregarFuncionario(String nombre, String id, String telefono) {
        boolean registrado = controlador.registrarFuncionario(nombre, id, telefono);
        if (!registrado) {
            throw new IllegalArgumentException("Identificación duplicada.");
        }
        return true;
    }

    public boolean eliminarFuncionario(String id) {
        boolean tieneReservacionesActivas = controlador.listarReservacionesPorEmpleado(id).stream()
                .anyMatch(Reservacion::esActiva);
        if (tieneReservacionesActivas) {
            throw new IllegalArgumentException("El funcionario tiene reservaciones activas.");
        }
        return controlador.eliminarFuncionario(id);
    }

    public List<Funcionario> listarFuncionarios() {
        return controlador.listarFuncionarios();
    }

    public List<Funcionario> buscarFuncionariosPorTexto(String texto) {
        return controlador.buscarFuncionariosPorTexto(texto);
    }

    public boolean actualizarFuncionario(String id, String nombre, String telefono) {
        return controlador.actualizarFuncionario(id, nombre, telefono);
    }

    // ---- Programación semanal de actividades ----

    /**
     * Arma la agenda de la semana (lunes a domingo) que contiene
     * {@code fechaEnLaSemana}, agrupada por día y por hora dentro del rango
     * {@link #HORA_INICIO}-{@link #HORA_FIN}. Una reservación que cubre más
     * de una hora aparece en cada franja que ocupa.
     */
    public Map<DayOfWeek, Map<Integer, List<InfoReserva>>> obtenerMatrizSemana(LocalDate fechaEnLaSemana) {
        LocalDate lunes = fechaEnLaSemana.with(DayOfWeek.MONDAY);
        LocalDateTime desde = lunes.atStartOfDay();
        LocalDateTime hasta = lunes.plusDays(7).atStartOfDay();

        Map<DayOfWeek, Map<Integer, List<InfoReserva>>> matriz = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek dia : DayOfWeek.values()) {
            Map<Integer, List<InfoReserva>> horas = new TreeMap<>();
            for (int hora = HORA_INICIO; hora < HORA_FIN; hora++) {
                horas.put(hora, new ArrayList<>());
            }
            matriz.put(dia, horas);
        }

        LocalDate domingo = lunes.plusDays(6);
        for (InfoReserva reserva : reservaConsulta.listarEnRango(desde, hasta)) {
            // Recorremos día por día la parte de la reservación que cae dentro
            // de esta semana: si empieza antes del lunes o termina después del
            // domingo (cruza la medianoche entre semanas), cada franja debe
            // quedar en la fecha real que ocupa, no en el día en que empezó.
            LocalDate primerDia = reserva.inicio().toLocalDate().isBefore(lunes) ? lunes : reserva.inicio().toLocalDate();
            LocalDate ultimoDia = reserva.fin().toLocalDate().isAfter(domingo) ? domingo : reserva.fin().toLocalDate();

            for (LocalDate fecha = primerDia; !fecha.isAfter(ultimoDia); fecha = fecha.plusDays(1)) {
                LocalDateTime inicioDelDia = fecha.atStartOfDay();
                LocalDateTime finDelDia = fecha.plusDays(1).atStartOfDay();
                LocalDateTime inicioEfectivo = reserva.inicio().isAfter(inicioDelDia) ? reserva.inicio() : inicioDelDia;
                LocalDateTime finEfectivo = reserva.fin().isBefore(finDelDia) ? reserva.fin() : finDelDia;

                int horaInicioReserva = inicioEfectivo.getHour();
                // Si termina justo en punto, esa hora ya no está ocupada; si
                // termina a la mitad, sí cuenta como ocupada esa franja.
                int horaFinReserva = finEfectivo.getMinute() == 0 ? finEfectivo.getHour() : finEfectivo.getHour() + 1;

                int desdeHora = Math.max(HORA_INICIO, horaInicioReserva);
                int hastaHora = Math.min(HORA_FIN, horaFinReserva);
                for (int hora = desdeHora; hora < hastaHora; hora++) {
                    matriz.get(fecha.getDayOfWeek()).get(hora).add(reserva);
                }
            }
        }
        return matriz;
    }

    // ---- Estadísticas de actividades ----

    /**
     * Cuenta cuántas reservaciones caen en cada semana (lunes a domingo,
     * mismo criterio que {@link #obtenerMatrizSemana}) dentro del rango
     * [desde, hasta]. Ambas fechas se normalizan a la semana que las
     * contiene, así que el primer y el último resultado siempre son semanas
     * completas.
     */
    public List<EstadisticaSemana> contarPorSemana(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Debe indicar ambas fechas.");
        }
        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha 'desde' no puede ser posterior a 'hasta'.");
        }

        List<EstadisticaSemana> resultado = new ArrayList<>();
        LocalDate lunes = desde.with(DayOfWeek.MONDAY);
        LocalDate ultimoLunes = hasta.with(DayOfWeek.MONDAY);

        // Una sola consulta para todo el rango en vez de una por semana;
        // después se cuenta cuántas caen en cada semana filtrando en memoria.
        List<InfoReserva> todas = reservaConsulta.listarEnRango(lunes.atStartOfDay(), ultimoLunes.plusDays(7).atStartOfDay());

        while (!lunes.isAfter(ultimoLunes)) {
            LocalDate domingo = lunes.plusDays(6);
            LocalDateTime inicioSemana = lunes.atStartOfDay();
            LocalDateTime finSemana = lunes.plusDays(7).atStartOfDay();
            long cantidad = todas.stream()
                    .filter(r -> r.inicio().isBefore(finSemana) && inicioSemana.isBefore(r.fin()))
                    .count();
            resultado.add(new EstadisticaSemana(lunes, domingo, (int) cantidad));
            lunes = lunes.plusWeeks(1);
        }
        return resultado;
    }

    public record EstadisticaSemana(LocalDate inicioSemana, LocalDate finSemana, int cantidad) {
    }
}
