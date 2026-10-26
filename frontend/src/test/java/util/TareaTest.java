package util;

import org.junit.jupiter.api.Test;
import protocolo.CodigoError;
import protocolo.ServicioException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TareaTest {

    @Test
    void errorAlMostrarElResultadoSeEntregaComoErrorInesperado() throws Exception {
        CompletableFuture<ServicioException> recibido = new CompletableFuture<>();

        Tarea.ejecutar(() -> "dato",
                dato -> { throw new IllegalStateException("falla en la vista"); },
                recibido::complete);

        ServicioException e = recibido.get(3, TimeUnit.SECONDS);
        assertEquals(CodigoError.INTERNO, e.getCodigo());
        assertEquals("Ocurrió un error inesperado.", e.getMessage());
    }
}
