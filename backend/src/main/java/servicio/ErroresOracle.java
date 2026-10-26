package servicio;

import protocolo.CodigoError;
import protocolo.ServicioException;
import java.sql.SQLException;
import java.util.logging.Logger;

/** No se envían SQL, mensajes del driver, datos ni trazas al cliente. */
final class ErroresOracle {
    enum Accion { CONSULTA, ESCRITURA, ELIMINACION, RESERVA, CANCELACION }
    private static final Logger LOG = Logger.getLogger(ErroresOracle.class.getName());

    private ErroresOracle() { }

    static ServicioException traducir(SQLException e, Accion accion) {
        registrar(e);
        int codigo = Math.abs(e.getErrorCode());
        if (accion != Accion.CONSULTA) {
            if (codigo == 2292 && accion == Accion.ELIMINACION)
                return new ServicioException(CodigoError.RECURSO_EN_USO, "El registro tiene datos asociados; se conserva su historial.");
            if (codigo == 20003 && accion == Accion.CANCELACION)
                return new ServicioException(CodigoError.PERMISO, "No tiene permiso para gestionar esta reserva.");
            if (codigo == 20003 && accion == Accion.RESERVA)
                return new ServicioException(CodigoError.VALIDACION, "El funcionario de la sesión ya no existe o no admite reservas. Inicie sesión nuevamente.");
            if (codigo == 20004 && accion == Accion.RESERVA)
                return new ServicioException(CodigoError.DISPONIBILIDAD, "La disponibilidad cambió. Consulte e intente nuevamente.");
            if (codigo == 1)
                return new ServicioException(CodigoError.VALIDACION, "Ya existe un registro con ese identificador.");
            if (codigo == 20001 && accion == Accion.ELIMINACION)
                return new ServicioException(CodigoError.VALIDACION,
                        "No existe un registro eliminable con ese identificador (el administrador no se puede eliminar).");
            if (codigo == 2291 || codigo == 20001 || codigo == 20002 || codigo == 12899)
                return new ServicioException(CodigoError.VALIDACION, accion == Accion.RESERVA
                        ? "Datos inexistentes o inválidos. Revise los campos y el horario."
                        : "Datos inexistentes o inválidos. Revise los campos.");
        }
        return new ServicioException(CodigoError.PERSISTENCIA,
                "No se pudo completar la operación en Oracle. Verifique el resultado antes de reintentar.");
    }

    static void registrar(SQLException e) {
        // No registrar getMessage(): puede incluir SQL, valores o credenciales.
        LOG.warning("Fallo JDBC: código=" + e.getErrorCode() + ", estado=" + e.getSQLState());
    }
}
