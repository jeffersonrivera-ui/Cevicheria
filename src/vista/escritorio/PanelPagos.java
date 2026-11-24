package vista.escritorio;

import controlador.PagoOperaciones;
import dao.DetallePedidoDAO;
import dao.PagoDAO;
import dao.PedidoDAO;
import dao.impl.DetallePedidoDAOImpl;
import dao.impl.PagoDAOImpl;
import dao.impl.PedidoDAOImpl;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Sección Pagos: abonos de pedidos e historial de pagos (administrador). */
public class PanelPagos extends ModuloBase {

    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String[] METODOS = {"Efectivo", "Yape", "Plin", "Tarjeta", "Transferencia"};

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
        List<Map<String, Object>> pagos = new ArrayList<>();
        List<Pedido> pedidos = new ArrayList<>();
        Map<Integer, double[]> cuentas = new HashMap<>(); // id pedido -> {total, pagado}
    }

    private final PagoDAO pagoDAO = new PagoDAOImpl();
    private final PedidoDAO pedidoDAO = new PedidoDAOImpl();
    private final DetallePedidoDAO detalleDAO = new DetallePedidoDAOImpl();

    private Datos datos = new Datos();
    private boolean cargandoCombo = false;

    private final JLabel lblTotalPagos;
    private final JLabel lblRecaudado;
    private final JLabel lblPendientes;

    private final JTextField txtIdPago = new CampoHint("Ingrese el ID del pago...");
    private final JTextField txtIdPedidoFiltro = new CampoHint("Ingrese el ID del pedido...");
    private final TablaPaginada tabla = new TablaPaginada(
            new String[]{"ID pago", "ID pedido", "Monto (S/)", "Método de pago", "Fecha", "Estado"}, 5, true);

    private final JComboBox<Opcion> cmbPedido = new JComboBox<>();
    private final JLabel lblTotalPedido = Estilo.etiqueta("S/ 0.00", Estilo.NEGRITA, new Color(0x15803D));
    private final JLabel lblPagado = Estilo.etiqueta("S/ 0.00", Estilo.NEGRITA, new Color(0x15803D));
    private final JLabel lblSaldo = Estilo.etiqueta("S/ 0.00", Estilo.NEGRITA, new Color(0x15803D));
    private final JSpinner spnMonto = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 1000000.0, 0.5));
    private final JComboBox<String> cmbMetodo = new JComboBox<>(METODOS);
    private final JLabel lblRestante = Estilo.etiqueta("S/ 0.00", Estilo.SUBTITULO, new Color(0x15803D));

    public PanelPagos() {
        super("Pagos", "Registra abonos y consulta el historial de pagos", "💳", Estilo.PRIMARIO, true);

        lblTotalPagos = agregarEstadistica("☷", new Color(0x38A3F5), "Total de pagos", Estilo.PRIMARIO_OSCURO);
        lblRecaudado = agregarEstadistica("S/", Estilo.EXITO, "Monto recaudado", new Color(0x15803D));
        lblPendientes = agregarEstadistica("⌛", Estilo.PELIGRO, "Saldos pendientes", Estilo.PELIGRO);
        setAnchoEstadisticas(640);

        // ----- Izquierda -----
        txtIdPago.setPreferredSize(new Dimension(100, 36));
        txtIdPago.setMinimumSize(new Dimension(100, 36));
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
        arriba.add(barraBusqueda("Buscar por ID de pago:", txtIdPago, extra, btnBuscar), BorderLayout.NORTH);
        arriba.add(tituloDePanel("≡", "Historial de pagos"), BorderLayout.CENTER);
        panelIzquierdo.add(arriba, BorderLayout.NORTH);

        tabla.setColorPastilla(t -> "ADELANTO".equalsIgnoreCase(t)
                ? new Color[]{new Color(0xFEF3C7), new Color(0xB45309)}
                : Estilo.coloresPastilla(t));
        tabla.setAnchos(50, 60, 70, 90, 110, 80, 50);
        tabla.setTituloAcciones("Acción");
        tabla.setAcciones(new TablaPaginada.Accion("◉", new Color(0x1E6FEB), Color.WHITE, this::verDetalle));
        panelIzquierdo.add(tabla, BorderLayout.CENTER);

        // ----- Derecha -----
        cmbPedido.setFont(Estilo.FUENTE);
        cmbPedido.setPreferredSize(new Dimension(120, 36));
        cmbMetodo.setFont(Estilo.FUENTE);
        cmbMetodo.setPreferredSize(new Dimension(120, 36));
        spnMonto.setFont(Estilo.FUENTE);
        spnMonto.setPreferredSize(new Dimension(120, 36));
        spnMonto.setEditor(new JSpinner.NumberEditor(spnMonto, "0.00"));

        JPanel cuenta = new JPanel(new GridLayout(3, 1, 0, 2));
        cuenta.setOpaque(false);
        cuenta.add(fila("Total del pedido:", lblTotalPedido));
        cuenta.add(fila("Total pagado actualmente:", lblPagado));
        cuenta.add(fila("Saldo pendiente:", lblSaldo));
        JPanel tarjetaCuenta = fondo(new Color(0xE3EEFB), new BorderLayout());
        tarjetaCuenta.setBorder(new EmptyBorder(6, 12, 6, 12));
        tarjetaCuenta.add(cuenta, BorderLayout.CENTER);

        JPanel barraRestante = fondo(new Color(0xDCFCE7), new FlowLayout(FlowLayout.LEFT, 8, 8));
        barraRestante.add(Estilo.etiqueta("Saldo restante:", Estilo.NEGRITA, Estilo.PRIMARIO_OSCURO));
        barraRestante.add(lblRestante);
        barraRestante.setPreferredSize(new Dimension(300, 46));

        JButton btnRegistrar = Estilo.boton("Registrar pago", Estilo.EXITO);
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
        col.add(alinear(tituloDePanel("+", "Registrar pago")));
        col.add(Box.createVerticalStrut(10));
        col.add(alinear(conEtiqueta("Pedido:", true, cmbPedido)));
        col.add(Box.createVerticalStrut(8));
        col.add(alinear(tarjetaCuenta));
        col.add(Box.createVerticalStrut(8));
        col.add(alinear(conEtiqueta("Monto a pagar (S/):", true, spnMonto)));
        col.add(Box.createVerticalStrut(8));
        col.add(alinear(conEtiqueta("Método de pago:", true, cmbMetodo)));
        col.add(Box.createVerticalStrut(10));
        col.add(alinear(barraRestante));
        col.add(Box.createVerticalStrut(12));
        col.add(alinear(botones));
        col.add(Box.createVerticalStrut(12));
        col.add(alinear(Estilo.aviso("Puedes registrar pagos parciales hasta completar el total del pedido.",
                "info", 210)));
        col.add(Box.createVerticalStrut(8));
        col.add(alinear(Estilo.aviso("No se puede pagar un pedido cancelado ni registrar un monto superior al "
                + "saldo pendiente.", "alerta", 210)));
        panelDerecho.add(col, BorderLayout.NORTH);
        setAnchoDerecho(370);

        // ----- Eventos -----
        btnBuscar.addActionListener(e -> aplicarFiltros());
        txtIdPago.addActionListener(e -> aplicarFiltros());
        txtIdPedidoFiltro.addActionListener(e -> aplicarFiltros());
        btnRegistrar.addActionListener(e -> registrar());
        btnLimpiar.addActionListener(e -> limpiar());
        cmbPedido.addActionListener(e -> {
            if (!cargandoCombo) {
                alElegirPedido();
            }
        });
        spnMonto.addChangeListener(e -> recalcularRestante());

        recargar(null);

        // Al volver a esta sección se vuelve a leer la base (por ejemplo, para ver pedidos nuevos).
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                recargar(null);
            }
        });
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

    private static String dos(double v) {
        return String.format(Locale.US, "%.2f", v);
    }

    // Un pedido tiene un solo registro de pago: "Adelanto" mientras queda saldo, "Pagado" cuando se completa.
    // (Para registros antiguos con varias filas se calcula con el total acumulado del pedido.)
    private String etiquetaEstado(Map<String, Object> pago) {
        String estado = nz((String) pago.get("estado"));
        if (!esPagado(pago)) {
            return estado;
        }
        double[] cuenta = datos.cuentas.get(entero(pago.get("id_pedido")));
        if (cuenta == null || cuenta[0] <= 0) {
            return estado;
        }
        return cuenta[1] + 0.01 >= cuenta[0] ? "Pagado" : "Adelanto";
    }

    private static boolean esPagado(Map<String, Object> p) {
        String e = String.valueOf(p.get("estado"));
        return "Pagado".equalsIgnoreCase(e) || "Adelanto".equalsIgnoreCase(e);
    }

    private static String tres(int n) {
        return String.format("%03d", n);
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

    // Lee pagos, pedidos y cuentas (total y pagado de cada pedido) sin bloquear la ventana.
    private void recargar(Integer pedidoAElegir) {
        new SwingWorker<Datos, Void>() {
            @Override
            protected Datos doInBackground() {
                Datos d = new Datos();
                d.pagos = pagoDAO.listarPagos();
                d.pedidos = pedidoDAO.listar();
                Map<Integer, Double> pagado = new HashMap<>();
                for (Map<String, Object> p : d.pagos) {
                    if (esPagado(p)) {
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
                    error("No se pudieron cargar los pagos: " + ex.getMessage());
                    return;
                }
                actualizarEstadisticas();
                llenarComboPedidos(pedidoAElegir);
                aplicarFiltros();
            }
        }.execute();
    }

    private void actualizarEstadisticas() {
        double recaudado = 0;
        for (Map<String, Object> p : datos.pagos) {
            if (esPagado(p)) {
                recaudado += num(p.get("monto"));
            }
        }
        double pendientes = 0;
        for (Pedido p : datos.pedidos) {
            if (!"CANCELADO".equalsIgnoreCase(p.getEstado())) {
                double[] c = datos.cuentas.get(p.getId());
                if (c != null && c[0] > 0) {
                    pendientes += Math.max(0, c[0] - c[1]);
                }
            }
        }
        lblTotalPagos.setText(String.valueOf(datos.pagos.size()));
        lblRecaudado.setText(Estilo.soles(recaudado));
        lblPendientes.setText(Estilo.soles(pendientes));
    }

    private void llenarComboPedidos(Integer elegir) {
        cargandoCombo = true;
        cmbPedido.removeAllItems();
        cmbPedido.addItem(new Opcion(0, "Seleccione un pedido..."));
        List<Pedido> orden = new ArrayList<>(datos.pedidos);
        orden.sort(Comparator.comparingInt(Pedido::getId).reversed());
        int indice = 0;
        for (Pedido p : orden) {
            double[] c = datos.cuentas.get(p.getId());
            boolean pagable = !"CANCELADO".equalsIgnoreCase(p.getEstado()) && c != null && c[0] > 0
                    && c[0] - c[1] > 0.01;
            if (pagable) {
                cmbPedido.addItem(new Opcion(p.getId(), "Pedido N.° " + tres(p.getId()) + " – " + clienteDe(p)));
                if (elegir != null && elegir == p.getId()) {
                    indice = cmbPedido.getItemCount() - 1;
                }
            }
        }
        cmbPedido.setSelectedIndex(indice);
        cargandoCombo = false;
        alElegirPedido();
    }

    private void alElegirPedido() {
        Opcion o = (Opcion) cmbPedido.getSelectedItem();
        double total = 0;
        double pagado = 0;
        if (o != null && o.id > 0) {
            double[] c = datos.cuentas.get(o.id);
            if (c != null) {
                total = c[0];
                pagado = c[1];
            }
        }
        double saldo = Math.max(0, total - pagado);
        lblTotalPedido.setText(Estilo.soles(total));
        lblPagado.setText(Estilo.soles(pagado));
        lblSaldo.setText(Estilo.soles(saldo));
        spnMonto.setValue(Math.round(saldo * 100) / 100.0);
        recalcularRestante();
    }

    private double saldoElegido() {
        Opcion o = (Opcion) cmbPedido.getSelectedItem();
        if (o == null || o.id <= 0) {
            return 0;
        }
        double[] c = datos.cuentas.get(o.id);
        return c == null ? 0 : Math.max(0, c[0] - c[1]);
    }

    private double montoEscrito() {
        try {
            spnMonto.commitEdit();
        } catch (java.text.ParseException ignorada) {
        }
        return ((Number) spnMonto.getValue()).doubleValue();
    }

    private void recalcularRestante() {
        lblRestante.setText(Estilo.soles(Math.max(0, saldoElegido() - ((Number) spnMonto.getValue()).doubleValue())));
    }

    private void aplicarFiltros() {
        String idPagoTxt = txtIdPago.getText().trim();
        String idPedidoTxt = txtIdPedidoFiltro.getText().trim();
        int idPago = 0;
        int idPedido = 0;
        try {
            if (!idPagoTxt.isEmpty()) {
                idPago = Integer.parseInt(idPagoTxt);
                if (idPago <= 0) {
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
            advertencia("Los ID de pago y de pedido deben ser números enteros positivos.");
            return;
        }

        List<Map<String, Object>> orden = new ArrayList<>(datos.pagos);
        orden.sort((a, b) -> Integer.compare(entero(b.get("id_pago")), entero(a.get("id_pago"))));
        List<Object[]> filas = new ArrayList<>();
        for (Map<String, Object> p : orden) {
            if (idPago > 0 && entero(p.get("id_pago")) != idPago) {
                continue;
            }
            if (idPedido > 0 && entero(p.get("id_pedido")) != idPedido) {
                continue;
            }
            filas.add(new Object[]{p.get("id_pago"), p.get("id_pedido"), dos(num(p.get("monto"))),
                    nz((String) p.get("metodo_pago")), fechaBonita(p.get("fecha")), etiquetaEstado(p)});
        }
        tabla.setFilas(filas);
        if (filas.isEmpty() && (idPago > 0 || idPedido > 0)) {
            aviso("No se encontraron pagos con ese criterio.");
        }
    }

    // ---------- Acciones ----------

    private void verDetalle(Object[] fila) {
        int id = ((Number) fila[0]).intValue();
        Map<String, Object> p = pagoDAO.buscarPago(id);
        if (p == null) {
            advertencia("No se encontró el pago N.° " + id + ".");
            return;
        }
        int idPedido = entero(p.get("id_pedido"));
        double[] c = datos.cuentas.get(idPedido);
        String cuenta = c == null ? "" : "\n\nTotal del pedido: " + Estilo.soles(c[0])
                + "\nTotal pagado: " + Estilo.soles(c[1])
                + "\nSaldo pendiente: " + Estilo.soles(Math.max(0, c[0] - c[1]));
        aviso("Pago N.° " + id + "\nPedido N.° " + idPedido
                + "\nMonto: " + Estilo.soles(num(p.get("monto")))
                + "\nMétodo de pago: " + nz((String) p.get("metodo_pago"))
                + "\nFecha: " + fechaBonita(p.get("fecha"))
                + "\nEstado: " + etiquetaEstado(p) + cuenta);
    }

    private void limpiar() {
        cmbPedido.setSelectedIndex(0);
        cmbMetodo.setSelectedIndex(0);
        tabla.limpiarSeleccion();
    }

    private double pagadoActual(int idPedido) {
        double suma = 0;
        for (Map<String, Object> p : pagoDAO.listarPagos()) {
            if (entero(p.get("id_pedido")) == idPedido && esPagado(p)) {
                suma += num(p.get("monto"));
            }
        }
        return suma;
    }

    private void registrar() {
        Opcion ped = (Opcion) cmbPedido.getSelectedItem();
        if (ped == null || ped.id <= 0) {
            advertencia("Seleccione el pedido que se va a pagar.");
            return;
        }
        double monto = montoEscrito();
        if (monto <= 0) {
            advertencia("El monto a pagar debe ser mayor que cero.");
            return;
        }
        double saldo = saldoElegido();
        if (monto > saldo + 0.01) {
            advertencia("El monto supera el saldo pendiente.\nSaldo pendiente: " + Estilo.soles(saldo));
            return;
        }
        String metodo = (String) cmbMetodo.getSelectedItem();
        if (metodo == null || metodo.trim().isEmpty()) {
            advertencia("Seleccione el método de pago.");
            return;
        }
        if (!confirmar("¿Registrar el pago?\n\n" + ped.texto + "\nMonto: " + Estilo.soles(monto)
                + "\nMétodo: " + metodo + "\nSaldo restante: " + Estilo.soles(Math.max(0, saldo - monto)))) {
            return;
        }

        double antes = pagadoActual(ped.id);
        PagoOperaciones.registrarPago(ped.id, monto, metodo);
        double despues = pagadoActual(ped.id);

        if (despues > antes + 0.001) {
            double restante = Math.max(0, saldo - monto);
            // Si el pedido quedó pagado por completo, la venta se registra sola.
            boolean ventaAutomatica = false;
            if (restante <= 0.01) {
                controlador.VentaOperaciones.registrarVenta(ped.id);
                ventaAutomatica = true;
            }
            aviso("Pago registrado correctamente.\nPedido N.° " + ped.id + " – Monto: " + Estilo.soles(monto)
                    + "\nSaldo pendiente: " + Estilo.soles(restante)
                    + (ventaAutomatica ? "\n\nEl pedido quedó pagado por completo. Su venta se registró "
                    + "automáticamente en \"Ventas\"." : ""));
            recargar(null);
            limpiar();
        } else {
            error("No se pudo registrar el pago.\nRevise que el pedido no esté cancelado, que tenga platos y que "
                    + "el monto no supere el saldo pendiente.");
            recargar(ped.id);
        }
    }
}
