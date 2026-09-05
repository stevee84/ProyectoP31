package consulta;

import model.CategoriaRecurso;
import model.Recurso;

import java.util.List;

public interface CatalogoConsulta {
    List<CategoriaRecurso> listarCategorias();
    List<Recurso> listarRecursosPorCategoria(String categoriaId);
}
