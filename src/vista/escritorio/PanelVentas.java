package vista.escritorio;

import controlador.VentaOperaciones;
import dao.DetallePedidoDAO;
import dao.PagoDAO;
import dao.PedidoDAO;
import dao.VentaDAO;
import dao.impl.DetallePedidoDAOImpl;
import dao.impl.PagoDAOImpl;
import dao.impl.PedidoDAOImpl;
import dao.impl.VentaDAOImpl;
import modelo.Pedido;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Sección Ventas: registro de ventas de pedidos pagados e historial (administrador). */
public class PanelVentas extends ModuloBase {

    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Elemento de combo con id (id 0 = texto "Seleccione...").
    private static final class Opcion {
        final int id;
        final String texto;

        Opcion(int id, String texto) {
            this.id = id;
            this.texto = texto;
        }

        @Override
        public String toString() {
            return texto;
        }
    }

    // Resultado de leer todo de la base en segundo plano.
    private static final class Datos {
        List<Map<String, Object>> ventas = new ArrayList<>();
        List<Pedido> pedidos = new ArrayList<>();
        Map<Integer, double[]> cuentas = new HashMap<>(); // id pedido -> {total, pagado}
        Set<Integer> pedidosConVenta = new HashSet<>();
    }

    private final VentaDAO ventaDAO = new VentaDAOImpl();
    private final PagoDAO pagoDAO = new PagoDAOImpl();
    private final PedidoDAO pedidoDAO = new PedidoDAOImpl();
    private final DetallePedidoDAO detalleDAO = new DetallePedidoDAOImpl();

    private Datos datos = new Datos();
    private boolean cargandoCombo = false;

    private final JLabel lblTotalVentas;
    private final JLabel lblIngresos;
    private final JLabel lblHoy;

    private final JTextField txtIdVenta = new CampoHint("Ingrese el ID de venta...");
    private final JTextField txtIdPedidoFiltro = new CampoHint("Ingrese el ID del pedido...");
    private final TablaPaginada tabla = new TablaPaginada(
            new String[]{"ID venta", "ID pedido", "Fecha", "Total (S/)"}, -1, true);

    private final JComboBox<Opcion> cmbPedido = new JComboBox<>();
    private final Estilo.Pastilla pastillaEstado = new Estilo.Pastilla();
    private final JLabel lblTotalPedido = Estilo.etiqueta("S/ 0.00", Estilo.NEGRITA, Estilo.PRIMARIO);
    private final JLabel lblPagado = Estilo.etiqueta("S/ 0.00", Estilo.NEGRITA, Estilo.PRIMARIO);
    private final JLabel lblSaldo = Estilo.etiqueta("S/ 0.00", Estilo.NEGRITA, new Color(0x15803D));

    public PanelVentas() {
        super("Ventas", "Registra ventas de pedidos pagados",
                "📊", Estilo.PRIMARIO, true);

        lblTotalVentas = agregarEstadistica("☷", new Color(0x38A3F5), "Total de ventas", Estilo.PRIMARIO_OSCURO);
        lblIngresos = agregarEstadistica("S/", Estilo.EXITO, "Ingresos (S/)", new Color(0x15803D));
        lblHoy = agregarEstadistica("📅", Estilo.PELIGRO, "Ventas de hoy", Estilo.PELIGRO);
        setAnchoEstadisticas(640);

        // ----- Izquierda -----
        txtIdVenta.setPreferredSize(new Dimension(100, 36));
        txtIdVenta.setMinimumSize(new Dimension(100, 36));
        txtIdPedidoFiltro.setPreferredSize(new Dimension(130, 36));
        JPanel colPedido = new JPanel(new BorderLayout(0, 4));
        colPedido.setOpaque(false);
        colPedido.add(Estilo.etiqueta("ID del pedido:", Estilo.PEQUENA_NEGRITA, Estilo.TEXTO), BorderLayout.NORTH);
        colPedido.add(txtIdPedidoFiltro, BorderLayout.CENTER);
        colPedido.setPreferredSize(new Dimension(130, 58));
        JPanel extra = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        extra.setOpaque(false);
        extra.add(colPedido);
        extra.setPreferredSize(new Dimension(130, 58));

        JButton btnBuscar = Estilo.boton("Buscar", Estilo.PRIMARIO);
        JPanel arriba = new JPanel(new BorderLayout(0, 10));
        arriba.setOpaque(false);
        arriba.add(barraBusqueda("Buscar por ID de venta:", txtIdVenta, extra, btnBuscar), BorderLayout.NORTH);
        arriba.add(tituloDePanel("≡", "Historial de ventas"), BorderLayout.CENTER);
        panelIzquierdo.add(arriba, BorderLayout.NORTH);

        tabla.setAnchos(55, 65, 120, 90, 55);
        tabla.setTituloAcciones("Acción");
        tabla.setAcciones(new TablaPaginada.Accion("◉", new Color(0x1E6FEB), Color.WHITE, this::verDetalle));
        panelIzquierdo.add(tabla, BorderLayout.CENTER);

        // ----- Derecha -----
        cmbPedido.setFont(Estilo.FUENTE);
        cmbPedido.setPreferredSize(new Dimension(120, 36));

        JPanel cuenta = new JPanel(new GridLayout(4, 1, 0, 2));
        cuenta.setOpaque(false);
        cuenta.add(filaPastilla("Estado del pedido:"));
        cuenta.add(fila("Total del pedido:", lblTotalPedido));
        cuenta.add(fila("Total pagado:", lblPagado));
        cuenta.add(fila("Saldo pendiente:", lblSaldo));
        JPanel tarjetaCuenta = fondo(new Color(0xE3EEFB), new BorderLayout());
        tarjetaCuenta.setBorder(new EmptyBorder(6, 12, 6, 12));
        tarjetaCuenta.add(cuenta, BorderLayout.CENTER);

        JButton btnRegistrar = Estilo.boton("Registrar venta", Estilo.EXITO);
        JButton btnLimpiar = Estilo.boton("↻  Limpiar", Estilo.GRIS);
        JPanel botones = new JPanel(new GridBagLayout());
        botones.setOpaque(false);
        GridBagConstraints gb = new GridBagConstraints();
        gb.fill = GridBagConstraints.HORIZONTAL;
        gb.weightx = 3;
        gb.insets = new Insets(0, 0, 0, 10);
        botones.add(btnRegistrar, gb);
        gb.weightx = 2;
        gb.insets = new Insets(0, 0, 0, 0);
        botones.add(btnLimpiar, gb);

        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.add(alinear(tituloDePanel("+", "Registrar venta")));
        col.add(Box.createVerticalStrut(10));
        col.add(alinear(conEtiqueta("ID del pedido:", true, cmbPedido)));
        col.add(Box.createVerticalStrut(8));
        col.add(alinear(tarjetaCuenta));
        col.add(Box.createVerticalStrut(12));
        col.add(alinear(botones));
        col.add(Box.createVerticalStrut(12));
        col.add(alinear(Estilo.aviso("La venta se registra cuando el pedido está completamente pagado.",
                "info", 210)));
        col.add(Box.createVerticalStrut(8));
        col.add(alinear(Estilo.aviso("No se permite registrar ventas de pedidos cancelados, sin detalles, con "
                + "saldo pendiente o que ya tengan una venta.", "alerta", 210)));
        col.add(Box.createVerticalStrut(8));
        col.add(alinear(Estilo.aviso("Cada pedido solo puede tener una venta registrada.", "info", 210)));
        panelDerecho.add(col, BorderLayout.NORTH);
        setAnchoDerecho(370);

        // ----- Eventos -----
        btnBuscar.addActionListener(e -> aplicarFiltros());
        txtIdVenta.addActionListener(e -> aplicarFiltros());
        txtIdPedidoFiltro.addActionListener(e -> aplicarFiltros());
        btnRegistrar.addActionListener(e -> registrar());
        btnLimpiar.addActionListener(e -> limpiar());
        cmbPedido.addActionListener(e -> {
            if (!cargandoCombo) {
                alElegirPedido();
            }
        });

        pastillaEstado.setVisible(false);
        recargar();
    }

    // ---------- Construcción ----------

    private JPanel fondo(Color color, LayoutManager lm) {
        JPanel p = new JPanel(lm) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        return p;
    }

    private JPanel fila(String texto, JLabel valor) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(Estilo.etiqueta(texto, Estilo.PEQUENA_NEGRITA, Estilo.TEXTO), BorderLayout.WEST);
        valor.setHorizontalAlignment(SwingConstants.RIGHT);
        p.add(valor, BorderLayout.EAST);
        return p;
    }

    private JPanel filaPastilla(String texto) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(Estilo.etiqueta(texto, Estilo.PEQUENA_NEGRITA, Estilo.TEXTO), BorderLayout.WEST);
        JPanel der = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        der.setOpaque(false);
        der.add(pastillaEstado);
        p.add(der, BorderLayout.EAST);
        return p;
    }

    private JPanel conEtiqueta(String texto, boolean obligatorio, JComponent campo) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.add(Estilo.etiquetaCampo(texto, obligatorio), BorderLayout.NORTH);
        p.add(campo, BorderLayout.CENTER);
        return p;
    }

    private JComponent alinear(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
        return c;
    }

    // ---------- Datos ----------

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private static double num(Object o) {
        return o instanceof Number ? ((Number) o).doubleValue() : 0;
    }

    private static int entero(Object o) {
        return o instanceof Number ? ((Number) o).intValue() : 0;
    }

    private static String tres(int n) {
        return String.format("%03d", n);
    }

    private static String diaIso(Object f) {
        String s = f == null ? "" : f.toString();
        return s.length() >= 10 ? s.substring(0, 10) : s;
    }

    private static String fechaBonita(Object f) {
        if (f == null) {
            return "";
        }
        String s = f.toString().trim();
        try {
            if (s.length() >= 19) {
                return LocalDateTime.parse(s.substring(0, 19).replace(' ', 'T')).format(FECHA_HORA);
            }
            if (s.length() >= 10) {
                return LocalDate.parse(s.substring(0, 10)).format(FECHA);
            }
        } catch (Exception ignorada) {
        }
        return s;
    }

    private static String clienteDe(Pedido p) {
        return p.getCliente() == null ? "" : (nz(p.getCliente().getNombres()) + " "
                + nz(p.getCliente().getApellidos())).trim();
    }

    // Lee ventas, pedidos y las cuentas de cada pedido sin bloquear la ventana.
    private void recargar() {
        new SwingWorker<Datos, Void>() {
            @Override
            protected Datos doInBackground() {
                Datos d = new Datos();
                d.ventas = ventaDAO.listarVentas();
                d.pedidos = pedidoDAO.listar();
                for (Map<String, Object> v : d.ventas) {
                    d.pedidosConVenta.add(entero(v.get("id_pedido")));
                }
                Map<Integer, Double> pagado = new HashMap<>();
                for (Map<String, Object> p : pagoDAO.listarPagos()) {
                    if ("Pagado".equalsIgnoreCase(String.valueOf(p.get("estado")))) {
                        pagado.merge(entero(p.get("id_pedido")), num(p.get("monto")), Double::sum);
                    }
                }
                for (Pedido p : d.pedidos) {
                    double total = detalleDAO.calcularTotal(p.getId());
                    d.cuentas.put(p.getId(), new double[]{Math.max(total, 0), pagado.getOrDefault(p.getId(), 0.0)});
                }
                return d;
            }

            @Override
            protected void done() {
                try {
                    datos = get();
                } catch (Exception ex) {
                    error("No se pudieron cargar las ventas: " + ex.getMessage());
                    return;
                }
                actualizarEstadisticas();
                llenarComboPedidos();
                aplicarFiltros();
            }
        }.execute();
    }

    private void actualizarEstadisticas() {
        double ingresos = 0;
        int hoy = 0;
        String hoyIso = LocalDate.now().toString();
        for (Map<String, Object> v : datos.ventas) {
            ingresos += num(v.get("total"));
            if (hoyIso.equals(diaIso(v.get("fecha")))) {
                hoy++;
            }
        }
        lblTotalVentas.setText(String.valueOf(datos.ventas.size()));
        lblIngresos.setText(Estilo.soles(ingresos));
        lblHoy.setText(String.valueOf(hoy));
    }

    // Solo los pedidos que cumplen todo lo que exige el sistema para venderse.
    private boolean vendible(Pedido p) {
        double[] c = datos.cuentas.get(p.getId());
        return !"CANCELADO".equalsIgnoreCase(p.getEstado()) && !datos.pedidosConVenta.contains(p.getId())
                && c != null && c[0] > 0 && Math.abs(c[1] - c[0]) <= 0.01;
    }

    private void llenarComboPedidos() {
        cargandoCombo = true;
        cmbPedido.removeAllItems();
        cmbPedido.addItem(new Opcion(0, "Seleccione un pedido..."));
        List<Pedido> orden = new ArrayList<>(datos.pedidos);
        orden.sort(Comparator.comparingInt(Pedido::getId).reversed());
        for (Pedido p : orden) {
            if (vendible(p)) {
                cmbPedido.addItem(new Opcion(p.getId(), "Pedido N.° " + tres(p.getId()) + " – " + clienteDe(p)));
            }
        }
        cmbPedido.setSelectedIndex(0);
        cargandoCombo = false;
        alElegirPedido();
    }

    private Pedido pedidoDeLista(int id) {
        for (Pedido p : datos.pedidos) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }

    private void alElegirPedido() {
        Opcion o = (Opcion) cmbPedido.getSelectedItem();
        double total = 0;
        double pagado = 0;
        pastillaEstado.setVisible(false);
        if (o != null && o.id > 0) {
            double[] c = datos.cuentas.get(o.id);
            if (c != null) {
                total = c[0];
                pagado = c[1];
            }
            Pedido p = pedidoDeLista(o.id);
            if (p != null) {
                pastillaEstado.poner(nz(p.getEstado()).toUpperCase());
                pastillaEstado.setVisible(true);
            }
        }
        lblTotalPedido.setText(Estilo.soles(total));
        lblPagado.setText(Estilo.soles(pagado));
        lblSaldo.setText(Estilo.soles(Math.max(0, total - pagado)));
    }

    private void aplicarFiltros() {
        String idVentaTxt = txtIdVenta.getText().trim();
        String idPedidoTxt = txtIdPedidoFiltro.getText().trim();
        int idVenta = 0;
        int idPedido = 0;
        try {
            if (!idVentaTxt.isEmpty()) {
                idVenta = Integer.parseInt(idVentaTxt);
                if (idVenta <= 0) {
                    throw new NumberFormatException();
                }
            }
            if (!idPedidoTxt.isEmpty()) {
                idPedido = Integer.parseInt(idPedidoTxt);
                if (idPedido <= 0) {
                    throw new NumberFormatException();
                }
            }
        } catch (NumberFormatException e) {
            advertencia("Los ID de venta y de pedido deben ser números enteros positivos.");
            return;
        }

        List<Map<String, Object>> orden = new ArrayList<>(datos.ventas);
        orden.sort((a, b) -> Integer.compare(entero(b.get("id_venta")), entero(a.get("id_venta"))));
        List<Object[]> filas = new ArrayList<>();
        for (Map<String, Object> v : orden) {
            if (idVenta > 0 && entero(v.get("id_venta")) != idVenta) {
                continue;
            }
            if (idPedido > 0 && entero(v.get("id_pedido")) != idPedido) {
                continue;
            }
            filas.add(new Object[]{v.get("id_venta"), v.get("id_pedido"), fechaBonita(v.get("fecha")),
                    Estilo.soles(num(v.get("total")))});
        }
        tabla.setFilas(filas);
        if (filas.isEmpty() && (idVenta > 0 || idPedido > 0)) {
            aviso("No se encontraron ventas con ese criterio.");
        }
    }

    // ---------- Acciones ----------

    private void verDetalle(Object[] fila) {
        int id = ((Number) fila[0]).intValue();
        Map<String, Object> v = ventaDAO.buscarVenta(id);
        if (v == null) {
            advertencia("No se encontró la venta N.° " + id + ".");
            return;
        }
        int idPedido = entero(v.get("id_pedido"));
        Pedido p = pedidoDeLista(idPedido);
        String cliente = p == null ? "" : "\nCliente: " + clienteDe(p);
        aviso("Venta N.° " + id + "\nPedido N.° " + idPedido + cliente
                + "\nFecha: " + fechaBonita(v.get("fecha"))
                + "\nTotal: " + Estilo.soles(num(v.get("total"))));
    }

    private void limpiar() {
        cmbPedido.setSelectedIndex(0);
        tabla.limpiarSeleccion();
    }

    private boolean tieneVenta(int idPedido) {
        for (Map<String, Object> v : ventaDAO.listarVentas()) {
            if (entero(v.get("id_pedido")) == idPedido) {
                return true;
            }
        }
        return false;
    }

    private void registrar() {
        Opcion ped = (Opcion) cmbPedido.getSelectedItem();
        if (ped == null || ped.id <= 0) {
            advertencia("Seleccione el pedido que se va a vender.");
            return;
        }
        double[] c = datos.cuentas.get(ped.id);
        double total = c == null ? 0 : c[0];
        if (!confirmar("¿Registrar la venta del " + ped.texto + "?\nTotal: " + Estilo.soles(total)
                + "\nCada pedido solo puede tener una venta.")) {
            return;
        }

        VentaOperaciones.registrarVenta(ped.id);

        if (tieneVenta(ped.id)) {
            aviso("Venta registrada correctamente.\nPedido N.° " + ped.id + " – Total: " + Estilo.soles(total));
            recargar();
        } else {
            error("No se pudo registrar la venta.\nRevise que el pedido esté completamente pagado, que no esté "
                    + "cancelado y que no tenga ya una venta.");
            recargar();
        }
    }
}
