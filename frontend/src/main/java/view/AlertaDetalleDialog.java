package view;

import model.Alerta;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

/** Ventana emergente con el detalle de una alerta: encargado, fecha, horas y recursos. */
public class AlertaDetalleDialog extends JDialog {

    public AlertaDetalleDialog(Window propietario, Alerta alerta) {
        super(propietario, alerta.actividad(), ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(EstiloUI.BACKGROUND);

        JPanel campos = new JPanel(new GridBagLayout());
        campos.setBackground(EstiloUI.BACKGROUND);
        campos.setBorder(BorderFactory.createEmptyBorder(14, 18, 6, 18));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 4, 3, 10);
        gbc.anchor = GridBagConstraints.NORTHWEST;

        String[][] filas = {
                {"Encargado:", alerta.encargado()},
                {"Fecha:", alerta.fecha()},
                {"Desde:", alerta.desde()},
                {"Hasta:", alerta.hasta()},
        };
        for (int i = 0; i < filas.length; i++) {
            agregarFila(campos, gbc, i, filas[i][0], new JLabel(filas[i][1]));
        }

        JList<String> listaRecursos = new JList<>(alerta.recursos().toArray(new String[0]));
        listaRecursos.setFont(EstiloUI.NORMAL);
        listaRecursos.setEnabled(false);
        JScrollPane scroll = new JScrollPane(listaRecursos);
        scroll.setPreferredSize(new Dimension(190, 110));
        agregarFila(campos, gbc, filas.length, "Recursos", scroll);

        JButton btnOk = new JButton("OK");
        EstiloUI.estilizarBotonPrimario(btnOk);
        btnOk.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnOk);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.setBackground(EstiloUI.BACKGROUND);
        botones.add(btnOk);

        add(campos, BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(propietario);
    }

    private static void agregarFila(JPanel panel, GridBagConstraints gbc, int fila,
                                    String etiqueta, java.awt.Component valor) {
        JLabel lbl = new JLabel(etiqueta);
        EstiloUI.estilizarEtiqueta(lbl);
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.anchor = valor instanceof JScrollPane ? GridBagConstraints.WEST : GridBagConstraints.NORTHWEST;
        panel.add(lbl, gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        if (valor instanceof JLabel l) l.setFont(EstiloUI.NORMAL);
        panel.add(valor, gbc);
    }
}
