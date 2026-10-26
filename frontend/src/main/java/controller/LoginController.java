package controller;

import dto.ResultadoSesionDTO;
import dto.UsuarioDTO;
import model.SesionModel;
import util.Tarea;
import view.CambioClaveDialog;
import view.LoginFrame;

import java.util.function.Consumer;

public class LoginController {

    private final LoginFrame view;
    private final SesionModel modelo;
    private final Consumer<UsuarioDTO> onLoginExitoso;

    public LoginController(LoginFrame view, SesionModel modelo, Consumer<UsuarioDTO> onLoginExitoso) {
        this.view = view;
        this.modelo = modelo;
        this.onLoginExitoso = onLoginExitoso;

        view.setOnLogin(this::login);
    }

    private void login() {
        String id = view.getId();
        String clave = view.getPassword();

        if (id.isBlank() || clave.isBlank()) {
            view.mostrarError("Debe ingresar identificacion y contrasena.");
            return;
        }

        view.setOcupado(true);
        Tarea.ejecutar(() -> modelo.iniciarSesion(id, clave),
                resultado -> {
                    view.setOcupado(false);
                    continuar(resultado);
                },
                error -> {
                    view.setOcupado(false);
                    view.mostrarError(error.getMessage());
                });
    }

    private void continuar(ResultadoSesionDTO resultado) {
        if (resultado.requiereCambioClave() && !pedirCambioClave()) {
            // Sin cambiar la clave no se permite entrar en el primer ingreso.
            Tarea.ejecutar(() -> { modelo.cerrarSesion(); return null; }, r -> { }, e -> { });
            return;
        }
        view.cerrar();
        onLoginExitoso.accept(resultado.usuario());
    }

    /** Diálogo modal; devuelve true si el servidor confirmó el cambio. */
    private boolean pedirCambioClave() {
        CambioClaveDialog dialogo = new CambioClaveDialog(view);
        dialogo.setOnGuardar(nueva -> {
            dialogo.setOcupado(true);
            Tarea.ejecutar(
                    () -> { modelo.cambiarClave(nueva); return null; },
                    r -> dialogo.marcarCambiada(),
                    e -> dialogo.mostrarError(e.getMessage()));
        });
        dialogo.setVisible(true);
        return dialogo.fueCambiada();
    }
}
