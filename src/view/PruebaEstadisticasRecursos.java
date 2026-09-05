package view;

import controller.ControladorReservaciones;
import controller.EstadisticasRecursosController;
import model.CategoriaRecurso;

import javax.swing.*;
import java.time.LocalDate;
import java.util.List;

public class PruebaEstadisticasRecursos {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ControladorReservaciones controladorGeneral =
                    new ControladorReservaciones();

            controladorGeneral.iniciarSesion(
                    "ADMIN",
                    "ADMIN"
            );

            List<CategoriaRecurso> categorias =
                    controladorGeneral.listarCategorias();

            CategoriaRecurso categoria1 =
                    categorias.get(0);

            CategoriaRecurso categoria2 =
                    categorias.get(1);

            controladorGeneral.registrarRecurso(
                    "EST-001",
                    categoria1.getId(),
                    "Recurso estadístico 1"
            );

            controladorGeneral.registrarRecurso(
                    "EST-002",
                    categoria2.getId(),
                    "Recurso estadístico 2"
            );

            controladorGeneral.registrarFuncionario(
                    "Funcionario de estadísticas",
                    "F002",
                    "8888-9999"
            );

            controladorGeneral.cerrarSesion();

            controladorGeneral.iniciarSesion(
                    "F002",
                    "F002"
            );

            LocalDate fecha = LocalDate.now();

            controladorGeneral.crearReservacion(
                    List.of(categoria1.getId()),
                    "Actividad 1",
                    fecha.atTime(8, 0),
                    fecha.atTime(9, 0)
            );

            controladorGeneral.crearReservacion(
                    List.of(categoria1.getId()),
                    "Actividad 2",
                    fecha.atTime(10, 0),
                    fecha.atTime(11, 0)
            );

            controladorGeneral.crearReservacion(
                    List.of(categoria2.getId()),
                    "Actividad 3",
                    fecha.atTime(12, 0),
                    fecha.atTime(13, 0)
            );

            EstadisticasRecursosController controladorEstadisticas =
                    new EstadisticasRecursosController(
                            controladorGeneral
                    );

            JFrame ventana =
                    new JFrame(
                            "Estadísticas de recursos"
                    );

            ventana.setSize(900, 550);

            ventana.setDefaultCloseOperation(
                    JFrame.EXIT_ON_CLOSE
            );

            ventana.setLocationRelativeTo(null);

            ventana.add(
                    new EstadisticasRecursosPanel(
                            controladorEstadisticas
                    )
            );

            ventana.setVisible(true);
        });
    }
}
