package view;

import controller.ReservaController;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import model.CategoriaRecurso;
import model.Reservacion;
import model.Recurso;
import model.ResultadoReserva;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

public class ReservaPanel extends JPanel {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private ReservaController controlador;
    private JTable tablaReservas;

    private JTextField txtActividad;
    private JTextField txtFecha;
    private JTextField txtHoraInicio;
    private JTextField txtHoraFin;
    private JList<CategoriaRecurso> listaCategorias;

    public ReservaPanel(ReservaController controlador) {
        this.controlador = controlador;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Campos de texto del formulario ---
        txtActividad = new JTextField(20);
        txtFecha = new JTextField("aaaa-mm-dd", 10);
        txtHoraInicio = new JTextField("HH:mm", 6);
        txtHoraFin = new JTextField("HH:mm", 6);

        JPanel panelCampos = new JPanel(new GridLayout(2, 4, 5, 5));
        panelCampos.add(new JLabel("Actividad:"));
        panelCampos.add(txtActividad);
        panelCampos.add(new JLabel("Fecha:"));
        panelCampos.add(txtFecha);
        panelCampos.add(new JLabel("Hora inicio:"));
        panelCampos.add(txtHoraInicio);
        panelCampos.add(new JLabel("Hora fin:"));
        panelCampos.add(txtHoraFin);

        // --- Lista de categorías, selección múltiple ---
        listaCategorias = new JList<>();
        listaCategorias.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listaCategorias.setVisibleRowCount(4);
        listaCategorias.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof CategoriaRecurso categoria) {
                    setText(categoria.getDescripcion());
                }
                return c;
            }
        });
        JScrollPane scrollCategorias = new JScrollPane(listaCategorias);
        scrollCategorias.setBorder(BorderFactory.createTitledBorder("Categorías requeridas (selección múltiple)"));
        cargarCategorias();

        JButton btnAplicar = new JButton("Aplicar");
        btnAplicar.addActionListener(e -> aplicarReserva());

        JButton btnCancelarSeleccionada = new JButton("Cancelar reserva seleccionada");
        btnCancelarSeleccionada.addActionListener(e -> cancelarReservaSeleccionada());

        JButton btnLimpiar = new JButton("Limpiar");
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        // Placeholders: la extracción con IA es el paso 8 (pendiente), y el reporte
        // PDF probablemente use una utilidad compartida por todo el equipo, todavía
        // no definida. Se dejan deshabilitados para no prometer una funcionalidad
        // que todavía no existe.
        JButton btnLlenarConIA = new JButton("Llenar con IA");
        btnLlenarConIA.setEnabled(false);
        btnLlenarConIA.setToolTipText("Pendiente: integración con IA (paso 8)");

        JButton btnGenerarPdf = new JButton("Generar PDF");
        btnGenerarPdf.setEnabled(false);
        btnGenerarPdf.setToolTipText("Pendiente: utilidad de reportes PDF");

        JPanel panelBotones = new JPanel(new FlowLayout());
        panelBotones.add(btnAplicar);
        panelBotones.add(btnCancelarSeleccionada);
        panelBotones.add(btnLimpiar);
        panelBotones.add(btnLlenarConIA);
        panelBotones.add(btnGenerarPdf);

        JPanel panelFormulario = new JPanel(new BorderLayout(5, 5));
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Nueva reserva"));
        panelFormulario.add(panelCampos, BorderLayout.NORTH);
        panelFormulario.add(scrollCategorias, BorderLayout.CENTER);
        panelFormulario.add(panelBotones, BorderLayout.SOUTH);

        // --- Tabla "Mis reservas" ---
        DefaultTableModel modeloTabla = new DefaultTableModel(
                new String[]{"Id", "Actividad", "Fecha", "Horario", "Recursos", "Estado"},
                0
        );

        tablaReservas = new JTable(modeloTabla);
        tablaReservas.setDefaultEditor(Object.class, null);

        JScrollPane scrollTabla = new JScrollPane(tablaReservas);
        scrollTabla.setBorder(BorderFactory.createTitledBorder("Mis reservas"));

        add(panelFormulario, BorderLayout.NORTH);
        add(scrollTabla, BorderLayout.CENTER);

        cargarReservas();
    }

    private void cargarCategorias() {
        List<CategoriaRecurso> categorias = controlador.listarCategorias();
        listaCategorias.setListData(categorias.toArray(new CategoriaRecurso[0]));
    }

    public void cargarReservas() {
        DefaultTableModel modeloTabla = (DefaultTableModel) tablaReservas.getModel();
        modeloTabla.setRowCount(0);

        for (Reservacion reservacion : controlador.misReservas()) {
            String recursos = reservacion.getRecursos().stream()
                    .map(Recurso::getCodigo)
                    .collect(Collectors.joining(", "));

            String horario = reservacion.getInicio().format(FORMATO_HORA)
                    + " - " + reservacion.getFin().format(FORMATO_HORA);

            modeloTabla.addRow(new Object[]{
                    reservacion.getId(),
                    reservacion.getDescripcionActividad(),
                    reservacion.getInicio().format(FORMATO_FECHA),
                    horario,
                    recursos,
                    reservacion.getEstado()
            });
        }
    }

    private void aplicarReserva() {
        // --- Parsear fecha y horas. Si el formato está mal, avisamos y no seguimos. ---
        LocalDate fecha;
        LocalTime horaInicio;
        LocalTime horaFin;
        try {
            fecha = LocalDate.parse(txtFecha.getText().trim(), FORMATO_FECHA);
            horaInicio = LocalTime.parse(txtHoraInicio.getText().trim(), FORMATO_HORA);
            horaFin = LocalTime.parse(txtHoraFin.getText().trim(), FORMATO_HORA);
        } catch (DateTimeParseException error) {
            JOptionPane.showMessageDialog(this,
                    "Revise el formato: fecha aaaa-mm-dd, horas HH:mm (ej. 09:00).",
                    "Datos inválidos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // --- Categorías seleccionadas en la lista ---
        List<CategoriaRecurso> categoriasSeleccionadas = listaCategorias.getSelectedValuesList();
        if (categoriasSeleccionadas.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Debe seleccionar al menos una categoría.",
                    "Datos inválidos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<String> idsCategorias = categoriasSeleccionadas.stream()
                .map(CategoriaRecurso::getId)
                .toList();

        LocalDateTime inicio = LocalDateTime.of(fecha, horaInicio);
        LocalDateTime fin = LocalDateTime.of(fecha, horaFin);

        // --- Intentar la reserva. SolicitudReserva valida lo que falte (actividad vacía, etc.) ---
        ResultadoReserva resultado;
        try {
            resultado = controlador.crearReservacion(idsCategorias, txtActividad.getText().trim(), inicio, fin);
        } catch (IllegalArgumentException error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "Datos inválidos",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (resultado.esExito()) {
            JOptionPane.showMessageDialog(this,
                    "Reserva registrada correctamente (id=" + resultado.getReservacion().getId() + ").");
            limpiarFormulario();
            cargarReservas();
        } else {
            String categorias = resultado.getCategoriasNoDisponibles().stream()
                    .map(CategoriaRecurso::getDescripcion)
                    .collect(Collectors.joining(", "));
            JOptionPane.showMessageDialog(this,
                    "No hay disponibilidad en: " + categorias + ". Corrija e intente de nuevo.",
                    "Sin disponibilidad", JOptionPane.WARNING_MESSAGE);
            // OJO: a propósito NO se limpia el formulario acá -> el funcionario puede corregir y reintentar.
        }
    }

    private void limpiarFormulario() {
        txtActividad.setText("");
        txtFecha.setText("aaaa-mm-dd");
        txtHoraInicio.setText("HH:mm");
        txtHoraFin.setText("HH:mm");
        listaCategorias.clearSelection();
    }

    private void cancelarReservaSeleccionada() {
        int fila = tablaReservas.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione una reserva de la tabla para cancelarla.",
                    "Nada seleccionado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) tablaReservas.getValueAt(fila, 0);

        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Desea cancelar la reserva seleccionada?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            controlador.cancelarReservacion(id);
            JOptionPane.showMessageDialog(this, "Reserva cancelada correctamente.");
            cargarReservas();
        } catch (IllegalStateException error) {
            JOptionPane.showMessageDialog(this, error.getMessage(),
                    "No se pudo cancelar", JOptionPane.WARNING_MESSAGE);
        }
    }
}
