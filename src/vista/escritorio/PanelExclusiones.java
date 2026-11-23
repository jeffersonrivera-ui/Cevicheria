package vista.escritorio;

import controlador.DetalleIngredienteExcluidoOperaciones;
import controlador.PedidosOperaciones;
import dao.DetalleIngredienteExcluidoDAO;
import dao.DetallePedidoDAO;
import dao.PedidoDAO;
import dao.PlatoIngredienteDAO;
import dao.impl.DetalleIngredienteExcluidoDAOImpl;
import dao.impl.DetallePedidoDAOImpl;
import dao.impl.PedidoDAOImpl;
import dao.impl.PlatoIngredienteDAOImpl;
import modelo.DetallePedido;
import modelo.Ingrediente;
import modelo.Pedido;
import modelo.PlatoIngrediente;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Sección Exclusiones: ingredientes que se quitan de un plato dentro de un pedido (solo administrador). */
public class PanelExclusiones extends ModuloBase {

    // Elemento de combo con id (id 0 = texto "Seleccione...").
    private static final class Opcion {
        final int id;
        final String texto;
        final String extra;

        Opcion(int id, String texto, String extra) {
            this.id = id;
            this.texto = texto;
            this.extra = extra;
        }

        @Override
        public String toString() {
            return texto;
        }
    }

    private final PedidoDAO pedidoDAO = new PedidoDAOImpl();
    private final DetallePedidoDAO detalleDAO = new DetallePedidoDAOImpl();
    private final DetalleIngredienteExcluidoDAO exclusionDAO = new DetalleIngredienteExcluidoDAOImpl();
    private final PlatoIngredienteDAO recetaDAO = new PlatoIngredienteDAOImpl();

    private Pedido pedidoSel = null;
    private DetallePedido detalleSel = null;

    private final JLabel lblPedido;
    private final JLabel lblDetalles;
    private final JLabel lblExcluidos;

    private final JTextField txtBuscar = new CampoHint("Ingrese ID del pedido o nombre del cliente...");
    private final JComboBox<Opcion> cmbDetalle = new JComboBox<>();
    private final JLabel lblInfoPedido = Estilo.etiqueta("", Estilo.PEQUENA_NEGRITA, Estilo.PRIMARIO_OSCURO);
    private final Estilo.Pastilla pastillaEstado = new Estilo.Pastilla();
    private final TablaPaginada tabla = new TablaPaginada(
            new String[]{"ID ingrediente", "Ingrediente", "Unidad"}, -1, true);

    private final JTextField txtIdPedido = Estilo.campo();
    private final JTextField txtIdDetalle = Estilo.campo();
    private final JComboBox<Opcion> cmbIngrediente = new JComboBox<>();
    private final JLabel lblIngSel = Estilo.etiqueta("-", Estilo.NEGRITA, Estilo.PRIMARIO_OSCURO);
    private final JLabel lblUnidad = Estilo.etiqueta("-", Estilo.NEGRITA, Estilo.PRIMARIO_OSCURO);

    private boolean cargandoCombos = false;

    public PanelExclusiones() {
        super("Exclusiones", "Administra los ingredientes que se excluyen de cada plato en un pedido",
                "⊘", Estilo.PRIMARIO, true);

        lblPedido = agregarEstadistica("☷", new Color(0x38A3F5), "Pedido seleccionado", Estilo.PRIMARIO);
        lblDetalles = agregarEstadistica("♨", Estilo.EXITO, "Detalles del pedido", new Color(0x15803D));
        lblExcluidos = agregarEstadistica("⊘", Estilo.PELIGRO, "Ingredientes excluidos", Estilo.PELIGRO);
        setAnchoEstadisticas(650);

        // ----- Izquierda -----
        txtBuscar.setPreferredSize(new Dimension(100, 36));
        txtBuscar.setMinimumSize(new Dimension(100, 36));
        JButton btnBuscar = Estilo.boton("Buscar pedido", Estilo.PRIMARIO);
        JPanel zonaBusqueda = barraBusqueda("Buscar pedido por ID o cliente:", txtBuscar, null, btnBuscar);

        cmbDetalle.setFont(Estilo.FUENTE);
        cmbDetalle.setPreferredSize(new Dimension(220, 36));
        JPanel filaDetalle = new JPanel(new BorderLayout(10, 0));
        filaDetalle.setOpaque(false);
        filaDetalle.add(Estilo.etiqueta("Seleccionar detalle (plato):", Estilo.PEQUENA_NEGRITA, Estilo.TEXTO),
                BorderLayout.WEST);
        filaDetalle.add(cmbDetalle, BorderLayout.CENTER);

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
        barraInfo.setPreferredSize(new Dimension(300, 40));

        JPanel arriba = new JPanel();
        arriba.setOpaque(false);
        arriba.setLayout(new BoxLayout(arriba, BoxLayout.Y_AXIS));
        arriba.add(alinear(zonaBusqueda));
        arriba.add(Box.createVerticalStrut(10));
        arriba.add(alinear(filaDetalle));
        arriba.add(Box.createVerticalStrut(10));
        arriba.add(alinear(barraInfo));
        arriba.add(Box.createVerticalStrut(12));
        arriba.add(alinear(tituloDePanel("≡", "Ingredientes excluidos del detalle")));
        panelIzquierdo.add(arriba, BorderLayout.NORTH);

        tabla.setAnchos(90, 160, 80, 90);
        tabla.setTextoPie("Mostrando %1$d de %1$d registros");
        tabla.setAcciones(new TablaPaginada.Accion("✖", new Color(0xEF4444), Color.WHITE, this::quitarDesdeTabla));
        panelIzquierdo.add(tabla, BorderLayout.CENTER);

        // ----- Derecha -----
        txtIdPedido.setEditable(false);
        txtIdPedido.setBackground(new Color(0xEEF2F8));
        txtIdDetalle.setEditable(false);
        txtIdDetalle.setBackground(new Color(0xEEF2F8));
        cmbIngrediente.setFont(Estilo.FUENTE);
        cmbIngrediente.setPreferredSize(new Dimension(120, 36));

        JPanel ids = new JPanel(new GridLayout(1, 2, 12, 0));
        ids.setOpaque(false);
        ids.add(conEtiqueta("ID del pedido:", false, txtIdPedido));
        ids.add(conEtiqueta("ID del detalle:", false, txtIdDetalle));

        JPanel seleccionado = new JPanel(new GridLayout(1, 2, 12, 0)) {
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
        seleccionado.setOpaque(false);
        seleccionado.setBorder(new EmptyBorder(8, 12, 8, 12));
        seleccionado.add(pareja("Ingrediente seleccionado:", lblIngSel));
        seleccionado.add(pareja("Unidad:", lblUnidad));

        JButton btnExcluir = Estilo.boton("+  Excluir ingrediente", Estilo.EXITO);
        JButton btnLimpiar = Estilo.boton("↻  Limpiar", Estilo.GRIS);

        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.add(alinear(tituloDePanel("+", "Agregar exclusión")));
        col.add(Box.createVerticalStrut(10));
        col.add(alinear(ids));
        col.add(Box.createVerticalStrut(8));
        col.add(alinear(conEtiqueta("Ingrediente de la receta:", true, cmbIngrediente)));
        col.add(Box.createVerticalStrut(10));
        col.add(alinear(seleccionado));
        col.add(Box.createVerticalStrut(12));
        JPanel botones = new JPanel(new GridBagLayout());
        botones.setOpaque(false);
        GridBagConstraints gb = new GridBagConstraints();
        gb.fill = GridBagConstraints.HORIZONTAL;
        gb.weightx = 3;
        gb.insets = new Insets(0, 0, 0, 10);
        botones.add(btnExcluir, gb);
        gb.weightx = 2;
        gb.insets = new Insets(0, 0, 0, 0);
        botones.add(btnLimpiar, gb);
        col.add(alinear(botones));
        col.add(Box.createVerticalStrut(12));
        col.add(alinear(Estilo.aviso("Solo puedes excluir ingredientes que formen parte de la receta del plato "
                + "seleccionado.", "info", 210)));
        col.add(Box.createVerticalStrut(8));
        col.add(alinear(Estilo.aviso("No se pueden modificar exclusiones si el pedido no está <b>PENDIENTE</b> o "
                + "<b>EN PREPARACION</b>, si el inventario <b>ya fue</b> descontado o si existen pagos registrados.",
                "alerta", 210)));
        col.add(Box.createVerticalStrut(8));
        col.add(alinear(Estilo.aviso("La exclusión se aplica al detalle seleccionado, no a todo el pedido.", "info", 210)));
        panelDerecho.add(col, BorderLayout.NORTH);
        setAnchoDerecho(370);

        // ----- Eventos -----
        btnBuscar.addActionListener(e -> buscarPedido());
        txtBuscar.addActionListener(e -> buscarPedido());
        cmbDetalle.addActionListener(e -> {
            if (!cargandoCombos) {
                alElegirDetalle();
            }
        });
        cmbIngrediente.addActionListener(e -> {
            if (!cargandoCombos) {
                mostrarIngredienteElegido();
            }
        });
        btnExcluir.addActionListener(e -> excluir());
        btnLimpiar.addActionListener(e -> limpiar());
        tabla.setEscucha(new TablaPaginada.Escucha() {
            @Override
            public void seleccionar(Object[] fila) {
                // La fila solo se marca; se quita con el icono rojo.
            }
        });

        mostrarPedido(null);
    }

    // ---------- Construcción ----------

    private JPanel conEtiqueta(String texto, boolean obligatorio, JComponent campo) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.add(Estilo.etiquetaCampo(texto, obligatorio), BorderLayout.NORTH);
        p.add(campo, BorderLayout.CENTER);
        return p;
    }

    private JPanel pareja(String titulo, JLabel valor) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setOpaque(false);
        p.add(Estilo.etiqueta(titulo, Estilo.PEQUENA, Estilo.TEXTO), BorderLayout.NORTH);
        p.add(valor, BorderLayout.CENTER);
        return p;
    }

    private JComponent alinear(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
        return c;
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private static String nombre(String nombres, String apellidos) {
        return (nz(nombres) + " " + nz(apellidos)).trim();
    }

    private static String dosDigitos(int n) {
        return n < 10 ? "0" + n : String.valueOf(n);
    }

    // ---------- Búsqueda de pedido ----------

    private void buscarPedido() {
        String texto = txtBuscar.getText().trim();
        if (texto.isEmpty()) {
            advertencia("Ingrese el ID del pedido o el nombre del cliente.");
            return;
        }
        List<Pedido> todos;
        try {
            todos = new ArrayList<>(pedidoDAO.listar());
        } catch (Exception ex) {
            error("No se pudieron cargar los pedidos: " + ex.getMessage());
            return;
        }
        todos.sort(Comparator.comparingInt(Pedido::getId).reversed());

        String t = texto.toLowerCase();
        List<Pedido> coincidencias = new ArrayList<>();
        for (Pedido p : todos) {
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
                    + nombre(p.getCliente().getNombres(), p.getCliente().getApellidos())
                    + "  -  " + nz(p.getFecha()) + "  -  " + nz(p.getEstado()), "");
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
        mostrarPedido(p);
    }

    private void mostrarPedido(Pedido p) {
        pedidoSel = p;
        detalleSel = null;
        if (p == null) {
            lblPedido.setText("-");
            lblDetalles.setText("0 platos");
            lblExcluidos.setText("0");
            lblInfoPedido.setText(Estilo.html("Pedido: ninguno. Búsquelo por ID o cliente.", Estilo.PEQUENA_NEGRITA));
            pastillaEstado.setVisible(false);
            txtIdPedido.setText("");
            txtIdDetalle.setText("");
            cargandoCombos = true;
            cmbDetalle.removeAllItems();
            cmbDetalle.addItem(new Opcion(0, "Seleccione un detalle...", ""));
            cmbIngrediente.removeAllItems();
            cmbIngrediente.addItem(new Opcion(0, "Seleccione un ingrediente...", ""));
            cargandoCombos = false;
            tabla.setFilas(new ArrayList<>());
            mostrarIngredienteElegido();
            return;
        }
        lblPedido.setText("N.° " + dosDigitos(p.getId()));
        txtIdPedido.setText(String.valueOf(p.getId()));
        String cliente = p.getCliente() == null ? "" : nombre(p.getCliente().getNombres(), p.getCliente().getApellidos());
        String mesero = p.getMesero() == null ? "" : nombre(p.getMesero().getNombres(), p.getMesero().getApellidos());
        lblInfoPedido.setText(Estilo.html("Pedido: " + dosDigitos(p.getId()) + "   |   Cliente: " + cliente
                + "   |   Mesero: " + mesero + "   |   Estado:", Estilo.PEQUENA_NEGRITA));
        pastillaEstado.poner(nz(p.getEstado()).toUpperCase());
        pastillaEstado.setVisible(true);
        cargarDetalles(0);
    }

    // ---------- Detalles / ingredientes ----------

    private void cargarDetalles(int idAElegir) {
        List<DetallePedido> ds = detalleDAO.listarPorPedido(pedidoSel.getId());
        cargandoCombos = true;
        cmbDetalle.removeAllItems();
        cmbDetalle.addItem(new Opcion(0, "Seleccione un detalle...", ""));
        int indice = 0;
        int totalExcluidos = 0;
        for (DetallePedido d : ds) {
            cmbDetalle.addItem(new Opcion(d.getId(), "Detalle N.° " + dosDigitos(d.getId()) + " – "
                    + d.getPlato().getNombre(), ""));
            if (d.getId() == idAElegir) {
                indice = cmbDetalle.getItemCount() - 1;
            }
            totalExcluidos += exclusionDAO.listarPorDetalle(d.getId()).size();
        }
        cmbDetalle.setSelectedIndex(indice);
        cargandoCombos = false;
        lblDetalles.setText(ds.size() + (ds.size() == 1 ? " plato" : " platos"));
        lblExcluidos.setText(String.valueOf(totalExcluidos));
        alElegirDetalle();
    }

    private void alElegirDetalle() {
        Opcion o = (Opcion) cmbDetalle.getSelectedItem();
        detalleSel = (o == null || o.id <= 0) ? null : detalleDAO.buscarPorId(o.id);
        txtIdDetalle.setText(detalleSel == null ? "" : String.valueOf(detalleSel.getId()));
        cargarExclusionesYReceta();
    }

    private void cargarExclusionesYReceta() {
        List<Object[]> filas = new ArrayList<>();
        List<Integer> excluidos = new ArrayList<>();
        if (detalleSel != null) {
            for (Ingrediente i : exclusionDAO.listarPorDetalle(detalleSel.getId())) {
                excluidos.add(i.getId());
                filas.add(new Object[]{i.getId(), i.getNombre(), nz(i.getUnidad())});
            }
        }
        tabla.setFilas(filas);

        cargandoCombos = true;
        cmbIngrediente.removeAllItems();
        cmbIngrediente.addItem(new Opcion(0, "Seleccione un ingrediente...", ""));
        if (detalleSel != null) {
            List<PlatoIngrediente> receta = recetaDAO.listarPorPlato(detalleSel.getPlato().getId());
            for (PlatoIngrediente pi : receta) {
                Ingrediente ing = pi.getIngrediente();
                if (!excluidos.contains(ing.getId())) {
                    cmbIngrediente.addItem(new Opcion(ing.getId(), ing.getNombre(), nz(ing.getUnidad())));
                }
            }
        }
        cmbIngrediente.setSelectedIndex(0);
        cargandoCombos = false;
        mostrarIngredienteElegido();
    }

    private void mostrarIngredienteElegido() {
        Opcion o = (Opcion) cmbIngrediente.getSelectedItem();
        if (o == null || o.id <= 0) {
            lblIngSel.setText("-");
            lblUnidad.setText("-");
        } else {
            lblIngSel.setText(o.texto);
            lblUnidad.setText(o.extra.isEmpty() ? "-" : o.extra);
        }
    }

    private void limpiar() {
        if (cmbIngrediente.getItemCount() > 0) {
            cmbIngrediente.setSelectedIndex(0);
        }
        tabla.limpiarSeleccion();
    }

    // ---------- Acciones ----------

    private boolean pedidoModificable() {
        if (pedidoSel == null) {
            advertencia("Primero busque y seleccione un pedido.");
            return false;
        }
        Pedido p = PedidosOperaciones.buscarPedidoObjeto(pedidoSel.getId());
        if (p == null) {
            advertencia("El pedido ya no existe.");
            mostrarPedido(null);
            return false;
        }
        String estado = nz(p.getEstado()).toUpperCase();
        if (!estado.equals("PENDIENTE") && !estado.equals("EN PREPARACION")) {
            advertencia("El pedido N.° " + p.getId() + " está " + estado + ".\n"
                    + "Solo se pueden modificar exclusiones cuando está PENDIENTE o EN PREPARACIÓN.");
            mostrarPedido(p);
            return false;
        }
        return true;
    }

    private void excluir() {
        if (!pedidoModificable()) {
            return;
        }
        if (detalleSel == null) {
            advertencia("Seleccione el detalle (plato) del pedido.");
            return;
        }
        Opcion ing = (Opcion) cmbIngrediente.getSelectedItem();
        if (ing == null || ing.id <= 0) {
            advertencia("Seleccione el ingrediente que desea excluir.");
            return;
        }
        int idDetalle = detalleSel.getId();
        int idIngrediente = ing.id;

        DetalleIngredienteExcluidoOperaciones.registrarExclusion(idDetalle, idIngrediente);

        if (estaExcluido(idDetalle, idIngrediente)) {
            aviso("Ingrediente excluido correctamente.\n" + ing.texto + " ya no se usará en este plato del pedido.");
            cargarDetalles(idDetalle);
        } else {
            error("No se pudo registrar la exclusión.\nPuede que el inventario del pedido ya se haya descontado "
                    + "o que el pedido tenga pagos registrados.");
        }
    }

    private void quitarDesdeTabla(Object[] fila) {
        if (detalleSel == null) {
            return;
        }
        int idIngrediente = ((Number) fila[0]).intValue();
        String nombreIng = String.valueOf(fila[1]);
        if (!pedidoModificable()) {
            return;
        }
        if (!confirmar("¿Quitar la exclusión de \"" + nombreIng + "\"?\n"
                + "El ingrediente volverá a usarse en este plato.")) {
            return;
        }
        int idDetalle = detalleSel.getId();

        DetalleIngredienteExcluidoOperaciones.eliminarExclusion(idDetalle, idIngrediente);

        if (!estaExcluido(idDetalle, idIngrediente)) {
            aviso("Exclusión eliminada correctamente.");
            cargarDetalles(idDetalle);
        } else {
            error("No se pudo eliminar la exclusión.\nPuede que el inventario del pedido ya se haya descontado "
                    + "o que el pedido tenga pagos registrados.");
        }
    }

    private boolean estaExcluido(int idDetalle, int idIngrediente) {
        for (Ingrediente i : exclusionDAO.listarPorDetalle(idDetalle)) {
            if (i.getId() == idIngrediente) {
                return true;
            }
        }
        return false;
    }
}
