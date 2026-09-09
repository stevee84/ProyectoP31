package controller;

import model.Empleado;
import service.SesionService;
import view.CambioClaveDialog;
import view.LoginFrame;

import java.util.function.Consumer;

public class LoginController {

    private final LoginFrame view;
    private final SesionService service;
    private final Consumer<Empleado> onLoginExitoso;

    public LoginController(LoginFrame view, SesionService service, Consumer<Empleado> onLoginExitoso) {
        this.view = view;
        this.service = service;
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
            SesionService.ResultadoSesion resultado = service.iniciarSesion(id, pass);
            if (resultado.empleado() == null) {
                view.mostrarError("Credenciales incorrectas.");
                return;
            }

            if (resultado.requiereCambioContraseña()) {
                CambioClaveDialog dialogo = new CambioClaveDialog(view);
                dialogo.setOnGuardar(nueva -> {
                    try {
                        service.cambiarContrasena(nueva);
                        dialogo.marcarCambiada();
                    } catch (Exception e) {
                        dialogo.mostrarError(e.getMessage());
                    }
                });
                dialogo.setVisible(true);

                if (!dialogo.fueCambiada()) {
                    service.cerrarSesion();
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
