package view;

import controller.CalendarizacionRecursosController;
import model.CategoriaRecurso;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import model.Recurso;
import java.time.format.DateTimeParseException;
import java.util.List;

public class CalendarizacionRecursosPanel
        extends JPanel {

    private CalendarizacionRecursosController controlador;

    private JTextField txtFecha;
    private JComboBox<CategoriaRecurso> comboCategoria;
    private JButton btnMostrar;

    private JTable tablaCalendarizacion;

    public CalendarizacionRecursosPanel(
            CalendarizacionRecursosController controlador
    ) {
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

        txtFecha = new JTextField(10);
        txtFecha.setText(
                LocalDate.now().toString()
        );

        comboCategoria = new JComboBox<>();

        btnMostrar = new JButton("Mostrar");

        JPanel panelSeleccion =
                new JPanel(new FlowLayout());

        panelSeleccion.setBorder(
                BorderFactory.createTitledBorder(
                        "Consultar calendarización"
                )
        );

        panelSeleccion.add(new JLabel("Fecha:"));
        panelSeleccion.add(txtFecha);

        panelSeleccion.add(
                new JLabel("Categoría:")
        );
        panelSeleccion.add(comboCategoria);
        panelSeleccion.add(btnMostrar);

        DefaultTableModel modeloTabla =
                new DefaultTableModel(
                        new String[]{"Hora"},
                        0
                );

        tablaCalendarizacion =
                new JTable(modeloTabla);

        tablaCalendarizacion.setDefaultEditor(
                Object.class,
                null
        );

        tablaCalendarizacion.setAutoResizeMode(
                JTable.AUTO_RESIZE_OFF
        );

        JScrollPane scrollTabla =
                new JScrollPane(tablaCalendarizacion);

        scrollTabla.setBorder(
                BorderFactory.createTitledBorder(
                        "Calendarización de recursos"
                )
        );

        add(panelSeleccion, BorderLayout.NORTH);
        add(scrollTabla, BorderLayout.CENTER);

        cargarCategorias();
        btnMostrar.addActionListener(
                e -> mostrarCalendarizacion()
        );
    }

    private void cargarCategorias() {
        comboCategoria.removeAllItems();

        for (CategoriaRecurso categoria :
                controlador.listarCategorias()) {

            comboCategoria.addItem(categoria);
        }
    }

    private void mostrarCalendarizacion() {
        CategoriaRecurso categoria =
                (CategoriaRecurso)
                        comboCategoria.getSelectedItem();

        if (categoria == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Debe seleccionar una categoría."
            );
            return;
        }

        LocalDate fecha;

        try {
            fecha = LocalDate.parse(
                    txtFecha.getText().trim()
            );
        } catch (DateTimeParseException error) {
            JOptionPane.showMessageDialog(
                    this,
                    "La fecha debe tener el formato AAAA-MM-DD."
            );
            return;
        }

        List<Recurso> recursos =
                controlador.listarRecursosPorCategoria(
                        categoria.getId()
                );

        if (recursos.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Esta categoría no tiene recursos."
            );
            return;
        }

        String[] columnas =
                new String[recursos.size() + 1];

        columnas[0] = "Hora";

        for (int i = 0; i < recursos.size(); i++) {
            Recurso recurso = recursos.get(i);

            columnas[i + 1] =
                    recurso.getCodigo()
                            + " - "
                            + recurso.getDescripcion();
        }

        DefaultTableModel modeloTabla =
                new DefaultTableModel(
                        columnas,
                        0
                );

        for (int hora = 0; hora < 24; hora++) {
            Object[] fila =
                    new Object[recursos.size() + 1];

            fila[0] = String.format(
                    "%02d:00",
                    hora
            );

            for (int i = 0; i < recursos.size(); i++) {
                Recurso recurso = recursos.get(i);

                fila[i + 1] =
                        controlador.obtenerInformacionCelda(
                                recurso,
                                fecha,
                                hora
                        );
            }

            modeloTabla.addRow(fila);
        }

        tablaCalendarizacion.setModel(modeloTabla);

        tablaCalendarizacion.setDefaultEditor(
                Object.class,
                null
        );
        tablaCalendarizacion
                .getColumnModel()
                .getColumn(0)
                .setPreferredWidth(70);

        for (int i = 1;
             i < tablaCalendarizacion.getColumnCount();
             i++) {

            tablaCalendarizacion
                    .getColumnModel()
                    .getColumn(i)
                    .setPreferredWidth(250);
        }

        tablaCalendarizacion.setRowHeight(25);
    }

}
