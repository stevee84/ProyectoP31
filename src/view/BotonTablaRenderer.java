package view;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Renderizador y editor para columnas de accion en tablas.
 * Dibuja un boton pequeno con texto e icono en cada fila.
 */
public final class BotonTablaRenderer extends AbstractCellEditor
        implements TableCellRenderer, javax.swing.table.TableCellEditor {

    private final JButton boton = new JButton();
    private final java.util.function.IntConsumer accion;

    /**
     * @param texto  texto del boton (ej. "Modificar", "Eliminar")
     * @param color  color de fondo del boton
     * @param accion callback que recibe el indice de fila del modelo
     */
    public BotonTablaRenderer(String texto, Color color, java.util.function.IntConsumer accion) {
        this.accion = accion;
        boton.setText(texto);
        boton.setFont(EstiloUI.SMALL);
        boton.setForeground(Color.WHITE);
        boton.setBackground(color);
        boton.setOpaque(true);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setMargin(new Insets(2, 6, 2, 6));
        boton.addActionListener(e -> {
            JTable tabla = (JTable) SwingUtilities.getAncestorOfClass(JTable.class, boton);
            int fila = tabla.convertRowIndexToModel(tabla.getEditingRow());
            fireEditingStopped();
            accion.accept(fila);
        });
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        return boton;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
        return boton;
    }

    @Override
    public Object getCellEditorValue() {
        return "";
    }
}
