package vista.escritorio;

import controlador.PedidosOperaciones;
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

/**
 * Lista temporal para el mesero: pedidos que cocina ya dejó LISTO y que hay que llevar a la mesa.
 * Al marcarlos como entregados salen de esta lista.
 */
public class PanelPedidosListos extends ModuloBase {

    private static final DateTimeFormatter FECHA_UI = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PedidoDAO pedidoDAO = new PedidoDAOImpl();
    private final DetallePedidoDAO detalleDAO = new DetallePedidoDAOImpl();
    private final DetalleBebidaDAO bebidaDetDAO = new DetalleBebidaDAOImpl();
    private final DetalleIngredienteExcluidoDAO exclusionDAO = new DetalleIngredienteExcluidoDAOImpl();

    private final JLabel lblListos;
    private final TablaPaginada tabla = new TablaPaginada(
            new String[]{"ID", "Cliente", "Mesero", "Platos", "Total (S/)", "Estado"}, 5, false);
    private final JEditorPane detalle = new JEditorPane("text/html", "");
    private final JButton btnEntregar = Estilo.boton("Marcar como entregado", Estilo.EXITO);

    private List<Pedido> listos = new ArrayList<>();
    private int idSeleccionado = 0;
    private javax.swing.Timer reloj;

    public PanelPedidosListos() {
        super("Pedidos listos", "Pedidos que cocina ya terminó y deben llevarse a la mesa", "✓",
                Estilo.EXITO, true);

        lblListos = agregarEstadistica("✓", Estilo.EXITO, "Listos para llevar", new Color(0x15803D));
        setAnchoDerecho(380);

        JButton btnActualizar = Estilo.boton("Actualizar", Estilo.PRIMARIO);
        JPanel barra = new JPanel(new BorderLayout(12, 0));
        barra.setOpaque(false);
        barra.add(Estilo.etiqueta("Se actualiza solo cada 10 segundos. Al entregar un pedido sale de esta lista.",
                Estilo.PEQUENA, Estilo.TEXTO_SUAVE), BorderLayout.CENTER);
        btnActualizar.setPreferredSize(new Dimension(120, 34));
        barra.add(btnActualizar, BorderLayout.EAST);
        panelIzquierdo.add(barra, BorderLayout.NORTH);
        panelIzquierdo.add(tabla, BorderLayout.CENTER);

        detalle.setEditable(false);
        detalle.setOpaque(false);
        JScrollPane sp = new JScrollPane(detalle);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setPreferredSize(new Dimension(300, 360));
        JPanel centro = new JPanel(new BorderLayout(0, 12));
        centro.setOpaque(false);
        centro.add(sp, BorderLayout.CENTER);
        btnEntregar.setPreferredSize(new Dimension(120, 40));
        centro.add(btnEntregar, BorderLayout.SOUTH);
        panelDerecho.add(tituloDePanel("≡", "Detalle del pedido"), BorderLayout.NORTH);
        panelDerecho.add(centro, BorderLayout.CENTER);
        mostrarVacio();

        tabla.setEscucha(new TablaPaginada.Escucha() {
            @Override
            public void seleccionar(Object[] fila) {
                idSeleccionado = ((Number) fila[0]).intValue();
                mostrarDetalle(idSeleccionado);
            }
        });
        btnActualizar.addActionListener(e -> cargar());
        btnEntregar.addActionListener(e -> entregar());

        // Se lee de nuevo al entrar y cada 10 s mientras la sección está a la vista.
        reloj = new javax.swing.Timer(10000, e -> {
            if (isShowing()) {
                cargar();
            }
        });
        reloj.start();
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                cargar();
            }
        });
        cargar();
    }

    @Override
    protected boolean tieneBotonActualizarPropio() {
        return true;
    }

    @Override
    public void removeNotify() {
        if (reloj != null) {
            reloj.stop();
        }
        super.removeNotify();
    }

    private static String dos(double v) {
        return String.format(Locale.US, "%.2f", v);
    }

    private static String esc(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String nombreCliente(Pedido p) {
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
            listos = new ArrayList<>(pedidoDAO.listar());
            listos.removeIf(p -> !"LISTO".equalsIgnoreCase(p.getEstado()));
            listos.sort(Comparator.comparingInt(Pedido::getId));
            lblListos.setText(String.valueOf(listos.size()));

            List<Object[]> filas = new ArrayList<>();
            for (Pedido p : listos) {
                List<DetallePedido> det = detalleDAO.listarPorPedido(p.getId());
                StringBuilder platos = new StringBuilder();
                double total = 0;
                for (DetallePedido d : det) {
                    if (platos.length() > 0) {
                        platos.append(", ");
                    }
                    platos.append(d.getCantidad()).append(" ").append(d.getPlato().getNombre());
                    total += d.getCantidad() * d.getPrecio();
                }
                for (DetalleBebida b : bebidaDetDAO.listarPorPedido(p.getId())) {
                    if (platos.length() > 0) {
                        platos.append(", ");
                    }
                    platos.append(b.getCantidad()).append(" ").append(b.getBebida().getNombre());
                    total += b.calcularSubtotal();
                }
                filas.add(new Object[]{p.getId(), nombreCliente(p), nombreMesero(p), platos.toString(),
                        dos(total), "LISTO"});
            }
            tabla.setFilas(filas);

            boolean sigue = false;
            for (Pedido p : listos) {
                sigue |= p.getId() == idSeleccionado;
            }
            if (!sigue) {
                idSeleccionado = 0;
                mostrarVacio();
            }
        } catch (Exception ex) {
            error("No se pudieron cargar los pedidos listos: " + ex.getMessage());
        }
    }

    private void entregar() {
        if (idSeleccionado <= 0) {
            advertencia("Seleccione un pedido de la lista para marcarlo como entregado.");
            return;
        }
        if (!confirmar("¿Marcar el pedido N.° " + idSeleccionado + " como entregado?\nSaldrá de esta lista.")) {
            return;
        }
        int id = idSeleccionado;
        PedidosOperaciones.actualizarEstadoPedido(id, "ENTREGADO");
        Pedido despues = PedidosOperaciones.buscarPedidoObjeto(id);
        if (despues == null || !"ENTREGADO".equalsIgnoreCase(despues.getEstado())) {
            error("No se pudo marcar el pedido como entregado.");
        } else {
            aviso("Pedido N.° " + id + " entregado.");
            idSeleccionado = 0;
            tabla.limpiarSeleccion();
        }
        cargar();
    }

    private void mostrarVacio() {
        detalle.setText("<html><body style='font-family:Segoe UI;font-size:12px;color:#64748B'>"
                + "Seleccione un pedido para ver qué platos debe llevar.</body></html>");
    }

    private void mostrarDetalle(int idPedido) {
        Pedido ped = null;
        for (Pedido p : listos) {
            if (p.getId() == idPedido) {
                ped = p;
            }
        }
        if (ped == null) {
            mostrarVacio();
            return;
        }
        StringBuilder h = new StringBuilder("<html><body style='font-family:Segoe UI;font-size:12px;color:#0F2A5C'>");
        h.append("<p><b>Pedido N.° ").append(ped.getId()).append("</b><br>Cliente: ").append(esc(nombreCliente(ped)))
                .append("<br>Mesero: ").append(esc(nombreMesero(ped)))
                .append("<br>Fecha: ").append(fecha(ped.getFecha())).append("</p><hr>");
        for (DetallePedido d : detalleDAO.listarPorPedido(idPedido)) {
            h.append("<p><b>").append(d.getCantidad()).append(" x ").append(esc(d.getPlato().getNombre()))
                    .append("</b>");
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
            h.append("<p><b>").append(b.getCantidad()).append(" x ").append(esc(b.getBebida().getNombre()))
                    .append("</b> (bebida)</p>");
        }
        h.append("</body></html>");
        detalle.setText(h.toString());
        detalle.setCaretPosition(0);
    }
}
