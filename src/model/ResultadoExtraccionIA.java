package model;

import java.util.Objects;

/**
 * Resultado de intentar extraer datos de reserva desde una frase en lenguaje natural.
 * Mismo patrón que {@link ResultadoReserva}: constructor privado + métodos de fábrica,
 * para que sea imposible construir un resultado contradictorio (éxito sin datos, o
 * fracaso con datos).
 * <p>
 * "Fracaso" acá significa que la llamada a la IA en sí falló (sin conexión, error del
 * servicio, etc.) — no que los datos extraídos estén incompletos. Datos incompletos
 * SÍ son un "éxito": ver {@link DatosReservaExtraidos}, cuyos campos pueden venir nulos.
 */
public final class ResultadoExtraccionIA {

    private final boolean exito;
    private final DatosReservaExtraidos datos;
    private final String mensajeError;

    private ResultadoExtraccionIA(boolean exito, DatosReservaExtraidos datos, String mensajeError) {
        this.exito = exito;
        this.datos = datos;
        this.mensajeError = mensajeError;
    }

    public static ResultadoExtraccionIA exito(DatosReservaExtraidos datos) {
        Objects.requireNonNull(datos, "Los datos extraídos no pueden ser nulos en un resultado exitoso.");
        return new ResultadoExtraccionIA(true, datos, null);
    }

    public static ResultadoExtraccionIA fallo(String mensajeError) {
        if (mensajeError == null || mensajeError.isBlank()) {
            throw new IllegalArgumentException("Un resultado fallido debe indicar un mensaje de error.");
        }
        return new ResultadoExtraccionIA(false, null, mensajeError);
    }

    public boolean esExito() {
        return exito;
    }

    public DatosReservaExtraidos getDatos() {
        if (!exito) {
            throw new IllegalStateException("No hay datos: la extracción falló.");
        }
        return datos;
    }

    public String getMensajeError() {
        if (exito) {
            throw new IllegalStateException("No hay mensaje de error: la extracción fue exitosa.");
        }
        return mensajeError;
    }
}
