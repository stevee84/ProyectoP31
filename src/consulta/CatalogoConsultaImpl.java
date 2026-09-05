package consulta;

import controller.ControladorReservaciones;
import model.CategoriaRecurso;
import model.Recurso;

import java.util.List;

public class CatalogoConsultaImpl implements CatalogoConsulta {

    private final ControladorReservaciones controlador;

    public CatalogoConsultaImpl(ControladorReservaciones controlador) {
        this.controlador = controlador;
    }

    @Override
    public List<CategoriaRecurso> listarCategorias() {
        return controlador.listarCategorias();
    }

    @Override
    public List<Recurso> listarRecursosPorCategoria(String categoriaId) {
        return controlador.listarRecursosPorCategoria(categoriaId);
    }
}
