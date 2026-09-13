package view;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

/**
 * Iconos generados por codigo con Graphics2D.
 * Cero archivos externos, escalables y consistentes.
 * Cada icono se crea una sola vez (cache estatico).
 */
public final class Iconos {

    private Iconos() {}

    // ── Iconos cacheados ────────────────────────────────────────────

    private static final Icon LAPIZ = crearIcono(16, 16, (g, w, h) -> {
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(Color.WHITE);
        g.drawLine(3, 13, 12, 4);
        g.drawLine(10, 2, 14, 6);
        g.drawLine(12, 4, 14, 6);
        g.drawLine(3, 13, 5, 11);
        g.drawLine(3, 13, 2, 14);
        g.fillPolygon(new int[]{10, 12, 14}, new int[]{2, 4, 2}, 3);
    });

    private static final Icon BASURERO = crearIcono(16, 16, (g, w, h) -> {
        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(Color.WHITE);
        g.drawLine(3, 4, 13, 4);
        g.drawLine(6, 4, 6, 2);
        g.drawLine(6, 2, 10, 2);
        g.drawLine(10, 2, 10, 4);
        g.drawLine(4, 4, 5, 14);
        g.drawLine(12, 4, 11, 14);
        g.drawLine(5, 14, 11, 14);
        g.drawLine(7, 6, 7, 12);
        g.drawLine(9, 6, 9, 12);
    });

    private static final Icon CANCELAR = crearIcono(16, 16, (g, w, h) -> {
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(Color.WHITE);
        g.drawLine(4, 4, 12, 12);
        g.drawLine(12, 4, 4, 12);
    });

    private static final Icon PDF = crearIcono(16, 16, (g, w, h) -> {
        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(EstiloUI.TEXT);
        g.drawRect(3, 1, 10, 14);
        g.drawLine(9, 1, 13, 5);
        g.drawLine(9, 1, 9, 5);
        g.drawLine(9, 5, 13, 5);
        g.drawLine(5, 8, 11, 8);
        g.drawLine(5, 10, 11, 10);
        g.drawLine(5, 12, 9, 12);
    });

    private static final Icon AGREGAR = crearIcono(16, 16, (g, w, h) -> {
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(8, 3, 8, 13);
        g.drawLine(3, 8, 13, 8);
    });

    private static final Icon PERSONAS = crearIcono(18, 18, (g, w, h) -> {
        g.setColor(EstiloUI.PRIMARY);
        g.fillOval(3, 3, 5, 5);
        g.fillArc(1, 9, 9, 8, 0, 180);
        g.fillOval(10, 3, 5, 5);
        g.fillArc(8, 9, 9, 8, 0, 180);
    });

    private static final Icon ETIQUETA = crearIcono(18, 18, (g, w, h) -> {
        g.setColor(EstiloUI.PRIMARY);
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D tag = new Path2D.Double();
        tag.moveTo(2, 5);
        tag.lineTo(10, 5);
        tag.lineTo(16, 9);
        tag.lineTo(10, 13);
        tag.lineTo(2, 13);
        tag.closePath();
        g.draw(tag);
        g.fillOval(4, 8, 3, 3);
    });

    private static final Icon RECURSO = crearIcono(18, 18, (g, w, h) -> {
        g.setColor(EstiloUI.PRIMARY);
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawRect(2, 5, 14, 11);
        g.drawLine(2, 5, 5, 2);
        g.drawLine(16, 5, 13, 2);
        g.drawLine(5, 2, 13, 2);
        g.drawLine(9, 2, 9, 5);
    });

    private static final Icon CALENDARIO = crearIcono(18, 18, (g, w, h) -> {
        g.setColor(EstiloUI.PRIMARY);
        g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawRect(2, 3, 14, 13);
        g.drawLine(2, 7, 16, 7);
        g.drawLine(6, 1, 6, 5);
        g.drawLine(12, 1, 12, 5);
        g.fillRect(5, 9, 2, 2);
        g.fillRect(8, 9, 2, 2);
        g.fillRect(11, 9, 2, 2);
        g.fillRect(5, 12, 2, 2);
        g.fillRect(8, 12, 2, 2);
    });

    private static final Icon AGENDA = crearIcono(18, 18, (g, w, h) -> {
        g.setColor(EstiloUI.PRIMARY);
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawOval(2, 2, 14, 14);
        g.drawLine(9, 9, 9, 4);
        g.drawLine(9, 9, 13, 9);
        g.fillOval(8, 8, 3, 3);
    });

    private static final Icon ESTADISTICAS = crearIcono(18, 18, (g, w, h) -> {
        g.setColor(EstiloUI.PRIMARY);
        g.fillRect(3, 10, 3, 6);
        g.fillRect(7, 6, 3, 10);
        g.fillRect(11, 2, 3, 14);
        g.setStroke(new BasicStroke(1.5f));
        g.setColor(EstiloUI.SECONDARY);
        g.drawLine(1, 16, 17, 16);
    });

    private static final Icon GRILLA = crearIcono(18, 18, (g, w, h) -> {
        g.setColor(EstiloUI.PRIMARY);
        g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawRect(2, 2, 14, 14);
        g.drawLine(2, 7, 16, 7);
        g.drawLine(2, 12, 16, 12);
        g.drawLine(7, 2, 7, 16);
        g.drawLine(12, 2, 12, 16);
    });

    // ── Accesores publicos ──────────────────────────────────────────

    public static Icon lapiz()        { return LAPIZ; }
    public static Icon basurero()     { return BASURERO; }
    public static Icon cancelar()     { return CANCELAR; }
    public static Icon pdf()          { return PDF; }
    public static Icon agregar()      { return AGREGAR; }
    public static Icon personas()     { return PERSONAS; }
    public static Icon etiqueta()     { return ETIQUETA; }
    public static Icon recurso()      { return RECURSO; }
    public static Icon calendario()   { return CALENDARIO; }
    public static Icon agenda()       { return AGENDA; }
    public static Icon estadisticas() { return ESTADISTICAS; }
    public static Icon grilla()       { return GRILLA; }

    // ── Infraestructura ─────────────────────────────────────────────

    @FunctionalInterface
    private interface Dibujante {
        void dibujar(Graphics2D g, int ancho, int alto);
    }

    private static Icon crearIcono(int ancho, int alto, Dibujante dibujante) {
        BufferedImage img = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        dibujante.dibujar(g, ancho, alto);
        g.dispose();
        return new ImageIcon(img);
    }
}
