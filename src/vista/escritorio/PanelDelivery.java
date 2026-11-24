package vista.escritorio;

import controlador.DeliveryOperaciones;
import controlador.DeliveryOperaciones.Resultado;
import dao.PedidoDAO;
import dao.impl.PedidoDAOImpl;
import modelo.Cliente;
import modelo.Delivery;
import modelo.Pedido;
import modelo.SeguimientoDelivery;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Sección Delivery: entregas a domicilio gestionadas a mano con inDrive, Rappi u otra plataforma. */
public class PanelDelivery extends ModuloBase {

    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

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

    private static final class Datos {
        List<Delivery> deliveries = new ArrayList<>();
        List<Pedido> pedidos = new ArrayList<>();
    }

    private final PedidoDAO pedidoDAO = new PedidoDAOImpl();

    private Datos datos = new Datos();
    private Delivery seleccionado = null;
    private boolean cargandoCombo = false;

    private final JLabel lblTotal;
    private final JLabel lblEnCurso;
    private final JLabel lblEntregados;

    private final JTextField txtBuscar = new CampoHint("Ej. DEL-001 o 1");
    private final JTextField txtPedidoFiltro = new CampoHint("ID del pedido...");
    private final TablaPaginada tabla = new TablaPaginada(
            new String[]{"Código", "Pedido", "Cliente", "Plataforma", "Costo S/", "Estado"},
            5, true);

    // Registro
    private final JComboBox<Opcion> cmbPedido = new JComboBox<>();
    private final JLabel lblCliente = Estilo.etiqueta(" ", Estilo.PEQUENA, Estilo.TEXTO_SUAVE);
    private final JComboBox<String> cmbPlataforma = new JComboBox<>(Delivery.PLATAFORMAS);
    private final JTextField txtCodigoExterno = Estilo.campo();
    private final JTextField txtDireccion = Estilo.campo();
    private final JTextField txtReferencia = Estilo.campo();
    private final JSpinner spnCosto = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 10000.0, 0.5));

    // Cambio de estado
    private final JLabel lblSeleccion = Estilo.etiqueta("Seleccione un delivery de la tabla.",
            Estilo.PEQUENA_NEGRITA, Estilo.PRIMARIO_OSCURO);
    private final JComboBox<String> cmbNuevoEstado = new JComboBox<>();
    private final JTextField txtDescripcion = Estilo.campo();
    private final JButton btnEstado = Estilo.boton("Actualizar estado", Estilo.PRIMARIO);
    private final JButton btnHistorial = Estilo.boton("Ver seguimiento", Estilo.GRIS);

    public PanelDelivery() {
        super("Delivery", "Entregas a domicilio con inDrive, Rappi u otra plataforma", "➤", Estilo.PRIMARIO, true);

        lblTotal = agregarEstadistica("☷", new Color(0x38A3F5), "Total deliveries", Estilo.PRIMARIO_OSCURO);
        lblEnCurso = agregarEstadistica("⌛", Estilo.ADVERTENCIA, "En curso", new Color(0xB45309));
        lblEntregados = agregarEstadistica("✓", Estilo.EXITO, "Entregados", new Color(0x15803D));
        setAnchoEstadisticas(640);

        // ----- Izquierda -----
        txtBuscar.setToolTipText("ID o código del delivery");
        txtPedidoFiltro.setPreferredSize(new Dimension(130, 36));
        JPanel colPedido = new JPanel(new BorderLayout(0, 4));
        colPedido.setOpaque(false);
        colPedido.add(Estilo.etiqueta("ID del pedido:", Estilo.PEQUENA_NEGRITA, Estilo.TEXTO), BorderLayout.NORTH);
        colPedido.add(txtPedidoFiltro, BorderLayout.CENTER);
        colPedido.setPreferredSize(new Dimension(130, 58));
        JPanel extra = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        extra.setOpaque(false);
        extra.add(colPedido);
        extra.setPreferredSize(new Dimension(130, 58));

        JButton btnBuscar = Estilo.boton("Buscar", Estilo.PRIMARIO);
        JPanel arriba = new JPanel(new BorderLayout(0, 10));
        arriba.setOpaque(false);
        arriba.add(barraBusqueda("Buscar por ID o código de delivery:", txtBuscar, extra, btnBuscar),
                BorderLayout.NORTH);
        arriba.add(tituloDePanel("≡", "Lista de deliveries"), BorderLayout.CENTER);
        panelIzquierdo.add(arriba, BorderLayout.NORTH);

        tabla.setAnchos(65, 55, 110, 82, 65, 105, 55);
        tabla.setTituloAcciones("Seguim.");
        tabla.setColorPastilla(PanelDelivery::coloresEstado);
        tabla.setAcciones(new TablaPaginada.Accion("◉", new Color(0x1E6FEB), Color.WHITE,
                fila -> mostrarSeguimiento(idDeCodigo(fila[0]))));
        panelIzquierdo.add(tabla, BorderLayout.CENTER);

        // ----- Derecha -----
        for (JComponent c : new JComponent[]{cmbPedido, cmbPlataforma, cmbNuevoEstado, spnCosto}) {
            c.setFont(Estilo.FUENTE);
            c.setPreferredSize(new Dimension(120, 36));
        }
        spnCosto.setEditor(new JSpinner.NumberEditor(spnCosto, "0.00"));

        JButton btnRegistrar = Estilo.boton("Registrar delivery", Estilo.EXITO);
        JButton btnLimpiar = Estilo.boton("↻  Limpiar", Estilo.GRIS);
        JPanel botonesRegistro = new JPanel(new GridBagLayout());
        botonesRegistro.setOpaque(false);
        GridBagConstraints gb = new GridBagConstraints();
        gb.fill = GridBagConstraints.HORIZONTAL;
        gb.weightx = 3;
        gb.insets = new Insets(0, 0, 0, 10);
        botonesRegistro.add(btnRegistrar, gb);
        gb.weightx = 2;
        gb.insets = new Insets(0, 0, 0, 0);
        botonesRegistro.add(btnLimpiar, gb);

        JPanel botonesEstado = new JPanel(new GridLayout(1, 2, 10, 0));
        botonesEstado.setOpaque(false);
        botonesEstado.add(btnEstado);
        botonesEstado.add(btnHistorial);

        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.add(alinear(tituloDePanel("+", "Registrar delivery")));
        col.add(Box.createVerticalStrut(8));
        col.add(alinear(conEtiqueta("Pedido:", true, cmbPedido)));
        col.add(alinear(lblCliente));
        col.add(Box.createVerticalStrut(6));
        col.add(alinear(conEtiqueta("Plataforma:", true, cmbPlataforma)));
        col.add(Box.createVerticalStrut(6));
        col.add(alinear(conEtiqueta("Código externo (de la plataforma):", false, txtCodigoExterno)));
        col.add(Box.createVerticalStrut(6));
        col.add(alinear(conEtiqueta("Dirección de entrega:", true, txtDireccion)));
        col.add(Box.createVerticalStrut(6));
        col.add(alinear(conEtiqueta("Referencia de entrega:", false, txtReferencia)));
        col.add(Box.createVerticalStrut(6));
        col.add(alinear(conEtiqueta("Costo del delivery (S/):", true, spnCosto)));
        col.add(Box.createVerticalStrut(12));
        col.add(alinear(botonesRegistro));
        col.add(Box.createVerticalStrut(16));
        col.add(alinear(separador()));
        col.add(Box.createVerticalStrut(12));
        col.add(alinear(tituloDePanel("⇄", "Actualizar estado")));
        col.add(Box.createVerticalStrut(8));
        lblSeleccion.setVerticalAlignment(SwingConstants.TOP);
        JPanel cajaSeleccion = new JPanel(new BorderLayout());
        cajaSeleccion.setOpaque(false);
        cajaSeleccion.setPreferredSize(new Dimension(100, 40));
        cajaSeleccion.add(lblSeleccion, BorderLayout.CENTER);
        col.add(alinear(cajaSeleccion));
        col.add(Box.createVerticalStrut(6));
        col.add(alinear(conEtiqueta("Nuevo estado:", true, cmbNuevoEstado)));
        col.add(Box.createVerticalStrut(6));
        col.add(alinear(conEtiqueta("Descripción (opcional):", false, txtDescripcion)));
        col.add(Box.createVerticalStrut(12));
        col.add(alinear(botonesEstado));
        col.add(Box.createVerticalStrut(12));
        col.add(alinear(Estilo.aviso("Flujo: Solicitado → Repartidor asignado → Pedido listo → En camino → "
                + "Entregado. Solo se puede cancelar antes de la entrega.", "info", 210)));
        col.add(Box.createVerticalStrut(8));
        col.add(alinear(Estilo.aviso("Cada pedido admite un único delivery y no se puede crear para pedidos "
                + "cancelados.", "alerta", 210)));
        panelDerecho.add(col, BorderLayout.NORTH);
        setAnchoDerecho(370);

        activarCambioDeEstado(false);

        // ----- Eventos -----
        tabla.setEscucha(new TablaPaginada.Escucha() {
            @Override
            public void seleccionar(Object[] fila) {
                elegirDelivery(idDeCodigo(fila[0]));
            }

            @Override
            public void ver(Object[] fila) {
                mostrarSeguimiento(idDeCodigo(fila[0]));
            }
        });
        btnBuscar.addActionListener(e -> buscar());
        txtBuscar.addActionListener(e -> buscar());
        txtPedidoFiltro.addActionListener(e -> buscar());
        btnRegistrar.addActionListener(e -> registrar());
        btnLimpiar.addActionListener(e -> limpiarRegistro());
        btnEstado.addActionListener(e -> cambiarEstado());
        btnHistorial.addActionListener(e -> {
            if (seleccionado != null) {
                mostrarSeguimiento(seleccionado.getIdDelivery());
            }
        });
        cmbPedido.addActionListener(e -> {
            if (!cargandoCombo) {
                alElegirPedido();
            }
        });

        recargar(null);

        // Al volver a esta sección se vuelve a leer la base (por ejemplo, para ver pedidos nuevos).
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                recargar(null);
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

    private JComponent separador() {
        JSeparator s = new JSeparator();
        s.setForeground(Estilo.BORDE);
        return s;
    }

    // ---------- Utilidades ----------

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private static int entero(Object o) {
        return o instanceof Number ? ((Number) o).intValue() : 0;
    }

    private static String bonito(String estado) {
        return nz(estado).replace('_', ' ');
    }

    // Texto breve para la pastilla de la tabla (el nombre completo se ve en el seguimiento).
    private static String cortoEstado(String estado) {
        return Delivery.REPARTIDOR_ASIGNADO.equals(estado) ? "ASIGNADO" : bonito(estado);
    }

    private int idDeCodigo(Object codigo) {
        Delivery d = DeliveryOperaciones.buscarPorCodigo(String.valueOf(codigo));
        return d == null ? 0 : d.getIdDelivery();
    }

    private static String fechaBonita(String f) {
        if (f == null) {
            return "";
        }
        try {
            if (f.length() >= 19) {
                return LocalDateTime.parse(f.substring(0, 19).replace(' ', 'T')).format(FECHA_HORA);
            }
        } catch (Exception ignorada) {
        }
        return f;
    }

    private static boolean enCurso(Delivery d) {
        return !d.estaFinalizado();
    }

    private static Color[] coloresEstado(String texto) {
        String t = nz(texto).trim().toUpperCase().replace(' ', '_');
        switch (t) {
            case Delivery.SOLICITADO:
                return new Color[]{new Color(0xFEF3C7), new Color(0xB45309)};
            case Delivery.REPARTIDOR_ASIGNADO:
            case "ASIGNADO":
                return new Color[]{new Color(0xDBEAFE), new Color(0x1D4ED8)};
            case Delivery.PEDIDO_LISTO:
                return new Color[]{new Color(0xCFFAFE), new Color(0x0E7490)};
            case Delivery.EN_CAMINO:
                return new Color[]{new Color(0xFFEDD5), new Color(0xC2410C)};
            case Delivery.ENTREGADO:
                return new Color[]{new Color(0xDCFCE7), new Color(0x15803D)};
            case Delivery.CANCELADO:
                return new Color[]{new Color(0xFEE2E2), new Color(0xDC2626)};
            default:
                return Estilo.coloresPastilla(texto);
        }
    }

    private String clienteDe(int idPedido) {
        for (Pedido p : datos.pedidos) {
            if (p.getId() == idPedido && p.getCliente() != null) {
                return (nz(p.getCliente().getNombres()) + " " + nz(p.getCliente().getApellidos())).trim();
            }
        }
        return "";
    }

    // ---------- Datos ----------

    private void recargar(Integer deliveryAElegir) {
        new SwingWorker<Datos, Void>() {
            @Override
            protected Datos doInBackground() {
                Datos d = new Datos();
                d.deliveries = DeliveryOperaciones.listarDeliveries();
                d.pedidos = pedidoDAO.listar();
                return d;
            }

            @Override
            protected void done() {
                try {
                    datos = get();
                } catch (Exception ex) {
                    error("No se pudieron cargar los deliveries: " + ex.getMessage());
                    return;
                }
                actualizarEstadisticas();
                llenarComboPedidos();
                mostrarTabla(datos.deliveries);
                if (deliveryAElegir != null) {
                    elegirDelivery(deliveryAElegir);
                } else if (seleccionado != null) {
                    elegirDelivery(seleccionado.getIdDelivery());
                }
            }
        }.execute();
    }

    private void actualizarEstadisticas() {
        int curso = 0;
        int entregados = 0;
        for (Delivery d : datos.deliveries) {
            if (enCurso(d)) {
                curso++;
            }
            if (Delivery.ENTREGADO.equals(d.getEstado())) {
                entregados++;
            }
        }
        lblTotal.setText(String.valueOf(datos.deliveries.size()));
        lblEnCurso.setText(String.valueOf(curso));
        lblEntregados.setText(String.valueOf(entregados));
    }

    private void mostrarTabla(List<Delivery> lista) {
        List<Object[]> filas = new ArrayList<>();
        for (Delivery d : lista) {
            filas.add(new Object[]{d.getCodigoDelivery(), d.getIdPedido(),
                    clienteDe(d.getIdPedido()), d.getPlataforma(),
                    String.format(Locale.US, "%.2f", d.getCostoDelivery()),
                    cortoEstado(d.getEstado())});
        }
        tabla.setFilas(filas);
    }

    private void buscar() {
        String texto = txtBuscar.getText().trim();
        String pedido = txtPedidoFiltro.getText().trim();
        if (texto.isEmpty() && pedido.isEmpty()) {
            mostrarTabla(datos.deliveries);
            return;
        }

        Set<Integer> ids = null;

        if (!texto.isEmpty()) {
            Delivery d = texto.matches("\\d+")
                    ? DeliveryOperaciones.buscarPorId(Integer.parseInt(texto))
                    : DeliveryOperaciones.buscarPorCodigo(texto);
            ids = new HashSet<>();
            if (d != null) {
                ids.add(d.getIdDelivery());
            }
        }
        if (!pedido.isEmpty()) {
            if (!pedido.matches("\\d+")) {
                advertencia("El ID del pedido debe ser un número.");
                return;
            }
            Delivery d = DeliveryOperaciones.buscarPorPedido(Integer.parseInt(pedido));
            Set<Integer> porPedido = new HashSet<>();
            if (d != null) {
                porPedido.add(d.getIdDelivery());
            }
            if (ids == null) {
                ids = porPedido;
            } else {
                ids.retainAll(porPedido);
            }
        }

        List<Delivery> encontrados = new ArrayList<>();
        for (Delivery d : datos.deliveries) {
            if (ids.contains(d.getIdDelivery())) {
                encontrados.add(d);
            }
        }
        mostrarTabla(encontrados);
        if (encontrados.isEmpty()) {
            aviso("No se encontró ningún delivery con esos datos.");
        }
    }

    // ---------- Registro ----------

    private void llenarComboPedidos() {
        cargandoCombo = true;
        Set<Integer> conDelivery = new HashSet<>();
        for (Delivery d : datos.deliveries) {
            conDelivery.add(d.getIdPedido());
        }
        cmbPedido.removeAllItems();
        cmbPedido.addItem(new Opcion(0, "Seleccione un pedido..."));
        List<Pedido> pedidos = new ArrayList<>(datos.pedidos);
        pedidos.sort((a, b) -> Integer.compare(b.getId(), a.getId()));
        for (Pedido p : pedidos) {
            if ("CANCELADO".equalsIgnoreCase(p.getEstado()) || conDelivery.contains(p.getId())) {
                continue;
            }
            String cli = p.getCliente() == null ? "" : (nz(p.getCliente().getNombres()) + " "
                    + nz(p.getCliente().getApellidos())).trim();
            cmbPedido.addItem(new Opcion(p.getId(), "N.° " + p.getId() + " - " + cli));
        }
        cargandoCombo = false;
        lblCliente.setText(" ");
    }

    private void alElegirPedido() {
        Opcion o = (Opcion) cmbPedido.getSelectedItem();
        if (o == null || o.id == 0) {
            lblCliente.setText(" ");
            return;
        }
        Pedido pedido = null;
        for (Pedido p : datos.pedidos) {
            if (p.getId() == o.id) {
                pedido = p;
            }
        }
        Cliente c = DeliveryOperaciones.clienteDelPedido(pedido);
        if (c == null) {
            lblCliente.setText(" ");
            return;
        }
        lblCliente.setText("Teléfono: " + nz(c.getTelefono()) + (nz(c.getDistrito()).isEmpty() ? ""
                : "  ·  Distrito: " + c.getDistrito()));
        // Se precargan los datos de entrega del cliente (se pueden cambiar).
        txtDireccion.setText(nz(c.getDireccion()));
        txtReferencia.setText(nz(c.getReferencia()));
    }

    private void registrar() {
        Opcion o = (Opcion) cmbPedido.getSelectedItem();
        if (o == null || o.id == 0) {
            advertencia("Seleccione el pedido que se enviará.");
            return;
        }
        if (txtDireccion.getText().trim().isEmpty()) {
            advertencia("Ingrese la dirección de entrega.");
            return;
        }
        if (txtDireccion.getText().trim().length() > 200 || txtReferencia.getText().trim().length() > 200) {
            advertencia("La dirección y la referencia admiten hasta 200 caracteres.");
            return;
        }
        if (txtCodigoExterno.getText().trim().length() > 50) {
            advertencia("El código externo admite hasta 50 caracteres.");
            return;
        }
        double costo = ((Number) spnCosto.getValue()).doubleValue();
        if (!confirmar("¿Registrar el delivery del pedido N.° " + o.id + " por "
                + cmbPlataforma.getSelectedItem() + " con costo " + Estilo.soles(costo) + "?")) {
            return;
        }
        Resultado r = DeliveryOperaciones.registrarDelivery(o.id, (String) cmbPlataforma.getSelectedItem(),
                txtCodigoExterno.getText(), txtDireccion.getText(), txtReferencia.getText(), costo);
        if (r.ok) {
            aviso(r.mensaje);
            limpiarRegistro();
            recargar(r.id);
        } else {
            error(r.mensaje);
            recargar(null);
        }
    }

    private void limpiarRegistro() {
        cargandoCombo = true;
        if (cmbPedido.getItemCount() > 0) {
            cmbPedido.setSelectedIndex(0);
        }
        cargandoCombo = false;
        cmbPlataforma.setSelectedIndex(0);
        txtCodigoExterno.setText("");
        txtDireccion.setText("");
        txtReferencia.setText("");
        spnCosto.setValue(0.0);
        lblCliente.setText(" ");
    }

    // ---------- Estado y seguimiento ----------

    private void activarCambioDeEstado(boolean activo) {
        cmbNuevoEstado.setEnabled(activo);
        txtDescripcion.setEnabled(activo);
        btnEstado.setEnabled(activo);
        btnHistorial.setEnabled(seleccionado != null);
    }

    private void elegirDelivery(int id) {
        Delivery d = DeliveryOperaciones.buscarPorId(id);
        seleccionado = d;
        cmbNuevoEstado.removeAllItems();
        txtDescripcion.setText("");
        if (d == null) {
            lblSeleccion.setText("Seleccione un delivery de la tabla.");
            activarCambioDeEstado(false);
            return;
        }
        String[] siguientes = DeliveryOperaciones.estadosSiguientes(d.getEstado());
        for (String s : siguientes) {
            cmbNuevoEstado.addItem(s);
        }
        String texto = Estilo.escapar(d.getCodigoDelivery()) + " · Pedido N.° " + d.getIdPedido()
                + " · Estado actual: " + Estilo.escapar(bonito(d.getEstado()));
        if (siguientes.length == 0) {
            texto += "<br>Este delivery ya no admite cambios.";
        }
        lblSeleccion.setText("<html><body style='width:300px'>" + texto + "</body></html>");
        activarCambioDeEstado(siguientes.length > 0);
    }

    private void cambiarEstado() {
        if (seleccionado == null || cmbNuevoEstado.getSelectedItem() == null) {
            advertencia("Seleccione un delivery de la tabla.");
            return;
        }
        String nuevo = (String) cmbNuevoEstado.getSelectedItem();
        String msg = Delivery.CANCELADO.equals(nuevo)
                ? "¿Cancelar el delivery " + seleccionado.getCodigoDelivery() + "? Esta acción no se puede deshacer."
                : "¿Cambiar el delivery " + seleccionado.getCodigoDelivery() + " a " + bonito(nuevo) + "?";
        if (!confirmar(msg)) {
            return;
        }
        int id = seleccionado.getIdDelivery();
        Resultado r = DeliveryOperaciones.actualizarEstado(id, nuevo, txtDescripcion.getText());
        if (r.ok) {
            aviso(r.mensaje.replace('_', ' '));
        } else {
            error(r.mensaje);
        }
        recargar(id);
    }

    private void mostrarSeguimiento(int idDelivery) {
        Delivery d = DeliveryOperaciones.buscarPorId(idDelivery);
        if (d == null) {
            error("No se encontró el delivery N.° " + idDelivery + ".");
            return;
        }
        DefaultTableModel modelo = Estilo.modeloNoEditable("Fecha", "Estado", "Descripción");
        List<SeguimientoDelivery> historial = DeliveryOperaciones.listarSeguimiento(idDelivery);
        for (SeguimientoDelivery s : historial) {
            modelo.addRow(new Object[]{fechaBonita(s.getFecha()), bonito(s.getEstado()), nz(s.getDescripcion())});
        }
        JTable t = Estilo.tabla(modelo);
        t.getColumnModel().getColumn(0).setPreferredWidth(120);
        t.getColumnModel().getColumn(1).setPreferredWidth(150);
        t.getColumnModel().getColumn(2).setPreferredWidth(280);
        JScrollPane sp = Estilo.scroll(t);
        sp.setPreferredSize(new Dimension(560, 190));

        String cab = "<html><b>" + Estilo.escapar(d.getCodigoDelivery()) + "</b> · Pedido N.° " + d.getIdPedido()
                + " · " + Estilo.escapar(d.getPlataforma())
                + (nz(d.getCodigoExterno()).isEmpty() ? "" : " (" + Estilo.escapar(d.getCodigoExterno()) + ")")
                + "<br>Dirección: " + Estilo.escapar(d.getDireccionEntrega())
                + (nz(d.getReferenciaEntrega()).isEmpty() ? "" : "<br>Referencia: "
                + Estilo.escapar(d.getReferenciaEntrega()))
                + "<br>Costo: " + Estilo.soles(d.getCostoDelivery())
                + " · Estado: " + Estilo.escapar(bonito(d.getEstado())) + "</html>";
        JPanel contenido = new JPanel(new BorderLayout(0, 10));
        contenido.add(Estilo.etiqueta(cab, Estilo.FUENTE, Estilo.TEXTO), BorderLayout.NORTH);
        contenido.add(sp, BorderLayout.CENTER);
        JOptionPane.showMessageDialog(this, contenido, "Seguimiento de " + d.getCodigoDelivery(),
                JOptionPane.INFORMATION_MESSAGE);
    }
}
