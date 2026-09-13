package model;

import consulta.InfoReserva;
import service.EstadisticasService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Fachada de la capa Model para estadisticas y agenda. El Controller solo
 * conoce esta clase, nunca importa "service" directamente.
 */
public class EstadisticasModel {

    private final EstadisticasService servicio;

    public EstadisticasModel(EstadisticasService servicio) {
        this.servicio = servicio;
    }

    public List<CategoriaRecurso> listarCategorias() {
        return servicio.listarCategorias();
    }

    public Map<DayOfWeek, Map<Integer, List<InfoReserva>>> obtenerMatrizSemana(LocalDate fechaEnLaSemana) {
        return servicio.obtenerMatrizSemana(fechaEnLaSemana);
    }

    public List<EstadisticaSemana> contarPorSemana(LocalDate desde, LocalDate hasta) {
        return servicio.contarPorSemana(desde, hasta);
    }

    public int contarReservasCategoria(CategoriaRecurso categoria, LocalDate desde, LocalDate hasta) {
        return servicio.contarReservasCategoria(categoria, desde, hasta);
    }

    public int getHoraInicio() {
        return EstadisticasService.HORA_INICIO;
    }

    public int getHoraFin() {
        return EstadisticasService.HORA_FIN;
    }
}
