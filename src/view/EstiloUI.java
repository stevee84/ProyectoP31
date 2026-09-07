package view;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * Constantes y utilidades de estilo visual para toda la aplicación.
 * Centraliza colores, fuentes y métodos de estilizado para mantener
 * una apariencia consistente en todos los paneles.
 */
public final class EstiloUI {

    private EstiloUI() {}

    // ── Colores ──────────────────────────────────────────────────────
    public static final Color PRIMARY       = new Color(41, 128, 185);
    public static final Color PRIMARY_DARK  = new Color(30, 100, 150);
    public static final Color SECONDARY     = new Color(52, 73, 94);
    public static final Color BACKGROUND    = new Color(245, 246, 250);
    public static final Color SURFACE       = Color.WHITE;
    public static final Color TEXT          = new Color(44, 62, 80);
    public static final Color TEXT_LIGHT    = new Color(127, 140, 141);
    public static final Color BORDER        = new Color(189, 195, 199);
    public static final Color ACCENT        = new Color(39, 174, 96);
    public static final Color DANGER        = new Color(192, 57, 43);
    public static final Color ROW_ALT       = new Color(235, 245, 255);
    public static final Color HEADER_BG     = new Color(41, 128, 185);
    public static final Color HEADER_FG     = Color.WHITE;

    // ── Fuentes ──────────────────────────────────────────────────────
    public static final Font TITULO  = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font NORMAL  = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font BOLD    = new Font("Segoe UI", Font.BOLD, 12);

    // ── Dimensiones ──────────────────────────────────────────────────
    public static final int PADDING      = 12;
    public static final int GAP          = 8;
    public static final int FIELD_HEIGHT = 28;
    public static final int BTN_HEIGHT   = 30;
    public static final int ROW_HEIGHT   = 26;

    // ── Métodos de estilizado ────────────────────────────────────────

    /** Aplica estilo consistente a un botón. */
    public static void estilizarBoton(JButton btn) {
        btn.setFont(NORMAL);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(4, 14, 4, 14));
    }

    /** Aplica estilo primario (azul) a un botón. */
    public static void estilizarBotonPrimario(JButton btn) {
        estilizarBoton(btn);
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
    }

    /** Aplica estilo consistente a un campo de texto. */
    public static void estilizarCampo(JTextField campo) {
        campo.setFont(NORMAL);
        campo.setMargin(new Insets(2, 6, 2, 6));
    }

    /** Aplica estilo consistente a una tabla. */
    public static void estilizarTabla(JTable tabla) {
        tabla.setFont(NORMAL);
        tabla.setRowHeight(ROW_HEIGHT);
        tabla.setGridColor(BORDER);
        tabla.setSelectionBackground(PRIMARY);
        tabla.setSelectionForeground(Color.WHITE);
        tabla.setShowHorizontalLines(true);
        tabla.setShowVerticalLines(false);
        tabla.setIntercellSpacing(new Dimension(0, 1));

        // Header
        JTableHeader header = tabla.getTableHeader();
        header.setFont(BOLD);
        header.setBackground(HEADER_BG);
        header.setForeground(HEADER_FG);
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 32));

        // Alternating rows
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? SURFACE : ROW_ALT);
                }
                c.setForeground(isSelected ? Color.WHITE : TEXT);
                setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
                return c;
            }
        });
    }

    /** Crea un TitledBorder consistente. */
    public static TitledBorder crearTitledBorder(String titulo) {
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                titulo
        );
        border.setTitleFont(BOLD);
        border.setTitleColor(SECONDARY);
        return border;
    }

    /** Crea un borde de margen estándar. */
    public static javax.swing.border.EmptyBorder margenEstandar() {
        return new javax.swing.border.EmptyBorder(PADDING, PADDING, PADDING, PADDING);
    }

    /** Aplica estilo a una etiqueta de título. */
    public static void estilizarEtiquetaTitulo(JLabel label) {
        label.setFont(TITULO);
        label.setForeground(SECONDARY);
    }

    /** Aplica estilo a una etiqueta normal. */
    public static void estilizarEtiqueta(JLabel label) {
        label.setFont(NORMAL);
        label.setForeground(TEXT);
    }

    /** Estiliza todos los botones hijos de un contenedor. */
    public static void estilizarBotonesEn(Container container) {
        for (Component c : container.getComponents()) {
            if (c instanceof JButton btn) {
                estilizarBoton(btn);
            }
            if (c instanceof Container sub) {
                estilizarBotonesEn(sub);
            }
        }
    }

    /** Estiliza todos los campos de texto hijos de un contenedor. */
    public static void estilizarCamposEn(Container container) {
        for (Component c : container.getComponents()) {
            if (c instanceof JTextField campo && !(c instanceof JPasswordField)) {
                estilizarCampo(campo);
            }
            if (c instanceof Container sub) {
                estilizarCamposEn(sub);
            }
        }
    }

    /** Estiliza todas las etiquetas hijas de un contenedor. */
    public static void estilizarEtiquetasEn(Container container) {
        for (Component c : container.getComponents()) {
            if (c instanceof JLabel label) {
                estilizarEtiqueta(label);
            }
            if (c instanceof Container sub) {
                estilizarEtiquetasEn(sub);
            }
        }
    }
}
