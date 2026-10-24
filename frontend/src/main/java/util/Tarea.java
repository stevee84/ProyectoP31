package util;

import protocolo.CodigoError;
import protocolo.ServicioException;

import javax.swing.SwingWorker;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Ejecuta una llamada al servidor fuera del hilo de Swing y entrega el resultado
 * (o el error) de vuelta en el hilo de Swing. Así la interfaz no se congela.
 *
 * <pre>
 * Tarea.ejecutar(() -> servicio.listarCategorias(),
 *         categorias -> vista.mostrar(categorias),
 *         error -> vista.mostrarError(error.getMessage()));
 * </pre>
 */
public final class Tarea {

    private Tarea() {
    }

    public static <T> void ejecutar(Supplier<T> llamada, Consumer<T> alTerminar, Consumer<ServicioException> alFallar) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() {
                return llamada.get();
            }

            @Override
            protected void done() {
                try {
                    alTerminar.accept(get());
                } catch (ExecutionException e) {
                    alFallar.accept(e.getCause() instanceof ServicioException s
                            ? s
                            : new ServicioException(CodigoError.INTERNO, "Ocurrió un error inesperado."));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }
}
