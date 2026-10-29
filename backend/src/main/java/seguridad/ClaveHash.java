package seguridad;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Hash de claves con PBKDF2WithHmacSHA256 y sal aleatoria por usuario.
 * Formato guardado en TUSUARIOS.CLAVE: {@code pbkdf2$iteraciones$salBase64$hashBase64}.
 * La clave en texto plano nunca se guarda ni se registra.
 */
public final class ClaveHash {

    public static final String PREFIJO = "pbkdf2";
    static final int ITERACIONES = 120_000;
    private static final int BYTES_SAL = 16;
    private static final int BITS_HASH = 256;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private ClaveHash() { }

    public static String generar(String clave) {
        byte[] sal = new byte[BYTES_SAL];
        ALEATORIO.nextBytes(sal);
        return formatear(ITERACIONES, sal, derivar(clave, sal, ITERACIONES));
    }

    /** false si la clave no coincide o el valor guardado no tiene el formato esperado. */
    public static boolean verificar(String clave, String guardado) {
        if (clave == null || guardado == null) return false;
        String[] partes = guardado.split("[$]");
        if (partes.length != 4 || !PREFIJO.equals(partes[0])) return false;
        try {
            int iteraciones = Integer.parseInt(partes[1]);
            byte[] sal = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            if (iteraciones <= 0 || sal.length == 0 || esperado.length == 0) return false;
            return MessageDigest.isEqual(esperado, derivar(clave, sal, iteraciones));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    static String formatear(int iteraciones, byte[] sal, byte[] hash) {
        Base64.Encoder b64 = Base64.getEncoder();
        return PREFIJO + "$" + iteraciones + "$" + b64.encodeToString(sal) + "$" + b64.encodeToString(hash);
    }

    private static byte[] derivar(String clave, byte[] sal, int iteraciones) {
        PBEKeySpec spec = new PBEKeySpec(clave.toCharArray(), sal, iteraciones, BITS_HASH);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("PBKDF2WithHmacSHA256 no está disponible en esta JVM.", e);
        } finally {
            spec.clearPassword();
        }
    }

    /** Genera un hash para escribirlo en un script (p. ej. DB/06): java seguridad.ClaveHash ADMIN */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Uso: java seguridad.ClaveHash <clave>");
            return;
        }
        System.out.println(generar(args[0]));
    }
}
