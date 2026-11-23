package vista.escritorio;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public abstract class ModuloBase extends JPanel {

    protected final Estilo.Tarjeta panelIzquierdo = new Estilo.Tarjeta(new BorderLayout(0, 12));
    // Panel que se ajusta al ancho visible del scroll (así nunca aparece scroll horizontal).
    private static final class PanelAjustable extends JPanel implements Scrollable {
        PanelAjustable(LayoutManager l) {
            super(l);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle r, int o, int d) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle r, int o, int d) {
            return 80;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    // Ancho de la franja de tarjetas de estadísticas (por defecto 700).
    protected void setAnchoEstadisticas(int ancho) {
        panelEstadisticas.setPreferredSize(new Dimension(ancho, 76));
        panelEstadisticas.revalidate();
    }

    private int anchoDerechoFijo = 0;
    private JComponent tarjetaDer;

    // Fija el ancho preferido de la columna derecha (el resto se reparte a la izquierda).
    protected void setAnchoDerecho(int ancho) {
        this.anchoDerechoFijo = ancho;
        if (tarjetaDer != null) {
            tarjetaDer.setMinimumSize(new Dimension(ancho, 100));
            panelIzquierdo.setMinimumSize(new Dimension(520, 100));
        }
    }

    protected final JPanel panelDerecho = new PanelAjustable(new BorderLayout(0, 10));
    private final JPanel panelEstadisticas = new JPanel(new GridLayout(1, 0, 12, 0));
    private final JPanel zonaNorte = new JPanel(new BorderLayout(0, 14));

    protected ModuloBase(String titulo, String subtitulo, String glifo, Color colorIcono, boolean conPanelDerecho) {
        super(new BorderLayout(0, 14));
        setOpaque(false);
        setBorder(new EmptyBorder(4, 20, 18, 20));

        JPanel cabecera = new JPanel(new BorderLayout(16, 0));
        cabecera.setOpaque(false);

        JPanel izq = new JPanel(new BorderLayout(12, 0));
        izq.setOpaque(false);
        izq.add(new Estilo.CajaIcono(glifo, colorIcono, Color.WHITE, 56), BorderLayout.WEST);
        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(Box.createVerticalGlue());
        textos.add(Estilo.etiqueta(titulo, Estilo.TITULO, Estilo.PRIMARIO_OSCURO));
        textos.add(Estilo.etiqueta("<html><body style='width:170px'>" + Estilo.escapar(subtitulo) + "</body></html>",
                Estilo.PEQUENA, Estilo.TEXTO_SUAVE));
        textos.add(Box.createVerticalGlue());
        izq.add(textos, BorderLayout.CENTER);

        panelEstadisticas.setOpaque(false);
        panelEstadisticas.setPreferredSize(new Dimension(700, 76));

        cabecera.add(izq, BorderLayout.CENTER);
        cabecera.add(panelEstadisticas, BorderLayout.EAST);
        zonaNorte.setOpaque(false);
        zonaNorte.add(cabecera, BorderLayout.NORTH);
        add(zonaNorte, BorderLayout.NORTH);

        JPanel cuerpo = new JPanel(new GridBagLayout());
        cuerpo.setOpaque(false);

        GridBagConstraints gi = new GridBagConstraints();
        gi.gridx = 0;
        gi.gridy = 0;
        gi.weightx = conPanelDerecho ? 0.6 : 1;
        gi.weighty = 1;
        gi.fill = GridBagConstraints.BOTH;
        cuerpo.add(panelIzquierdo, gi);

        if (conPanelDerecho) {
            Estilo.Tarjeta tarjetaDerecha = new Estilo.Tarjeta(new BorderLayout());
            tarjetaDerecha.setBorder(new EmptyBorder(8, 12, 8, 12));
            this.tarjetaDer = tarjetaDerecha;
            panelDerecho.setOpaque(false);
            panelDerecho.setBorder(new EmptyBorder(6, 6, 6, 6));
            JScrollPane sp = new JScrollPane(panelDerecho) {
                @Override
                public Dimension getPreferredSize() {
                    Dimension d = super.getPreferredSize();
                    if (anchoDerechoFijo > 0) {
                        return new Dimension(anchoDerechoFijo, d.height);
                    }
                    return new Dimension(Math.max(d.width, 370), d.height);
                }
            };
            sp.setBorder(BorderFactory.createEmptyBorder());
            sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            sp.setOpaque(false);
            sp.getViewport().setOpaque(false);
            sp.getVerticalScrollBar().setUnitIncrement(16);
            tarjetaDerecha.add(sp, BorderLayout.CENTER);

            GridBagConstraints gd = new GridBagConstraints();
            gd.gridx = 1;
            gd.gridy = 0;
            gd.weightx = 0.4;
            gd.weighty = 1;
            gd.fill = GridBagConstraints.BOTH;
            gd.insets = new Insets(0, 14, 0, 0);
            cuerpo.add(tarjetaDerecha, gd);
        }

        add(cuerpo, BorderLayout.CENTER);
    }

    protected void setFilaSuperior(JComponent fila) {
        zonaNorte.add(fila, BorderLayout.CENTER);
        zonaNorte.revalidate();
    }

    protected JLabel agregarEstadistica(String glifo, Color fondoIcono, String titulo, Color colorValor) {
        Estilo.Tarjeta t = new Estilo.Tarjeta(new BorderLayout(10, 0));
        t.setBorder(new EmptyBorder(10, 12, 10, 12));
        t.add(new Estilo.CajaIcono(glifo, fondoIcono, Color.WHITE, 42), BorderLayout.WEST);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        JLabel lblTitulo = Estilo.etiqueta(titulo, Estilo.PEQUENA, Estilo.TEXTO);
        JLabel valor = Estilo.etiqueta("0", new Font("Segoe UI", Font.BOLD, 24), colorValor);
        textos.add(lblTitulo);
        textos.add(valor);
        t.add(textos, BorderLayout.CENTER);

        panelEstadisticas.add(t);
        return valor;
    }

    protected JPanel tituloDePanel(String glifo, String texto) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        JLabel ic = new JLabel(glifo);
        ic.setFont(Estilo.fuenteSimbolo(glifo.codePointAt(0), 20));
        ic.setForeground(Estilo.PRIMARIO);
        p.add(ic);
        p.add(Estilo.etiqueta(texto, Estilo.SUBTITULO, Estilo.PRIMARIO_OSCURO));
        return p;
    }

    protected JPanel filaBotones(JButton... botones) {
        JPanel p = new JPanel(new GridLayout(1, botones.length, 10, 0));
        p.setOpaque(false);
        for (JButton b : botones) {
            p.add(b);
        }
        return p;
    }

    protected JPanel barraBusqueda(String etiqueta, JTextField campo, JComponent extra, JButton botonBuscar) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0;
        g.insets = new Insets(0, 0, 0, 10);
        g.anchor = GridBagConstraints.SOUTH;

        JPanel colCampo = new JPanel(new BorderLayout(0, 4));
        colCampo.setOpaque(false);
        colCampo.add(Estilo.etiqueta(etiqueta, Estilo.PEQUENA_NEGRITA, Estilo.TEXTO), BorderLayout.NORTH);
        colCampo.add(campo, BorderLayout.CENTER);
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        p.add(colCampo, g);

        int x = 1;
        if (extra != null) {
            g.gridx = x++;
            g.weightx = 0;
            g.fill = GridBagConstraints.NONE;
            p.add(extra, g);
        }
        g.gridx = x;
        g.weightx = 0;
        g.fill = GridBagConstraints.NONE;
        g.insets = new Insets(0, 0, 0, 0);
        p.add(botonBuscar, g);
        return p;
    }

    protected boolean confirmar(String mensaje) {
        return JOptionPane.showConfirmDialog(this, mensaje, "Confirmar",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    protected void aviso(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    protected void advertencia(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Atención", JOptionPane.WARNING_MESSAGE);
    }

    protected void error(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
