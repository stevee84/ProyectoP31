import controller.AplicacionController;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        // El tema del sistema debe aplicarse antes de crear cualquier componente.
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Si no está disponible, Swing sigue con su tema por defecto.
        }
        SwingUtilities.invokeLater(() -> new AplicacionController().iniciar());
    }
}
