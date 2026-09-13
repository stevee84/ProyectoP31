package controller;

import model.Empleado;
import model.ResultadoSesion;
import model.SesionModel;
import view.CambioClaveDialog;
import view.LoginFrame;

import java.util.function.Consumer;

public class LoginController {

    private final LoginFrame view;
    private final SesionModel modelo;
    private final Consumer<Empleado> onLoginExitoso;

    public LoginController(LoginFrame view, SesionModel modelo, Consumer<Empleado> onLoginExitoso) {
        this.view = view;
        this.modelo = modelo;
        this.onLoginExitoso = onLoginExitoso;

        view.setOnLogin(this::login);
    }

    private void login() {
        String id = view.getId();
        String pass = view.getPassword();

        if (id.isBlank() || pass.isBlank()) {
            view.mostrarError("Debe ingresar identificacion y contrasena.");
            return;
        }

        try {
            ResultadoSesion resultado = modelo.iniciarSesion(id, pass);
            if (resultado.empleado() == null) {
                view.mostrarError("Credenciales incorrectas.");
                return;
            }

            if (resultado.requiereCambioContraseña()) {
                CambioClaveDialog dialogo = new CambioClaveDialog(view);
                dialogo.setOnGuardar(nueva -> {
                    try {
                        modelo.cambiarContrasena(nueva);
                        dialogo.marcarCambiada();
                    } catch (Exception e) {
                        dialogo.mostrarError(e.getMessage());
                    }
                });
                dialogo.setVisible(true);

                if (!dialogo.fueCambiada()) {
                    modelo.cerrarSesion();
                    return;
                }
            }

            view.cerrar();
            onLoginExitoso.accept(resultado.empleado());
        } catch (Exception e) {
            view.mostrarError(e.getMessage());
        }
    }
}
