package controller;

import consulta.InfoReserva;
import service.EstadisticasService;
import view.AgendaSemanalPanel;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AgendaController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String[] DIAS = {"Lunes", "Martes", "Miercoles", "Jueves", "Viernes", "Sabado", "Domingo"};

    private final AgendaSemanalPanel view;
    private final EstadisticasService service;
    private LocalDate semanaActual;

    public AgendaController(AgendaSemanalPanel view, EstadisticasService service) {
        this.view = view;
        this.service = service;
        this.semanaActual = LocalDate.now();

        view.setOnSemanaAnterior(() -> {
            semanaActual = semanaActual.minusWeeks(1);
            cargarSemana();
        });
        view.setOnSemanaActual(() -> {
            semanaActual = LocalDate.now();
            cargarSemana();
        });
        view.setOnSemanaSiguiente(() -> {
            semanaActual = semanaActual.plusWeeks(1);
            cargarSemana();
        });

        cargarSemana();
    }

    private void cargarSemana() {
        try {
            LocalDate lunes = semanaActual.with(DayOfWeek.MONDAY);
            LocalDate domingo = lunes.plusDays(6);
            String etiqueta = FMT.format(lunes) + " - " + FMT.format(domingo);

            Map<DayOfWeek, Map<Integer, List<InfoReserva>>> matriz = service.obtenerMatrizSemana(semanaActual);

            Object[] columnas = new Object[DIAS.length + 1];
            columnas[0] = "Hora";
            System.arraycopy(DIAS, 0, columnas, 1, DIAS.length);

            int horaInicio = EstadisticasService.HORA_INICIO;
            int horaFin = EstadisticasService.HORA_FIN;
            int totalFilas = horaFin - horaInicio;
            Object[][] filas = new Object[totalFilas][DIAS.length + 1];

            DayOfWeek[] diasSemana = {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY};

            for (int h = 0; h < totalFilas; h++) {
                int hora = horaInicio + h;
                filas[h][0] = String.format("%02d:00", hora);
                for (int d = 0; d < diasSemana.length; d++) {
                    List<InfoReserva> reservas = matriz.get(diasSemana[d]).get(hora);
                    if (reservas == null || reservas.isEmpty()) {
                        filas[h][d + 1] = "";
                    } else {
                        filas[h][d + 1] = reservas.stream()
                                .map(r -> r.descripcionActividad() + " (" + r.nombreFuncionario() + ")")
                                .collect(Collectors.joining(", "));
                    }
                }
            }

            view.cargarSemana(etiqueta, columnas, filas);
        } catch (Exception e) {
            view.mostrarError(e.getMessage());
        }
    }
}
