package view;

import controller.EstadisticasRecursosController;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import model.CategoriaRecurso;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class EstadisticasRecursosPanel
        extends JPanel {

    private EstadisticasRecursosController controlador;

    private JTextField txtDesde;
    private JTextField txtHasta;
    private JButton btnCalcular;

    private JTable tablaEstadisticas;
    private GraficaRecursosPanel panelGrafica;

    public EstadisticasRecursosPanel(
            EstadisticasRecursosController controlador)
    {
        this.controlador = controlador;

        setLayout(new BorderLayout(10, 10));
        setBorder(
                BorderFactory.createEmptyBorder(
                        10,
                        10,
                        10,
                        10
                )
        );

        LocalDate fechaActual = LocalDate.now();

        txtDesde = new JTextField(10);
        txtDesde.setText(
                fechaActual
                        .withDayOfMonth(1)
                        .toString()
        );

        txtHasta = new JTextField(10);
        txtHasta.setText(
                fechaActual.toString()
        );

        btnCalcular = new JButton("Calcular");

        JPanel panelFechas =
                new JPanel(new FlowLayout());

        panelFechas.setBorder(
                BorderFactory.createTitledBorder(
                        "Período de consulta"
                )
        );

        panelFechas.add(new JLabel("Desde:"));
        panelFechas.add(txtDesde);

        panelFechas.add(new JLabel("Hasta:"));
        panelFechas.add(txtHasta);

        panelFechas.add(btnCalcular);

        DefaultTableModel modeloTabla =
                new DefaultTableModel(
                        new String[]{
                                "Categoría",
                                "Cantidad"
                        },
                        0
                );

        tablaEstadisticas =
                new JTable(modeloTabla);

        tablaEstadisticas.setDefaultEditor(
                Object.class,
                null
        );

        JScrollPane scrollTabla =
                new JScrollPane(tablaEstadisticas);

        scrollTabla.setBorder(
                BorderFactory.createTitledBorder(
                        "Resultados"
                )
        );

        panelGrafica =
                new GraficaRecursosPanel();

        JPanel panelCentro =
                new JPanel(new GridLayout(1, 2, 10, 10));

        panelCentro.add(scrollTabla);
        panelCentro.add(panelGrafica);

        add(panelFechas, BorderLayout.NORTH);
        add(panelCentro, BorderLayout.CENTER);

        btnCalcular.addActionListener(
                e -> calcularEstadisticas()
        );
    }

    private void calcularEstadisticas() {
        LocalDate desde;
        LocalDate hasta;

        try {
            desde = LocalDate.parse(
                    txtDesde.getText().trim()
            );

            hasta = LocalDate.parse(
                    txtHasta.getText().trim()
            );
        } catch (DateTimeParseException error) {
            JOptionPane.showMessageDialog(
                    this,
                    "Las fechas deben tener el formato AAAA-MM-DD."
            );
            return;
        }

        if (desde.isAfter(hasta)) {
            JOptionPane.showMessageDialog(
                    this,
                    "La fecha desde no puede ser posterior a la fecha hasta."
            );
            return;
        }

        DefaultTableModel modeloTabla =
                (DefaultTableModel)
                        tablaEstadisticas.getModel();

        modeloTabla.setRowCount(0);
        List<String> nombresCategorias =
                new ArrayList<>();

        List<Integer> cantidades =
                new ArrayList<>();

        for (CategoriaRecurso categoria :
                controlador.listarCategorias()) {

            int cantidad =
                    controlador.contarReservasCategoria(
                            categoria,
                            desde,
                            hasta
                    );

            if (cantidad > 0) {
                modeloTabla.addRow(new Object[]{
                        categoria.getDescripcion(),
                        cantidad
                });

                nombresCategorias.add(
                        categoria.getDescripcion()
                );

                cantidades.add(cantidad);
            }
        }

        panelGrafica.actualizarDatos(
                nombresCategorias,
                cantidades
        );

        if (modeloTabla.getRowCount() == 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "No hay recursos reservados en ese período."
            );
        }
    }

}
