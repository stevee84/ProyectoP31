package protocolo;

public record Respuesta(
        long idPeticion,
        boolean exito,
        Object resultado,
        CodigoError codigoError,
        String mensaje
) implements Mensaje {

    public static Respuesta exito(long idPeticion, Object resultado) {
        return new Respuesta(idPeticion, true, resultado, null, null);
    }

    public static Respuesta error(long idPeticion, CodigoError codigo, String mensaje) {
        return new Respuesta(idPeticion, false, null, codigo, mensaje);
    }
}
