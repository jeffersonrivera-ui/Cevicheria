package vista.escritorio;

import javax.swing.*;
import java.awt.*;

// Campo de texto con un texto de ayuda gris cuando está vacío.
public class CampoHint extends JTextField {
    private final String hint;

    public CampoHint(String hint) {
        this.hint = hint;
        Estilo.estilizarCampo(this);
        setPreferredSize(new Dimension(120, 34));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getText().isEmpty() && !isFocusOwner()) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(new Color(0x9AA6BD));
            g2.setFont(Estilo.PEQUENA);
            Insets in = getInsets();
            g2.drawString(hint, in.left, (getHeight() + g2.getFontMetrics().getAscent()
                    - g2.getFontMetrics().getDescent()) / 2);
            g2.dispose();
        }
    }
}
