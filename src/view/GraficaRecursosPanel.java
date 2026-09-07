package view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class GraficaRecursosPanel extends JPanel {

    private static final Color[] PALETA = {
        new Color(41, 128, 185),   // blue
        new Color(39, 174, 96),    // green
        new Color(192, 57, 43),    // red
        new Color(142, 68, 173),   // purple
        new Color(243, 156, 18),   // orange
        new Color(22, 160, 133),   // teal
        new Color(211, 84, 0),     // dark orange
        new Color(44, 62, 80)      // dark blue
    };

    private List<String> categorias;
    private List<Integer> cantidades;

    public GraficaRecursosPanel() {
        categorias = new ArrayList<>();
        cantidades = new ArrayList<>();
        setBackground(EstiloUI.SURFACE);
        setBorder(EstiloUI.crearTitledBorder("Grafica de barras"));
    }

    public void actualizarDatos(
            List<String> nuevasCategorias,
            List<Integer> nuevasCantidades
    ) {
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

        if (categorias.isEmpty()) {
            g2.setColor(EstiloUI.TEXT_LIGHT);
            g2.setFont(EstiloUI.NORMAL);
            g2.drawString("No hay datos para mostrar.", 20, 40);
            return;
        }

        int margenIzquierdo = 50;
        int margenDerecho = 20;
        int margenSuperior = 50;
        int margenInferior = 70;

        int anchoDisponible = getWidth() - margenIzquierdo - margenDerecho;
        int altoDisponible = getHeight() - margenSuperior - margenInferior;

        if (anchoDisponible <= 0 || altoDisponible <= 0) return;

        int cantidadMayor = 1;
        for (int cantidad : cantidades) {
            if (cantidad > cantidadMayor) {
                cantidadMayor = cantidad;
            }
        }

        // Axes
        g2.setColor(EstiloUI.BORDER);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(margenIzquierdo, margenSuperior, margenIzquierdo, margenSuperior + altoDisponible);
        g2.drawLine(margenIzquierdo, margenSuperior + altoDisponible,
                margenIzquierdo + anchoDisponible, margenSuperior + altoDisponible);

        // Grid lines
        g2.setStroke(new BasicStroke(0.5f));
        g2.setColor(new Color(220, 220, 220));
        int gridLines = 4;
        for (int i = 1; i <= gridLines; i++) {
            int y = margenSuperior + altoDisponible - (i * altoDisponible / gridLines);
            g2.drawLine(margenIzquierdo + 1, y, margenIzquierdo + anchoDisponible, y);
            // Y-axis labels
            g2.setColor(EstiloUI.TEXT_LIGHT);
            g2.setFont(EstiloUI.SMALL);
            String val = String.valueOf(i * cantidadMayor / gridLines);
            FontMetrics fmSmall = g2.getFontMetrics();
            g2.drawString(val, margenIzquierdo - fmSmall.stringWidth(val) - 5, y + fmSmall.getAscent() / 2);
            g2.setColor(new Color(220, 220, 220));
        }

        int espacio = anchoDisponible / categorias.size();
        int anchoBarra = Math.min(60, espacio * 2 / 3);
        int radioEsquina = 6;

        for (int i = 0; i < categorias.size(); i++) {
            int cantidad = cantidades.get(i);
            int altoBarra = cantidad * altoDisponible / cantidadMayor;

            int posicionX = margenIzquierdo + (i * espacio) + ((espacio - anchoBarra) / 2);
            int posicionY = margenSuperior + altoDisponible - altoBarra;

            Color colorBarra = PALETA[i % PALETA.length];

            // Gradient fill with rounded top
            GradientPaint gradiente = new GradientPaint(
                    posicionX, posicionY, colorBarra,
                    posicionX, posicionY + altoBarra, colorBarra.darker()
            );
            g2.setPaint(gradiente);

            // Rounded rectangle (only top corners rounded via clip)
            Shape barShape = new RoundRectangle2D.Float(
                    posicionX, posicionY, anchoBarra, altoBarra, radioEsquina, radioEsquina
            );
            g2.fill(barShape);

            // Value label above bar
            g2.setColor(EstiloUI.TEXT);
            g2.setFont(EstiloUI.BOLD);
            String valorTexto = String.valueOf(cantidad);
            FontMetrics fm = g2.getFontMetrics();
            int textoAncho = fm.stringWidth(valorTexto);
            g2.drawString(valorTexto, posicionX + (anchoBarra - textoAncho) / 2, posicionY - 6);

            // Category label below axis (rotated if many categories)
            String nombre = categorias.get(i);
            if (nombre.length() > 15) {
                nombre = nombre.substring(0, 12) + "...";
            }

            g2.setFont(EstiloUI.SMALL);
            FontMetrics fmLabel = g2.getFontMetrics();

            if (categorias.size() > 5) {
                // Rotate labels 45 degrees
                Graphics2D g2copy = (Graphics2D) g2.create();
                int labelX = posicionX + anchoBarra / 2;
                int labelY = margenSuperior + altoDisponible + 8;
                g2copy.setColor(EstiloUI.TEXT);
                g2copy.translate(labelX, labelY);
                g2copy.rotate(Math.toRadians(35));
                g2copy.drawString(nombre, 0, 0);
                g2copy.dispose();
            } else {
                g2.setColor(EstiloUI.TEXT);
                int labelWidth = fmLabel.stringWidth(nombre);
                g2.drawString(nombre,
                        posicionX + (anchoBarra - labelWidth) / 2,
                        margenSuperior + altoDisponible + 18);
            }
        }
    }
}
