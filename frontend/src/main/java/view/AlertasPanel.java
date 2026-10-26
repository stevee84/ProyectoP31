package view;

import model.Alerta;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * Área de alertas, a la derecha de la ventana principal y fuera de las pestañas,
 * por lo que siempre está visible. Solo informa: ver abre el detalle y descartar
 * quita la alerta de esta lista, sin enviar nada al Backend.
 */
public class AlertasPanel extends JPanel implements IVistaAlertas {

    private static final int COL_ACTIVIDAD = 0;
    private static final int COL_VER = 1;
    private static final int COL_DESCARTAR = 2;

    private final DefaultTableModel modeloTabla =
            new DefaultTableModel(new Object[]{"Actividad", "Ver", "Desc."}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }

                @Override
                public Class<?> getColumnClass(int column) {
                    return column == COL_ACTIVIDAD ? String.class : Icon.class;
                }
            };
    private final JTable tabla = new JTable(modeloTabla);
    private final JLabel lblTitulo = new JLabel("Alertas", Iconos.alertas(), JLabel.LEFT);

    private IntConsumer onVer;
    private IntConsumer onDescartar;

    public AlertasPanel() {
        setLayout(new BorderLayout(0, EstiloUI.GAP / 2));
        setBackground(EstiloUI.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(EstiloUI.GAP, 0, EstiloUI.GAP, EstiloUI.GAP));
        setPreferredSize(new Dimension(270, 0));

        lblTitulo.setFont(EstiloUI.TITULO);
        lblTitulo.setForeground(EstiloUI.TEXT);
        add(lblTitulo, BorderLayout.NORTH);

        EstiloUI.estilizarTabla(tabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.setFillsViewportHeight(true);
        configurarColumna(COL_VER, 34);
        configurarColumna(COL_DESCARTAR, 40);

        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int fila = tabla.rowAtPoint(e.getPoint());
                int columna = tabla.columnAtPoint(e.getPoint());
                if (fila < 0) return;
                int indice = tabla.convertRowIndexToModel(fila);
                if (columna == COL_VER && onVer != null) {
                    onVer.accept(indice);
                } else if (columna == COL_DESCARTAR && onDescartar != null) {
                    onDescartar.accept(indice);
                }
            }
        });
        tabla.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int columna = tabla.columnAtPoint(e.getPoint());
                boolean esIcono = tabla.rowAtPoint(e.getPoint()) >= 0
                        && (columna == COL_VER || columna == COL_DESCARTAR);
                tabla.setCursor(esIcono ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
                tabla.setToolTipText(esIcono ? (columna == COL_VER ? "Ver alerta" : "Descartar alerta") : null);
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(EstiloUI.BORDER));
        add(scroll, BorderLayout.CENTER);
    }

    private void configurarColumna(int indice, int ancho) {
        TableColumn col = tabla.getColumnModel().getColumn(indice);
        col.setPreferredWidth(ancho);
        col.setMaxWidth(ancho + 6);
        col.setMinWidth(ancho);
    }

    @Override
    public void mostrarAlertas(List<Alerta> alertas) {
        modeloTabla.setRowCount(0);
        for (Alerta a : alertas) {
            modeloTabla.addRow(new Object[]{a.titulo(), Iconos.verAlerta(), Iconos.descartarAlerta()});
        }
        lblTitulo.setText(alertas.isEmpty() ? "Alertas" : "Alertas (" + alertas.size() + ")");
    }

    @Override
    public void setOnVer(IntConsumer cb) { this.onVer = cb; }

    @Override
    public void setOnDescartar(IntConsumer cb) { this.onDescartar = cb; }

    @Override
    public void mostrarDetalle(Alerta alerta) {
        Window ventana = SwingUtilities.getWindowAncestor(this);
        new AlertaDetalleDialog(ventana, alerta).setVisible(true);
    }

    @Override
    public boolean confirmarDescarte(Alerta alerta) {
        return JOptionPane.showConfirmDialog(this, alerta.actividad(), "Descartar?",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    /** Usado por las pruebas visuales. */
    JTable getTabla() {
        return tabla;
    }
}
