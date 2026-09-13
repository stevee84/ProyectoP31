package model;

/**
 * Resultado de un intento de inicio de sesion.
 */
public record ResultadoSesion(Empleado empleado, boolean requiereCambioContraseña) {
}
