package vista.escritorio;

import javax.swing.*;

public class InicioEscritorio {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignorada) {
            }
            new VentanaLogin().setVisible(true);
        });
    }
}
