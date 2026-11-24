package vista.escritorio;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;

public final class Estilo {

    public static final Color FONDO = new Color(0xEAF0FB);
    public static final Color BLANCO = Color.WHITE;
    public static final Color BORDE = new Color(0xDCE5F4);
    public static final Color TEXTO = new Color(0x1E2A4A);
    public static final Color TEXTO_SUAVE = new Color(0x6B7A99);
    public static final Color MENU_ARRIBA = new Color(0x0B4DB0);
    public static final Color MENU_ABAJO = new Color(0x07306F);
    public static final Color MENU_ACTIVO = new Color(0x1E7BFF);
    public static final Color PRIMARIO = new Color(0x1E6FEB);
    public static final Color PRIMARIO_OSCURO = new Color(0x0B2F6B);
    public static final Color EXITO = new Color(0x16A34A);
    public static final Color ADVERTENCIA = new Color(0xF59E0B);
    public static final Color PELIGRO = new Color(0xEF4444);
    public static final Color INFO = new Color(0x2563EB);
    public static final Color GRIS = new Color(0x6B7C93);
    public static final Color ENCABEZADO_TABLA = new Color(0xDCE8FB);

    public static final Font FUENTE = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font NEGRITA = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font PEQUENA = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font PEQUENA_NEGRITA = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font TITULO = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font SUBTITULO = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font SIMBOLOS = new Font("Segoe UI Symbol", Font.BOLD, 16);

    private static final String[] FAMILIAS_SIMBOLOS = {"Segoe UI Symbol", "Segoe UI Emoji",
            "Arial Unicode MS", "DejaVu Sans", "Dialog"};

    private Estilo() {
    }

    // Devuelve una fuente capaz de dibujar el símbolo (evita los cuadrados).
    public static Font fuenteSimbolo(int codigo, int tam) {
        for (String familia : FAMILIAS_SIMBOLOS) {
            Font f = new Font(familia, Font.BOLD, tam);
            if (f.canDisplay(codigo)) {
                return f;
            }
        }
        return new Font("Dialog", Font.BOLD, tam);
    }

    // Si el texto tiene símbolos que la fuente base no dibuja, los envuelve en HTML con una fuente de símbolos.
    public static String html(String texto, Font base) {
        if (texto == null || texto.isEmpty() || texto.startsWith("<html>")
                || base.canDisplayUpTo(texto) == -1) {
            return texto;
        }
        StringBuilder sb = new StringBuilder("<html>");
        for (int i = 0; i < texto.length(); ) {
            int cp = texto.codePointAt(i);
            int n = Character.charCount(cp);
            String esc = escapar(texto.substring(i, i + n)).replace(" ", "&nbsp;");
            i += n;
            if (base.canDisplay(cp)) {
                sb.append(esc);
            } else {
                Font f = fuenteSimbolo(cp, base.getSize());
                sb.append("<span style=\"font-family:'").append(f.getFamily()).append("'\">")
                        .append(esc).append("</span>");
            }
        }
        return sb.append("</html>").toString();
    }

    // ---------- Componentes ----------

    public static class Tarjeta extends JPanel {
        private final int arco;

        public Tarjeta(LayoutManager layout) {
            this(layout, 18);
        }

        public Tarjeta(LayoutManager layout, int arco) {
            super(layout);
            this.arco = arco;
            setOpaque(false);
            setBorder(new EmptyBorder(14, 16, 14, 16));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arco, arco);
            g2.setColor(BORDE);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arco, arco);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class BotonRedondo extends JButton {
        private Color base;
        private boolean encima;

        public BotonRedondo(String texto, Color base) {
            super(texto);
            this.base = base;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setForeground(Color.WHITE);
            setFont(NEGRITA);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(9, 16, 9, 16));
            setText(html(texto, getFont()));
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

        public void setColorBase(Color c) {
            this.base = c;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color c = !isEnabled() ? new Color(0xB8C2D6) : (encima ? base.darker() : base);
            g2.setColor(c);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class Pastilla extends JLabel {
        private Color fondo;

        public Pastilla() {
            setFont(PEQUENA_NEGRITA);
            setOpaque(false);
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(new EmptyBorder(3, 12, 3, 12));
        }

        public Pastilla(String texto) {
            this();
            poner(texto);
        }

        public void poner(String texto) {
            Color[] c = coloresPastilla(texto);
            this.fondo = c[0];
            setForeground(c[1]);
            setText(html(texto, getFont()));
        }

        public void poner(String texto, Color fondo, Color letra) {
            this.fondo = fondo;
            setForeground(letra);
            setText(html(texto, getFont()));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fondo == null ? BORDE : fondo);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class CajaIcono extends JComponent {
        private final String glifo;
        private final Color fondo;
        private final Color letra;
        private final int tam;

        public CajaIcono(String glifo, Color fondo, Color letra, int tam) {
            this.glifo = glifo;
            this.fondo = fondo;
            this.letra = letra;
            this.tam = tam;
            setPreferredSize(new Dimension(tam, tam));
            setMinimumSize(new Dimension(tam, tam));
            setMaximumSize(new Dimension(tam, tam));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(fondo);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), tam / 3, tam / 3);
            g2.setColor(letra);
            g2.setFont(fuenteSimbolo(glifo.codePointAt(0), Math.max(12, (int) (tam * 0.5))));
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(glifo)) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(glifo, x, y);
            g2.dispose();
        }
    }

    public static class Logo extends JComponent {
        public Logo() {
            setPreferredSize(new Dimension(52, 38));
            setMinimumSize(new Dimension(52, 38));
            setMaximumSize(new Dimension(52, 38));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillOval(2, 8, 34, 22);
            Path2D cola = new Path2D.Double();
            cola.moveTo(32, 19);
            cola.lineTo(50, 6);
            cola.lineTo(50, 32);
            cola.closePath();
            g2.fill(cola);
            g2.setColor(MENU_ARRIBA);
            g2.fillOval(10, 15, 5, 5);
            g2.dispose();
        }
    }

    // ---------- Constructores rapidos ----------

    public static JButton boton(String texto, Color fondo) {
        return new BotonRedondo(texto, fondo);
    }

    public static JLabel etiqueta(String texto, Font fuente, Color color) {
        JLabel l = new JLabel();
        l.setFont(fuente);
        l.setText(html(texto, fuente));
        l.setForeground(color);
        return l;
    }

    public static JPanel tarjeta() {
        return new Tarjeta(new BorderLayout());
    }

    public static JTextField campo() {
        JTextField t = new JTextField();
        estilizarCampo(t);
        return t;
    }

    public static void estilizarCampo(JComponent c) {
        c.setFont(FUENTE);
        c.setForeground(TEXTO);
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xCBD5E8)),
                new EmptyBorder(6, 10, 6, 10)));
        c.setPreferredSize(new Dimension(160, 36));
    }

    public static JLabel etiquetaCampo(String texto, boolean obligatorio) {
        String html = "<html><nobr>" + escapar(texto) + (obligatorio ? " <font color='#DC2626'>*</font>" : "") + "</nobr></html>";
        JLabel l = new JLabel(html);
        l.setFont(PEQUENA_NEGRITA);
        l.setForeground(TEXTO);
        return l;
    }

    public static JPanel aviso(String texto, String tipo) {
        return aviso(texto, tipo, 230);
    }

    public static JPanel aviso(String texto, String tipo, int anchoTexto) {
        Color fondo;
        Color letra;
        String glifo;
        switch (tipo) {
            case "alerta":
                fondo = new Color(0xFEF3C7);
                letra = new Color(0xB45309);
                glifo = "\u26A0";
                break;
            case "error":
                fondo = new Color(0xFEE2E2);
                letra = new Color(0xB91C1C);
                glifo = "\u26A0";
                break;
            default:
                fondo = new Color(0xDBEAFE);
                letra = new Color(0x1D4ED8);
                glifo = "\u24D8";
                break;
        }
        final Color f = fondo;
        JPanel p = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(f);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(8, 12, 8, 12));
        JLabel ic = new JLabel(glifo);
        ic.setFont(fuenteSimbolo(glifo.codePointAt(0), 18));
        ic.setForeground(letra);
        JLabel tx = new JLabel("<html><body style='width:" + anchoTexto + "px'>" + texto + "</body></html>");
        tx.setFont(PEQUENA);
        tx.setForeground(letra);
        p.add(ic, BorderLayout.WEST);
        p.add(tx, BorderLayout.CENTER);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    // ---------- Tablas ----------

    public static DefaultTableModel modeloNoEditable(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    public static JTable tabla(DefaultTableModel modelo) {
        JTable t = new JTable(modelo);
        t.setFont(FUENTE);
        t.setForeground(TEXTO);
        t.setRowHeight(40);
        t.setSelectionBackground(new Color(0xD6E6FF));
        t.setSelectionForeground(TEXTO);
        t.setShowGrid(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(new Color(0xE5ECF8));
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setFillsViewportHeight(true);

        DefaultTableCellRenderer centro = new DefaultTableCellRenderer();
        centro.setHorizontalAlignment(SwingConstants.CENTER);
        t.setDefaultRenderer(Object.class, centro);

        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(100, 38));
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tb, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = new JLabel(v == null ? "" : v.toString(), SwingConstants.CENTER);
                l.setOpaque(true);
                l.setBackground(ENCABEZADO_TABLA);
                l.setForeground(PRIMARIO_OSCURO);
                l.setFont(NEGRITA);
                return l;
            }
        });
        return t;
    }

    public static JScrollPane scroll(JComponent c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(BLANCO);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    // ---------- Utilidades ----------

    public static String escapar(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public static String soles(double v) {
        return String.format(java.util.Locale.US, "S/ %,.2f", v);
    }

    public static Color colorEstado(String estado) {
        return coloresPastilla(estado)[1];
    }

    public static Color[] coloresPastilla(String texto) {
        String t = texto == null ? "" : texto.trim().toUpperCase();
        if (t.equals("NO DISPONIBLE") || t.equals("INACTIVO") || t.equals("CANCELADO")
                || t.equals("DESPEDIDO") || t.equals("RENUNCIADO") || t.equals("STOCK BAJO")
                || t.equals("SIN STOCK") || t.equals("ANULADO")) {
            return new Color[]{new Color(0xFEE2E2), new Color(0xDC2626)};
        }
        if (t.equals("PENDIENTE")) {
            return new Color[]{new Color(0xFEF3C7), new Color(0xB45309)};
        }
        if (t.equals("EN PREPARACION") || t.equals("MESERO")) {
            return new Color[]{new Color(0xDBEAFE), new Color(0x1D4ED8)};
        }
        if (t.equals("ENTREGADO") || t.equals("COCINERO")) {
            return new Color[]{new Color(0xEDE9FE), new Color(0x6D28D9)};
        }
        if (t.equals("ADMINISTRADOR")) {
            return new Color[]{new Color(0xFCE7F3), new Color(0xBE185D)};
        }
        if (t.equals("DESCONOCIDO")) {
            return new Color[]{new Color(0xE5E7EB), new Color(0x4B5563)};
        }
        return new Color[]{new Color(0xDCFCE7), new Color(0x15803D)};
    }
}
