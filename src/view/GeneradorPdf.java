package view;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Utilidad para generar reportes PDF a partir de un JTable.
 * Usa iText 5 para crear el documento con titulo, fecha, tabla
 * y pie de pagina.
 */
public final class GeneradorPdf {

    private static final Font FONT_TITULO = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD,
            new BaseColor(41, 128, 185));
    private static final Font FONT_SUBTITULO = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL,
            new BaseColor(127, 140, 141));
    private static final Font FONT_ENCABEZADO = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD,
            BaseColor.WHITE);
    private static final Font FONT_CELDA = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL,
            BaseColor.BLACK);
    private static final Font FONT_PIE = new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC,
            new BaseColor(127, 140, 141));

    private static final BaseColor COLOR_ENCABEZADO = new BaseColor(41, 128, 185);
    private static final BaseColor COLOR_FILA_ALT = new BaseColor(235, 245, 255);

    private GeneradorPdf() {}

    /**
     * Muestra un JFileChooser, genera el PDF con los datos del JTable
     * y lo abre automaticamente si el sistema lo soporta.
     *
     * @param padre        componente padre para los dialogos
     * @param tabla        JTable con los datos a exportar
     * @param titulo       titulo del reporte (ej. "Estadisticas de Recursos")
     * @param nombreArchivo nombre sugerido (ej. "estadisticas_recursos.pdf")
     */
    public static void exportar(JComponent padre, JTable tabla, String titulo, String nombreArchivo) {
        if (tabla == null) {
            throw new IllegalArgumentException("La tabla no puede ser nula.");
        }
        if (titulo == null) {
            throw new IllegalArgumentException("El titulo no puede ser nulo.");
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar reporte PDF");
        fileChooser.setSelectedFile(new File(nombreArchivo));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivo PDF (*.pdf)", "pdf"));

        int resultado = fileChooser.showSaveDialog(padre);
        if (resultado != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File archivo = fileChooser.getSelectedFile();
        if (!archivo.getName().toLowerCase().endsWith(".pdf")) {
            archivo = new File(archivo.getAbsolutePath() + ".pdf");
        }

        try {
            TableModel modelo = tabla.getModel();
            if (modelo.getRowCount() == 0) {
                throw new IllegalStateException("La tabla no tiene datos para exportar.");
            }
            generarPdf(archivo, tabla, titulo);
            JOptionPane.showMessageDialog(padre,
                    "PDF generado correctamente:\n" + archivo.getAbsolutePath(),
                    "PDF generado", JOptionPane.INFORMATION_MESSAGE);

            // Intentar abrir el PDF automaticamente
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(archivo);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(padre,
                    "Error al generar el PDF: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    static void generarPdf(File archivo, JTable tabla, String titulo) throws Exception {
        TableModel modeloInicial = tabla.getModel();
        if (modeloInicial.getColumnCount() == 0) {
            throw new IllegalStateException("La tabla no tiene columnas.");
        }

        Document documento = new Document(PageSize.A4, 40, 40, 50, 50);
        FileOutputStream salida = new FileOutputStream(archivo);
        try {
            PdfWriter.getInstance(documento, salida);
            documento.open();

            try {
                escribirContenido(documento, tabla, titulo);
            } finally {
                documento.close();
            }
        } finally {
            salida.close();
        }
    }

    private static void escribirContenido(Document documento, JTable tabla, String titulo) throws Exception {
        // Titulo
        Paragraph parTitulo = new Paragraph(titulo, FONT_TITULO);
        parTitulo.setAlignment(Element.ALIGN_CENTER);
        documento.add(parTitulo);

        // Subtitulo con fecha
        String fechaTexto = "Generado el " + LocalDate.now().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        Paragraph parFecha = new Paragraph(fechaTexto, FONT_SUBTITULO);
        parFecha.setAlignment(Element.ALIGN_CENTER);
        parFecha.setSpacingAfter(20);
        documento.add(parFecha);

        // Tabla
        TableModel modelo = tabla.getModel();
        int columnas = modelo.getColumnCount();
        PdfPTable pdfTabla = new PdfPTable(columnas);
        pdfTabla.setWidthPercentage(100);
        pdfTabla.setSpacingBefore(10);

        // Encabezados
        for (int col = 0; col < columnas; col++) {
            PdfPCell celda = new PdfPCell(new Phrase(modelo.getColumnName(col), FONT_ENCABEZADO));
            celda.setBackgroundColor(COLOR_ENCABEZADO);
            celda.setHorizontalAlignment(Element.ALIGN_CENTER);
            celda.setPadding(8);
            pdfTabla.addCell(celda);
        }

        // Filas con colores alternados
        for (int fila = 0; fila < modelo.getRowCount(); fila++) {
            for (int col = 0; col < columnas; col++) {
                Object valor = modelo.getValueAt(fila, col);
                PdfPCell celda = new PdfPCell(new Phrase(
                        valor != null ? valor.toString() : "", FONT_CELDA));
                celda.setPadding(6);
                celda.setHorizontalAlignment(Element.ALIGN_CENTER);
                if (fila % 2 != 0) {
                    celda.setBackgroundColor(COLOR_FILA_ALT);
                }
                pdfTabla.addCell(celda);
            }
        }

        documento.add(pdfTabla);

        // Total de registros
        Paragraph parTotal = new Paragraph(
                "Total de registros: " + modelo.getRowCount(), FONT_SUBTITULO);
        parTotal.setSpacingBefore(10);
        documento.add(parTotal);

        // Pie de pagina
        Paragraph parPie = new Paragraph(
                "Sistema de Reserva de Recursos - UNA EIF206 Programacion 3", FONT_PIE);
        parPie.setAlignment(Element.ALIGN_CENTER);
        parPie.setSpacingBefore(30);
        documento.add(parPie);
    }
}
}
