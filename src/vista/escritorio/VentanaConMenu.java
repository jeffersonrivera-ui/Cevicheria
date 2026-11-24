package vista.escritorio;

import modelo.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class VentanaConMenu extends JFrame {

    private final CardLayout cartas = new CardLayout();
    private final JPanel contenido = new JPanel(cartas);
    private final JPanel menu;
    private final Map<String, ItemMenu> items = new LinkedHashMap<>();
    private final Map<String, String> grupoDeSeccion = new LinkedHashMap<>();
    private final JLabel lblGrupo = Estilo.etiqueta("", Estilo.FUENTE, Estilo.TEXTO_SUAVE);
    private final JLabel lblSeccion = Estilo.etiqueta("", Estilo.SUBTITULO, Estilo.PRIMARIO_OSCURO);
    private final String rol;
    private String grupoActual = "";
    protected final Usuario usuario;

    private static class ItemMenu extends JButton {
        private boolean activo;
        private boolean encima;

        ItemMenu(String texto) {
            super(texto);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setForeground(Color.WHITE);
            setFont(Estilo.FUENTE);
            setText(Estilo.html(texto, getFont()));
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(new EmptyBorder(10, 16, 10, 12));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    encima = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    encima = false;
                    repaint();
                }
            });
        }

        void setActivo(boolean a) {
            this.activo = a;
            setFont(a ? Estilo.NEGRITA : Estilo.FUENTE);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (activo) {
                g2.setColor(Estilo.MENU_ACTIVO);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            } else if (encima) {
                g2.setColor(new Color(255, 255, 255, 30));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    protected VentanaConMenu(String titulo, String rol, Usuario usuario) {
        super(titulo);
        this.usuario = usuario;
        this.rol = rol;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 760);
        setMinimumSize(new Dimension(1100, 650));
        setLocationRelativeTo(null);

        menu = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, Estilo.MENU_ARRIBA, 0, getHeight(), Estilo.MENU_ABAJO));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(new EmptyBorder(18, 12, 14, 12));
        menu.setPreferredSize(new Dimension(230, 0));

        JPanel logo = new JPanel(new BorderLayout(10, 0));
        logo.setOpaque(false);
        logo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        logo.add(new Estilo.Logo(), BorderLayout.WEST);
        JPanel nombre = new JPanel();
        nombre.setOpaque(false);
        nombre.setLayout(new BoxLayout(nombre, BoxLayout.Y_AXIS));
        nombre.add(Estilo.etiqueta("CEVICHERÍA", new Font("Segoe UI", Font.BOLD, 19), Color.WHITE));
        nombre.add(Estilo.etiqueta("Sistema Administrativo", Estilo.PEQUENA, new Color(0xCFE0FF)));
        logo.add(nombre, BorderLayout.CENTER);
        menu.add(logo);
        menu.add(Box.createVerticalStrut(18));

        contenido.setOpaque(false);

        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);
        barra.setBorder(new EmptyBorder(14, 22, 10, 22));
        JPanel migas = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        migas.setOpaque(false);
        migas.add(lblGrupo);
        migas.add(Estilo.etiqueta("\u203A", Estilo.SUBTITULO, Estilo.PRIMARIO_OSCURO));
        migas.add(lblSeccion);
        barra.add(migas, BorderLayout.WEST);

        JPopupMenu popup = new JPopupMenu();
        JMenuItem salirItem = new JMenuItem("Cerrar sesión");
        salirItem.setFont(Estilo.FUENTE);
        salirItem.addActionListener(e -> cerrarSesion());
        popup.add(salirItem);
        JButton chip = Estilo.boton(usuario.getNombreUsuario() + " \u25BE", Color.WHITE);
        chip.setForeground(Estilo.PRIMARIO_OSCURO);
        chip.addActionListener(e -> popup.show(chip, 0, chip.getHeight()));
        barra.add(chip, BorderLayout.EAST);

        JPanel derecha = new JPanel(new BorderLayout());
        derecha.setBackground(Estilo.FONDO);
        derecha.add(barra, BorderLayout.NORTH);
        derecha.add(contenido, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(menu, BorderLayout.WEST);
        add(derecha, BorderLayout.CENTER);
    }

    protected void agregarGrupo(String nombre) {
        grupoActual = nombre;
        JLabel l = Estilo.etiqueta(nombre.toUpperCase(), Estilo.PEQUENA_NEGRITA, new Color(0xB7CCF2));
        l.setBorder(new EmptyBorder(10, 8, 6, 0));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        menu.add(l);
    }

    protected void agregarSeccion(String nombre, String glifo, JComponent panel) {
        ItemMenu b = new ItemMenu(glifo + "    " + nombre);
        b.addActionListener(e -> mostrar(nombre));
        items.put(nombre, b);
        grupoDeSeccion.put(nombre, grupoActual);
        menu.add(b);
        menu.add(Box.createVerticalStrut(3));
        panel.setOpaque(false);
        contenido.add(panel, nombre);
    }

    // Igual que agregarSeccion, pero la seccion se puede volver a crear con el boton "Actualizar".
    protected void agregarSeccion(String nombre, String glifo, java.util.function.Supplier<? extends JComponent> fabrica) {
        JComponent panel = fabrica.get();
        agregarSeccion(nombre, glifo, panel);
        activarActualizar(nombre, panel, fabrica);
    }

    private void activarActualizar(String nombre, JComponent panel,
                                   java.util.function.Supplier<? extends JComponent> fabrica) {
        Runnable alActualizar = () -> {
            JComponent nuevo = fabrica.get();
            nuevo.setOpaque(false);
            contenido.remove(panel);
            contenido.add(nuevo, nombre);
            activarActualizar(nombre, nuevo, fabrica);
            contenido.revalidate();
            contenido.repaint();
            cartas.show(contenido, nombre);
        };
        if (panel instanceof ModuloBase) {
            ((ModuloBase) panel).setAccionActualizar(alActualizar);
        } else if (panel instanceof PanelInicio) {
            ((PanelInicio) panel).setAccionActualizar(alActualizar);
        }
    }

    protected void terminarMenu() {
        menu.add(Box.createVerticalGlue());

        JPanel usuarioCard = new JPanel(new BorderLayout(8, 0));
        usuarioCard.setOpaque(false);
        usuarioCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        usuarioCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        usuarioCard.add(new Estilo.CajaIcono("\u263B", new Color(0xCFE0FF), Estilo.MENU_ARRIBA, 38), BorderLayout.WEST);
        JPanel t = new JPanel();
        t.setOpaque(false);
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        t.add(Estilo.etiqueta(usuario.getNombreUsuario(), Estilo.NEGRITA, Color.WHITE));
        t.add(Estilo.etiqueta(rol, Estilo.PEQUENA, new Color(0xCFE0FF)));
        usuarioCard.add(t, BorderLayout.CENTER);
        menu.add(usuarioCard);
        menu.add(Box.createVerticalStrut(10));

        JButton salir = Estilo.boton("Cerrar sesión", Estilo.PELIGRO);
        salir.setAlignmentX(Component.LEFT_ALIGNMENT);
        salir.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        salir.addActionListener(e -> cerrarSesion());
        menu.add(salir);

        if (!items.isEmpty()) {
            mostrar(items.keySet().iterator().next());
        }
    }

    protected void mostrar(String nombre) {
        cartas.show(contenido, nombre);
        for (Map.Entry<String, ItemMenu> e : items.entrySet()) {
            e.getValue().setActivo(e.getKey().equals(nombre));
        }
        lblGrupo.setText(nombreGrupo(grupoDeSeccion.get(nombre)));
        lblSeccion.setText(nombre);
    }

    private String nombreGrupo(String g) {
        return g == null || g.isEmpty() ? "General" : g;
    }

    protected void cerrarSesion() {
        int r = JOptionPane.showConfirmDialog(this, "¿Desea cerrar la sesión?",
                "Cerrar sesión", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            dispose();
            new VentanaLogin().setVisible(true);
        }
    }

    protected static JPanel panelPendiente(String titulo) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(4, 20, 18, 20));
        Estilo.Tarjeta c = new Estilo.Tarjeta(new GridBagLayout());
        c.add(Estilo.etiqueta("La sección \"" + titulo + "\" se genera en el siguiente bloque.",
                Estilo.FUENTE, Estilo.TEXTO_SUAVE));
        p.add(c, BorderLayout.CENTER);
        return p;
    }
}
