package model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Coleccion generica que encapsula el almacenamiento de elementos.
 * Reemplaza el uso directo de List en el proyecto.
 */
public class Coleccion<T> implements Iterable<T> {
    private final List<T> elementos;

    public Coleccion() { this.elementos = new ArrayList<>(); }

    public Coleccion(List<T> elementos) { this.elementos = new ArrayList<>(elementos); }

    public void agregar(T elemento) { elementos.add(elemento); }

    public boolean eliminar(T elemento) { return elementos.remove(elemento); }

    public boolean eliminarSi(Predicate<T> condicion) { return elementos.removeIf(condicion); }

    public T buscar(Predicate<T> condicion) {
        return elementos.stream().filter(condicion).findFirst().orElse(null);
    }

    public Coleccion<T> filtrar(Predicate<T> condicion) {
        return new Coleccion<>(elementos.stream().filter(condicion).toList());
    }

    public List<T> listar() { return List.copyOf(elementos); }

    public int tamanio() { return elementos.size(); }

    public boolean estaVacia() { return elementos.isEmpty(); }

    public boolean contiene(T elemento) { return elementos.contains(elemento); }

    public Stream<T> stream() { return elementos.stream(); }

    public int indiceDe(T elemento) { return elementos.indexOf(elemento); }

    public void reemplazarEn(int indice, T elemento) { elementos.set(indice, elemento); }

    @Override
    public Iterator<T> iterator() { return elementos.iterator(); }
}
