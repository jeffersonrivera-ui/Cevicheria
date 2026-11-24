package vista.escritorio;

import dao.ClienteDAO;
import dao.DetallePedidoDAO;
import dao.PedidoDAO;
import dao.PlatoDAO;
import dao.VentaDAO;
import dao.impl.ClienteDAOImpl;
import dao.impl.DetallePedidoDAOImpl;
import dao.impl.PedidoDAOImpl;
import dao.impl.PlatoDAOImpl;
import dao.impl.VentaDAOImpl;
import modelo.Pedido;
import modelo.Plato;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Arc2D;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class PanelInicio extends JPanel {

    private static final String[] DIAS = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};
    private static final String[] DIAS_LARGOS = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
    private static final String[] MESES = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio",
            "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
    private static final String[] ESTADOS = {"PENDIENTE", "EN PREPARACION", "LISTO", "ENTREGADO", "CANCELADO"};
    private static final String[] NOMBRES_ESTADOS = {"Pendiente", "En preparación", "Listo", "Entregado", "Cancelado"};
    private static final Color[] COLORES_ESTADOS = {new Color(0x2F80ED), new Color(0xF59E0B),
            new Color(0x22C55E), new Color(0x8B5CF6), new Color(0xEF4444)};

    private final PedidoDAO pedidoDAO = new PedidoDAOImpl();
    private final DetallePedidoDAO detalleDAO = new DetallePedidoDAOImpl();
    private final PlatoDAO platoDAO = new PlatoDAOImpl();
    private final ClienteDAO clienteDAO = new ClienteDAOImpl();
    private final VentaDAO ventaDAO = new VentaDAOImpl();

    private final Integer idMesero;
    private final JLabel lblPlatos = valor();
    private final JLabel lblClientes = valor();
    private final JLabel lblPedidosHoy = valor();
    private final JLabel lblCuartaTarjeta = valor();
    private final GraficoBarras barras = new GraficoBarras();
    private final GraficoBarras barrasPedidos = new GraficoBarras();
    private final GraficoDona dona = new GraficoDona();
    private final JPanel leyenda = new JPanel(new GridLayout(0, 1, 0, 6));
    private final JPanel ranking = new JPanel(new GridLayout(0, 1, 0, 8));
    private final TablaPaginada ultimos = new TablaPaginada(
            new String[]{"ID", "Cliente", "Total", "Estado", "Fecha"}, 3, false);
    private final Estilo.Tarjeta tarjetaBarras = new Estilo.Tarjeta(new BorderLayout(0, 6));

    private final JButton btnActualizarInicio = Estilo.boton("\u21BB  Actualizar", Estilo.GRIS);

    public void setAccionActualizar(Runnable accion) {
        btnActualizarInicio.addActionListener(e -> accion.run());
        btnActualizarInicio.setVisible(true);
    }

    public PanelInicio(Integer idMesero, Consumer<String> navegar, String[][] accesos) {
        super(new BorderLayout());
        this.idMesero = idMesero;
        setOpaque(false);

        JPanel contenido = new JPanel(new GridBagLayout());
        contenido.setOpaque(false);
        contenido.setBorder(new EmptyBorder(4, 20, 18, 20));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.weightx = 1;
        g.insets = new Insets(0, 0, 14, 0);

        // Cabecera
        g.gridx = 0;
        g.gridy = 0;
        g.gridwidth = 2;
        contenido.add(crearCabecera(), g);

        // Tarjetas
        g.gridy = 1;
        contenido.add(crearTarjetas(), g);

        // Graficos
        g.gridy = 2;
        g.gridwidth = 1;
        g.weightx = 0.62;
        g.weighty = 0.4;
        g.insets = new Insets(0, 0, 14, 14);
        tarjetaBarras.setPreferredSize(new Dimension(420, 230));
        contenido.add(tarjetaBarras, g);

        g.gridx = 1;
        g.weightx = 0.38;
        g.insets = new Insets(0, 0, 14, 0);
        contenido.add(crearTarjetaDona(), g);

        // Ultimos pedidos y accesos
        g.gridx = 0;
        g.gridy = 3;
        g.weightx = 0.62;
        g.weighty = 0.6;
        g.insets = new Insets(0, 0, 0, 14);
        contenido.add(crearTarjetaUltimos(), g);

        g.gridx = 1;
        g.weightx = 0.38;
        g.insets = new Insets(0, 0, 0, 0);
        contenido.add(crearAccesos(navegar, accesos), g);

        JScrollPane sp = new JScrollPane(contenido);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        add(sp, BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                cargar();
            }
        });
        cargar();
    }

    private static JLabel valor() {
        return Estilo.etiqueta("0", new Font("Segoe UI", Font.BOLD, 30), Estilo.PRIMARIO_OSCURO);
    }

    private JPanel crearCabecera() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JPanel t = new JPanel();
        t.setOpaque(false);
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        t.add(Estilo.etiqueta(idMesero == null ? "Panel principal" : "Mi panel de atención",
                new Font("Segoe UI", Font.BOLD, 28), Estilo.PRIMARIO_OSCURO));
        t.add(Estilo.etiqueta("Bienvenido al sistema de gestión de la cevichería", Estilo.FUENTE, Estilo.TEXTO_SUAVE));
        p.add(t, BorderLayout.WEST);

        LocalDate hoy = LocalDate.now();
        String fecha = DIAS_LARGOS[hoy.getDayOfWeek().getValue() - 1] + ", " + hoy.getDayOfMonth()
                + " de " + MESES[hoy.getMonthValue() - 1] + " de " + hoy.getYear();
        Estilo.Tarjeta chip = new Estilo.Tarjeta(new FlowLayout(FlowLayout.CENTER, 0, 0), 14);
        chip.setBorder(new EmptyBorder(10, 16, 10, 16));
        chip.add(Estilo.etiqueta(fecha, Estilo.NEGRITA, Estilo.PRIMARIO_OSCURO));
        JPanel envoltorio = new JPanel(new GridBagLayout());
        envoltorio.setOpaque(false);
        btnActualizarInicio.setVisible(false);
        envoltorio.add(btnActualizarInicio, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER,
                GridBagConstraints.NONE, new Insets(0, 0, 0, 12), 0, 0));
        envoltorio.add(chip, new GridBagConstraints());
        p.add(envoltorio, BorderLayout.EAST);
        return p;
    }

    private JPanel crearTarjetas() {
        JPanel fila = new JPanel(new GridLayout(1, 4, 14, 0));
        fila.setOpaque(false);
        fila.add(tarjetaDato("\u2668", new Color(0x2F80ED), "Platos", lblPlatos, "Activos", new Color(0xDBEAFE), new Color(0x1D4ED8)));
        fila.add(tarjetaDato("\u263A", new Color(0x22C55E), "Clientes", lblClientes, "Registrados", new Color(0xDCFCE7), new Color(0x15803D)));
        fila.add(tarjetaDato("\u2637", new Color(0xF97316), idMesero == null ? "Pedidos" : "Mis pedidos (semana)", lblPedidosHoy, idMesero == null ? "Hoy" : "Semana", new Color(0xFFEDD5), new Color(0xC2410C)));
        fila.add(tarjetaDato("\u2261", new Color(0x8B5CF6), idMesero == null ? "Ventas" : "Pendientes", lblCuartaTarjeta,
                idMesero == null ? "Hoy" : "Por atender", new Color(0xEDE9FE), new Color(0x6D28D9)));
        return fila;
    }

    private JPanel tarjetaDato(String glifo, Color colorIcono, String titulo, JLabel valor, String etiqueta, Color fondoPastilla, Color letraPastilla) {
        Estilo.Tarjeta t = new Estilo.Tarjeta(new BorderLayout(12, 0));
        t.add(new Estilo.CajaIcono(glifo, colorIcono, Color.WHITE, 54), BorderLayout.WEST);
        JPanel centro = new JPanel();
        centro.setOpaque(false);
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
        centro.add(Estilo.etiqueta(titulo, Estilo.FUENTE, Estilo.TEXTO));
        centro.add(valor);
        t.add(centro, BorderLayout.CENTER);

        Estilo.Pastilla pastilla = new Estilo.Pastilla();
        pastilla.poner(etiqueta, fondoPastilla, letraPastilla);
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        sur.setOpaque(false);
        sur.add(pastilla);
        t.add(sur, BorderLayout.SOUTH);
        return t;
    }

    private JPanel crearTarjetaDona() {
        Estilo.Tarjeta t = new Estilo.Tarjeta(new BorderLayout(0, 6));
        if (idMesero != null) {
            // Vista del mesero: ranking de quienes mas pedidos atendieron en el mes
            t.add(Estilo.etiqueta("Ranking de meseros del mes", Estilo.SUBTITULO, Estilo.PRIMARIO_OSCURO), BorderLayout.NORTH);
            ranking.setOpaque(false);
            JPanel arriba = new JPanel(new BorderLayout());
            arriba.setOpaque(false);
            arriba.setBorder(new EmptyBorder(10, 4, 0, 4));
            arriba.add(ranking, BorderLayout.NORTH);
            t.add(arriba, BorderLayout.CENTER);
            return t;
        }
        // Vista del administrador: pedidos de cada dia de la semana
        t.add(Estilo.etiqueta("Pedidos por día (semana)", Estilo.SUBTITULO, Estilo.PRIMARIO_OSCURO), BorderLayout.NORTH);
        t.add(barrasPedidos, BorderLayout.CENTER);
        return t;
    }

    private JPanel crearTarjetaUltimos() {
        Estilo.Tarjeta t = new Estilo.Tarjeta(new BorderLayout(0, 8));
        t.add(Estilo.etiqueta(idMesero == null ? "Últimos pedidos" : "Mis últimos pedidos", Estilo.SUBTITULO, Estilo.PRIMARIO_OSCURO), BorderLayout.NORTH);
        t.add(ultimos, BorderLayout.CENTER);
        t.setPreferredSize(new Dimension(420, 380));
        return t;
    }

    private JPanel crearAccesos(Consumer<String> navegar, String[][] accesos) {
        Estilo.Tarjeta t = new Estilo.Tarjeta(new BorderLayout(0, 10));
        t.add(Estilo.etiqueta("Accesos rápidos", Estilo.SUBTITULO, Estilo.PRIMARIO_OSCURO), BorderLayout.NORTH);
        JPanel rejilla = new JPanel(new GridLayout(0, 2, 10, 10));
        rejilla.setOpaque(false);
        for (String[] a : accesos) {
            Color fondo = Color.decode(a[3]);
            Color letra = Color.decode(a[4]);
            Estilo.BotonRedondo b = new Estilo.BotonRedondo(a[2] + "  " + a[0], fondo);
            b.setForeground(letra);
            b.setHorizontalAlignment(SwingConstants.LEFT);
            b.addActionListener(e -> navegar.accept(a[1]));
            rejilla.add(b);
        }
        t.add(rejilla, BorderLayout.CENTER);
        return t;
    }

    public void cargar() {
        try {
            List<Pedido> todos = pedidoDAO.listar();
            LocalDate hoy = LocalDate.now();
            LocalDate lunes = hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

            int[] porEstado = new int[ESTADOS.length];
            double[] semana = new double[7];
            double[] pedidosDia = new double[7];
            int pedidosHoy = 0;
            int pendientes = 0;
            java.util.List<Object[]> filas = new java.util.ArrayList<>();

            java.util.Map<Integer, Integer> conteoMes = new java.util.LinkedHashMap<>();
            java.util.Map<Integer, String> nombresMes = new java.util.HashMap<>();
            String mesActual = hoy.toString().substring(0, 7);

            for (Pedido p : todos) {
                String estado = p.getEstado() == null ? "" : p.getEstado().trim().toUpperCase();
                boolean mio = idMesero == null
                        || (p.getMesero() != null && p.getMesero().getId() == idMesero);
                boolean cancelado = estado.equals("CANCELADO");
                if (p.getMesero() != null && !cancelado && p.getFecha() != null
                        && p.getFecha().startsWith(mesActual)) {
                    int idM = p.getMesero().getId();
                    conteoMes.merge(idM, 1, Integer::sum);
                    nombresMes.put(idM, p.getMesero().getNombres() + " " + p.getMesero().getApellidos());
                }
                for (int i = 0; i < ESTADOS.length; i++) {
                    if (ESTADOS[i].equals(estado)) {
                        porEstado[i]++;
                    }
                }
                if (estado.equals("PENDIENTE") && mio) {
                    pendientes++;
                }
                LocalDate f = fechaDe(p.getFecha());
                if (f != null && !cancelado) {
                    long d = java.time.temporal.ChronoUnit.DAYS.between(lunes, f);
                    if (d >= 0 && d < 7) {
                        pedidosDia[(int) d]++;
                    }
                }
                if (f != null && mio && !cancelado) {
                    long dias = java.time.temporal.ChronoUnit.DAYS.between(lunes, f);
                    if (idMesero == null ? f.equals(hoy) : (dias >= 0 && dias < 7)) {
                        pedidosHoy++;
                    }
                    if (idMesero != null && dias >= 0 && dias < 7) {
                        semana[(int) dias] += 1;
                    }
                }
            }

            // Ultimos pedidos (los mas recientes primero)
            int mostrados = 0;
            for (int i = todos.size() - 1; i >= 0 && mostrados < 16; i--) {
                Pedido p = todos.get(i);
                if (idMesero != null && (p.getMesero() == null || p.getMesero().getId() != idMesero)) {
                    continue;
                }
                String cliente = p.getCliente() == null ? "" : p.getCliente().getNombres() + " "
                        + (p.getCliente().getApellidos() == null ? "" : p.getCliente().getApellidos());
                filas.add(new Object[]{"#" + p.getId(), cliente.trim(), Estilo.soles(detalleDAO.calcularTotal(p.getId())),
                        nombreEstado(p.getEstado()), p.getFecha()});
                mostrados++;
            }
            ultimos.setFilas(filas);

            int disponibles = 0;
            List<Plato> platos = platoDAO.listar();
            for (Plato pl : platos) {
                if ("ACTIVO".equals(platoDAO.obtenerEstado(pl.getId()))) {
                    disponibles++;
                }
            }
            lblPlatos.setText(String.valueOf(platos.size()));
            lblClientes.setText(String.valueOf(clienteDAO.listar().size()));
            lblPedidosHoy.setText(String.valueOf(pedidosHoy));

            if (idMesero == null) {
                double ventasHoy = 0;
                List<Map<String, Object>> ventas = ventaDAO.listarVentas();
                for (Map<String, Object> v : ventas) {
                    LocalDate f = fechaDe(String.valueOf(v.get("fecha")));
                    double total = v.get("total") instanceof Number ? ((Number) v.get("total")).doubleValue() : 0;
                    if (f == null) {
                        continue;
                    }
                    if (f.equals(hoy)) {
                        ventasHoy += total;
                    }
                    long dias = java.time.temporal.ChronoUnit.DAYS.between(lunes, f);
                    if (dias >= 0 && dias < 7) {
                        semana[(int) dias] += total;
                    }
                }
                lblCuartaTarjeta.setText(Estilo.soles(ventasHoy));
                lblCuartaTarjeta.setFont(new Font("Segoe UI", Font.BOLD, 24));
            } else {
                lblCuartaTarjeta.setText(String.valueOf(pendientes));
            }

            tarjetaBarras.removeAll();
            tarjetaBarras.add(Estilo.etiqueta(idMesero == null ? "Ventas de la semana" : "Pedidos que atendí esta semana",
                    Estilo.SUBTITULO, Estilo.PRIMARIO_OSCURO), BorderLayout.NORTH);
            barras.poner(semana, idMesero == null, hoy.getDayOfWeek().getValue() - 1);
            tarjetaBarras.add(barras, BorderLayout.CENTER);
            tarjetaBarras.revalidate();
            tarjetaBarras.repaint();

            if (idMesero != null) {
                llenarRanking(conteoMes, nombresMes);
                return;
            }
            barrasPedidos.poner(pedidosDia, false, hoy.getDayOfWeek().getValue() - 1);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar el panel principal: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Top 5 de meseros con mas pedidos en el mes; el propio mesero siempre aparece
    private void llenarRanking(java.util.Map<Integer, Integer> conteo, java.util.Map<Integer, String> nombres) {
        java.util.List<Integer> orden = new java.util.ArrayList<>(conteo.keySet());
        orden.sort((a, b) -> conteo.get(b) - conteo.get(a));
        int mayor = orden.isEmpty() ? 1 : conteo.get(orden.get(0));

        ranking.removeAll();
        for (int i = 0; i < orden.size() && i < 5; i++) {
            int id = orden.get(i);
            ranking.add(filaRanking(i + 1, nombres.get(id), conteo.get(id), mayor, id == idMesero));
        }
        int miPuesto = orden.indexOf(idMesero);
        if (miPuesto >= 5) {
            ranking.add(filaRanking(miPuesto + 1, nombres.get(idMesero), conteo.get(idMesero), mayor, true));
        } else if (miPuesto < 0) {
            ranking.add(filaRanking(orden.size() + 1, "Yo", 0, mayor, true));
        }
        if (orden.isEmpty()) {
            ranking.add(Estilo.etiqueta("Aún no hay pedidos este mes.", Estilo.PEQUENA, Estilo.TEXTO_SUAVE));
        }
        ranking.revalidate();
        ranking.repaint();
    }

    private JPanel filaRanking(int puesto, String nombre, int cantidad, int mayor, boolean esMio) {
        JPanel fila = new JPanel(new BorderLayout(10, 0));
        fila.setOpaque(false);
        String medalla = puesto == 1 ? "\uD83E\uDD47" : puesto == 2 ? "\uD83E\uDD48" : puesto == 3 ? "\uD83E\uDD49" : "";
        JLabel lblPuesto = Estilo.etiqueta(medalla.isEmpty() ? puesto + "." : puesto + ".", Estilo.NEGRITA,
                esMio ? Estilo.PRIMARIO : Estilo.TEXTO);
        lblPuesto.setPreferredSize(new Dimension(28, 20));
        fila.add(lblPuesto, BorderLayout.WEST);

        JPanel centro = new JPanel(new BorderLayout(0, 3));
        centro.setOpaque(false);
        centro.add(Estilo.etiqueta(esMio ? nombre + "  (yo)" : nombre,
                esMio ? Estilo.PEQUENA_NEGRITA : Estilo.PEQUENA, Estilo.TEXTO), BorderLayout.NORTH);
        final double parte = mayor == 0 ? 0 : (double) cantidad / mayor;
        final Color color = esMio ? Estilo.PRIMARIO : new Color(0xA9CCFF);
        JComponent barra = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xE5ECF8));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(color);
                g2.fillRoundRect(0, 0, Math.max(8, (int) (getWidth() * parte)), getHeight(), 8, 8);
                g2.dispose();
            }
        };
        barra.setPreferredSize(new Dimension(10, 8));
        centro.add(barra, BorderLayout.CENTER);
        fila.add(centro, BorderLayout.CENTER);
        fila.add(Estilo.etiqueta(cantidad + (cantidad == 1 ? " pedido" : " pedidos"), Estilo.PEQUENA_NEGRITA, Estilo.TEXTO),
                BorderLayout.EAST);
        return fila;
    }

    private static String nombreEstado(String estado) {
        String e = estado == null ? "" : estado.trim().toUpperCase();
        for (int i = 0; i < ESTADOS.length; i++) {
            if (ESTADOS[i].equals(e)) {
                return NOMBRES_ESTADOS[i];
            }
        }
        return estado == null ? "" : estado;
    }

    private static LocalDate fechaDe(String texto) {
        if (texto == null || texto.length() < 10) {
            return null;
        }
        try {
            return LocalDate.parse(texto.substring(0, 10));
        } catch (Exception e) {
            return null;
        }
    }

    // ---------- Graficos ----------

    private static class GraficoBarras extends JComponent {
        private double[] valores = new double[7];
        private boolean dinero = true;
        private int hoy = 0;

        void poner(double[] v, boolean dinero, int hoy) {
            this.valores = v;
            this.dinero = dinero;
            this.hoy = hoy;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            int izq = 62;
            int abajo = 24;
            int arriba = 14;
            double max = 0;
            for (double v : valores) {
                max = Math.max(max, v);
            }
            double tope = dinero ? Math.max(100, Math.ceil(max / 200.0) * 200) : Math.max(4, Math.ceil(max / 2.0) * 2);

            g2.setFont(Estilo.PEQUENA);
            FontMetrics fm = g2.getFontMetrics();
            int zonaAlto = h - abajo - arriba;
            for (int i = 0; i <= 5; i++) {
                double val = tope * i / 5.0;
                int y = arriba + zonaAlto - (int) (zonaAlto * i / 5.0);
                g2.setColor(new Color(0xE5ECF8));
                g2.drawLine(izq, y, w - 8, y);
                g2.setColor(Estilo.TEXTO_SUAVE);
                String et = dinero ? String.format(java.util.Locale.US, "S/ %,.0f", val) : String.valueOf((int) val);
                g2.drawString(et, izq - 8 - fm.stringWidth(et), y + 4);
            }

            int zonaAncho = w - izq - 8;
            int paso = zonaAncho / 7;
            int ancho = Math.max(10, paso - 26);
            for (int i = 0; i < 7; i++) {
                int alto = (int) (zonaAlto * (valores[i] / tope));
                int x = izq + i * paso + (paso - ancho) / 2;
                int y = arriba + zonaAlto - alto;
                g2.setColor(i == hoy ? new Color(0x1E6FEB) : new Color(0xA9CCFF));
                g2.fillRoundRect(x, y, ancho, Math.max(alto, 2), 6, 6);
                g2.setColor(i == hoy ? Estilo.TEXTO : Estilo.TEXTO_SUAVE);
                g2.setFont(i == hoy ? Estilo.PEQUENA_NEGRITA : Estilo.PEQUENA);
                String d = DIAS[i];
                g2.drawString(d, x + (ancho - g2.getFontMetrics().stringWidth(d)) / 2, h - 6);
                if (i == hoy && valores[i] > 0) {
                    String etq = dinero ? Estilo.soles(valores[i]) : String.valueOf((int) valores[i]);
                    g2.setFont(Estilo.PEQUENA_NEGRITA);
                    int tx = x + (ancho - g2.getFontMetrics().stringWidth(etq)) / 2;
                    g2.setColor(Estilo.PRIMARIO_OSCURO);
                    g2.drawString(etq, Math.max(izq, tx), Math.max(12, y - 4));
                }
            }
            g2.dispose();
        }
    }

    private static class GraficoDona extends JComponent {
        private int[] cantidades = new int[ESTADOS.length];

        GraficoDona() {
            setPreferredSize(new Dimension(150, 150));
        }

        void poner(int[] c) {
            this.cantidades = c;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int lado = Math.min(getWidth(), getHeight()) - 16;
            int x = (getWidth() - lado) / 2;
            int y = (getHeight() - lado) / 2;
            int total = 0;
            for (int c : cantidades) {
                total += c;
            }
            g2.setStroke(new BasicStroke(Math.max(14, lado / 6f), BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
            int grosor = Math.max(14, lado / 6);
            int dx = x + grosor / 2;
            int dy = y + grosor / 2;
            int dl = lado - grosor;
            if (total == 0) {
                g2.setColor(new Color(0xE5ECF8));
                g2.drawOval(dx, dy, dl, dl);
            } else {
                double inicio = 90;
                for (int i = 0; i < cantidades.length; i++) {
                    if (cantidades[i] == 0) {
                        continue;
                    }
                    double barrido = -360.0 * cantidades[i] / total;
                    g2.setColor(COLORES_ESTADOS[i]);
                    g2.draw(new Arc2D.Double(dx, dy, dl, dl, inicio, barrido, Arc2D.OPEN));
                    inicio += barrido;
                }
            }
            g2.setColor(Estilo.PRIMARIO_OSCURO);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 26));
            String t = String.valueOf(total);
            g2.drawString(t, getWidth() / 2 - g2.getFontMetrics().stringWidth(t) / 2, getHeight() / 2 + 6);
            g2.setFont(Estilo.PEQUENA);
            g2.setColor(Estilo.TEXTO_SUAVE);
            String s = "Pedidos";
            g2.drawString(s, getWidth() / 2 - g2.getFontMetrics().stringWidth(s) / 2, getHeight() / 2 + 24);
            g2.dispose();
        }
    }
}
