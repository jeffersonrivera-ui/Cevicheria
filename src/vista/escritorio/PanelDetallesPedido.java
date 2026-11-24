package vista.escritorio;

import controlador.DetallePedidoOperacione;
import controlador.PedidosOperaciones;
import dao.DetallePedidoDAO;
import dao.PedidoDAO;
import dao.PlatoDAO;
import dao.impl.DetallePedidoDAOImpl;
import dao.impl.PedidoDAOImpl;
import dao.impl.PlatoDAOImpl;
import modelo.DetallePedido;
import modelo.Pedido;
import modelo.Plato;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Sección Detalles de pedidos. Con idMesero == null la usa el administrador (todos los pedidos);
 * con un id, el mesero trabaja solo con sus pedidos.
 */
public class PanelDetallesPedido extends ModuloBase {

    private static final DateTimeFormatter FECHA_UI =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final int MAX_OBSERVACION = 100;

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

    private final PedidoDAO pedidoDAO = new PedidoDAOImpl();
    private final DetallePedidoDAO detalleDAO = new DetallePedidoDAOImpl();
    private final PlatoDAO platoDAO = new PlatoDAOImpl();
    private final Integer idMesero;

    private List<Pedido> pedidos = new ArrayList<>();
    private Pedido pedidoSel = null;
    private int idDetalleSel = 0;

    private final JLabel lblPedidosConDetalle;
    private final JLabel lblPlatos;
    private final JLabel lblAcumulado;

    // Busqueda
    private final JTextField txtBuscar = new CampoHint("Ingrese el ID del pedido o nombre del cliente...");
    private final JTextField txtFechaFiltro = new CampoHint("dd/mm/aaaa");

    // Pedido seleccionado
    private final JLabel lblInfoPedido = Estilo.etiqueta("Pedido seleccionado: ninguno", Estilo.PEQUENA_NEGRITA, Estilo.PRIMARIO_OSCURO);
    private final Estilo.Pastilla pastillaEstado = new Estilo.Pastilla();
    private final TablaPaginada tabla = new TablaPaginada(
            new String[]{"ID detalle", "Plato", "Cantidad", "Precio unit. (S/)", "Observación", "Subtotal (S/)"}, -1, true);
    private final JLabel lblTotalPedido = Estilo.etiqueta("S/ 0.00", Estilo.SUBTITULO, new Color(0x15803D));

    // Formulario
    private final JTextField txtIdPedido = Estilo.campo();
    private final JComboBox<Opcion> cmbPlato = new JComboBox<>();
    private final JSpinner spnCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
    private final JTextField txtPrecio = Estilo.campo();
    private final JTextArea txtObservacion = new JTextArea(3, 20);
    private final JLabel lblContador = Estilo.etiqueta("0 / " + MAX_OBSERVACION, Estilo.PEQUENA, Estilo.TEXTO_SUAVE);
    private final JLabel lblSubtotal = Estilo.etiqueta("0.00", Estilo.SUBTITULO, new Color(0x15803D));
    private double precioActual = 0;

    // Platos adicionales del formulario (botón "+ Agregar otro plato").
    private final JPanel panelExtras = new JPanel();
    private final List<FilaPlato> extras = new ArrayList<>();
    private JButton btnMasPlatos;

    // Una fila extra: plato + cantidad + precio unitario (igual que la primera).
    private final class FilaPlato extends JPanel {
        final JComboBox<Opcion> cmb = new JComboBox<>();
        final JSpinner spn = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        final JTextField precio = Estilo.campo();

        FilaPlato(int numero) {
            super(new GridBagLayout());
            setOpaque(false);

            for (int i = 0; i < cmbPlato.getItemCount(); i++) {
                cmb.addItem(cmbPlato.getItemAt(i));
            }
            cmb.setFont(Estilo.FUENTE);
            cmb.setPreferredSize(new Dimension(120, 36));
            spn.setFont(Estilo.FUENTE);
            spn.setPreferredSize(new Dimension(100, 36));
            precio.setEditable(false);
            precio.setBackground(new Color(0xEEF2F8));
            precio.setText("0.00");

            JButton quitar = new JButton("\u2716  Quitar");
            quitar.setFont(Estilo.PEQUENA_NEGRITA);
            quitar.setForeground(Estilo.PELIGRO);
            quitar.setContentAreaFilled(false);
            quitar.setBorderPainted(false);
            quitar.setFocusPainted(false);
            quitar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            quitar.addActionListener(e -> quitarFila(this));

            JPanel cab = new JPanel(new BorderLayout());
            cab.setOpaque(false);
            cab.add(Estilo.etiquetaCampo("Plato " + numero + ":", true), BorderLayout.WEST);
            cab.add(quitar, BorderLayout.EAST);

            GridBagConstraints g = new GridBagConstraints();
            g.fill = GridBagConstraints.HORIZONTAL;
            g.weightx = 1;
            g.gridx = 0;
            g.gridy = 0;
            g.gridwidth = 2;
            g.insets = new Insets(0, 0, 4, 0);
            add(cab, g);
            g.gridy = 1;
            g.insets = new Insets(0, 0, 8, 0);
            add(cmb, g);
            g.gridy = 2;
            g.gridwidth = 1;
            g.insets = new Insets(0, 0, 8, 6);
            add(conEtiqueta("Cantidad:", true, spn), g);
            g.gridx = 1;
            g.insets = new Insets(0, 6, 8, 0);
            add(conEtiqueta("Precio unitario (S/):", false, precio), g);

            cmb.addActionListener(e -> {
                actualizarPrecio();
                refrescarPrecioYSubtotal();
            });
            spn.addChangeListener(e -> refrescarPrecioYSubtotal());
        }

        int idPlato() {
            Opcion o = (Opcion) cmb.getSelectedItem();
            return o == null ? 0 : o.id;
        }

        int cantidad() {
            return ((Number) spn.getValue()).intValue();
        }

        double precioUnitario() {
            Plato p = platoDeLista(idPlato());
            return p == null ? 0 : p.getPrecio();
        }

        void actualizarPrecio() {
            precio.setText(String.format(java.util.Locale.US, "%.2f", precioUnitario()));
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }
    }

    public PanelDetallesPedido() {
        this(null);
    }

    public PanelDetallesPedido(Integer idMesero) {
        super("Detalles de pedidos", "Administra los platos y cantidades de cada pedido", "≡", Estilo.PRIMARIO, true);
        this.idMesero = idMesero;

        lblPedidosConDetalle = agregarEstadistica("☷", new Color(0x38A3F5), "Pedidos con detalles", Estilo.PRIMARIO_OSCURO);
        lblPlatos = agregarEstadistica("♨", Estilo.EXITO, "Platos registrados", new Color(0x15803D));
        lblAcumulado = agregarEstadistica("S/", Estilo.PELIGRO, "Total acumulado", Estilo.PELIGRO);

        // ----- Izquierda -----
        txtBuscar.setPreferredSize(new Dimension(100, 36));
        txtBuscar.setMinimumSize(new Dimension(100, 36));
        txtFechaFiltro.setPreferredSize(new Dimension(105, 36));
        JPanel colFecha = new JPanel(new BorderLayout(0, 4));
        colFecha.setOpaque(false);
        colFecha.add(Estilo.etiqueta("Fecha:", Estilo.PEQUENA_NEGRITA, Estilo.TEXTO), BorderLayout.NORTH);
        colFecha.add(txtFechaFiltro, BorderLayout.CENTER);
        colFecha.setPreferredSize(new Dimension(105, 58));
        JPanel extra = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        extra.setOpaque(false);
        extra.add(colFecha);
        extra.setPreferredSize(new Dimension(105, 58));

        JButton btnBuscar = Estilo.boton("Buscar pedido", Estilo.PRIMARIO);
        JPanel zonaBusqueda = barraBusqueda("Buscar pedido por ID o cliente:", txtBuscar, extra, btnBuscar);

        JPanel barraInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xE3EEFB));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        barraInfo.setOpaque(false);
        barraInfo.add(lblInfoPedido);
        barraInfo.add(pastillaEstado);
        pastillaEstado.setVisible(false);

        JPanel cabeceraLista = new JPanel(new BorderLayout(0, 8));
        cabeceraLista.setOpaque(false);
        cabeceraLista.add(tituloDePanel("≡", "Platos del pedido"), BorderLayout.NORTH);
        cabeceraLista.add(barraInfo, BorderLayout.CENTER);

        JPanel arriba = new JPanel(new BorderLayout(0, 12));
        arriba.setOpaque(false);
        arriba.add(zonaBusqueda, BorderLayout.NORTH);
        arriba.add(cabeceraLista, BorderLayout.CENTER);
        panelIzquierdo.add(arriba, BorderLayout.NORTH);

        tabla.setAnchos(50, 120, 60, 80, 110, 80, 80);
        tabla.setAcciones(
                new TablaPaginada.Accion("✎", new Color(0x1E6FEB), Color.WHITE, this::editarDesdeTabla),
                new TablaPaginada.Accion("✖", new Color(0xEF4444), Color.WHITE, this::eliminarDesdeTabla));
        tabla.setEscucha(new TablaPaginada.Escucha() {
            @Override
            public void seleccionar(Object[] fila) {
                llenarFormulario(((Number) fila[0]).intValue());
            }
        });

        JPanel filaTotal = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filaTotal.setOpaque(false);
        filaTotal.add(Estilo.etiqueta("Total del pedido:", Estilo.SUBTITULO, Estilo.PRIMARIO_OSCURO));
        filaTotal.add(lblTotalPedido);

        JPanel centro = new JPanel(new BorderLayout(0, 6));
        centro.setOpaque(false);
        centro.add(tabla, BorderLayout.CENTER);
        centro.add(filaTotal, BorderLayout.SOUTH);
        panelIzquierdo.add(centro, BorderLayout.CENTER);

        // ----- Derecha -----
        txtIdPedido.setEditable(false);
        txtIdPedido.setBackground(new Color(0xEEF2F8));
        txtPrecio.setEditable(false);
        txtPrecio.setBackground(new Color(0xEEF2F8));
        txtPrecio.setText("0.00");
        cmbPlato.setFont(Estilo.FUENTE);
        cmbPlato.setPreferredSize(new Dimension(120, 36));
        spnCantidad.setFont(Estilo.FUENTE);
        spnCantidad.setPreferredSize(new Dimension(100, 36));
        txtObservacion.setFont(Estilo.FUENTE);
        txtObservacion.setLineWrap(true);
        txtObservacion.setWrapStyleWord(true);
        txtObservacion.setBorder(new EmptyBorder(6, 8, 6, 8));
        JScrollPane spObs = new JScrollPane(txtObservacion);
        spObs.setBorder(BorderFactory.createLineBorder(new Color(0xCBD5E8)));
        spObs.setPreferredSize(new Dimension(120, 70));

        // El alto se calcula siempre de nuevo: crece al agregar más platos con el botón "+".
        JPanel form = new JPanel(new GridBagLayout()) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        form.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        g.insets = new Insets(0, 0, 8, 0);

        g.gridx = 0;
        g.gridy = 0;
        g.gridwidth = 2;
        form.add(conEtiqueta("ID del pedido:", false, txtIdPedido), g);
        g.gridy = 1;
        form.add(conEtiqueta("Plato:", true, cmbPlato), g);
        g.gridy = 2;
        g.gridwidth = 1;
        g.insets = new Insets(0, 0, 8, 6);
        form.add(conEtiqueta("Cantidad:", true, spnCantidad), g);
        g.gridx = 1;
        g.insets = new Insets(0, 6, 8, 0);
        form.add(conEtiqueta("Precio unitario (S/):", false, txtPrecio), g);
        g.gridx = 0;
        g.gridy = 3;
        g.gridwidth = 2;
        panelExtras.setOpaque(false);
        panelExtras.setLayout(new BoxLayout(panelExtras, BoxLayout.Y_AXIS));
        g.insets = new Insets(0, 0, 0, 0);
        form.add(panelExtras, g);
        g.gridy = 4;
        g.insets = new Insets(0, 0, 8, 0);
        btnMasPlatos = Estilo.boton("+  Agregar otro plato", Estilo.PRIMARIO);
        btnMasPlatos.setFont(Estilo.PEQUENA_NEGRITA);
        btnMasPlatos.setPreferredSize(new Dimension(120, 34));
        form.add(btnMasPlatos, g);
        g.gridy = 5;
        g.insets = new Insets(0, 0, 2, 0);
        form.add(conEtiqueta("Observación:", false, spObs), g);
        g.gridy = 6;
        g.insets = new Insets(0, 0, 8, 0);
        JPanel contador = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        contador.setOpaque(false);
        contador.add(lblContador);
        form.add(contador, g);

        JPanel barraSubtotal = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6)) {
            @Override
            protected void paintComponent(Graphics g2d) {
                Graphics2D g2 = (Graphics2D) g2d.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xE3EEFB));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g2d);
            }
        };
        barraSubtotal.setOpaque(false);
        barraSubtotal.add(Estilo.etiqueta("⚙", Estilo.SUBTITULO, Estilo.PRIMARIO));
        barraSubtotal.add(Estilo.etiqueta("Subtotal (S/):", Estilo.NEGRITA, Estilo.PRIMARIO_OSCURO));
        barraSubtotal.add(lblSubtotal);

        JButton btnAgregar = Estilo.boton("+  Agregar plato", Estilo.EXITO);
        JButton btnActualizar = Estilo.boton("\u270E  Actualizar", Estilo.PRIMARIO);
        JButton btnEliminar = Estilo.boton("Eliminar", Estilo.PELIGRO);
        JButton btnLimpiar = Estilo.boton("\u21BB  Limpiar", Estilo.GRIS);
        for (JButton b : new JButton[]{btnAgregar, btnActualizar, btnEliminar, btnLimpiar}) {
            b.setFont(Estilo.PEQUENA_NEGRITA);
        }

        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.add(alinear(tituloDePanel("+", "Agregar / Actualizar detalle")));
        col.add(Box.createVerticalStrut(10));
        form.setAlignmentX(Component.LEFT_ALIGNMENT);
        col.add(form);
        col.add(alinear(barraSubtotal));
        col.add(Box.createVerticalStrut(10));
        JPanel botones = new JPanel(new GridLayout(2, 2, 10, 8));
        botones.setOpaque(false);
        botones.add(btnAgregar);
        botones.add(btnActualizar);
        botones.add(btnEliminar);
        botones.add(btnLimpiar);
        col.add(alinear(botones));
        col.add(Box.createVerticalStrut(10));
        col.add(alinear(Estilo.aviso("Solo se pueden agregar o modificar platos cuando el pedido esté en estado "
                + "<b>PENDIENTE</b> o <b>EN PREPARACIÓN</b>.", "info", 210)));
        col.add(Box.createVerticalStrut(8));
        col.add(alinear(Estilo.aviso("No se pueden modificar ni eliminar detalles cuando el pedido esté finalizado "
                + "(<b>LISTO, ENTREGADO o CANCELADO</b>) o tenga pagos registrados.", "alerta", 210)));
        panelDerecho.add(col, BorderLayout.NORTH);
        setAnchoDerecho(370);
        setAnchoEstadisticas(610);

        // ----- Eventos -----
        btnBuscar.addActionListener(e -> buscarPedido());
        txtBuscar.addActionListener(e -> buscarPedido());
        txtFechaFiltro.addActionListener(e -> buscarPedido());
        btnMasPlatos.addActionListener(e -> agregarFila());
        btnAgregar.addActionListener(e -> agregar());
        btnActualizar.addActionListener(e -> actualizar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        cmbPlato.addActionListener(e -> {
            Opcion o = (Opcion) cmbPlato.getSelectedItem();
            if (o != null && o.id > 0 && idDetalleSel == 0) {
                Plato p = platoDeLista(o.id);
                precioActual = p == null ? 0 : p.getPrecio();
            } else if (o == null || o.id <= 0) {
                precioActual = 0;
            }
            refrescarPrecioYSubtotal();
        });
        spnCantidad.addChangeListener(e -> refrescarPrecioYSubtotal());
        txtObservacion.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                contar();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                contar();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                contar();
            }
        });

        cargarPlatos();
        cargarPedidos();
        mostrarPedido(null);

        // Al volver a esta sección se leen de nuevo platos disponibles y pedidos.
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                Opcion previo = (Opcion) cmbPlato.getSelectedItem();
                cargarPlatos();
                if (previo != null && previo.id > 0) {
                    for (int k = 0; k < cmbPlato.getItemCount(); k++) {
                        if (cmbPlato.getItemAt(k).id == previo.id) {
                            cmbPlato.setSelectedIndex(k);
                            break;
                        }
                    }
                }
                cargarPedidos();
            }
        });
    }

    // ---------- Construcción ----------

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

    private static String nombre(String nombres, String apellidos) {
        return (nz(nombres) + " " + nz(apellidos)).trim();
    }

    private static String fechaBonita(String iso) {
        if (iso == null || iso.isEmpty()) {
            return "";
        }
        try {
            return LocalDate.parse(iso.length() > 10 ? iso.substring(0, 10) : iso).format(FECHA_UI);
        } catch (Exception e) {
            return iso;
        }
    }

    private List<Plato> platosActivos = new ArrayList<>();

    private Plato platoDeLista(int id) {
        for (Plato p : platosActivos) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }

    private void cargarPlatos() {
        platosActivos = new ArrayList<>();
        cmbPlato.removeAllItems();
        cmbPlato.addItem(new Opcion(0, "Seleccione plato..."));
        try {
            for (Plato p : platoDAO.listar()) {
                if ("ACTIVO".equalsIgnoreCase(platoDAO.obtenerEstado(p.getId()))) {
                    platosActivos.add(p);
                    cmbPlato.addItem(new Opcion(p.getId(), p.getNombre()));
                }
            }
        } catch (Exception ex) {
            error("No se pudieron cargar los platos: " + ex.getMessage());
        }
    }

    private void cargarPedidos() {
        try {
            pedidos = new ArrayList<>(pedidoDAO.listar());
            if (idMesero != null) {
                pedidos.removeIf(p -> p.getMesero() == null || p.getMesero().getId() != idMesero);
            }
            pedidos.sort(Comparator.comparingInt(Pedido::getId).reversed());
        } catch (Exception ex) {
            error("No se pudieron cargar los pedidos: " + ex.getMessage());
            return;
        }
        calcularEstadisticas();
    }

    private void calcularEstadisticas() {
        final List<Pedido> copia = new ArrayList<>(pedidos);
        new SwingWorker<double[], Void>() {
            @Override
            protected double[] doInBackground() {
                int conDetalle = 0;
                int lineas = 0;
                double total = 0;
                for (Pedido p : copia) {
                    List<DetallePedido> ds = detalleDAO.listarPorPedido(p.getId());
                    if (!ds.isEmpty()) {
                        conDetalle++;
                        lineas += ds.size();
                        if (!"CANCELADO".equalsIgnoreCase(p.getEstado())) {
                            for (DetallePedido d : ds) {
                                total += d.calcularSubtotal();
                            }
                            total += controlador.DetalleBebidaOperaciones.calcularTotalBebidas(p.getId());
                        }
                    }
                }
                return new double[]{conDetalle, lineas, total};
            }

            @Override
            protected void done() {
                try {
                    double[] r = get();
                    lblPedidosConDetalle.setText(String.valueOf((int) r[0]));
                    lblPlatos.setText(String.valueOf((int) r[1]));
                    lblAcumulado.setText(Estilo.soles(r[2]));
                } catch (Exception ignorada) {
                }
            }
        }.execute();
    }

    private Pedido pedidoPermitido(int id) {
        Pedido p = PedidosOperaciones.buscarPedidoObjeto(id);
        if (p != null && idMesero != null && (p.getMesero() == null || p.getMesero().getId() != idMesero)) {
            return null;
        }
        return p;
    }

    // ---------- Búsqueda y selección de pedido ----------

    private void buscarPedido() {
        String texto = txtBuscar.getText().trim();
        String fechaTxt = txtFechaFiltro.getText().trim();
        if (texto.isEmpty() && fechaTxt.isEmpty()) {
            advertencia("Ingrese el ID del pedido, el nombre del cliente o una fecha.");
            return;
        }
        LocalDate fecha = null;
        if (!fechaTxt.isEmpty()) {
            try {
                fecha = LocalDate.parse(fechaTxt, FECHA_UI);
            } catch (Exception e) {
                advertencia("La fecha no es válida. Use el formato dd/mm/aaaa (por ejemplo 26/01/2025).");
                return;
            }
        }
        cargarPedidos();

        List<Pedido> coincidencias = new ArrayList<>();
        String t = texto.toLowerCase();
        for (Pedido p : pedidos) {
            String cliente = p.getCliente() == null ? "" : nombre(p.getCliente().getNombres(), p.getCliente().getApellidos());
            boolean okTexto = t.isEmpty() || String.valueOf(p.getId()).equals(t) || cliente.toLowerCase().contains(t);
            String f = nz(p.getFecha());
            boolean okFecha = fecha == null || (f.length() >= 10 && f.substring(0, 10).equals(fecha.toString()));
            if (okTexto && okFecha) {
                coincidencias.add(p);
            }
        }

        if (coincidencias.isEmpty()) {
            advertencia("No se encontró ningún pedido con ese criterio.");
            return;
        }
        // Un ID exacto tiene prioridad sobre las coincidencias por nombre.
        if (fecha == null && texto.matches("\\d+")) {
            for (Pedido p : coincidencias) {
                if (String.valueOf(p.getId()).equals(texto)) {
                    seleccionarPedido(p.getId());
                    return;
                }
            }
        }
        if (coincidencias.size() == 1) {
            seleccionarPedido(coincidencias.get(0).getId());
            return;
        }
        Opcion[] opciones = new Opcion[coincidencias.size()];
        for (int i = 0; i < opciones.length; i++) {
            Pedido p = coincidencias.get(i);
            opciones[i] = new Opcion(p.getId(), "N.° " + p.getId() + "  -  "
                    + nombre(p.getCliente().getNombres(), p.getCliente().getApellidos())
                    + "  -  " + fechaBonita(p.getFecha()) + "  -  " + nz(p.getEstado()));
        }
        Object elegido = JOptionPane.showInputDialog(this,
                "Se encontraron " + opciones.length + " pedidos. Elija uno:", "Seleccionar pedido",
                JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);
        if (elegido != null) {
            seleccionarPedido(((Opcion) elegido).id);
        }
    }

    private void seleccionarPedido(int id) {
        Pedido p = pedidoPermitido(id);
        if (p == null) {
            advertencia("No existe un pedido con el ID " + id + ".");
            return;
        }
        limpiarFormulario();
        mostrarPedido(p);
    }

    private void mostrarPedido(Pedido p) {
        pedidoSel = p;
        if (p == null) {
            lblInfoPedido.setText(Estilo.html("Pedido seleccionado: ninguno. Búsquelo por ID, cliente o fecha.",
                    Estilo.PEQUENA_NEGRITA));
            pastillaEstado.setVisible(false);
            txtIdPedido.setText("");
            tabla.setFilas(new ArrayList<>());
            lblTotalPedido.setText("S/ 0.00");
            return;
        }
        txtIdPedido.setText(String.valueOf(p.getId()));
        String cliente = p.getCliente() == null ? "" : nombre(p.getCliente().getNombres(), p.getCliente().getApellidos());
        String mesero = p.getMesero() == null ? "" : nombre(p.getMesero().getNombres(), p.getMesero().getApellidos());
        lblInfoPedido.setText(Estilo.html("Pedido seleccionado: " + p.getId() + "   |   Cliente: " + cliente
                + "   |   Mesero: " + mesero + "   |   Estado:", Estilo.PEQUENA_NEGRITA));
        pastillaEstado.poner(nz(p.getEstado()).toUpperCase());
        pastillaEstado.setVisible(true);
        cargarDetalles();
    }

    private void cargarDetalles() {
        if (pedidoSel == null) {
            return;
        }
        List<DetallePedido> ds = detalleDAO.listarPorPedido(pedidoSel.getId());
        List<Object[]> filas = new ArrayList<>();
        double total = 0;
        for (DetallePedido d : ds) {
            String obs = nz(d.getObservacion()).trim();
            filas.add(new Object[]{d.getId(), d.getPlato().getNombre(), d.getCantidad(),
                    String.format(java.util.Locale.US, "%.2f", d.getPrecio()),
                    obs.isEmpty() ? "Sin observaciones" : obs,
                    String.format(java.util.Locale.US, "%.2f", d.calcularSubtotal())});
            total += d.calcularSubtotal();
        }
        tabla.setFilas(filas);
        double bebidas = controlador.DetalleBebidaOperaciones.calcularTotalBebidas(pedidoSel.getId());
        lblTotalPedido.setText(Estilo.soles(total + bebidas));
        lblTotalPedido.setToolTipText(bebidas > 0 ? "Incluye bebidas por " + Estilo.soles(bebidas) : null);
    }

    // ---------- Formulario ----------

    private void contar() {
        String t = txtObservacion.getText();
        if (t.length() > MAX_OBSERVACION) {
            SwingUtilities.invokeLater(() -> txtObservacion.setText(t.substring(0, MAX_OBSERVACION)));
            return;
        }
        lblContador.setText(t.length() + " / " + MAX_OBSERVACION);
    }

    private void refrescarPrecioYSubtotal() {
        int cant = ((Number) spnCantidad.getValue()).intValue();
        txtPrecio.setText(String.format(java.util.Locale.US, "%.2f", precioActual));
        double total = precioActual * cant;
        for (FilaPlato f : extras) {
            f.actualizarPrecio();
            total += f.precioUnitario() * f.cantidad();
        }
        lblSubtotal.setText(String.format(java.util.Locale.US, "%.2f", total));
    }

    private void agregarFila() {
        if (idDetalleSel != 0) {
            advertencia("Hay un detalle seleccionado para editar. Pulse Limpiar para agregar platos nuevos.");
            return;
        }
        if (extras.size() >= 9) {
            advertencia("Puede agregar hasta 10 platos a la vez.");
            return;
        }
        FilaPlato f = new FilaPlato(extras.size() + 2);
        extras.add(f);
        panelExtras.add(f);
        refrescarFilas();
    }

    private void quitarFila(FilaPlato f) {
        extras.remove(f);
        panelExtras.remove(f);
        refrescarFilas();
    }

    private void quitarFilas() {
        extras.clear();
        panelExtras.removeAll();
        refrescarFilas();
    }

    private void refrescarFilas() {
        panelExtras.revalidate();
        panelExtras.repaint();
        refrescarPrecioYSubtotal();
        Container c = panelExtras.getParent();
        while (c != null) {
            c.revalidate();
            c = c.getParent();
        }
    }

    private void limpiarFormulario() {
        quitarFilas();
        btnMasPlatos.setEnabled(true);
        idDetalleSel = 0;
        cmbPlato.setEnabled(true);
        cmbPlato.setSelectedIndex(0);
        spnCantidad.setValue(1);
        txtObservacion.setText("");
        precioActual = 0;
        refrescarPrecioYSubtotal();
        tabla.limpiarSeleccion();
    }

    private void llenarFormulario(int idDetalle) {
        DetallePedido d = detalleDAO.buscarPorId(idDetalle);
        if (d == null) {
            return;
        }
        quitarFilas();
        btnMasPlatos.setEnabled(false);
        idDetalleSel = idDetalle;
        // El plato de un detalle existente no se cambia: se muestra bloqueado.
        int idPlato = d.getPlato().getId();
        boolean estaEnLista = false;
        for (int i = 0; i < cmbPlato.getItemCount(); i++) {
            if (cmbPlato.getItemAt(i).id == idPlato) {
                cmbPlato.setSelectedIndex(i);
                estaEnLista = true;
            }
        }
        if (!estaEnLista) {
            Opcion o = new Opcion(idPlato, d.getPlato().getNombre() + " (no disponible)");
            cmbPlato.addItem(o);
            cmbPlato.setSelectedItem(o);
        }
        cmbPlato.setEnabled(false);
        precioActual = d.getPrecio();
        spnCantidad.setValue(d.getCantidad());
        txtObservacion.setText(nz(d.getObservacion()));
        refrescarPrecioYSubtotal();
    }

    private void editarDesdeTabla(Object[] fila) {
        llenarFormulario(((Number) fila[0]).intValue());
    }

    private void eliminarDesdeTabla(Object[] fila) {
        llenarFormulario(((Number) fila[0]).intValue());
        eliminar();
    }

    // ---------- Acciones ----------

    // Vuelve a leer el pedido y confirma que sigue admitiendo cambios en sus platos.
    private boolean pedidoEditable() {
        if (pedidoSel == null) {
            advertencia("Primero seleccione un pedido con el buscador.");
            return false;
        }
        Pedido p = pedidoPermitido(pedidoSel.getId());
        if (p == null) {
            advertencia("El pedido ya no existe.");
            mostrarPedido(null);
            return false;
        }
        pedidoSel = p;
        String estado = nz(p.getEstado()).toUpperCase();
        if (!estado.equals("PENDIENTE") && !estado.equals("EN PREPARACION")) {
            advertencia("El pedido N.° " + p.getId() + " está " + estado + ".\n"
                    + "Solo se pueden agregar, modificar o eliminar platos cuando está PENDIENTE o EN PREPARACIÓN.");
            mostrarPedido(p);
            return false;
        }
        return true;
    }

    private int cantidadElegida() {
        return ((Number) spnCantidad.getValue()).intValue();
    }

    private void agregar() {
        if (!pedidoEditable()) {
            return;
        }
        if (idDetalleSel != 0) {
            advertencia("Hay un detalle seleccionado para editar. Pulse Limpiar para agregar un plato nuevo.");
            return;
        }

        // Lista de platos a agregar: el primero del formulario y los de "+ Agregar otro plato".
        List<int[]> lista = new ArrayList<>();
        Opcion plato = (Opcion) cmbPlato.getSelectedItem();
        if (plato == null || plato.id <= 0) {
            advertencia("Seleccione el plato.");
            return;
        }
        if (cantidadElegida() <= 0) {
            advertencia("La cantidad debe ser mayor que cero.");
            return;
        }
        lista.add(new int[]{plato.id, cantidadElegida()});
        for (int i = 0; i < extras.size(); i++) {
            FilaPlato f = extras.get(i);
            if (f.idPlato() <= 0) {
                advertencia("Seleccione el plato " + (i + 2) + " o quítelo de la lista.");
                return;
            }
            lista.add(new int[]{f.idPlato(), f.cantidad()});
        }

        int idPedido = pedidoSel.getId();
        String obs = txtObservacion.getText().trim();
        int agregados = 0;
        List<Integer> ids = new ArrayList<>();
        StringBuilder fallidos = new StringBuilder();

        for (int[] item : lista) {
            int idDetalle = DetallePedidoOperacione.registrarDetalle(idPedido, item[0], item[1], obs);
            if (idDetalle > 0 && detalleDAO.buscarPorId(idDetalle) != null) {
                agregados++;
                ids.add(idDetalle);
            } else {
                Plato p = platoDeLista(item[0]);
                fallidos.append("\n- ").append(p == null ? "Plato N.° " + item[0] : p.getNombre());
            }
        }

        if (agregados > 0) {
            String msg = agregados == 1
                    ? "Plato agregado correctamente al pedido N.° " + idPedido + ".\n"
                    + "Si el cliente no quiere algún ingrediente, regístrelo en \"Exclusiones\" (detalle N.° "
                    + ids.get(0) + ")."
                    : agregados + " platos agregados correctamente al pedido N.° " + idPedido + ".\n"
                    + "Si el cliente no quiere algún ingrediente, regístrelo en \"Exclusiones\".";
            if (fallidos.length() > 0) {
                msg += "\n\nNo se pudieron agregar:" + fallidos;
            }
            aviso(msg);
            limpiarFormulario();
            cargarDetalles();
            calcularEstadisticas();
        } else {
            error("No se pudo agregar el plato.\nVerifique que el plato esté disponible y que el pedido no tenga "
                    + "pagos registrados.");
        }
    }

    private void actualizar() {
        if (idDetalleSel == 0) {
            advertencia("Seleccione un detalle de la tabla para actualizarlo.");
            return;
        }
        if (!pedidoEditable()) {
            return;
        }
        int cantidad = cantidadElegida();
        String obs = txtObservacion.getText().trim();
        int id = idDetalleSel;

        DetallePedidoOperacione.actualizarDetalle(id, cantidad, obs);

        DetallePedido d = detalleDAO.buscarPorId(id);
        if (d != null && d.getCantidad() == cantidad && obs.equals(nz(d.getObservacion()).trim())) {
            aviso("Detalle actualizado correctamente.");
            limpiarFormulario();
            cargarDetalles();
            calcularEstadisticas();
        } else {
            error("No se pudo actualizar el detalle.\nEl pedido puede tener pagos registrados.");
        }
    }

    private void eliminar() {
        if (idDetalleSel == 0) {
            advertencia("Seleccione un detalle de la tabla para eliminarlo.");
            return;
        }
        if (!pedidoEditable()) {
            return;
        }
        int id = idDetalleSel;
        DetallePedido d = detalleDAO.buscarPorId(id);
        String plato = d == null ? "" : d.getPlato().getNombre();
        if (!confirmar("¿Eliminar el plato \"" + plato + "\" (detalle N.° " + id + ") del pedido?\n"
                + "También se quitarán sus exclusiones de ingredientes.")) {
            return;
        }

        DetallePedidoOperacione.eliminarDetalle(id);

        if (detalleDAO.buscarPorId(id) == null) {
            aviso("Detalle eliminado correctamente.");
            limpiarFormulario();
            cargarDetalles();
            calcularEstadisticas();
        } else {
            error("No se pudo eliminar el detalle.\nEl pedido puede tener pagos registrados.");
        }
    }
}
