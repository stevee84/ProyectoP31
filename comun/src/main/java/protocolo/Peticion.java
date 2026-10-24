package protocolo;

/**
 * Llamada remota del cliente. idPeticion permite correlacionar la respuesta,
 * aunque lleguen eventos intercalados.
 */
public record Peticion(long idPeticion, Operacion operacion, Object[] datos) implements Mensaje {
}
