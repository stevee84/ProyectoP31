package view;

import controller.ControladorReservaciones;
import controller.RecursoController;

import javax.swing.*;

public class PruebaRecursos {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ControladorReservaciones controladorGeneral =
                    new ControladorReservaciones();

            controladorGeneral.iniciarSesion(
                    "ADMIN",
                    "ADMIN"
            );

            RecursoController recursoController =
                    new RecursoController(
                            controladorGeneral
                    );

            JFrame ventana = new JFrame("Recursos");

            ventana.setSize(750, 550);
            ventana.setDefaultCloseOperation(
                    JFrame.EXIT_ON_CLOSE
            );
            ventana.setLocationRelativeTo(null);

            ventana.add(
                    new RecursosPanel(recursoController)
            );

            ventana.setVisible(true);
        });
    }
}
