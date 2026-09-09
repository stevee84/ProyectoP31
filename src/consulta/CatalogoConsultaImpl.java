package consulta;

import model.CategoriaRecurso;
import model.ModeloReservaciones;
import model.Recurso;

import java.util.List;

public class CatalogoConsultaImpl implements CatalogoConsulta {

    private final ModeloReservaciones modelo;

    public CatalogoConsultaImpl(ModeloReservaciones modelo) {
        this.modelo = modelo;
    }

    @Override
    public List<CategoriaRecurso> listarCategorias() {
        return modelo.listarCategorias();
    }

    @Override
    public List<Recurso> listarRecursosPorCategoria(String categoriaId) {
        return modelo.listarRecursosPorCategoria(categoriaId);
    }
}
