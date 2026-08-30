package view;

import controller.CategoriaController;
import controller.ControladorReservaciones;

import javax.swing.*;

public class PruebaCategorias {

    public static void main(String[] args) {
        ControladorReservaciones controladorGeneral =
                new ControladorReservaciones();

        controladorGeneral.iniciarSesion(
                "ADMIN",
                "ADMIN"
        );

        CategoriaController categoriaController =
                new CategoriaController(controladorGeneral);

        JFrame ventana = new JFrame("Categorias");

        ventana.setSize(700, 500);
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setLocationRelativeTo(null);

        ventana.add(
                new CategoriasPanel(categoriaController)
        );

        ventana.setVisible(true);
    }
}
