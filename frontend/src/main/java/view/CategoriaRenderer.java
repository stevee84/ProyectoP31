package view;

import dto.CategoriaDTO;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import java.awt.Component;

/**
 * Muestra solo la descripción de una CategoriaDTO en combos y listas
 * (el toString() del record imprimiría todos sus campos).
 * Un valor null se muestra con el texto indicado, p. ej. "Todas".
 */
public class CategoriaRenderer extends DefaultListCellRenderer {

    private final String textoNulo;

    public CategoriaRenderer() {
        this("");
    }

    public CategoriaRenderer(String textoNulo) {
        this.textoNulo = textoNulo;
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        setText(value instanceof CategoriaDTO c ? c.descripcion() : textoNulo);
        return this;
    }
}
