package view;

import controller.CalendarizacionRecursosController;
import controller.ControladorReservaciones;
import model.CategoriaRecurso;

import javax.swing.*;
import java.time.LocalDate;
import java.util.List;

public class PruebaCalendarizacionRecursos {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ControladorReservaciones controladorGeneral =
                    new ControladorReservaciones();

            controladorGeneral.iniciarSesion(
                    "ADMIN",
                    "ADMIN"
            );

            CategoriaRecurso categoria =
                    controladorGeneral
                            .listarCategorias()
                            .get(0);

            controladorGeneral.registrarRecurso(
                    "REC-001",
                    categoria.getId(),
                    "Recurso A"
            );

            controladorGeneral.registrarRecurso(
                    "REC-002",
                    categoria.getId(),
                    "Recurso B"
            );

            controladorGeneral.registrarFuncionario(
                    "Funcionario de prueba",
                    "F001",
                    "8888-8888"
            );

            controladorGeneral.cerrarSesion();

            controladorGeneral.iniciarSesion(
                    "F001",
                    "F001"
            );

            LocalDate fecha = LocalDate.now();

            controladorGeneral.crearReservacion(
                    List.of(categoria.getId()),
                    "Reunión de prueba",
                    fecha.atTime(10, 0),
                    fecha.atTime(12, 0)
            );

            CalendarizacionRecursosController
                    controladorCalendarizacion =
                    new CalendarizacionRecursosController(
                            controladorGeneral
                    );

            JFrame ventana =
                    new JFrame(
                            "Calendarización de recursos"
                    );

            ventana.setSize(1000, 600);

            ventana.setDefaultCloseOperation(
                    JFrame.EXIT_ON_CLOSE
            );

            ventana.setLocationRelativeTo(null);

            ventana.add(
                    new CalendarizacionRecursosPanel(
                            controladorCalendarizacion
                    )
            );

            ventana.setVisible(true);
        });
    }
}
