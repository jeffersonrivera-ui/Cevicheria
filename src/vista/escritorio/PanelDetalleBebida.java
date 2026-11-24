package vista.escritorio;

import controlador.DetalleBebidaOperaciones;
import controlador.PedidosOperaciones;
import dao.BebidaDAO;
import dao.PedidoDAO;
import dao.impl.BebidaDAOImpl;
import dao.impl.PedidoDAOImpl;
import modelo.Bebida;
import modelo.DetalleBebida;
import modelo.Pedido;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Sección de administración: bebidas de cada pedido, con cantidad editable. */
public class PanelDetalleBebida extends ModuloBase {

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
    private final BebidaDAO bebidaDAO = new BebidaDAOImpl();

    private List<Pedido> pedidos = new ArrayList<>();
    private List<Bebida> bebidas = new ArrayList<>();
    private Pedido pedidoSel = null;
    private int idDetalleSel = 0;
    private double precioActual = 0;

    private final JLabel lblPedidosConBebida;
    private final JLabel lblUnidades;
    private final JLabel lblAcumulado;

    private final JTextField txtBuscar = new CampoHint("Ingrese el ID del pedido o nombre del cliente...");
    private final JLabel lblInfoPedido = Estilo.etiqueta("Pedido seleccionado: ninguno", Estilo.PEQUENA_NEGRITA, Estilo.PRIMARIO_OSCURO);
    private final Estilo.Pastilla pastillaEstado = new Estilo.Pastilla();
    private final TablaPaginada tabla = new TablaPaginada(
            new String[]{"ID detalle", "Bebida", "Cantidad", "Precio unit. (S/)", "Subtotal (S/)"}, -1, true);
    private final JLabel lblTotalBebidas = Estilo.etiqueta("S/ 0.00", Estilo.SUBTITULO, new Color(0x15803D));

    private final JTextField txtIdPedido = Estilo.campo();
    private final JComboBox<Opcion> cmbBebida = new JComboBox<>();
    private final JSpinner spnCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
    private final JTextField txtPrecio = Estilo.campo();
    private final JLabel lblSubtotal = Estilo.etiqueta("0.00", Estilo.SUBTITULO, new Color(0x15803D));

    public PanelDetalleBebida() {
        super("Detalle de bebidas", "Administra las bebidas y cantidades de cada pedido", "☕", Estilo.PRIMARIO, true);

        lblPedidosConBebida = agregarEstadistica("☷", new Color(0x38A3F5), "Pedidos con bebidas", Estilo.PRIMARIO_OSCURO);
        lblUnidades = agregarEstadistica("☕", Estilo.EXITO, "Bebidas pedidas", new Color(0x15803D));
        lblAcumulado = agregarEstadistica("S/", Estilo.PELIGRO, "Total en bebidas", Estilo.PELIGRO);

        // ----- Izquierda -----
        txtBuscar.setPreferredSize(new Dimension(100, 36));
        txtBuscar.setMinimumSize(new Dimension(100, 36));
        JButton btnBuscar = Estilo.boton("Buscar pedido", Estilo.PRIMARIO);
        JPanel zonaBusqueda = barraBusqueda("Buscar pedido por ID o cliente:", txtBuscar, null, btnBuscar);

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
        cabeceraLista.add(tituloDePanel("☕", "Bebidas del pedido"), BorderLayout.NORTH);
        cabeceraLista.add(barraInfo, BorderLayout.CENTER);

        JPanel arriba = new JPanel(new BorderLayout(0, 12));
        arriba.setOpaque(false);
        arriba.add(zonaBusqueda, BorderLayout.NORTH);
        arriba.add(cabeceraLista, BorderLayout.CENTER);
        panelIzquierdo.add(arriba, BorderLayout.NORTH);

        tabla.setAnchos(60, 160, 70, 100, 100);
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
        filaTotal.add(Estilo.etiqueta("Total de bebidas del pedido:", Estilo.SUBTITULO, Estilo.PRIMARIO_OSCURO));
        filaTotal.add(lblTotalBebidas);

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
        cmbBebida.setFont(Estilo.FUENTE);
        cmbBebida.setPreferredSize(new Dimension(120, 36));
        cmbBebida.setMinimumSize(new Dimension(100, 36));
        spnCantidad.setFont(Estilo.FUENTE);
        spnCantidad.setPreferredSize(new Dimension(100, 36));
        spnCantidad.setMinimumSize(new Dimension(80, 36));

        JPanel form = new JPanel(new GridBagLayout());
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
        form.add(conEtiqueta("Bebida:", true, cmbBebida), g);
        g.gridy = 2;
        g.gridwidth = 1;
        g.insets = new Insets(0, 0, 8, 6);
        form.add(conEtiqueta("Cantidad:", true, spnCantidad), g);
        g.gridx = 1;
        g.insets = new Insets(0, 6, 8, 0);
        form.add(conEtiqueta("Precio unitario (S/):", false, txtPrecio), g);

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
        barraSubtotal.add(Estilo.etiqueta("Subtotal (S/):", Estilo.NEGRITA, Estilo.PRIMARIO_OSCURO));
        barraSubtotal.add(lblSubtotal);

        JButton btnAgregar = Estilo.boton("+  Agregar bebida", Estilo.EXITO);
        JButton btnActualizar = Estilo.boton("✎  Actualizar", Estilo.PRIMARIO);
        JButton btnEliminar = Estilo.boton("Eliminar", Estilo.PELIGRO);
        JButton btnLimpiar = Estilo.boton("↻  Limpiar", Estilo.GRIS);
        for (JButton b : new JButton[]{btnAgregar, btnActualizar, btnEliminar, btnLimpiar}) {
            b.setFont(Estilo.PEQUENA_NEGRITA);
        }

        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.add(alinear(tituloDePanel("+", "Agregar / Actualizar bebida")));
        col.add(Box.createVerticalStrut(10));
        col.add(alinear(form));
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
        col.add(alinear(Estilo.aviso("Solo se puede cambiar la cantidad o quitar bebidas cuando el pedido esté "
                + "<b>PENDIENTE</b> o <b>EN PREPARACIÓN</b>.", "info", 210)));
        col.add(Box.createVerticalStrut(8));
        col.add(alinear(Estilo.aviso("No se puede modificar si el pedido está finalizado o tiene pagos registrados.",
                "alerta", 210)));
        panelDerecho.add(col, BorderLayout.NORTH);
        setAnchoDerecho(370);
        setAnchoEstadisticas(610);

        // ----- Eventos -----
        btnBuscar.addActionListener(e -> buscarPedido());
        txtBuscar.addActionListener(e -> buscarPedido());
        btnAgregar.addActionListener(e -> agregar());
        btnActualizar.addActionListener(e -> actualizar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        cmbBebida.addActionListener(e -> {
            Opcion o = (Opcion) cmbBebida.getSelectedItem();
            if (idDetalleSel == 0) {
                Bebida b = o == null ? null : bebidaDeLista(o.id);
                precioActual = b == null ? 0 : b.getPrecio();
            }
            refrescarSubtotal();
        });
        spnCantidad.addChangeListener(e -> refrescarSubtotal());

        cargarBebidas();
        cargarPedidos();
        mostrarPedido(null);

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                if (idDetalleSel == 0) {
                    cargarBebidas();
                }
                cargarPedidos();
                if (pedidoSel != null) {
                    Pedido p = PedidosOperaciones.buscarPedidoObjeto(pedidoSel.getId());
                    mostrarPedido(p);
                }
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

    private static String nombre(String a, String b) {
        return (nz(a) + " " + nz(b)).trim();
    }

    private Bebida bebidaDeLista(int id) {
        for (Bebida b : bebidas) {
            if (b.getId() == id) {
                return b;
            }
        }
        return null;
    }

    private void cargarBebidas() {
        bebidas = new ArrayList<>();
        cmbBebida.removeAllItems();
        cmbBebida.addItem(new Opcion(0, "Seleccione bebida..."));
        try {
            for (Bebida b : bebidaDAO.listar()) {
                bebidas.add(b);
                cmbBebida.addItem(new Opcion(b.getId(), b.getNombre() + "  (quedan " + b.getStock() + ")"));
            }
        } catch (Exception ex) {
            error("No se pudieron cargar las bebidas: " + ex.getMessage());
        }
    }

    private void cargarPedidos() {
        try {
            pedidos = new ArrayList<>(pedidoDAO.listar());
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
                int conBebida = 0;
                int unidades = 0;
                double total = 0;
                for (Pedido p : copia) {
                    List<DetalleBebida> ds = new dao.impl.DetalleBebidaDAOImpl().listarPorPedido(p.getId());
                    if (!ds.isEmpty()) {
                        conBebida++;
                        for (DetalleBebida d : ds) {
                            unidades += d.getCantidad();
                            if (!"CANCELADO".equalsIgnoreCase(p.getEstado())) {
                                total += d.calcularSubtotal();
                            }
                        }
                    }
                }
                return new double[]{conBebida, unidades, total};
            }

            @Override
            protected void done() {
                try {
                    double[] r = get();
                    lblPedidosConBebida.setText(String.valueOf((int) r[0]));
                    lblUnidades.setText(String.valueOf((int) r[1]));
                    lblAcumulado.setText(Estilo.soles(r[2]));
                } catch (Exception ignorada) {
                }
            }
        }.execute();
    }

    // ---------- Búsqueda ----------

    private void buscarPedido() {
        String texto = txtBuscar.getText().trim();
        if (texto.isEmpty()) {
            advertencia("Ingrese el ID del pedido o el nombre del cliente.");
            return;
        }
        cargarPedidos();
        String t = texto.toLowerCase();
        List<Pedido> coincidencias = new ArrayList<>();
        for (Pedido p : pedidos) {
            String cliente = p.getCliente() == null ? "" : nombre(p.getCliente().getNombres(), p.getCliente().getApellidos());
            if (String.valueOf(p.getId()).equals(t) || cliente.toLowerCase().contains(t)) {
                coincidencias.add(p);
            }
        }
        if (coincidencias.isEmpty()) {
            advertencia("No se encontró ningún pedido con ese criterio.");
            return;
        }
        if (texto.matches("\\d+")) {
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
                    + nombre(p.getCliente().getNombres(), p.getCliente().getApellidos()) + "  -  " + nz(p.getEstado()));
        }
        Object elegido = JOptionPane.showInputDialog(this,
                "Se encontraron " + opciones.length + " pedidos. Elija uno:", "Seleccionar pedido",
                JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);
        if (elegido != null) {
            seleccionarPedido(((Opcion) elegido).id);
        }
    }

    private void seleccionarPedido(int id) {
        Pedido p = PedidosOperaciones.buscarPedidoObjeto(id);
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
            lblInfoPedido.setText(Estilo.html("Pedido seleccionado: ninguno. Búsquelo por ID o cliente.",
                    Estilo.PEQUENA_NEGRITA));
            pastillaEstado.setVisible(false);
            txtIdPedido.setText("");
            tabla.setFilas(new ArrayList<>());
            lblTotalBebidas.setText("S/ 0.00");
            return;
        }
        txtIdPedido.setText(String.valueOf(p.getId()));
        String cliente = p.getCliente() == null ? "" : nombre(p.getCliente().getNombres(), p.getCliente().getApellidos());
        lblInfoPedido.setText(Estilo.html("Pedido seleccionado: " + p.getId() + "   |   Cliente: " + cliente
                + "   |   Estado:", Estilo.PEQUENA_NEGRITA));
        pastillaEstado.poner(nz(p.getEstado()).toUpperCase());
        pastillaEstado.setVisible(true);
        cargarDetalles();
    }

    private void cargarDetalles() {
        if (pedidoSel == null) {
            return;
        }
        List<Object[]> filas = new ArrayList<>();
        double total = 0;
        for (DetalleBebida d : new dao.impl.DetalleBebidaDAOImpl().listarPorPedido(pedidoSel.getId())) {
            filas.add(new Object[]{d.getId(), d.getBebida().getNombre(), d.getCantidad(),
                    String.format(Locale.US, "%.2f", d.getPrecio()),
                    String.format(Locale.US, "%.2f", d.calcularSubtotal())});
            total += d.calcularSubtotal();
        }
        tabla.setFilas(filas);
        lblTotalBebidas.setText(Estilo.soles(total));
    }

    // ---------- Formulario ----------

    private int cantidadElegida() {
        return ((Number) spnCantidad.getValue()).intValue();
    }

    private void refrescarSubtotal() {
        txtPrecio.setText(String.format(Locale.US, "%.2f", precioActual));
        lblSubtotal.setText(String.format(Locale.US, "%.2f", precioActual * cantidadElegida()));
    }

    private void limpiarFormulario() {
        idDetalleSel = 0;
        cmbBebida.setEnabled(true);
        cmbBebida.setSelectedIndex(0);
        spnCantidad.setValue(1);
        precioActual = 0;
        refrescarSubtotal();
        tabla.limpiarSeleccion();
    }

    private void llenarFormulario(int idDetalle) {
        DetalleBebida d = DetalleBebidaOperaciones.buscarDetalleBebida(idDetalle);
        if (d == null) {
            return;
        }
        idDetalleSel = idDetalle;
        int idBebida = d.getBebida().getId();
        boolean estaEnLista = false;
        for (int i = 0; i < cmbBebida.getItemCount(); i++) {
            if (cmbBebida.getItemAt(i).id == idBebida) {
                cmbBebida.setSelectedIndex(i);
                estaEnLista = true;
            }
        }
        if (!estaEnLista) {
            Opcion o = new Opcion(idBebida, d.getBebida().getNombre());
            cmbBebida.addItem(o);
            cmbBebida.setSelectedItem(o);
        }
        cmbBebida.setEnabled(false);
        precioActual = d.getPrecio();
        spnCantidad.setValue(d.getCantidad());
        refrescarSubtotal();
    }

    private void editarDesdeTabla(Object[] fila) {
        llenarFormulario(((Number) fila[0]).intValue());
    }

    private void eliminarDesdeTabla(Object[] fila) {
        llenarFormulario(((Number) fila[0]).intValue());
        eliminar();
    }

    // ---------- Acciones ----------

    private boolean pedidoEditable() {
        if (pedidoSel == null) {
            advertencia("Primero seleccione un pedido con el buscador.");
            return false;
        }
        Pedido p = PedidosOperaciones.buscarPedidoObjeto(pedidoSel.getId());
        if (p == null) {
            advertencia("El pedido ya no existe.");
            mostrarPedido(null);
            return false;
        }
        pedidoSel = p;
        String estado = nz(p.getEstado()).toUpperCase();
        if (!estado.equals("PENDIENTE") && !estado.equals("EN PREPARACION")) {
            advertencia("El pedido N.° " + p.getId() + " está " + estado + ".\n"
                    + "Solo se pueden agregar, modificar o eliminar bebidas cuando está PENDIENTE o EN PREPARACIÓN.");
            mostrarPedido(p);
            return false;
        }
        return true;
    }

    private void agregar() {
        if (!pedidoEditable()) {
            return;
        }
        if (idDetalleSel != 0) {
            advertencia("Hay una bebida seleccionada para editar. Pulse Limpiar para agregar una nueva.");
            return;
        }
        Opcion o = (Opcion) cmbBebida.getSelectedItem();
        if (o == null || o.id <= 0) {
            advertencia("Seleccione la bebida.");
            return;
        }
        int id = DetalleBebidaOperaciones.registrarDetalleBebida(pedidoSel.getId(), o.id, cantidadElegida());
        if (id > 0) {
            aviso("Bebida agregada correctamente al pedido N.° " + pedidoSel.getId() + ".");
            limpiarFormulario();
            cargarDetalles();
            calcularEstadisticas();
        } else {
            error("No se pudo agregar la bebida.\nVerifique que el pedido no tenga pagos registrados.");
        }
    }

    private void actualizar() {
        if (idDetalleSel == 0) {
            advertencia("Seleccione una bebida de la tabla para cambiar su cantidad.");
            return;
        }
        if (!pedidoEditable()) {
            return;
        }
        int cantidad = cantidadElegida();
        if (DetalleBebidaOperaciones.actualizarCantidad(idDetalleSel, cantidad)) {
            aviso("Cantidad actualizada correctamente.");
            limpiarFormulario();
            cargarDetalles();
            calcularEstadisticas();
        } else {
            error("No se pudo actualizar la cantidad.\nEl pedido puede tener pagos registrados.");
        }
    }

    private void eliminar() {
        if (idDetalleSel == 0) {
            advertencia("Seleccione una bebida de la tabla para eliminarla.");
            return;
        }
        if (!pedidoEditable()) {
            return;
        }
        int id = idDetalleSel;
        DetalleBebida d = DetalleBebidaOperaciones.buscarDetalleBebida(id);
        String nombre = d == null ? "" : d.getBebida().getNombre();
        if (!confirmar("¿Quitar la bebida \"" + nombre + "\" (detalle N.° " + id + ") del pedido?")) {
            return;
        }
        DetalleBebidaOperaciones.eliminarDetalleBebida(id);
        if (DetalleBebidaOperaciones.buscarDetalleBebida(id) == null) {
            aviso("Bebida eliminada correctamente.");
            limpiarFormulario();
            cargarDetalles();
            calcularEstadisticas();
        } else {
            error("No se pudo eliminar la bebida.\nEl pedido puede tener pagos registrados.");
        }
    }
}
