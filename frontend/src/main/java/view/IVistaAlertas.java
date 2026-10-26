package view;

import model.Alerta;

import java.util.List;
import java.util.function.IntConsumer;

/**
 * Lo que AlertasController necesita de la vista. Separarlo permite probar el
 * controlador con una vista simulada, sin ventanas ni JOptionPane.
 */
public interface IVistaAlertas {

    void mostrarAlertas(List<Alerta> alertas);

    void setOnVer(IntConsumer indice);

    void setOnDescartar(IntConsumer indice);

    void mostrarDetalle(Alerta alerta);

    /** true solo si el usuario confirma que desea quitar la alerta de la lista. */
    boolean confirmarDescarte(Alerta alerta);
}
