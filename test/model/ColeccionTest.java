package model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ColeccionTest {

    @Test
    void testAgregar() {
        Coleccion<String> coleccion = new Coleccion<>();
        coleccion.agregar("a");
        coleccion.agregar("b");
        coleccion.agregar("c");

        assertEquals(3, coleccion.tamanio());
        assertFalse(coleccion.estaVacia());
    }

    @Test
    void testBuscar() {
        Coleccion<Integer> coleccion = new Coleccion<>();
        coleccion.agregar(1);
        coleccion.agregar(2);
        coleccion.agregar(3);

        Integer encontrado = coleccion.buscar(n -> n == 2);
        Integer noEncontrado = coleccion.buscar(n -> n == 99);

        assertEquals(2, encontrado);
        assertNull(noEncontrado);
    }

    @Test
    void testFiltrar() {
        Coleccion<Integer> coleccion = new Coleccion<>();
        for (int i = 1; i <= 5; i++) {
            coleccion.agregar(i);
        }

        Coleccion<Integer> pares = coleccion.filtrar(n -> n % 2 == 0);

        assertEquals(2, pares.tamanio());
        List<Integer> lista = pares.listar();
        assertTrue(lista.contains(2));
        assertTrue(lista.contains(4));
    }

    @Test
    void testEliminarSi() {
        Coleccion<Integer> coleccion = new Coleccion<>();
        for (int i = 1; i <= 5; i++) {
            coleccion.agregar(i);
        }

        boolean cambio = coleccion.eliminarSi(n -> n > 3);

        assertTrue(cambio);
        assertEquals(3, coleccion.tamanio());
        assertFalse(coleccion.listar().contains(4));
        assertFalse(coleccion.listar().contains(5));
    }
}
