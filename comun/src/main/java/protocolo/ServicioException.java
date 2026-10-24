package protocolo;

/**
 * Error que el servidor devuelve al cliente. El código permite al Frontend
 * decidir cómo mostrarlo; el mensaje ya es comprensible para el usuario.
 */
public class ServicioException extends RuntimeException {

    private final CodigoError codigo;

    public ServicioException(CodigoError codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public CodigoError getCodigo() {
        return codigo;
    }
}
