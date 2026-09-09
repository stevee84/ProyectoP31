package service;

import exception.SesionException;
import model.CategoriaRecurso;
import model.ModeloReservaciones;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CategoriaServiceTest {

    @Test
    void testRegistrarCategoria() {
        ModeloReservaciones modelo = new ModeloReservaciones();
        SesionService sesion = new SesionService(modelo, null);
        sesion.iniciarSesion("ADMIN", "ADMIN");
        CategoriaService catService = new CategoriaService(modelo, null, sesion);

        CategoriaRecurso creada = catService.registrar("Salas de reunion");

        assertNotNull(creada);
        List<CategoriaRecurso> lista = catService.listar();
        assertTrue(lista.stream().anyMatch(c -> c.getId().equals(creada.getId())));
    }

    @Test
    void testEliminarCategoria() {
        ModeloReservaciones modelo = new ModeloReservaciones();
        SesionService sesion = new SesionService(modelo, null);
        sesion.iniciarSesion("ADMIN", "ADMIN");
        CategoriaService catService = new CategoriaService(modelo, null, sesion);

        CategoriaRecurso creada = catService.registrar("Auditorios");
        boolean eliminada = catService.eliminar(creada.getId());

        assertTrue(eliminada);
        List<CategoriaRecurso> lista = catService.listar();
        assertFalse(lista.stream().anyMatch(c -> c.getId().equals(creada.getId())));
    }

    @Test
    void testRegistrarSinSesion() {
        ModeloReservaciones modelo = new ModeloReservaciones();
        SesionService sesion = new SesionService(modelo, null);
        CategoriaService catService = new CategoriaService(modelo, null, sesion);

        assertThrows(SesionException.class, () -> catService.registrar("Sin sesion"));
    }
}
