package contrato;

import java.util.regex.Pattern;

/**
 * Reglas de formato compartidas por el servidor (que siempre las aplica) y los
 * diálogos del cliente (que las adelantan para avisar antes de enviar).
 */
public final class Reglas {

    /** Solo dígitos, espacios, + y -; de 7 a 20 caracteres y al menos 7 dígitos. */
    private static final Pattern TELEFONO = Pattern.compile("(?=(?:\\D*\\d){7})[0-9+\\- ]{7,20}");

    public static final String MENSAJE_TELEFONO =
            "El teléfono solo admite dígitos, espacios, + y -, con 7 a 20 caracteres (al menos 7 dígitos).";
    public static final String MENSAJE_CLAVE_IGUAL_ID =
            "La nueva clave no puede ser igual a su identificación.";
    public static final String MENSAJE_CLAVE_IGUAL_ACTUAL =
            "La nueva clave debe ser diferente de la clave actual.";

    public static final String MENSAJE_CAMBIO_CLAVE_PENDIENTE =
            "Debe cambiar su clave antes de continuar.";
    public static final String MENSAJE_CATEGORIA_DUPLICADA =
            "Ya existe una categoría con esa descripción.";

    private Reglas() { }

    public static boolean telefonoValido(String telefono) {
        return telefono != null && TELEFONO.matcher(telefono.trim()).matches();
    }

    /** La clave nueva no puede ser el id del usuario (la clave inicial de todo funcionario). */
    public static boolean claveIgualId(String nuevaClave, String idUsuario) {
        return nuevaClave != null && idUsuario != null && nuevaClave.trim().equalsIgnoreCase(idUsuario.trim());
    }
}
