package vista.escritorio;

import controlador.DetalleBebidaOperaciones;
import controlador.DetallePedidoOperacione;
import controlador.PedidosOperaciones;
import dao.BebidaDAO;
import dao.ClienteDAO;
import dao.DetalleIngredienteExcluidoDAO;
import dao.EmpleadoDAO;
import dao.impl.EmpleadoDAOImpl;
import modelo.Empleado;
import dao.PedidoDAO;
import dao.PlatoDAO;
import dao.PlatoIngredienteDAO;
import dao.impl.BebidaDAOImpl;
import dao.impl.ClienteDAOImpl;
import dao.impl.DetalleIngredienteExcluidoDAOImpl;
import dao.impl.PedidoDAOImpl;
import dao.impl.PlatoDAOImpl;
import dao.impl.PlatoIngredienteDAOImpl;
import modelo.Bebida;
import modelo.Cliente;
import modelo.Plato;
import modelo.PlatoIngrediente;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Pantalla única del mesero: cliente + platos disponibles + ingredientes que no desea,
 * todo en un solo paso. Registra el pedido, sus detalles y las exclusiones.
 */
public class PanelTomarPedido extends ModuloBase {

    private static final int MAX_PLATOS = 15;
    private static final int MAX_OTRO = 100;

    private static final class Opcion {
        final int id;
        final String texto;
        final double precio;

        Opcion(int id, String texto, double precio) {
            this.id = id;
            this.texto = texto;
            this.precio = precio;
        }

        @Override
        public String toString() {
            return texto;
        }
    }

    private final PlatoDAO platoDAO = new PlatoDAOImpl();
    private final PlatoIngredienteDAO recetaDAO = new PlatoIngredienteDAOImpl();
    private final EmpleadoDAO empleadoDAO = new EmpleadoDAOImpl();
    private final ClienteDAO clienteDAO = new ClienteDAOImpl();
    private final PedidoDAO pedidoDAO = new PedidoDAOImpl();
    private final DetalleIngredienteExcluidoDAO exclusionDAO = new DetalleIngredienteExcluidoDAOImpl();

    private final BebidaDAO bebidaDAO = new BebidaDAOImpl();
    private List<Opcion> platosDisponibles = new ArrayList<>();
    private List<Opcion> bebidasLista = new ArrayList<>();
    private List<Cliente> clientes = new ArrayList<>();

    private final JPanel panelFilas = new JPanel();
    private final List<FilaPlato> filas = new ArrayList<>();
    private final JPanel panelBebidas = new JPanel();
    private final List<FilaBebida> filasBebida = new ArrayList<>();
    private final JLabel lblTituloBebidas = Estilo.etiqueta("Bebidas", Estilo.SUBTITULO, Estilo.PRIMARIO_OSCURO);
    private final JButton btnMasBebidas = Estilo.boton("+  Agregar bebida", Estilo.GRIS);
    private final JButton btnMasPlatos = Estilo.boton("+  Agregar otro plato", Estilo.PRIMARIO);

    private final JComboBox<Opcion> cmbMesero = new JComboBox<>();
    private final JComboBox<Opcion> cmbCliente = new JComboBox<>();
    private final JTextField txtNombres = Estilo.campo();
    private final JTextField txtApellidos = Estilo.campo();
    private final JTextField txtTelefono = Estilo.campo();
    private final JTextField txtCorreo = Estilo.campo();
    private final JTextField txtDireccion = Estilo.campo();
    private final JTextField txtReferencia = Estilo.campo();
    private final JTextField txtDistrito = Estilo.campo();
    private boolean llenandoCliente = false;
    private final JLabel lblPlatos = Estilo.etiqueta("0", Estilo.SUBTITULO, Estilo.PRIMARIO_OSCURO);
    private final JLabel lblTotal = Estilo.etiqueta("S/ 0.00", Estilo.SUBTITULO, new Color(0x15803D));
    private final JLabel lblDisponibles;
    private final JLabel lblPedidosHoy;

    // Boton "Quitar" dibujado a mano (no depende de simbolos de la fuente): pildora roja suave con icono de tacho.
    private static final class BotonQuitar extends JButton {
        private boolean encima;

        BotonQuitar() {
            super("Quitar");
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setFont(Estilo.PEQUENA_NEGRITA);
            setForeground(new Color(0xB91C1C));
            setHorizontalAlignment(SwingConstants.RIGHT);
            setBorder(new EmptyBorder(5, 30, 5, 12));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText("Quitar del pedido");
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    encima = true;
                    repaint();
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    encima = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            g2.setColor(encima ? new Color(0xFECACA) : new Color(0xFEE2E2));
            g2.fillRoundRect(0, 0, w - 1, h - 1, h, h);
            g2.setColor(new Color(0xFCA5A5));
            g2.drawRoundRect(0, 0, w - 1, h - 1, h, h);

            // Icono de tacho
            g2.setColor(new Color(0xB91C1C));
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int x = 12;
            int y = (h - 13) / 2;
            g2.drawLine(x, y + 3, x + 10, y + 3);          // tapa
            g2.drawLine(x + 3, y + 3, x + 3, y + 1);       // asa
            g2.drawLine(x + 3, y + 1, x + 7, y + 1);
            g2.drawLine(x + 7, y + 1, x + 7, y + 3);
            g2.drawRoundRect(x + 1, y + 5, 8, 8, 2, 2);    // cuerpo
            g2.drawLine(x + 4, y + 7, x + 4, y + 11);      // rayas
            g2.drawLine(x + 6, y + 7, x + 6, y + 11);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ---------- Una fila de plato ----------
    private final class FilaPlato extends JPanel {
        final JComboBox<Opcion> cmb = new JComboBox<>();
        final JSpinner spn = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        final JLabel lblSubtotal = Estilo.etiqueta("S/ 0.00", Estilo.NEGRITA, new Color(0x15803D));
        final JLabel lblSin = Estilo.etiqueta("Sin cambios en los ingredientes", Estilo.PEQUENA, Estilo.TEXTO_SUAVE);
        final JLabel lblTitulo = Estilo.etiqueta("Plato", Estilo.NEGRITA, Estilo.PRIMARIO_OSCURO);
        final JButton btnQuitar = new BotonQuitar();
        final Set<Integer> excluidos = new LinkedHashSet<>();
        final List<String> nombresExcluidos = new ArrayList<>();
        String otros = "";

        FilaPlato() {
            super(new GridBagLayout());
            setOpaque(false);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0xD5E0F0), 1, true),
                    new EmptyBorder(10, 12, 10, 12)));

            cmb.setFont(Estilo.FUENTE);
            cmb.setPreferredSize(new Dimension(200, 36));
            cmb.setMinimumSize(new Dimension(100, 36));
            llenarCombo(cmb);
            spn.setFont(Estilo.FUENTE);
            spn.setPreferredSize(new Dimension(80, 36));
            spn.setMinimumSize(new Dimension(80, 36));
            lblSubtotal.setMinimumSize(new Dimension(80, 20));
            lblSubtotal.setPreferredSize(new Dimension(80, 20));

            btnQuitar.setFont(Estilo.PEQUENA_NEGRITA);
            btnQuitar.setForeground(Estilo.PELIGRO);
            btnQuitar.setContentAreaFilled(false);
            btnQuitar.setBorderPainted(false);
            btnQuitar.setFocusPainted(false);
            btnQuitar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnQuitar.addActionListener(e -> quitarFila(this));

            JButton btnIng = Estilo.boton("Ingredientes del plato", Estilo.GRIS);
            btnIng.setFont(Estilo.PEQUENA_NEGRITA);
            btnIng.setPreferredSize(new Dimension(190, 34));
            btnIng.addActionListener(e -> elegirIngredientes(this));

            JPanel cab = new JPanel(new BorderLayout());
            cab.setOpaque(false);
            cab.add(lblTitulo, BorderLayout.WEST);
            cab.add(btnQuitar, BorderLayout.EAST);

            GridBagConstraints g = new GridBagConstraints();
            g.fill = GridBagConstraints.HORIZONTAL;
            g.gridy = 0;
            g.gridx = 0;
            g.gridwidth = 4;
            g.weightx = 1;
            g.insets = new Insets(0, 0, 6, 0);
            add(cab, g);

            g.gridy = 1;
            g.gridwidth = 1;
            g.weightx = 1;
            g.insets = new Insets(0, 0, 8, 10);
            add(cmb, g);
            g.gridx = 1;
            g.weightx = 0;
            g.insets = new Insets(0, 0, 8, 6);
            add(Estilo.etiqueta("Cant.:", Estilo.PEQUENA_NEGRITA, Estilo.TEXTO), g);
            g.gridx = 2;
            g.insets = new Insets(0, 0, 8, 12);
            add(spn, g);
            g.gridx = 3;
            g.insets = new Insets(0, 0, 8, 0);
            add(lblSubtotal, g);

            g.gridy = 2;
            g.gridx = 0;
            g.weightx = 0;
            g.insets = new Insets(0, 0, 0, 10);
            add(btnIng, g);
            g.gridx = 1;
            g.gridwidth = 3;
            g.weightx = 1;
            g.insets = new Insets(0, 0, 0, 0);
            add(lblSin, g);

            cmb.addActionListener(e -> {
                excluidos.clear();
                nombresExcluidos.clear();
                otros = "";
                refrescarResumen();
                actualizarTotales();
            });
            spn.addChangeListener(e -> actualizarTotales());
        }

        Opcion plato() {
            return (Opcion) cmb.getSelectedItem();
        }

        int cantidad() {
            try {
                spn.commitEdit();
            } catch (java.text.ParseException ignorada) {
            }
            return ((Number) spn.getValue()).intValue();
        }

        double subtotal() {
            Opcion p = plato();
            return p == null || p.id <= 0 ? 0 : p.precio * cantidad();
        }

        void refrescarResumen() {
            List<String> partes = new ArrayList<>(nombresExcluidos);
            if (!otros.isEmpty()) {
                partes.add(otros);
            }
            if (partes.isEmpty()) {
                lblSin.setText("Sin cambios en los ingredientes");
                lblSin.setForeground(Estilo.TEXTO_SUAVE);
            } else {
                lblSin.setText("<html><nobr>Sin: " + escapar(String.join(", ", partes)) + "</nobr></html>");
                lblSin.setForeground(new Color(0xB45309));
            }
        }

        String observacion() {
            return otros.isEmpty() ? null : "Sin: " + otros;
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }
    }

    // ---------- Una fila de bebida ----------
    private final class FilaBebida extends JPanel {
        final JComboBox<Opcion> cmb = new JComboBox<>();
        final JSpinner spn = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        final JLabel lblSubtotal = Estilo.etiqueta("S/ 0.00", Estilo.NEGRITA, new Color(0x15803D));
        final JLabel lblTitulo = Estilo.etiqueta("Bebida", Estilo.NEGRITA, Estilo.PRIMARIO_OSCURO);
        final JButton btnQuitar = new BotonQuitar();

        FilaBebida() {
            super(new GridBagLayout());
            setOpaque(false);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0xD5E0F0), 1, true),
                    new EmptyBorder(10, 12, 10, 12)));

            cmb.setFont(Estilo.FUENTE);
            cmb.setPreferredSize(new Dimension(200, 36));
            cmb.setMinimumSize(new Dimension(100, 36));
            llenarComboBebidas(cmb);
            spn.setFont(Estilo.FUENTE);
            spn.setPreferredSize(new Dimension(80, 36));
            spn.setMinimumSize(new Dimension(80, 36));
            lblSubtotal.setMinimumSize(new Dimension(80, 20));
            lblSubtotal.setPreferredSize(new Dimension(80, 20));

            btnQuitar.setFont(Estilo.PEQUENA_NEGRITA);
            btnQuitar.setForeground(Estilo.PELIGRO);
            btnQuitar.setContentAreaFilled(false);
            btnQuitar.setBorderPainted(false);
            btnQuitar.setFocusPainted(false);
            btnQuitar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnQuitar.addActionListener(e -> quitarFilaBebida(this));

            JPanel cab = new JPanel(new BorderLayout());
            cab.setOpaque(false);
            cab.add(lblTitulo, BorderLayout.WEST);
            cab.add(btnQuitar, BorderLayout.EAST);

            GridBagConstraints g = new GridBagConstraints();
            g.fill = GridBagConstraints.HORIZONTAL;
            g.gridy = 0;
            g.gridx = 0;
            g.gridwidth = 4;
            g.weightx = 1;
            g.insets = new Insets(0, 0, 6, 0);
            add(cab, g);

            g.gridy = 1;
            g.gridwidth = 1;
            g.weightx = 1;
            g.insets = new Insets(0, 0, 0, 10);
            add(cmb, g);
            g.gridx = 1;
            g.weightx = 0;
            g.insets = new Insets(0, 0, 0, 6);
            add(Estilo.etiqueta("Cant.:", Estilo.PEQUENA_NEGRITA, Estilo.TEXTO), g);
            g.gridx = 2;
            g.insets = new Insets(0, 0, 0, 12);
            add(spn, g);
            g.gridx = 3;
            g.insets = new Insets(0, 0, 0, 0);
            add(lblSubtotal, g);

            cmb.addActionListener(e -> actualizarTotales());
            spn.addChangeListener(e -> actualizarTotales());
        }

        Opcion bebida() {
            return (Opcion) cmb.getSelectedItem();
        }

        int cantidad() {
            try {
                spn.commitEdit();
            } catch (java.text.ParseException ignorada) {
            }
            return ((Number) spn.getValue()).intValue();
        }

        double subtotal() {
            Opcion b = bebida();
            return b == null || b.id <= 0 ? 0 : b.precio * cantidad();
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }
    }

    // 0 = sin mesero fijo (se elige de la lista); mayor que 0 = el mesero que inicio sesion
    private final int idMeseroFijo;

    public PanelTomarPedido() {
        this(0);
    }

    public PanelTomarPedido(int idMeseroFijo) {
        super("Tomar pedido", "Registra el pedido del cliente: platos y lo que no desea, en un solo paso",
                "☷", Estilo.PRIMARIO, true);
        this.idMeseroFijo = idMeseroFijo;

        lblDisponibles = agregarEstadistica("♨", new Color(0x38A3F5), "Platos disponibles", Estilo.PRIMARIO_OSCURO);
        lblPedidosHoy = agregarEstadistica("☷", new Color(0xF97316), "Pedidos de hoy", new Color(0xC2410C));
        setAnchoDerecho(400);

        // Izquierda: lista de platos
        panelFilas.setOpaque(false);
        panelFilas.setLayout(new BoxLayout(panelFilas, BoxLayout.Y_AXIS));
        panelBebidas.setOpaque(false);
        panelBebidas.setLayout(new BoxLayout(panelBebidas, BoxLayout.Y_AXIS));
        lblTituloBebidas.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblTituloBebidas.setBorder(new EmptyBorder(6, 0, 8, 0));
        lblTituloBebidas.setVisible(false);
        JPanel columna = new JPanel();
        columna.setOpaque(false);
        columna.setLayout(new BoxLayout(columna, BoxLayout.Y_AXIS));
        panelFilas.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelBebidas.setAlignmentX(Component.LEFT_ALIGNMENT);
        columna.add(panelFilas);
        columna.add(lblTituloBebidas);
        columna.add(panelBebidas);
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setOpaque(false);
        contenedor.add(columna, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(contenedor);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        btnMasPlatos.setFont(Estilo.PEQUENA_NEGRITA);
        btnMasPlatos.setPreferredSize(new Dimension(120, 36));
        btnMasPlatos.addActionListener(e -> agregarFila());

        panelIzquierdo.add(tituloDePanel("♨", "Platos y bebidas del pedido"), BorderLayout.NORTH);
        panelIzquierdo.add(scroll, BorderLayout.CENTER);
        btnMasBebidas.setFont(Estilo.PEQUENA_NEGRITA);
        btnMasBebidas.setPreferredSize(new Dimension(120, 36));
        btnMasBebidas.addActionListener(e -> agregarFilaBebida());
        JPanel botonesAgregar = new JPanel(new GridLayout(1, 2, 10, 0));
        botonesAgregar.setOpaque(false);
        botonesAgregar.add(btnMasPlatos);
        botonesAgregar.add(btnMasBebidas);
        panelIzquierdo.add(botonesAgregar, BorderLayout.SOUTH);

        // Derecha: mesero + cliente + resumen
        cmbMesero.setFont(Estilo.FUENTE);
        cmbMesero.setPreferredSize(new Dimension(160, 36));
        cmbCliente.setFont(Estilo.FUENTE);
        cmbCliente.setPreferredSize(new Dimension(160, 36));
        cmbCliente.addActionListener(e -> alElegirCliente());

        JPanel resumen = new JPanel(new GridLayout(2, 2, 6, 6));
        resumen.setOpaque(false);
        resumen.add(Estilo.etiqueta("Platos en el pedido:", Estilo.PEQUENA_NEGRITA, Estilo.TEXTO));
        resumen.add(lblPlatos);
        resumen.add(Estilo.etiqueta("Total a pagar:", Estilo.PEQUENA_NEGRITA, Estilo.TEXTO));
        resumen.add(lblTotal);

        Formulario fMesero = new Formulario();
        fMesero.agregar("Mesero:", true, cmbMesero);

        Formulario fCliente = new Formulario();
        fCliente.agregar("Cliente:", false, cmbCliente);
        fCliente.agregar("Nombres:", true, txtNombres);
        fCliente.agregar("Apellidos:", true, txtApellidos);
        fCliente.agregar("Teléfono:", false, txtTelefono);
        fCliente.agregar("Correo:", false, txtCorreo);
        fCliente.agregar("Dirección:", false, txtDireccion);
        fCliente.agregar("Referencia:", false, txtReferencia);
        fCliente.agregar("Distrito:", false, txtDistrito);

        JButton btnRegistrar = Estilo.boton("Registrar pedido", Estilo.EXITO);
        JButton btnLimpiar = Estilo.boton("Limpiar", Estilo.GRIS);

        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        anadir(col, tituloDePanel("\u2615", "¿Quién toma el pedido?"));
        anadir(col, fMesero);
        if (idMeseroFijo > 0) {
            cmbMesero.setEnabled(false);
            anadir(col, Estilo.aviso("Los pedidos se registran a su nombre, según el usuario con el que inició sesión.", "info"));
        } else {
            anadir(col, Estilo.aviso("Esta computadora es compartida: elija su nombre antes de registrar.", "alerta"));
        }
        anadir(col, tituloDePanel("\u263A", "Registro de cliente"));
        anadir(col, fCliente);
        anadir(col, Estilo.aviso("Elija un cliente existente o deje \"Cliente nuevo\" y complete sus datos; "
                + "se registrará solo. La dirección, referencia y distrito se usan luego en el delivery.", "info"));
        anadir(col, resumen);
        anadir(col, btnRegistrar);
        anadir(col, btnLimpiar);
        anadir(col, Estilo.aviso("En cada plato, \"Ingredientes del plato\" muestra la receta para marcar lo que "
                + "el cliente no quiere. Si el ingrediente no aparece, puede escribirlo.", "info"));

        panelDerecho.add(tituloDePanel("☰", "Mesero, cliente y resumen"), BorderLayout.NORTH);
        panelDerecho.add(col, BorderLayout.CENTER);

        btnRegistrar.addActionListener(e -> registrar());
        btnLimpiar.addActionListener(e -> reiniciar());

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                recargarDatos();
            }
        });

        recargarDatos();
        agregarFila();
    }

    private void anadir(JPanel col, JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
        col.add(c);
        col.add(Box.createVerticalStrut(12));
    }

    // ---------- Datos ----------

    @Override
    protected String avisoAlActualizar() {
        return "Al actualizar se borra el pedido que está armando. ¿Desea continuar?";
    }

    private static String dos(double v) {
        return String.format(Locale.US, "%.2f", v);
    }

    private static String escapar(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private static String nombreCompleto(Cliente c) {
        String ap = c.getApellidos() == null ? "" : c.getApellidos().trim();
        return (c.getNombres() + ("-".equals(ap) || ap.isEmpty() ? "" : " " + ap)).trim();
    }

    private void llenarCombo(JComboBox<Opcion> cmb) {
        cmb.addItem(new Opcion(0, "Seleccione un plato...", 0));
        for (Opcion o : platosDisponibles) {
            cmb.addItem(o);
        }
    }

    private void recargarDatos() {
        try {
            platoDAO.actualizarDisponibilidad();
            List<Opcion> nuevos = new ArrayList<>();
            for (Plato p : platoDAO.listar()) {
                if ("ACTIVO".equals(platoDAO.obtenerEstado(p.getId()))) {
                    nuevos.add(new Opcion(p.getId(), p.getNombre() + "  -  S/ " + dos(p.getPrecio()), p.getPrecio()));
                }
            }
            platosDisponibles = nuevos;
            lblDisponibles.setText(String.valueOf(nuevos.size()));

            List<Opcion> bebs = new ArrayList<>();
            for (Bebida b : bebidaDAO.listar()) {
                if (b.getStock() <= 0) {
                    continue;
                }
                bebs.add(new Opcion(b.getId(), b.getNombre() + "  -  S/ " + dos(b.getPrecio())
                        + "  (quedan " + b.getStock() + ")", b.getPrecio()));
            }
            bebidasLista = bebs;
            for (FilaBebida fb : filasBebida) {
                Opcion previa = fb.bebida();
                fb.cmb.removeAllItems();
                llenarComboBebidas(fb.cmb);
                if (previa != null && previa.id > 0) {
                    for (int k = 0; k < fb.cmb.getItemCount(); k++) {
                        if (fb.cmb.getItemAt(k).id == previa.id) {
                            fb.cmb.setSelectedIndex(k);
                            break;
                        }
                    }
                }
            }

            // Mantiene el plato elegido en cada fila si sigue disponible.
            for (FilaPlato fila : filas) {
                Opcion previo = fila.plato();
                fila.cmb.removeAllItems();
                llenarCombo(fila.cmb);
                if (previo != null && previo.id > 0) {
                    for (int k = 0; k < fila.cmb.getItemCount(); k++) {
                        if (fila.cmb.getItemAt(k).id == previo.id) {
                            fila.cmb.setSelectedIndex(k);
                            break;
                        }
                    }
                }
            }

            clientes = clienteDAO.listar();
            int clientePrevio = idOpcion(cmbCliente);
            llenandoCliente = true;
            cmbCliente.removeAllItems();
            cmbCliente.addItem(new Opcion(0, "Cliente nuevo", 0));
            for (Cliente c : clientes) {
                cmbCliente.addItem(new Opcion(c.getId(), nombreCompleto(c), 0));
            }
            seleccionar(cmbCliente, clientePrevio);
            llenandoCliente = false;

            int mesPrevio = idOpcion(cmbMesero);
            cmbMesero.removeAllItems();
            cmbMesero.addItem(new Opcion(0, "Seleccione mesero...", 0));
            for (Empleado e : empleadoDAO.listar()) {
                if (idMeseroFijo > 0 && e.getId() != idMeseroFijo) {
                    continue;
                }
                if ("MESERO".equalsIgnoreCase(e.getCargo()) && "ACTIVO".equalsIgnoreCase(e.getEstadoLaboral())) {
                    cmbMesero.addItem(new Opcion(e.getId(), e.getNombres() + " " + e.getApellidos(), 0));
                }
            }
            seleccionar(cmbMesero, idMeseroFijo > 0 ? idMeseroFijo : mesPrevio);

            long hoy = 0;
            String hoyTexto = java.time.LocalDate.now().toString();
            for (modelo.Pedido p : pedidoDAO.listar()) {
                if (String.valueOf(p.getFecha()).startsWith(hoyTexto)) {
                    hoy++;
                }
            }
            lblPedidosHoy.setText(String.valueOf(hoy));
        } catch (Exception ex) {
            error("No se pudieron cargar los datos: " + ex.getMessage());
        }
        actualizarTotales();
    }

    // ---------- Filas ----------

    private void agregarFila() {
        if (filas.size() >= MAX_PLATOS) {
            advertencia("Puede agregar hasta " + MAX_PLATOS + " platos en un pedido.");
            return;
        }
        FilaPlato f = new FilaPlato();
        filas.add(f);
        panelFilas.add(f);
        panelFilas.add(Box.createVerticalStrut(10));
        refrescarFilas();
    }

    private void quitarFila(FilaPlato f) {
        int pos = filas.indexOf(f);
        if (pos < 0) {
            return;
        }
        filas.remove(f);
        // Cada fila va seguida de un separador (strut).
        panelFilas.remove(pos * 2 + 1);
        panelFilas.remove(pos * 2);
        if (filas.isEmpty()) {
            agregarFila();
            return;
        }
        refrescarFilas();
    }

    private void refrescarFilas() {
        for (int i = 0; i < filas.size(); i++) {
            filas.get(i).lblTitulo.setText("Plato " + (i + 1));
            filas.get(i).btnQuitar.setVisible(filas.size() > 1);
        }
        panelFilas.revalidate();
        panelFilas.repaint();
        actualizarTotales();
    }

    private void llenarComboBebidas(JComboBox<Opcion> cmb) {
        cmb.addItem(new Opcion(0, "Seleccione una bebida...", 0));
        for (Opcion o : bebidasLista) {
            cmb.addItem(o);
        }
    }

    private void agregarFilaBebida() {
        if (bebidasLista.isEmpty()) {
            advertencia("No hay bebidas registradas. El administrador puede crearlas en la sección Bebidas.");
            return;
        }
        if (filasBebida.size() >= MAX_PLATOS) {
            advertencia("Puede agregar hasta " + MAX_PLATOS + " bebidas en un pedido.");
            return;
        }
        FilaBebida f = new FilaBebida();
        filasBebida.add(f);
        panelBebidas.add(f);
        panelBebidas.add(Box.createVerticalStrut(10));
        refrescarBebidas();
    }

    private void quitarFilaBebida(FilaBebida f) {
        int pos = filasBebida.indexOf(f);
        if (pos < 0) {
            return;
        }
        filasBebida.remove(f);
        panelBebidas.remove(pos * 2 + 1);
        panelBebidas.remove(pos * 2);
        refrescarBebidas();
    }

    private void refrescarBebidas() {
        for (int i = 0; i < filasBebida.size(); i++) {
            filasBebida.get(i).lblTitulo.setText("Bebida " + (i + 1));
        }
        lblTituloBebidas.setVisible(!filasBebida.isEmpty());
        panelBebidas.revalidate();
        panelBebidas.repaint();
        actualizarTotales();
    }

    private void actualizarTotales() {
        double total = 0;
        int cantidadPlatos = 0;
        for (FilaPlato f : filas) {
            f.lblSubtotal.setText("S/ " + dos(f.subtotal()));
            if (f.plato() != null && f.plato().id > 0) {
                total += f.subtotal();
                cantidadPlatos += f.cantidad();
            }
        }
        for (FilaBebida b : filasBebida) {
            b.lblSubtotal.setText("S/ " + dos(b.subtotal()));
            total += b.subtotal();
        }
        lblPlatos.setText(String.valueOf(cantidadPlatos));
        lblTotal.setText("S/ " + dos(total));
    }

    private void reiniciar() {
        filas.clear();
        panelFilas.removeAll();
        filasBebida.clear();
        panelBebidas.removeAll();
        lblTituloBebidas.setVisible(false);
        if (cmbCliente.getItemCount() > 0) {
            cmbCliente.setSelectedIndex(0);
        }
        alElegirCliente();
        agregarFila();
    }

    // ---------- Ingredientes que no desea ----------

    private void elegirIngredientes(FilaPlato fila) {
        Opcion plato = fila.plato();
        if (plato == null || plato.id <= 0) {
            advertencia("Primero seleccione el plato.");
            return;
        }
        List<PlatoIngrediente> receta = recetaDAO.listarPorPlato(plato.id);

        JPanel contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBorder(new EmptyBorder(4, 4, 4, 4));

        JLabel titulo = new JLabel("<html><b>" + escapar(plato.texto.split("  -  ")[0])
                + "</b><br>Marque los ingredientes que el cliente <b>NO</b> desea:</html>");
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenido.add(titulo);
        contenido.add(Box.createVerticalStrut(8));

        List<JCheckBox> checks = new ArrayList<>();
        List<Integer> idsIng = new ArrayList<>();
        List<String> nombres = new ArrayList<>();
        if (receta.isEmpty()) {
            JLabel vacio = new JLabel("Este plato no tiene ingredientes en su receta.");
            vacio.setAlignmentX(Component.LEFT_ALIGNMENT);
            contenido.add(vacio);
        }
        for (PlatoIngrediente r : receta) {
            JCheckBox cb = new JCheckBox(r.getIngrediente().getNombre());
            cb.setFont(Estilo.FUENTE);
            cb.setAlignmentX(Component.LEFT_ALIGNMENT);
            cb.setSelected(fila.excluidos.contains(r.getIngrediente().getId()));
            checks.add(cb);
            idsIng.add(r.getIngrediente().getId());
            nombres.add(r.getIngrediente().getNombre());
            contenido.add(cb);
        }

        contenido.add(Box.createVerticalStrut(10));
        JLabel lblOtro = new JLabel("¿Otro ingrediente que no desea? (si no aparece arriba, escríbalo)");
        lblOtro.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenido.add(lblOtro);
        contenido.add(Box.createVerticalStrut(4));
        JTextField txtOtro = new JTextField(fila.otros, 28);
        txtOtro.setFont(Estilo.FUENTE);
        txtOtro.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtOtro.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        contenido.add(txtOtro);

        JScrollPane sp = new JScrollPane(contenido);
        sp.setBorder(null);
        sp.setPreferredSize(new Dimension(420, Math.min(420, 170 + checks.size() * 28)));

        int r = JOptionPane.showConfirmDialog(this, sp, "Ingredientes del plato",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) {
            return;
        }
        String otro = txtOtro.getText().trim();
        if (otro.length() > MAX_OTRO) {
            advertencia("El texto del ingrediente no puede pasar de " + MAX_OTRO + " caracteres.");
            return;
        }
        fila.excluidos.clear();
        fila.nombresExcluidos.clear();
        for (int k = 0; k < checks.size(); k++) {
            if (checks.get(k).isSelected()) {
                fila.excluidos.add(idsIng.get(k));
                fila.nombresExcluidos.add(nombres.get(k));
            }
        }
        fila.otros = otro;
        fila.refrescarResumen();
        refrescarFilas();
    }

    // ---------- Registro ----------

    private static int idOpcion(JComboBox<Opcion> cmb) {
        Opcion o = (Opcion) cmb.getSelectedItem();
        return o == null ? 0 : o.id;
    }

    private static void seleccionar(JComboBox<Opcion> cmb, int id) {
        for (int k = 0; k < cmb.getItemCount(); k++) {
            if (cmb.getItemAt(k).id == id) {
                cmb.setSelectedIndex(k);
                return;
            }
        }
    }

    // Cliente existente: rellena y bloquea los datos. "Cliente nuevo": campos libres.
    private void alElegirCliente() {
        if (llenandoCliente) {
            return;
        }
        int id = idOpcion(cmbCliente);
        Cliente elegido = null;
        for (Cliente c : clientes) {
            if (c.getId() == id) {
                elegido = c;
            }
        }
        boolean existente = elegido != null;
        txtNombres.setText(existente ? elegido.getNombres() : "");
        txtApellidos.setText(existente ? elegido.getApellidos() : "");
        txtTelefono.setText(existente && elegido.getTelefono() != null ? elegido.getTelefono() : "");
        // Los datos de contacto y entrega se pueden completar aunque el cliente ya exista.
        txtCorreo.setText(existente && elegido.getCorreo() != null ? elegido.getCorreo() : "");
        txtDireccion.setText(existente && elegido.getDireccion() != null ? elegido.getDireccion() : "");
        txtReferencia.setText(existente && elegido.getReferencia() != null ? elegido.getReferencia() : "");
        txtDistrito.setText(existente && elegido.getDistrito() != null ? elegido.getDistrito() : "");
        for (JTextField t : new JTextField[]{txtNombres, txtApellidos, txtTelefono}) {
            t.setEditable(!existente);
            t.setBackground(existente ? new Color(0xEEF2F8) : Color.WHITE);
        }
    }

    private void registrar() {
        int idMesero = idOpcion(cmbMesero);
        if (idMesero <= 0) {
            advertencia("Seleccione el mesero que toma el pedido.");
            return;
        }
        int idCliente = idOpcion(cmbCliente);
        String nombres = txtNombres.getText().trim();
        String apellidos = txtApellidos.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String correo = txtCorreo.getText().trim();
        String direccion = txtDireccion.getText().trim();
        String referencia = txtReferencia.getText().trim();
        String distrito = txtDistrito.getText().trim();
        if (!correo.isEmpty() && (correo.length() > 100 || !correo.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+"))) {
            advertencia("Ingrese un correo válido (ejemplo: nombre@correo.com) o déjelo vacío.");
            return;
        }
        if (direccion.length() > 200 || referencia.length() > 200) {
            advertencia("La dirección y la referencia admiten hasta 200 caracteres.");
            return;
        }
        if (distrito.length() > 80) {
            advertencia("El distrito admite hasta 80 caracteres.");
            return;
        }
        if (idCliente <= 0) {
            if (nombres.isEmpty() || apellidos.isEmpty()) {
                advertencia("Complete nombres y apellidos del cliente.");
                return;
            }
            if (!telefono.isEmpty() && !telefono.matches("[0-9+ ]{6,15}")) {
                advertencia("El teléfono solo puede tener números (de 6 a 15 dígitos).");
                return;
            }
            if (nombres.length() > 100 || apellidos.length() > 100) {
                advertencia("Los nombres y apellidos admiten hasta 100 caracteres.");
                return;
            }
        }
        String nombre = (nombres + " " + apellidos).trim();
        for (int i = 0; i < filas.size(); i++) {
            if (filas.get(i).plato() == null || filas.get(i).plato().id <= 0) {
                advertencia("Seleccione el plato " + (i + 1) + " o quítelo del pedido.");
                return;
            }
        }
        for (int i = 0; i < filasBebida.size(); i++) {
            if (filasBebida.get(i).bebida() == null || filasBebida.get(i).bebida().id <= 0) {
                advertencia("Seleccione la bebida " + (i + 1) + " o quítela del pedido.");
                return;
            }
        }
        if (!confirmar("¿Registrar el pedido?\n\nMesero: " + cmbMesero.getSelectedItem() + "\nCliente: " + nombre + "\nPlatos: " + lblPlatos.getText()
                + "\nTotal: " + lblTotal.getText())) {
            return;
        }

        // 1. Cliente (existente o nuevo).
        if (idCliente <= 0) {
            idCliente = clienteDAO.insertar(new Cliente(0, nombres, telefono, apellidos,
                    direccion, referencia, distrito, correo));
            if (idCliente <= 0) {
                error("No se pudo registrar al cliente.");
                return;
            }
        } else {
            // Cliente existente: si el mesero completó o cambió sus datos de contacto/entrega, se guardan.
            Cliente actual = clienteDAO.buscarPorId(idCliente);
            if (actual != null && (!nz(actual.getCorreo()).equals(correo)
                    || !nz(actual.getDireccion()).equals(direccion)
                    || !nz(actual.getReferencia()).equals(referencia)
                    || !nz(actual.getDistrito()).equals(distrito))) {
                actual.setCorreo(correo);
                actual.setDireccion(direccion);
                actual.setReferencia(referencia);
                actual.setDistrito(distrito);
                clienteDAO.actualizar(actual);
            }
        }

        // 2. Pedido.
        int idPedido = PedidosOperaciones.registrarPedido(idCliente, idMesero);
        if (idPedido <= 0) {
            error("No se pudo registrar el pedido.");
            return;
        }

        // 3. Detalles y exclusiones.
        int agregados = 0;
        StringBuilder fallidos = new StringBuilder();
        for (FilaPlato f : filas) {
            int idDetalle = DetallePedidoOperacione.registrarDetalle(idPedido, f.plato().id, f.cantidad(), f.observacion());
            if (idDetalle <= 0) {
                fallidos.append("\n- ").append(f.plato().texto.split("  -  ")[0]);
                continue;
            }
            agregados++;
            for (int idIng : f.excluidos) {
                exclusionDAO.insertar(idDetalle, idIng);
            }
        }

        if (agregados == 0) {
            pedidoDAO.eliminar(idPedido);
            error("No se pudo registrar el pedido: ningún plato se pudo agregar.\n"
                    + "Verifique que los platos sigan disponibles.");
            recargarDatos();
            return;
        }

        for (FilaBebida fb : filasBebida) {
            int idDb = DetalleBebidaOperaciones.registrarDetalleBebida(idPedido, fb.bebida().id, fb.cantidad());
            if (idDb <= 0) {
                fallidos.append("\n- ").append(fb.bebida().texto.split("  -  ")[0]);
            }
        }

        String msg = "Pedido N.° " + idPedido + " registrado correctamente.\nCliente: " + nombre
                + "\nTotal: S/ " + dos(totalDePedido(idPedido));
        if (fallidos.length() > 0) {
            msg += "\n\nNo se pudieron agregar estos productos:" + fallidos;
        }
        aviso(msg);
        reiniciar();
        recargarDatos();
    }

    private double totalDePedido(int idPedido) {
        return new dao.impl.DetallePedidoDAOImpl().calcularTotal(idPedido);
    }
}
