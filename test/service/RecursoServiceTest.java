package service;

import exception.ValidacionException;
import model.CategoriaRecurso;
import model.ModeloReservaciones;
import model.Recurso;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RecursoServiceTest {

    @Test
    void testRegistrarRecurso() {
        ModeloReservaciones modelo = new ModeloReservaciones();
        SesionService sesion = new SesionService(modelo, null);
        sesion.iniciarSesion("ADMIN", "ADMIN");
        CategoriaService catService = new CategoriaService(modelo, null, sesion);
        RecursoService recursoService = new RecursoService(modelo, null, sesion);

        CategoriaRecurso categoria = catService.registrar("Laboratorios");
        boolean ok = recursoService.registrar("R001", categoria.getId(), "Laboratorio de Computo 1");

        assertTrue(ok);
        List<Recurso> recursos = recursoService.listarPorCategoria(categoria.getId());
        assertTrue(recursos.stream().anyMatch(r -> r.getCodigo().equals("R001")));
    }

    @Test
    void testEliminarRecurso() {
        ModeloReservaciones modelo = new ModeloReservaciones();
        SesionService sesion = new SesionService(modelo, null);
        sesion.iniciarSesion("ADMIN", "ADMIN");
        CategoriaService catService = new CategoriaService(modelo, null, sesion);
        RecursoService recursoService = new RecursoService(modelo, null, sesion);

        CategoriaRecurso categoria = catService.registrar("Salones");
        recursoService.registrar("R002", categoria.getId(), "Salon 202");

        boolean eliminado = recursoService.eliminar("R002");

        assertTrue(eliminado);
        assertThrows(ValidacionException.class, () -> recursoService.buscar("R002"));
    }
}
