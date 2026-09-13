package view;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

/**
 * Barra de busqueda reutilizable que filtra una JTable en tiempo real.
 * Usa TableRowSorter con RowFilter para filtrar por todas las columnas visibles.
 */
public final class BarraBusquedaTabla extends JPanel {

    private final JTextField campoFiltro = new JTextField(20);

    /**
     * Crea la barra y la conecta a la tabla dada.
     * @param tabla            tabla a filtrar
     * @param columnasExcluidas indices de columnas a ignorar (ej. columnas de botones)
     */
    public BarraBusquedaTabla(JTable tabla, int... columnasExcluidas) {
        setLayout(new FlowLayout(FlowLayout.LEFT, EstiloUI.GAP, 4));
        setBackground(EstiloUI.BACKGROUND);

        JLabel lbl = new JLabel("🔍");
        lbl.setFont(EstiloUI.NORMAL);
        EstiloUI.estilizarCampo(campoFiltro);
        campoFiltro.setToolTipText("Escriba para filtrar...");

        add(lbl);
        add(campoFiltro);

        // Configurar el sorter
        DefaultTableModel modelo = (DefaultTableModel) tabla.getModel();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modelo);
        tabla.setRowSorter(sorter);

        // Determinar columnas de datos (excluir las de botones)
        java.util.Set<Integer> excluidas = new java.util.HashSet<>();
        for (int c : columnasExcluidas) excluidas.add(c);

        campoFiltro.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filtrar(); }
            @Override public void removeUpdate(DocumentEvent e) { filtrar(); }
            @Override public void changedUpdate(DocumentEvent e) { filtrar(); }

            private void filtrar() {
                String texto = campoFiltro.getText().trim();
                if (texto.isEmpty()) {
                    sorter.setRowFilter(null);
                    return;
                }
                // Construir indices de columnas de datos
                int totalCols = modelo.getColumnCount();
                java.util.List<Integer> cols = new java.util.ArrayList<>();
                for (int i = 0; i < totalCols; i++) {
                    if (!excluidas.contains(i)) cols.add(i);
                }
                int[] indices = cols.stream().mapToInt(Integer::intValue).toArray();
                try {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(texto), indices));
                } catch (java.util.regex.PatternSyntaxException ex) {
                    // Ignorar patrones invalidos mientras el usuario escribe
                }
            }
        });
    }
}
