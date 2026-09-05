import controller.ControladorReservaciones;
import controller.UsuariosActividadesController;
import consulta.ReservaConsultaAdapter;
import view.LoginFrame;
import view.VentanaPrincipal;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ControladorReservaciones controlador = new ControladorReservaciones();
            ReservaConsultaAdapter reservaConsulta = new ReservaConsultaAdapter(controlador);
            UsuariosActividadesController usuariosCtrl =
                    new UsuariosActividadesController(controlador, reservaConsulta);

            LoginFrame login = new LoginFrame(usuariosCtrl, empleado -> {
                new VentanaPrincipal(controlador, usuariosCtrl, empleado).setVisible(true);
            });
            login.setVisible(true);
        });
    }
}
