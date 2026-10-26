package controller;

import dto.CategoriaDTO;
import dto.RecursoDTO;
import model.CategoriaModel;
import model.RecursoModel;
import util.Tarea;
import view.RecursoDialog;
import view.RecursosPanel;

import javax.swing.SwingUtilities;
import java.awt.Frame;
import java.util.List;

public class RecursoController {

    private final RecursosPanel view;
    private final RecursoModel modelo;
    private final CategoriaModel categoriaModelo;
    /** Identifica la última consulta del listado; las respuestas anteriores se descartan. */
    private int ultimaSolicitudRecursos;

    public RecursoController(RecursosPanel view, RecursoModel modelo, CategoriaModel categoriaModelo) {
        this.view = view;
        this.modelo = modelo;
        this.categoriaModelo = categoriaModelo;

        view.setOnAbrirAgregar(this::abrirDialogoAgregar);
        view.setOnAbrirModificar(args -> {
            if (args.length > 0) abrirDialogoModificar(args[0]);
        });
        view.setOnEliminar(this::eliminar);
        view.setOnFiltrar(this::cargarRecursos);
        view.setOnVisible(this::cargarCategorias);

        cargarCategorias();
        cargarRecursos();
    }

    private Frame getFrame() {
        return (Frame) SwingUtilities.getWindowAncestor(view);
    }

    private void abrirDialogoAgregar() {
        conCategorias(categorias -> {
            RecursoDialog dialog = new RecursoDialog(getFrame(), false, "", null, "", categorias);
            dialog.setOnGuardar(datos -> {
                dialog.setOcupado(true);
                Tarea.ejecutar(
                        () -> { modelo.registrar(datos[0], datos[1], datos[2]); return null; },
                        r -> {
                            dialog.cerrar();
                            view.mostrarMensaje("Recurso registrado.");
                            cargarRecursos();
                        },
                        e -> dialog.mostrarError(e.getMessage()));
            });
            dialog.setVisible(true);
        });
    }

    private void abrirDialogoModificar(int filaModelo) {
        RecursoDTO recurso = view.getRecurso(filaModelo);
        conCategorias(categorias -> {
            RecursoDialog dialog = new RecursoDialog(getFrame(), true,
                    recurso.codigo(), recurso.categoria(), recurso.descripcion(), categorias);
            dialog.setOnGuardar(nuevos -> {
                dialog.setOcupado(true);
                Tarea.ejecutar(
                        () -> { modelo.actualizar(nuevos[0], nuevos[1], nuevos[2]); return null; },
                        r -> {
                            dialog.cerrar();
                            view.mostrarMensaje("Recurso modificado.");
                            cargarRecursos();
                        },
                        e -> dialog.mostrarError(e.getMessage()));
            });
            dialog.setVisible(true);
        });
    }

    private void eliminar(int filaModelo) {
        String codigo = view.getRecurso(filaModelo).codigo();
        if (!view.confirmarAccion("Desea eliminar el recurso " + codigo + "?")) {
            return;
        }
        Tarea.ejecutar(() -> { modelo.eliminar(codigo); return null; },
                r -> {
                    view.mostrarMensaje("Recurso eliminado.");
                    cargarRecursos();
                },
                e -> view.mostrarError(e.getMessage()));
    }

    /** Los diálogos siempre usan el catálogo actualizado del servidor. */
    private void conCategorias(java.util.function.Consumer<List<CategoriaDTO>> accion) {
        Tarea.ejecutar(categoriaModelo::listar, accion, e -> view.mostrarError(e.getMessage()));
    }

    private void cargarCategorias() {
        Tarea.ejecutar(categoriaModelo::listar, view::cargarCategorias, e -> view.mostrarError(e.getMessage()));
    }

    private void cargarRecursos() {
        CategoriaDTO filtro = view.getCategoriaFiltro();
        String idCategoria = filtro != null ? filtro.id() : null;
        int solicitud = ++ultimaSolicitudRecursos;
        Tarea.ejecutar(() -> modelo.listar(idCategoria),
                recursos -> {
                    if (solicitud == ultimaSolicitudRecursos) {
                        view.cargarRecursos(recursos);
                    }
                },
                e -> {
                    if (solicitud == ultimaSolicitudRecursos) {
                        view.mostrarError(e.getMessage());
                    }
                });
    }
}
