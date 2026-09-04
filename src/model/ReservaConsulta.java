package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// Contrato de solo lectura para consultar reservas desde otros modulos sin acceder directamente al reservas.xml ni a ModeloReservaciones

public interface ReservaConsulta {
    // Reservas ACTIVAS cuyo intervalo se solapa con el dia dado
    // Es el que va a utilizar Josua en calendarizacion de recursos
    List<ReservaInfo> buscarActivasEnFecha(LocalDate fecha);

    /*
            Reservas ACTIVAS que se solapa con el rango [desde, hasta]
            Lo tienen que usar los 2.
            Steven en programacion semanal de actividades y en ambas pantallas de estadisticas (los 2),
            que se agrupan por semana o por categoria dentro mde un rango de fechas
     */
    List<ReservaInfo> buscarActivasEnRango(LocalDateTime desde, LocalDateTime hasta);
}