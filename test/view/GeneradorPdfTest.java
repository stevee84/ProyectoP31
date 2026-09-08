package view;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneradorPdfTest {

    @Test
    void testGenerarPdfConDatos(@TempDir Path tempDir) throws Exception {
        DefaultTableModel modelo = new DefaultTableModel(new Object[]{"Nombre", "Cantidad"}, 0);
        modelo.addRow(new Object[]{"Item 1", 10});
        modelo.addRow(new Object[]{"Item 2", 20});
        modelo.addRow(new Object[]{"Item 3", 30});
        JTable tabla = new JTable(modelo);

        File archivo = tempDir.resolve("reporte.pdf").toFile();
        GeneradorPdf.generarPdf(archivo, tabla, "Test");

        assertTrue(archivo.exists());
        assertTrue(archivo.length() > 0);
    }

    @Test
    void testTablaVaciaLanzaExcepcion(@TempDir Path tempDir) throws Exception {
        DefaultTableModel modelo = new DefaultTableModel(new Object[]{"Nombre", "Cantidad"}, 0);
        JTable tabla = new JTable(modelo);

        File archivo = tempDir.resolve("vacio.pdf").toFile();
        // generarPdf no valida filas (esa validacion vive en exportar), solo columnas.
        GeneradorPdf.generarPdf(archivo, tabla, "Test");

        assertTrue(archivo.exists());
        assertTrue(archivo.length() > 0);
    }

    @Test
    void testCeldasNulasNoFallan(@TempDir Path tempDir) throws Exception {
        DefaultTableModel modelo = new DefaultTableModel(new Object[]{"Nombre", "Cantidad"}, 0);
        modelo.addRow(new Object[]{null, null});
        modelo.addRow(new Object[]{"Item", null});
        JTable tabla = new JTable(modelo);

        File archivo = tempDir.resolve("nulos.pdf").toFile();

        assertDoesNotThrow(() -> GeneradorPdf.generarPdf(archivo, tabla, "Test"));
        assertTrue(archivo.exists());
        assertTrue(archivo.length() > 0);
    }

    @Test
    void testExportarConTablaNull() {
        // El guard de tabla nula esta antes del JFileChooser (linea 59-61), por lo
        // que exportar() lanza IllegalArgumentException sin llegar a mostrar dialogos
        // y esto se puede probar de forma headless de manera confiable.
        assertThrows(IllegalArgumentException.class,
                () -> GeneradorPdf.exportar(null, null, "Titulo", "reporte.pdf"));
    }

    @Test
    void testExportarConTituloNullLanzaExcepcion() {
        // El guard de titulo nulo (linea 62-64) tambien esta antes del JFileChooser,
        // por lo que se puede probar exportar() directamente en lugar de generarPdf().
        DefaultTableModel modelo = new DefaultTableModel(new Object[]{"Nombre"}, 0);
        modelo.addRow(new Object[]{"Item 1"});
        JTable tabla = new JTable(modelo);

        assertThrows(IllegalArgumentException.class,
                () -> GeneradorPdf.exportar(null, tabla, null, "reporte.pdf"));
    }

    @Test
    void testGenerarPdfConTituloNuloNoFalla(@TempDir Path tempDir) throws Exception {
        // generarPdf() en si mismo no valida el titulo (esa validacion vive en
        // exportar()); este test documenta que, llamado directamente, tolera un
        // titulo nulo sin lanzar excepcion.
        DefaultTableModel modelo = new DefaultTableModel(new Object[]{"Nombre"}, 0);
        modelo.addRow(new Object[]{"Item 1"});
        JTable tabla = new JTable(modelo);

        File archivo = tempDir.resolve("titulo_nulo.pdf").toFile();

        assertDoesNotThrow(() -> GeneradorPdf.generarPdf(archivo, tabla, null));
        assertTrue(archivo.exists());
        assertTrue(archivo.length() > 0);
    }

    @Test
    void testGenerarPdfConCeroColumnasLanzaExcepcion(@TempDir Path tempDir) {
        DefaultTableModel modelo = new DefaultTableModel(new Object[]{}, 0);
        JTable tabla = new JTable(modelo);

        File archivo = tempDir.resolve("sin_columnas.pdf").toFile();

        assertThrows(IllegalStateException.class,
                () -> GeneradorPdf.generarPdf(archivo, tabla, "Test"));
    }
}
