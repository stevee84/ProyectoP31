package view;

import controller.ControladorReservaciones;
import controller.ReservaController;
import model.DatosReservaExtraidos;
import model.ExtractorReservaIA;
import model.ExtractorReservaIAFalso;
import model.ResultadoExtraccionIA;
import model.ResultadoReserva;

import javax.swing.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class PruebaReservaPanel {

    public static void main(String[] args) {
        ControladorReservaciones controladorGeneral = new ControladorReservaciones();

        // 1) Como admin: preparar datos de prueba (funcionario y recursos).
        controladorGeneral.iniciarSesion("ADMIN", "ADMIN");
        controladorGeneral.registrarFuncionario("Juan Perez", "111", "3323");

        String idCategoriaLaptop = controladorGeneral.listarCategorias().stream()
                .filter(c -> c.getDescripcion().equals("Laptop Windows"))
                .findFirst().orElseThrow().getId();
        String idCategoriaSala10 = controladorGeneral.listarCategorias().stream()
                .filter(c -> c.getDescripcion().equals("Sala para 10 personas"))
                .findFirst().orElseThrow().getId();
        String idCategoriaSalaJuntas = controladorGeneral.listarCategorias().stream()
                .filter(c -> c.getDescripcion().equals("Sala de juntas"))
                .findFirst().orElseThrow().getId();

        controladorGeneral.registrarRecurso("LAP-1", idCategoriaLaptop, "Laptop #238715");
        controladorGeneral.registrarRecurso("SALA-1", idCategoriaSala10, "Sala 1 primer piso");
        controladorGeneral.registrarRecurso("JUNTAS-1", idCategoriaSalaJuntas, "Sala de juntas piso 2");
        controladorGeneral.cerrarSesion();

        // 2) Como funcionario: crear una reserva de prueba para que la tabla no arranque vacía.
        controladorGeneral.iniciarSesion("111", "111");
        ResultadoReserva resultado = controladorGeneral.crearReservacion(
                List.of(idCategoriaLaptop), "Reunion de prueba",
                LocalDateTime.of(2026, 9, 5, 9, 0), LocalDateTime.of(2026, 9, 5, 10, 0));
        System.out.println("Reserva de prueba creada: " + resultado.esExito());

        // 3) Abrir la ventana ya logueado como ese mismo funcionario.
        // Extractor falso: simula que la IA identificó actividad, fecha, horas y la
        // categoría "Laptop Windows" a partir de una frase.
        ExtractorReservaIA extractorDePrueba = new ExtractorReservaIAFalso(
                ResultadoExtraccionIA.exito(new DatosReservaExtraidos(
                        "Reunion extraida por IA",
                        LocalDate.of(2026, 9, 8),
                        LocalTime.of(14, 0),
                        LocalTime.of(15, 0),
                        List.of(idCategoriaLaptop))));

        ReservaController reservaController = new ReservaController(controladorGeneral, extractorDePrueba);

        JFrame ventana = new JFrame("Reservas");
        ventana.setSize(700, 500);
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setLocationRelativeTo(null);
        ventana.add(new ReservaPanel(reservaController));
        ventana.setVisible(true);
    }
}
