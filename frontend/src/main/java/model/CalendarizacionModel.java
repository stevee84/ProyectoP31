package model;

import contrato.IServicioAplicacion;
import dto.CategoriaDTO;
import dto.MatrizCalendarioDTO;
import dto.RecursoDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * Calendarización de una categoría en una fecha. Se pide la matriz completa en
 * una sola llamada, en vez de una solicitud por celda como en el Proyecto 1.
 */
public class CalendarizacionModel {

    private final IServicioAplicacion servicio;

    public CalendarizacionModel(IServicioAplicacion servicio) {
        this.servicio = servicio;
    }

    public List<CategoriaDTO> listarCategorias() {
        return servicio.listarCategorias();
    }

    public MatrizCalendarioDTO obtener(LocalDate fecha, String idCategoria) {
        return servicio.obtenerCalendarizacion(fecha, idCategoria);
    }

    /** Columna 0 con la hora; una columna por recurso, en el orden que envió el servidor. */
    public static TablaDatos aTabla(MatrizCalendarioDTO matriz) {
        List<RecursoDTO> recursos = matriz.recursos();
        String[] columnas = new String[recursos.size() + 1];
        columnas[0] = "Hora";
        for (int i = 0; i < recursos.size(); i++) {
            columnas[i + 1] = recursos.get(i).descripcion();
        }

        int horas = IServicioAplicacion.HORA_FIN - IServicioAplicacion.HORA_INICIO;
        Object[][] filas = new Object[horas][columnas.length];
        for (int h = 0; h < horas; h++) {
            filas[h][0] = String.format("%02d:00", IServicioAplicacion.HORA_INICIO + h);
            List<String> fila = h < matriz.celdas().size() ? matriz.celdas().get(h) : List.of();
            for (int r = 0; r < recursos.size(); r++) {
                filas[h][r + 1] = r < fila.size() ? fila.get(r) : "";
            }
        }
        return new TablaDatos(columnas, filas);
    }
}
