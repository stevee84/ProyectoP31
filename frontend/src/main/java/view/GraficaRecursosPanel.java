package view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel de grafico de barras verticales con estilo limpio:
 * fondo blanco, borde negro, titulo centrado en negritas,
 * leyenda inferior, ejes con valores decimales y gridlines.
 */
public class GraficaRecursosPanel extends JPanel {

    private List<String> categorias;
    private List<Integer> cantidades;
    private String titulo = "Grafico";
    private String leyenda = "Datos";
    private Color colorBarras = new Color(41, 128, 185); // azul por defecto

    public GraficaRecursosPanel() {
        categorias = new ArrayList<>();
        cantidades = new ArrayList<>();
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                EstiloUI.crearTitledBorder("Grafico"),
                BorderFactory.createLineBorder(Color.BLACK, 1)
        ));
    }

    /** Configura el titulo centrado del grafico y la etiqueta de leyenda. */
    public void configurar(String titulo, String leyenda, Color color) {
        this.titulo = titulo;
        this.leyenda = leyenda;
        this.colorBarras = color;
        repaint();
    }

    public void actualizarDatos(List<String> nuevasCategorias, List<Integer> nuevasCantidades) {
        categorias = new ArrayList<>(nuevasCategorias);
        cantidades = new ArrayList<>(nuevasCantidades);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics grafico) {
        super.paintComponent(grafico);

        Graphics2D g2 = (Graphics2D) grafico;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        Insets ins = getInsets();
        int areaX = ins.left;
        int areaY = ins.top;
        int areaW = getWidth() - ins.left - ins.right;
        int areaH = getHeight() - ins.top - ins.bottom;

        // Fondo blanco interior
        g2.setColor(Color.WHITE);
        g2.fillRect(areaX, areaY, areaW, areaH);

        if (categorias.isEmpty()) {
            g2.setColor(EstiloUI.TEXT_LIGHT);
            g2.setFont(EstiloUI.NORMAL);
            g2.drawString("No hay datos para mostrar.", areaX + 20, areaY + 40);
            return;
        }

        // Margenes internos
        int margenIzq = 55;
        int margenDer = 15;
        int margenSup = 35;
        int margenInf = 55;

        int chartX = areaX + margenIzq;
        int chartY = areaY + margenSup;
        int chartW = areaW - margenIzq - margenDer;
        int chartH = areaH - margenSup - margenInf;

        if (chartW <= 0 || chartH <= 0) return;

        // Titulo centrado en negritas
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        FontMetrics fmTitulo = g2.getFontMetrics();
        int tituloW = fmTitulo.stringWidth(titulo);
        g2.drawString(titulo, areaX + (areaW - tituloW) / 2, areaY + margenSup - 10);

        // Calcular maximo
        int maxVal = 1;
        for (int c : cantidades) {
            if (c > maxVal) maxVal = c;
        }

        // Escala Y: dividir en 8 pasos (0.00, 0.25, 0.50, ... hasta maxVal)
        int numDivisiones = 8;
        double paso = (double) maxVal / numDivisiones;

        // Gridlines y etiquetas Y
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        FontMetrics fmY = g2.getFontMetrics();
        for (int i = 0; i <= numDivisiones; i++) {
            double valor = i * paso;
            int y = chartY + chartH - (int) (i * chartH / (double) numDivisiones);

            // Gridline
            g2.setColor(new Color(200, 200, 200));
            g2.setStroke(new BasicStroke(0.5f));
            if (i > 0) {
                g2.drawLine(chartX, y, chartX + chartW, y);
            }

            // Etiqueta Y
            g2.setColor(Color.BLACK);
            String etiqueta = String.format("%.2f", valor);
            int etqW = fmY.stringWidth(etiqueta);
            g2.drawString(etiqueta, chartX - etqW - 4, y + fmY.getAscent() / 2 - 1);
        }

        // Ejes
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(chartX, chartY, chartX, chartY + chartH);
        g2.drawLine(chartX, chartY + chartH, chartX + chartW, chartY + chartH);

        // Etiqueta eje Y: "Cantidad" rotada
        g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        Graphics2D g2r = (Graphics2D) g2.create();
        FontMetrics fmEjeY = g2r.getFontMetrics();
        String ejeY = "Cantidad";
        g2r.setColor(Color.BLACK);
        g2r.translate(areaX + 14, chartY + (chartH + fmEjeY.stringWidth(ejeY)) / 2);
        g2r.rotate(-Math.PI / 2);
        g2r.drawString(ejeY, 0, 0);
        g2r.dispose();

        // Barras
        int n = categorias.size();
        int espacio = chartW / n;
        int anchoBarra = Math.min(50, espacio * 2 / 3);

        for (int i = 0; i < n; i++) {
            int cantidad = cantidades.get(i);
            int altoBarra = (int) ((double) cantidad / maxVal * chartH);

            int bx = chartX + i * espacio + (espacio - anchoBarra) / 2;
            int by = chartY + chartH - altoBarra;

            // Barra con color solido
            g2.setColor(colorBarras);
            g2.fill(new Rectangle2D.Double(bx, by, anchoBarra, altoBarra));

            // Borde de la barra
            g2.setColor(colorBarras.darker());
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new Rectangle2D.Double(bx, by, anchoBarra, altoBarra));

            // Etiqueta de categoria debajo del eje X
            String nombre = categorias.get(i);
            if (nombre.length() > 12) {
                nombre = nombre.substring(0, 9) + "...";
            }
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            FontMetrics fmCat = g2.getFontMetrics();
            int catW = fmCat.stringWidth(nombre);
            g2.drawString(nombre, bx + (anchoBarra - catW) / 2, chartY + chartH + 15);
        }

        // Leyenda centrada al fondo
        int leyendaY = chartY + chartH + 35;
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        FontMetrics fmLeg = g2.getFontMetrics();
        int cuadroSize = 12;
        int legTextW = fmLeg.stringWidth(leyenda);
        int legTotalW = cuadroSize + 5 + legTextW;
        int legX = areaX + (areaW - legTotalW) / 2;

        // Cuadro de color
        g2.setColor(colorBarras);
        g2.fillRect(legX, leyendaY - cuadroSize + 2, cuadroSize, cuadroSize);
        g2.setColor(Color.BLACK);
        g2.drawRect(legX, leyendaY - cuadroSize + 2, cuadroSize, cuadroSize);

        // Texto de leyenda
        g2.setColor(Color.BLACK);
        g2.drawString(leyenda, legX + cuadroSize + 5, leyendaY);
    }
}
