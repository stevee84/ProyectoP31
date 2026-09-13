package model;

import java.time.LocalDate;

/**
 * Datos de una semana estadística: rango de fechas y cantidad de reservaciones.
 */
public record EstadisticaSemana(LocalDate inicioSemana, LocalDate finSemana, int cantidad) {
}
