package view;

import controller.UsuariosActividadesController.EstadisticaSemana;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/**
 * Gráfico de barras simple (dibujado a mano con {@link Graphics2D}, sin
 * librerías externas): una barra por semana, con la cantidad de actividades
 * encima y el inicio de la semana debajo. Se refresca llamando
 * {@link #setDatos(List)}.
 */
public class GraficoBarrasPanel extends JPanel {

    private static final DateTimeFormatter FORMATO_SEMANA = DateTimeFormatter.ofPattern("dd/MM");
    private static final int MARGEN_IZQ = 40;
    private static final int MARGEN_DER = 20;
    private static final int MARGEN_SUP = 24;
    private static final int MARGEN_INF = 40;

    private List<EstadisticaSemana> datos = Collections.emptyList();

    public GraficoBarrasPanel() {
        setPreferredSize(new Dimension(760, 220));
        setBackground(Color.WHITE);
    }

    public void setDatos(List<EstadisticaSemana> datos) {
        this.datos = datos == null ? Collections.emptyList() : datos;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (datos.isEmpty()) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        FontMetrics fm = g2.getFontMetrics();

        int ancho = getWidth() - MARGEN_IZQ - MARGEN_DER;
        int alto = getHeight() - MARGEN_SUP - MARGEN_INF;
        int ejeY = MARGEN_SUP + alto;

        int maximo = Math.max(1, datos.stream().mapToInt(EstadisticaSemana::cantidad).max().orElse(1));
        int anchoBarra = Math.max(1, ancho / datos.size());

        g2.setColor(Color.DARK_GRAY);
        g2.drawLine(MARGEN_IZQ, MARGEN_SUP, MARGEN_IZQ, ejeY);
        g2.drawLine(MARGEN_IZQ, ejeY, MARGEN_IZQ + ancho, ejeY);

        for (int i = 0; i < datos.size(); i++) {
            EstadisticaSemana semana = datos.get(i);
            int alturaBarra = (int) Math.round((double) semana.cantidad() / maximo * (alto - 20));
            int x = MARGEN_IZQ + i * anchoBarra + 4;
            int y = ejeY - alturaBarra;
            int w = Math.max(1, anchoBarra - 8);

            g2.setColor(new Color(70, 130, 180));
            g2.fillRect(x, y, w, alturaBarra);
            g2.setColor(Color.BLACK);
            g2.drawRect(x, y, w, alturaBarra);

            String cantidadTxt = String.valueOf(semana.cantidad());
            g2.drawString(cantidadTxt, x + w / 2 - fm.stringWidth(cantidadTxt) / 2, y - 4);

            String semanaTxt = FORMATO_SEMANA.format(semana.inicioSemana());
            g2.drawString(semanaTxt, x + w / 2 - fm.stringWidth(semanaTxt) / 2, ejeY + 15);
        }
    }
}
