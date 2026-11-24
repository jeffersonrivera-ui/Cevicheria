package vista.escritorio;

import dao.DetalleIngredienteExcluidoDAO;
import dao.DetalleBebidaDAO;
import dao.DetallePedidoDAO;
import dao.PedidoDAO;
import dao.impl.DetalleIngredienteExcluidoDAOImpl;
import dao.impl.DetalleBebidaDAOImpl;
import dao.impl.DetallePedidoDAOImpl;
import dao.impl.PedidoDAOImpl;
import modelo.DetalleBebida;
import modelo.DetallePedido;
import modelo.Ingrediente;
import modelo.Pedido;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Historial de pedidos del mesero: solo consulta de sus propios pedidos y su detalle. */
public class PanelHistorialPedidos extends ModuloBase {

    private static final DateTimeFormatter FECHA_UI = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PedidoDAO pedidoDAO = new PedidoDAOImpl();
    private final DetallePedidoDAO detalleDAO = new DetallePedidoDAOImpl();
    private final DetalleBebidaDAO bebidaDetDAO = new DetalleBebidaDAOImpl();
    private final DetalleIngredienteExcluidoDAO exclusionDAO = new DetalleIngredienteExcluidoDAOImpl();

    private final JLabel lblTotal;
    private final JLabel lblPendientes;
    private final JLabel lblEntregados;

    private final JTextField txtBuscar = new CampoHint("ID, cliente o mesero...");
    private final JComboBox<String> cmbEstado = new JComboBox<>(
            new String[]{"Todos", "PENDIENTE", "EN PREPARACION", "LISTO", "ENTREGADO", "CANCELADO"});
    private final TablaPaginada tabla = new TablaPaginada(
            new String[]{"ID", "Fecha", "Cliente", "Mesero", "Total (S/)", "Estado"}, 5, false);
    private final JEditorPane detalle = new JEditorPane("text/html", "");

    private List<Pedido> pedidos = new ArrayList<>();

    public PanelHistorialPedidos() {
        super("Historial de pedidos", "Consulta los pedidos registrados por todos los meseros", "☰", Estilo.PRIMARIO, true);

        lblTotal = agregarEstadistica("☷", new Color(0x38A3F5), "Total de pedidos", Estilo.PRIMARIO_OSCURO);
        lblPendientes = agregarEstadistica("♨", new Color(0xF97316), "En curso", new Color(0xC2410C));
        lblEntregados = agregarEstadistica("✓", Estilo.EXITO, "Entregados", new Color(0x15803D));

        cmbEstado.setFont(Estilo.FUENTE);
        cmbEstado.setPreferredSize(new Dimension(160, 36));
        JPanel extra = new JPanel(new BorderLayout(0, 4));
        extra.setOpaque(false);
        extra.add(Estilo.etiqueta("Estado:", Estilo.PEQUENA_NEGRITA, Estilo.TEXTO), BorderLayout.NORTH);
        extra.add(cmbEstado, BorderLayout.CENTER);
        JButton btnBuscar = Estilo.boton("Buscar", Estilo.PRIMARIO);
        panelIzquierdo.add(barraBusqueda("Buscar pedido:", txtBuscar, extra, btnBuscar), BorderLayout.NORTH);
        panelIzquierdo.add(tabla, BorderLayout.CENTER);

        detalle.setEditable(false);
        detalle.setOpaque(false);
        JScrollPane sp = new JScrollPane(detalle);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setPreferredSize(new Dimension(300, 360));
        panelDerecho.add(tituloDePanel("≡", "Detalle del pedido"), BorderLayout.NORTH);
        panelDerecho.add(sp, BorderLayout.CENTER);
        mostrarVacio();

        tabla.setEscucha(new TablaPaginada.Escucha() {
            @Override
            public void seleccionar(Object[] fila) {
                mostrarDetalle(((Number) fila[0]).intValue());
            }
        });
        btnBuscar.addActionListener(e -> filtrar());
        txtBuscar.addActionListener(e -> filtrar());
        cmbEstado.addActionListener(e -> filtrar());

        // Al volver a esta sección se leen de nuevo los pedidos.
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                cargar();
            }
        });
        cargar();
    }

    private static String dos(double v) {
        return String.format(Locale.US, "%.2f", v);
    }

    private static String esc(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String nombre(Pedido p) {
        if (p.getCliente() == null) {
            return "";
        }
        String ap = p.getCliente().getApellidos() == null ? "" : p.getCliente().getApellidos().trim();
        return (p.getCliente().getNombres() + ("-".equals(ap) || ap.isEmpty() ? "" : " " + ap)).trim();
    }

    private static String nombreMesero(Pedido p) {
        return p.getMesero() == null ? "" : (p.getMesero().getNombres() + " " + p.getMesero().getApellidos()).trim();
    }

    private static String fecha(String iso) {
        try {
            return LocalDate.parse(iso.substring(0, 10)).format(FECHA_UI);
        } catch (Exception e) {
            return iso == null ? "" : iso;
        }
    }

    private void cargar() {
        try {
            pedidos = new ArrayList<>(pedidoDAO.listar());
            pedidos.sort(Comparator.comparingInt(Pedido::getId).reversed());
            int enCurso = 0;
            int entregados = 0;
            for (Pedido p : pedidos) {
                String e = String.valueOf(p.getEstado()).toUpperCase();
                if ("ENTREGADO".equals(e)) {
                    entregados++;
                } else if (!"CANCELADO".equals(e)) {
                    enCurso++;
                }
            }
            lblTotal.setText(String.valueOf(pedidos.size()));
            lblPendientes.setText(String.valueOf(enCurso));
            lblEntregados.setText(String.valueOf(entregados));
            filtrar();
        } catch (Exception ex) {
            error("No se pudieron cargar los pedidos: " + ex.getMessage());
        }
    }

    private void filtrar() {
        String t = txtBuscar.getText().trim().toLowerCase();
        String estado = (String) cmbEstado.getSelectedItem();
        List<Object[]> filas = new ArrayList<>();
        for (Pedido p : pedidos) {
            boolean okTexto = t.isEmpty() || String.valueOf(p.getId()).equals(t) || nombre(p).toLowerCase().contains(t)
                    || nombreMesero(p).toLowerCase().contains(t);
            boolean okEstado = "Todos".equals(estado) || estado.equalsIgnoreCase(p.getEstado());
            if (okTexto && okEstado) {
                filas.add(new Object[]{p.getId(), fecha(p.getFecha()), nombre(p), nombreMesero(p),
                        dos(detalleDAO.calcularTotal(p.getId())), p.getEstado()});
            }
        }
        tabla.setFilas(filas);
    }

    private void mostrarVacio() {
        detalle.setText("<html><body style='font-family:Segoe UI;font-size:12px;color:#64748B'>"
                + "Seleccione un pedido de la tabla para ver sus platos.</body></html>");
    }

    private void mostrarDetalle(int idPedido) {
        Pedido ped = null;
        for (Pedido p : pedidos) {
            if (p.getId() == idPedido) {
                ped = p;
            }
        }
        if (ped == null) {
            mostrarVacio();
            return;
        }
        StringBuilder h = new StringBuilder("<html><body style='font-family:Segoe UI;font-size:12px;color:#0F2A5C'>");
        h.append("<p><b>Pedido N.° ").append(ped.getId()).append("</b><br>Cliente: ").append(esc(nombre(ped)))
                .append("<br>Mesero: ").append(esc(nombreMesero(ped))).append("<br>Fecha: ").append(fecha(ped.getFecha()))
                .append("<br>Estado: <b>").append(esc(ped.getEstado())).append("</b></p><hr>");
        List<DetallePedido> detalles = detalleDAO.listarPorPedido(idPedido);
        double total = 0;
        if (detalles.isEmpty()) {
            h.append("<p>Este pedido no tiene platos registrados.</p>");
        }
        for (DetallePedido d : detalles) {
            double sub = d.getCantidad() * d.getPrecio();
            total += sub;
            h.append("<p><b>").append(d.getCantidad()).append(" x ").append(esc(d.getPlato().getNombre()))
                    .append("</b> &nbsp; S/ ").append(dos(sub));
            List<String> sin = new ArrayList<>();
            for (Ingrediente i : exclusionDAO.listarPorDetalle(d.getId())) {
                sin.add(i.getNombre());
            }
            if (!sin.isEmpty()) {
                h.append("<br><font color='#B45309'>Sin: ").append(esc(String.join(", ", sin))).append("</font>");
            }
            if (d.getObservacion() != null && !d.getObservacion().trim().isEmpty()) {
                h.append("<br><font color='#B45309'>").append(esc(d.getObservacion())).append("</font>");
            }
            h.append("</p>");
        }
        for (DetalleBebida b : bebidaDetDAO.listarPorPedido(idPedido)) {
            total += b.calcularSubtotal();
            h.append("<p><b>").append(b.getCantidad()).append(" x ").append(esc(b.getBebida().getNombre()))
                    .append("</b> (bebida) &nbsp; S/ ").append(dos(b.calcularSubtotal())).append("</p>");
        }
        h.append("<hr><p><b>Total: S/ ").append(dos(total)).append("</b></p></body></html>");
        detalle.setText(h.toString());
        detalle.setCaretPosition(0);
    }
}
