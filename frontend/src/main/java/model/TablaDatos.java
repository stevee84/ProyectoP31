package model;

import java.util.Arrays;

/**
 * Columnas y filas listas para un JTable. Lo arman los modelos a partir de los DTO,
 * así la conversión se prueba sin Swing y la vista solo pinta.
 */
public record TablaDatos(String[] columnas, Object[][] filas) {

    public TablaDatos {
        columnas = columnas.clone();
        filas = Arrays.stream(filas).map(Object[]::clone).toArray(Object[][]::new);
    }

    public int cantidadFilas() {
        return filas.length;
    }

    public Object valor(int fila, int columna) {
        return filas[fila][columna];
    }
}
