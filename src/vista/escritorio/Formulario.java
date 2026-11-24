package vista.escritorio;

import javax.swing.*;
import java.awt.*;

public class Formulario extends JPanel {

    private int fila = 0;

    public Formulario() {
        super(new GridBagLayout());
        setOpaque(false);
    }

    public void agregar(String etiqueta, boolean obligatorio, JComponent campo) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = fila;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(6, 0, 6, 12);
        JLabel lbl = Estilo.etiquetaCampo(etiqueta, obligatorio);
        lbl.setMinimumSize(lbl.getPreferredSize());
        add(lbl, g);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = fila;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(6, 0, 6, 0);
        add(campo, c);
        fila++;
    }

    public void agregarCompleto(JComponent componente) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = fila;
        g.gridwidth = 2;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 0, 6, 0);
        add(componente, g);
        fila++;
    }
}
