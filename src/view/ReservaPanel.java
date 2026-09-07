package view;

import controller.ReservaController;
import javax.swing.*;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import model.CategoriaRecurso;
import model.DatosReservaExtraidos;
import model.Reservacion;
import model.Recurso;
import model.ResultadoExtraccionIA;
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
    private JTextField txtFrase;
    private JButton btnLlenarConIA;
    private JList<CategoriaRecurso> listaCategorias;

    public ReservaPanel(ReservaController controlador) {
        this.controlador = controlador;
        setLayout(new BorderLayout(EstiloUI.GAP, EstiloUI.GAP));
        setBorder(EstiloUI.margenEstandar());
        setBackground(EstiloUI.BACKGROUND);

        // --- Frase en lenguaje natural + boton para llenar con IA ---
        txtFrase = new JTextField();
        EstiloUI.estilizarCampo(txtFrase);
        btnLlenarConIA = new JButton("Extraer con IA");
        EstiloUI.estilizarBotonPrimario(btnLlenarConIA);
        btnLlenarConIA.addActionListener(e -> llenarConIA());

        JPanel panelFrase = new JPanel(new BorderLayout(EstiloUI.GAP, EstiloUI.GAP));
        panelFrase.setBackground(EstiloUI.BACKGROUND);
        JLabel lblFrase = new JLabel("Frase:");
        EstiloUI.estilizarEtiqueta(lblFrase);
        panelFrase.add(lblFrase, BorderLayout.WEST);
        panelFrase.add(txtFrase, BorderLayout.CENTER);
        panelFrase.add(btnLlenarConIA, BorderLayout.EAST);

        // --- Campos de texto del formulario ---
        txtActividad = new JTextField(20);
        txtFecha = new JTextField("aaaa-mm-dd", 10);
        txtHoraInicio = new JTextField("HH:mm", 6);
        txtHoraFin = new JTextField("HH:mm", 6);
        EstiloUI.estilizarCampo(txtActividad);
        EstiloUI.estilizarCampo(txtFecha);
        EstiloUI.estilizarCampo(txtHoraInicio);
        EstiloUI.estilizarCampo(txtHoraFin);

        JPanel panelCampos = new JPanel(new GridBagLayout());
        panelCampos.setBackground(EstiloUI.BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String[] etiquetas = {"Actividad:", "Fecha:", "Hora inicio:", "Hora fin:"};
        JTextField[] campos = {txtActividad, txtFecha, txtHoraInicio, txtHoraFin};
        for (int i = 0; i < etiquetas.length; i++) {
            JLabel lbl = new JLabel(etiquetas[i]);
            EstiloUI.estilizarEtiqueta(lbl);
            gbc.gridx = (i % 2) * 2;
            gbc.gridy = i / 2;
            gbc.anchor = GridBagConstraints.EAST;
            gbc.weightx = 0;
            panelCampos.add(lbl, gbc);
            gbc.gridx = (i % 2) * 2 + 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 1.0;
            panelCampos.add(campos[i], gbc);
        }

        JPanel panelCamposCompleto = new JPanel(new BorderLayout(EstiloUI.GAP, EstiloUI.GAP));
        panelCamposCompleto.setBackground(EstiloUI.BACKGROUND);
        panelCamposCompleto.add(panelFrase, BorderLayout.NORTH);
        panelCamposCompleto.add(panelCampos, BorderLayout.SOUTH);

        // --- Lista de categorias, seleccion multiple ---
        listaCategorias = new JList<>();
        listaCategorias.setFont(EstiloUI.NORMAL);
        listaCategorias.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listaCategorias.setVisibleRowCount(4);
        listaCategorias.setSelectionBackground(EstiloUI.PRIMARY);
        listaCategorias.setSelectionForeground(Color.WHITE);
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
        scrollCategorias.setBorder(EstiloUI.crearTitledBorder("Categorias requeridas (seleccion multiple)"));
        cargarCategorias();
        cargarReservas();

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                cargarCategorias();
                cargarReservas();
            }
        });

        JButton btnAplicar = new JButton("Aplicar");
        JButton btnCancelarSeleccionada = new JButton("Cancelar reserva seleccionada");
        JButton btnLimpiar = new JButton("Limpiar");
        JButton btnGenerarPdf = new JButton("Generar PDF");
        btnGenerarPdf.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Funcionalidad de PDF pendiente de implementacion.", "PDF", JOptionPane.INFORMATION_MESSAGE));

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, EstiloUI.GAP, EstiloUI.GAP));
        panelBotones.setBackground(EstiloUI.BACKGROUND);
        for (JButton btn : new JButton[]{btnAplicar, btnCancelarSeleccionada, btnLimpiar, btnGenerarPdf}) {
            EstiloUI.estilizarBoton(btn);
            panelBotones.add(btn);
        }

        btnAplicar.addActionListener(e -> aplicarReserva());
        btnCancelarSeleccionada.addActionListener(e -> cancelarReservaSeleccionada());
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        JPanel panelFormulario = new JPanel(new BorderLayout(EstiloUI.GAP, EstiloUI.GAP));
        panelFormulario.setBackground(EstiloUI.BACKGROUND);
        panelFormulario.setBorder(EstiloUI.crearTitledBorder("Nueva reserva"));
        panelFormulario.add(panelCamposCompleto, BorderLayout.NORTH);
        panelFormulario.add(scrollCategorias, BorderLayout.CENTER);
        panelFormulario.add(panelBotones, BorderLayout.SOUTH);

        // --- Tabla "Mis reservas" ---
        DefaultTableModel modeloTabla = new DefaultTableModel(
                new String[]{"Id", "Actividad", "Fecha", "Horario", "Recursos", "Estado"}, 0
        );
        tablaReservas = new JTable(modeloTabla);
        tablaReservas.setDefaultEditor(Object.class, null);
        EstiloUI.estilizarTabla(tablaReservas);

        JScrollPane scrollTabla = new JScrollPane(tablaReservas);
        scrollTabla.setBorder(EstiloUI.crearTitledBorder("Mis reservas"));

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
                    "Datos invalidos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<CategoriaRecurso> categoriasSeleccionadas = listaCategorias.getSelectedValuesList();
        if (categoriasSeleccionadas.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Debe seleccionar al menos una categoria.",
                    "Datos invalidos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<String> idsCategorias = categoriasSeleccionadas.stream()
                .map(CategoriaRecurso::getId)
                .toList();

        LocalDateTime inicio = LocalDateTime.of(fecha, horaInicio);
        LocalDateTime fin = LocalDateTime.of(fecha, horaFin);

        ResultadoReserva resultado;
        try {
            resultado = controlador.crearReservacion(idsCategorias, txtActividad.getText().trim(), inicio, fin);
        } catch (RuntimeException error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "Datos invalidos",
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
        }
    }

    private void limpiarFormulario() {
        txtFrase.setText("");
        txtActividad.setText("");
        txtFecha.setText("aaaa-mm-dd");
        txtHoraInicio.setText("HH:mm");
        txtHoraFin.setText("HH:mm");
        listaCategorias.clearSelection();
    }

    private void llenarConIA() {
        String frase = txtFrase.getText().trim();
        if (frase.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Escriba una frase describiendo la reserva antes de extraer.",
                    "Frase vacia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        btnLlenarConIA.setEnabled(false);
        btnLlenarConIA.setText("Extrayendo...");

        new SwingWorker<ResultadoExtraccionIA, Void>() {
            @Override
            protected ResultadoExtraccionIA doInBackground() {
                return controlador.extraerDatosDesdeFrase(frase);
            }

            @Override
            protected void done() {
                btnLlenarConIA.setEnabled(true);
                btnLlenarConIA.setText("Extraer con IA");
                try {
                    ResultadoExtraccionIA resultado = get();

                    if (!resultado.esExito()) {
                        JOptionPane.showMessageDialog(ReservaPanel.this,
                                "No se pudo extraer la informacion: " + resultado.getMensajeError()
                                        + "\nPuede completar el formulario manualmente.",
                                "Extraccion con IA fallo", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    DatosReservaExtraidos datos = resultado.getDatos();

                    if (datos.descripcionActividad() != null) {
                        txtActividad.setText(datos.descripcionActividad());
                    }
                    if (datos.fecha() != null) {
                        txtFecha.setText(datos.fecha().format(FORMATO_FECHA));
                    }
                    if (datos.horaInicio() != null) {
                        txtHoraInicio.setText(datos.horaInicio().format(FORMATO_HORA));
                    }
                    if (datos.horaFin() != null) {
                        txtHoraFin.setText(datos.horaFin().format(FORMATO_HORA));
                    }
                    if (!datos.idsCategorias().isEmpty()) {
                        seleccionarCategoriasPorId(datos.idsCategorias());
                    }

                    JOptionPane.showMessageDialog(ReservaPanel.this,
                            "Formulario rellenado con IA. Revise y modifique lo que haga falta antes de aplicar.");
                } catch (Exception error) {
                    JOptionPane.showMessageDialog(ReservaPanel.this,
                            "Error al extraer datos con IA: " + error.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void seleccionarCategoriasPorId(List<String> idsCategorias) {
        List<CategoriaRecurso> todas = controlador.listarCategorias();
        int[] indices = java.util.stream.IntStream.range(0, todas.size())
                .filter(i -> idsCategorias.contains(todas.get(i).getId()))
                .toArray();
        listaCategorias.setSelectedIndices(indices);
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
        } catch (RuntimeException error) {
            JOptionPane.showMessageDialog(this, error.getMessage(),
                    "No se pudo cancelar", JOptionPane.WARNING_MESSAGE);
        }
    }
}
