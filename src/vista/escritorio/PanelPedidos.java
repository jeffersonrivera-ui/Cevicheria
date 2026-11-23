package vista.escritorio;

import controlador.PedidosOperaciones;
import dao.ClienteDAO;
import dao.EmpleadoDAO;
import dao.PedidoDAO;
import dao.impl.ClienteDAOImpl;
import dao.impl.EmpleadoDAOImpl;
import dao.impl.PedidoDAOImpl;
import modelo.Cliente;
import modelo.Empleado;
import modelo.Pedido;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Sección Pedidos. Con idMesero == null la usa el administrador (todos los pedidos);
 * con un id, el mesero ve y registra solo sus pedidos.
 */
public class PanelPedidos extends ModuloBase {

    private static final DateTimeFormatter FECHA_UI =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private static final String[] ESTADOS = {"PENDIENTE", "EN PREPARACION", "LISTO", "ENTREGADO", "CANCELADO"};

    // Elemento de combo con id (el id 0 es el texto "Seleccione...").
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
    private final ClienteDAO clienteDAO = new ClienteDAOImpl();
    private final EmpleadoDAO empleadoDAO = new EmpleadoDAOImpl();
    private final Integer idMesero;

    private List<Pedido> cache = new ArrayList<>();

    private final JLabel lblTotal;
    private final JLabel lblPreparacion;
    private final JLabel lblListos;

    // Busqueda
    private final JTextField txtBuscar = new CampoHint("Ingrese ID o nombre del cliente...");
    private final JComboBox<String> cmbEstadoFiltro = new JComboBox<>(
            new String[]{"Todos", "PENDIENTE", "EN PREPARACION", "LISTO", "ENTREGADO", "CANCELADO"});
    private final JTextField txtFechaFiltro = new CampoHint("dd/mm/aaaa");
    private final TablaPaginada tabla = new TablaPaginada(
            new String[]{"ID", "Fecha", "Cliente", "Mesero", "Estado"}, 4, true);

    // Registro
    private final JComboBox<Opcion> cmbCliente = new JComboBox<>();
    private final JComboBox<Opcion> cmbMesero = new JComboBox<>();

    // Actualizar estado
    private final JTextField txtIdPedido = new CampoHint("Ingrese el ID...");
    private final JComboBox<String> cmbNuevoEstado = new JComboBox<>(new String[]{
            "Seleccione estado...", "PENDIENTE", "EN PREPARACION", "LISTO", "ENTREGADO", "CANCELADO"});

    public PanelPedidos() {
        this(null);
    }

    public PanelPedidos(Integer idMesero) {
        super("Pedidos", "Administra los pedidos, clientes, meseros y estados", "☷", Estilo.PRIMARIO, true);
        this.idMesero = idMesero;

        lblTotal = agregarEstadistica("☷", new Color(0x38A3F5), idMesero == null ? "Total de pedidos" : "Mis pedidos",
                Estilo.PRIMARIO_OSCURO);
        lblPreparacion = agregarEstadistica("♨", Estilo.EXITO, "En preparación", new Color(0x15803D));
        lblListos = agregarEstadistica("☕", Estilo.PELIGRO, "Listos para entregar", Estilo.PELIGRO);

        // ----- Izquierda -----
        cmbEstadoFiltro.setFont(Estilo.FUENTE);
        cmbEstadoFiltro.setPreferredSize(new Dimension(120, 36));
        txtFechaFiltro.setPreferredSize(new Dimension(105, 36));
        txtBuscar.setPreferredSize(new Dimension(100, 36));
        txtBuscar.setMinimumSize(new Dimension(100, 36));

        JPanel extra = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        extra.setOpaque(false);
        JPanel colEstado = conEtiqueta("Estado:", cmbEstadoFiltro);
        colEstado.setPreferredSize(new Dimension(120, 58));
        JPanel colFecha = conEtiqueta("Fecha:", txtFechaFiltro);
        colFecha.setPreferredSize(new Dimension(105, 58));
        extra.add(colEstado);
        extra.add(Box.createHorizontalStrut(10));
        extra.add(colFecha);
        extra.setPreferredSize(new Dimension(235, 58));

        JButton btnBuscar = Estilo.boton("Buscar", Estilo.PRIMARIO);
        panelIzquierdo.add(barraBusqueda("Buscar por ID o cliente:", txtBuscar, extra, btnBuscar), BorderLayout.NORTH);
        tabla.setAnchos(36, 80, 100, 95, 100, 80);
        tabla.setAcciones(
                new TablaPaginada.Accion("◉", new Color(0x1E6FEB), Color.WHITE, this::verDetalle),
                new TablaPaginada.Accion("↻", new Color(0x22C55E), Color.WHITE, this::prepararCambioEstado));
        panelIzquierdo.add(tabla, BorderLayout.CENTER);

        // ----- Derecha -----
        for (JComboBox<?> c : new JComboBox<?>[]{cmbCliente, cmbMesero, cmbNuevoEstado}) {
            c.setFont(Estilo.FUENTE);
            c.setPreferredSize(new Dimension(120, 36));
        }
        txtIdPedido.setPreferredSize(new Dimension(120, 36));

        JPanel registro = new JPanel(new GridBagLayout());
        registro.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 8, 6);
        g.gridx = 0;
        registro.add(conEtiquetaObligatoria("ID del cliente:", cmbCliente), g);
        g.gridx = 1;
        g.insets = new Insets(0, 6, 8, 0);
        registro.add(conEtiquetaObligatoria("ID del mesero:", cmbMesero), g);

        JButton btnRegistrar = Estilo.boton("+  Registrar", Estilo.EXITO);
        JButton btnLimpiar = Estilo.boton("↻  Limpiar", Estilo.GRIS);
        btnRegistrar.addActionListener(e -> registrar());
        btnLimpiar.addActionListener(e -> limpiarRegistro());

        JPanel bloqueRegistro = new JPanel();
        bloqueRegistro.setOpaque(false);
        bloqueRegistro.setLayout(new BoxLayout(bloqueRegistro, BoxLayout.Y_AXIS));
        bloqueRegistro.add(alinear(tituloDePanel("+", "Registrar pedido")));
        bloqueRegistro.add(Box.createVerticalStrut(8));
        bloqueRegistro.add(alinear(registro));
        bloqueRegistro.add(alinear(Estilo.aviso(
                "Un nuevo pedido se registra con el estado inicial <b>PENDIENTE</b>.", "info", 200)));
        bloqueRegistro.add(Box.createVerticalStrut(10));
        bloqueRegistro.add(alinear(filaBotones(btnRegistrar, btnLimpiar)));

        JPanel estado = new JPanel(new GridBagLayout());
        estado.setOpaque(false);
        g = new GridBagConstraints();
        g.gridy = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 8, 6);
        g.gridx = 0;
        estado.add(conEtiquetaObligatoria("ID del pedido:", txtIdPedido), g);
        g.gridx = 1;
        g.insets = new Insets(0, 6, 8, 0);
        estado.add(conEtiquetaObligatoria("Nuevo estado:", cmbNuevoEstado), g);

        JButton btnEstado = Estilo.boton("↻  Actualizar estado", Estilo.PRIMARIO);
        btnEstado.addActionListener(e -> actualizarEstado());
        JButton btnEliminar = Estilo.boton("Eliminar", Estilo.PELIGRO);
        btnEliminar.addActionListener(e -> eliminar());
        JPanel filaEstado = new JPanel(new GridBagLayout());
        filaEstado.setOpaque(false);
        GridBagConstraints gb = new GridBagConstraints();
        gb.fill = GridBagConstraints.HORIZONTAL;
        gb.weightx = 1;
        gb.insets = new Insets(0, 0, 0, 10);
        filaEstado.add(btnEstado, gb);
        gb.weightx = 0;
        gb.insets = new Insets(0, 0, 0, 0);
        filaEstado.add(btnEliminar, gb);

        JPanel bloqueEstado = new JPanel();
        bloqueEstado.setOpaque(false);
        bloqueEstado.setLayout(new BoxLayout(bloqueEstado, BoxLayout.Y_AXIS));
        bloqueEstado.add(alinear(tituloDePanel("✎", "Actualizar estado del pedido")));
        bloqueEstado.add(Box.createVerticalStrut(8));
        bloqueEstado.add(alinear(estado));
        bloqueEstado.add(alinear(filaEstado));
        bloqueEstado.add(Box.createVerticalStrut(10));
        bloqueEstado.add(alinear(Estilo.aviso("Flujo de estados del pedido: <b>PENDIENTE → EN PREPARACIÓN → "
                + "LISTO → ENTREGADO</b>. El estado <b>CANCELADO</b> solo se aplica cuando el pedido puede "
                + "ser cancelado.", "info", 200)));
        bloqueEstado.add(Box.createVerticalStrut(8));
        bloqueEstado.add(alinear(Estilo.aviso("Al pasar a <b>LISTO</b> se descuenta el inventario según la receta; "
                + "verifica que haya stock suficiente y que el plato tenga receta.", "alerta", 200)));
        bloqueEstado.add(Box.createVerticalStrut(8));
        bloqueEstado.add(alinear(Estilo.aviso("No se puede eliminar un pedido <b>ENTREGADO</b> o con pagos o ventas "
                + "registrados.", "error", 200)));

        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.add(bloqueRegistro);
        col.add(Box.createVerticalStrut(14));
        JSeparator sep = new JSeparator();
        sep.setForeground(Estilo.BORDE);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        col.add(sep);
        col.add(Box.createVerticalStrut(14));
        col.add(bloqueEstado);
        panelDerecho.add(col, BorderLayout.NORTH);

        setAnchoDerecho(370);

        // ----- Eventos -----
        btnBuscar.addActionListener(e -> aplicarFiltros());
        txtBuscar.addActionListener(e -> aplicarFiltros());
        txtFechaFiltro.addActionListener(e -> aplicarFiltros());
        cmbEstadoFiltro.addActionListener(e -> aplicarFiltros());
        tabla.setEscucha(new TablaPaginada.Escucha() {
            @Override
            public void seleccionar(Object[] fila) {
                txtIdPedido.setText(String.valueOf(fila[0]));
            }
        });

        cargarCombos();
        cargar();
    }

    // ---------- Construcción ----------

    private JPanel conEtiqueta(String texto, JComponent campo) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.add(Estilo.etiqueta(texto, Estilo.PEQUENA_NEGRITA, Estilo.TEXTO), BorderLayout.NORTH);
        p.add(campo, BorderLayout.CENTER);
        return p;
    }

    private JPanel conEtiquetaObligatoria(String texto, JComponent campo) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.add(Estilo.etiquetaCampo(texto, true), BorderLayout.NORTH);
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

    private void cargarCombos() {
        cmbCliente.removeAllItems();
        cmbCliente.addItem(new Opcion(0, "Seleccione cliente..."));
        for (Cliente c : clienteDAO.listar()) {
            cmbCliente.addItem(new Opcion(c.getId(),
                    c.getId() + " - " + nombre(c.getNombres(), c.getApellidos())));
        }

        cmbMesero.removeAllItems();
        cmbMesero.addItem(new Opcion(0, "Seleccione mesero..."));
        for (Empleado e : empleadoDAO.listar()) {
            if ("MESERO".equalsIgnoreCase(e.getCargo()) && "ACTIVO".equalsIgnoreCase(e.getEstadoLaboral())) {
                cmbMesero.addItem(new Opcion(e.getId(), e.getId() + " - " + nombre(e.getNombres(), e.getApellidos())));
            }
        }
        if (idMesero != null) {
            for (int i = 0; i < cmbMesero.getItemCount(); i++) {
                if (cmbMesero.getItemAt(i).id == idMesero) {
                    cmbMesero.setSelectedIndex(i);
                }
            }
            cmbMesero.setEnabled(false);
        }
    }

    private void cargar() {
        try {
            cache = new ArrayList<>(pedidoDAO.listar());
            if (idMesero != null) {
                cache.removeIf(p -> p.getMesero() == null || p.getMesero().getId() != idMesero);
            }
            cache.sort(Comparator.comparingInt(Pedido::getId).reversed());

            int prep = 0;
            int listos = 0;
            for (Pedido p : cache) {
                if ("EN PREPARACION".equalsIgnoreCase(p.getEstado())) {
                    prep++;
                } else if ("LISTO".equalsIgnoreCase(p.getEstado())) {
                    listos++;
                }
            }
            lblTotal.setText(String.valueOf(cache.size()));
            lblPreparacion.setText(String.valueOf(prep));
            lblListos.setText(String.valueOf(listos));
            aplicarFiltros();
        } catch (Exception ex) {
            error("No se pudieron cargar los pedidos: " + ex.getMessage());
        }
    }

    private void aplicarFiltros() {
        String texto = txtBuscar.getText().trim().toLowerCase();
        String estadoF = (String) cmbEstadoFiltro.getSelectedItem();
        String fechaTxt = txtFechaFiltro.getText().trim();

        LocalDate fechaF = null;
        if (!fechaTxt.isEmpty()) {
            try {
                fechaF = LocalDate.parse(fechaTxt, FECHA_UI);
            } catch (Exception e) {
                advertencia("La fecha no es válida. Use el formato dd/mm/aaaa (por ejemplo 26/01/2025).");
                return;
            }
        }

        List<Object[]> filas = new ArrayList<>();
        for (Pedido p : cache) {
            String cliente = p.getCliente() == null ? "" : nombre(p.getCliente().getNombres(), p.getCliente().getApellidos());
            String mesero = p.getMesero() == null ? "" : nombre(p.getMesero().getNombres(), p.getMesero().getApellidos());

            boolean okTexto = texto.isEmpty() || String.valueOf(p.getId()).equals(texto)
                    || cliente.toLowerCase().contains(texto);
            boolean okEstado = "Todos".equals(estadoF) || estadoF.equalsIgnoreCase(p.getEstado());
            boolean okFecha = fechaF == null || fechaF.toString().equals(nz(p.getFecha()).length() >= 10
                    ? p.getFecha().substring(0, 10) : p.getFecha());
            if (okTexto && okEstado && okFecha) {
                filas.add(new Object[]{p.getId(), fechaBonita(p.getFecha()), cliente, mesero,
                        nz(p.getEstado()).toUpperCase()});
            }
        }
        tabla.setFilas(filas);
    }

    private Pedido buscarEnCache(int id) {
        for (Pedido p : cache) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }

    // ---------- Acciones de la tabla ----------

    private void verDetalle(Object[] fila) {
        int id = ((Number) fila[0]).intValue();
        Pedido p = buscarEnCache(id);
        if (p == null) {
            return;
        }
        aviso("Pedido N.° " + p.getId()
                + "\nFecha: " + fechaBonita(p.getFecha())
                + "\nCliente: " + nombre(p.getCliente().getNombres(), p.getCliente().getApellidos())
                + "  (ID " + p.getCliente().getId() + ")"
                + "\nMesero: " + nombre(p.getMesero().getNombres(), p.getMesero().getApellidos())
                + "  (ID " + p.getMesero().getId() + ")"
                + "\nEstado: " + nz(p.getEstado())
                + "\n\nLos platos del pedido se administran en \"Detalles de pedidos\".");
    }

    private void prepararCambioEstado(Object[] fila) {
        txtIdPedido.setText(String.valueOf(fila[0]));
        String actual = String.valueOf(fila[4]);
        String siguiente = null;
        if ("PENDIENTE".equals(actual)) {
            siguiente = "EN PREPARACION";
        } else if ("EN PREPARACION".equals(actual)) {
            siguiente = "LISTO";
        } else if ("LISTO".equals(actual)) {
            siguiente = "ENTREGADO";
        }
        cmbNuevoEstado.setSelectedItem(siguiente == null ? "Seleccione estado..." : siguiente);
    }

    // ---------- Registro ----------

    private void limpiarRegistro() {
        cmbCliente.setSelectedIndex(0);
        if (idMesero == null) {
            cmbMesero.setSelectedIndex(0);
        }
        txtIdPedido.setText("");
        cmbNuevoEstado.setSelectedIndex(0);
        tabla.limpiarSeleccion();
    }

    private void registrar() {
        Opcion cliente = (Opcion) cmbCliente.getSelectedItem();
        Opcion mesero = (Opcion) cmbMesero.getSelectedItem();
        if (cliente == null || cliente.id <= 0) {
            advertencia("Seleccione un cliente.");
            return;
        }
        if (mesero == null || mesero.id <= 0) {
            advertencia("Seleccione un mesero activo.");
            return;
        }

        int antes = pedidoDAO.listar().size();
        int id = PedidosOperaciones.registrarPedido(cliente.id, mesero.id);
        int despues = pedidoDAO.listar().size();

        if (id > 0 && despues > antes) {
            aviso("Pedido N.° " + id + " registrado correctamente con el estado PENDIENTE.\n"
                    + "Agregue los platos desde \"Detalles de pedidos\".");
            limpiarRegistro();
            cargar();
        } else {
            error("No se pudo registrar el pedido.\nVerifique que el cliente exista y que el mesero esté activo.");
        }
    }

    // ---------- Estado ----------

    private int idEscrito() {
        String t = txtIdPedido.getText().trim();
        if (t.isEmpty()) {
            advertencia("Ingrese el ID del pedido.");
            return -1;
        }
        try {
            int id = Integer.parseInt(t);
            if (id <= 0) {
                throw new NumberFormatException();
            }
            return id;
        } catch (NumberFormatException e) {
            advertencia("El ID del pedido debe ser un número entero positivo.");
            return -1;
        }
    }

    private static boolean transicionPermitida(String actual, String nuevo) {
        return ("PENDIENTE".equals(actual) && ("EN PREPARACION".equals(nuevo) || "CANCELADO".equals(nuevo)))
                || ("EN PREPARACION".equals(actual) && ("LISTO".equals(nuevo) || "CANCELADO".equals(nuevo)))
                || ("LISTO".equals(actual) && "ENTREGADO".equals(nuevo));
    }

    private void actualizarEstado() {
        int id = idEscrito();
        if (id < 0) {
            return;
        }
        if (cmbNuevoEstado.getSelectedIndex() <= 0) {
            advertencia("Seleccione el nuevo estado.");
            return;
        }
        String nuevo = (String) cmbNuevoEstado.getSelectedItem();

        Pedido p = PedidosOperaciones.buscarPedidoObjeto(id);
        if (p == null || (idMesero != null && p.getMesero().getId() != idMesero)) {
            advertencia("No existe un pedido con el ID " + id + ".");
            return;
        }
        String actual = nz(p.getEstado()).toUpperCase();
        if (actual.equals(nuevo)) {
            advertencia("El pedido ya está en estado " + nuevo + ".");
            return;
        }
        if (!transicionPermitida(actual, nuevo)) {
            advertencia("No se permite cambiar de " + actual + " a " + nuevo + ".\n"
                    + "Flujo: PENDIENTE → EN PREPARACIÓN → LISTO → ENTREGADO (CANCELADO solo desde "
                    + "PENDIENTE o EN PREPARACIÓN).");
            return;
        }
        String extra = "LISTO".equals(nuevo) ? "\nSe descontará el inventario según las recetas." : "";
        if (!confirmar("¿Cambiar el pedido N.° " + id + " de " + actual + " a " + nuevo + "?" + extra)) {
            return;
        }

        PedidosOperaciones.actualizarEstadoPedido(id, nuevo);

        Pedido despues = PedidosOperaciones.buscarPedidoObjeto(id);
        if (despues != null && nuevo.equalsIgnoreCase(despues.getEstado())) {
            aviso("Estado actualizado correctamente.\nPedido N.° " + id + ": " + nuevo);
            txtIdPedido.setText("");
            cmbNuevoEstado.setSelectedIndex(0);
            cargar();
        } else if ("LISTO".equals(nuevo)) {
            error("No se pudo marcar el pedido como LISTO.\nRevise que los platos del pedido tengan receta "
                    + "y que haya stock suficiente de los ingredientes.");
        } else {
            error("No se pudo actualizar el estado del pedido.");
        }
    }

    private void eliminar() {
        int id = idEscrito();
        if (id < 0) {
            return;
        }
        Pedido p = PedidosOperaciones.buscarPedidoObjeto(id);
        if (p == null || (idMesero != null && p.getMesero().getId() != idMesero)) {
            advertencia("No existe un pedido con el ID " + id + ".");
            return;
        }
        if ("ENTREGADO".equalsIgnoreCase(p.getEstado())) {
            advertencia("No se puede eliminar un pedido ENTREGADO.");
            return;
        }
        if (!confirmar("¿Eliminar el pedido N.° " + id + " y todos sus detalles?\nEsta acción no se puede deshacer.")) {
            return;
        }

        PedidosOperaciones.eliminarPedido(id);

        if (!PedidosOperaciones.existePedido(id)) {
            aviso("Pedido eliminado correctamente.");
            limpiarRegistro();
            cargar();
        } else {
            error("No se pudo eliminar el pedido.\nPuede tener pagos o una venta registrados.");
        }
    }
}
