package view;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

/**
 * Iconos generados por codigo con Graphics2D.
 * Cero archivos externos, escalables y consistentes.
 */
public final class Iconos {

    private Iconos() {}

    // ── Iconos para botones de tabla (16x16) ────────────────────────

    /** Lapiz para boton Modificar. */
    public static Icon lapiz() {
        return crearIcono(16, 16, (g, w, h) -> {
            g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(Color.WHITE);
            // Cuerpo del lapiz (rotado)
            g.drawLine(3, 13, 12, 4);
            g.drawLine(10, 2, 14, 6);
            g.drawLine(12, 4, 14, 6);
            g.drawLine(3, 13, 5, 11);
            // Punta
            g.drawLine(3, 13, 2, 14);
            // Goma
            g.fillPolygon(new int[]{10, 12, 14}, new int[]{2, 4, 2}, 3);
        });
    }

    /** Basurero para boton Eliminar. */
    public static Icon basurero() {
        return crearIcono(16, 16, (g, w, h) -> {
            g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(Color.WHITE);
            // Tapa
            g.drawLine(3, 4, 13, 4);
            g.drawLine(6, 4, 6, 2);
            g.drawLine(6, 2, 10, 2);
            g.drawLine(10, 2, 10, 4);
            // Cuerpo
            g.drawLine(4, 4, 5, 14);
            g.drawLine(12, 4, 11, 14);
            g.drawLine(5, 14, 11, 14);
            // Lineas internas
            g.drawLine(7, 6, 7, 12);
            g.drawLine(9, 6, 9, 12);
        });
    }

    /** X para boton Cancelar. */
    public static Icon cancelar() {
        return crearIcono(16, 16, (g, w, h) -> {
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(Color.WHITE);
            g.drawLine(4, 4, 12, 12);
            g.drawLine(12, 4, 4, 12);
        });
    }

    /** Documento PDF. */
    public static Icon pdf() {
        return crearIcono(16, 16, (g, w, h) -> {
            g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(EstiloUI.TEXT);
            // Hoja
            g.drawRect(3, 1, 10, 14);
            // Esquina doblada
            g.drawLine(9, 1, 13, 5);
            g.drawLine(9, 1, 9, 5);
            g.drawLine(9, 5, 13, 5);
            // Lineas de texto
            g.drawLine(5, 8, 11, 8);
            g.drawLine(5, 10, 11, 10);
            g.drawLine(5, 12, 9, 12);
        });
    }

    // ── Iconos para tabs (18x18) ────────────────────────────────────

    /** Personas (Funcionarios). */
    public static Icon personas() {
        return crearIcono(18, 18, (g, w, h) -> {
            g.setColor(EstiloUI.PRIMARY);
            // Persona izquierda
            g.fillOval(3, 3, 5, 5);
            g.fillArc(1, 9, 9, 8, 0, 180);
            // Persona derecha
            g.fillOval(10, 3, 5, 5);
            g.fillArc(8, 9, 9, 8, 0, 180);
        });
    }

    /** Etiqueta/tag (Categorias). */
    public static Icon etiqueta() {
        return crearIcono(18, 18, (g, w, h) -> {
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
    }

    /** Caja/recurso (Recursos). */
    public static Icon recurso() {
        return crearIcono(18, 18, (g, w, h) -> {
            g.setColor(EstiloUI.PRIMARY);
            g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            // Caja
            g.drawRect(2, 5, 14, 11);
            // Tapa
            g.drawLine(2, 5, 5, 2);
            g.drawLine(16, 5, 13, 2);
            g.drawLine(5, 2, 13, 2);
            // Linea central
            g.drawLine(9, 2, 9, 5);
        });
    }

    /** Calendario (Reservas). */
    public static Icon calendario() {
        return crearIcono(18, 18, (g, w, h) -> {
            g.setColor(EstiloUI.PRIMARY);
            g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawRect(2, 3, 14, 13);
            g.drawLine(2, 7, 16, 7);
            // Ganchos
            g.drawLine(6, 1, 6, 5);
            g.drawLine(12, 1, 12, 5);
            // Puntos de dias
            g.fillRect(5, 9, 2, 2);
            g.fillRect(8, 9, 2, 2);
            g.fillRect(11, 9, 2, 2);
            g.fillRect(5, 12, 2, 2);
            g.fillRect(8, 12, 2, 2);
        });
    }

    /** Reloj/agenda (Actividades/Agenda). */
    public static Icon agenda() {
        return crearIcono(18, 18, (g, w, h) -> {
            g.setColor(EstiloUI.PRIMARY);
            g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawOval(2, 2, 14, 14);
            // Manecillas
            g.drawLine(9, 9, 9, 4);
            g.drawLine(9, 9, 13, 9);
            g.fillOval(8, 8, 3, 3);
        });
    }

    /** Grafico de barras (Estadisticas). */
    public static Icon estadisticas() {
        return crearIcono(18, 18, (g, w, h) -> {
            g.setColor(EstiloUI.PRIMARY);
            // Barras
            g.fillRect(3, 10, 3, 6);
            g.fillRect(7, 6, 3, 10);
            g.fillRect(11, 2, 3, 14);
            // Eje
            g.setStroke(new BasicStroke(1.5f));
            g.setColor(EstiloUI.SECONDARY);
            g.drawLine(1, 16, 17, 16);
        });
    }

    /** Grilla/calendarizacion. */
    public static Icon grilla() {
        return crearIcono(18, 18, (g, w, h) -> {
            g.setColor(EstiloUI.PRIMARY);
            g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawRect(2, 2, 14, 14);
            // Lineas horizontales
            g.drawLine(2, 7, 16, 7);
            g.drawLine(2, 12, 16, 12);
            // Lineas verticales
            g.drawLine(7, 2, 7, 16);
            g.drawLine(12, 2, 12, 16);
        });
    }

    // ── Metodo de boton con icono (sin texto) ───────────────────────

    /** Icono para agregar (+). */
    public static Icon agregar() {
        return crearIcono(16, 16, (g, w, h) -> {
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(8, 3, 8, 13);
            g.drawLine(3, 8, 13, 8);
        });
    }

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
