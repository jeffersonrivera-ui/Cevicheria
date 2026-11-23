package vista.escritorio;

import controlador.PedidosOperaciones;
import dao.DetalleIngredienteExcluidoDAO;
import dao.DetallePedidoDAO;
import dao.PedidoDAO;
import dao.impl.DetalleIngredienteExcluidoDAOImpl;
import dao.impl.DetallePedidoDAOImpl;
import dao.impl.PedidoDAOImpl;
import modelo.DetallePedido;
import modelo.Ingrediente;
import modelo.Pedido;
import modelo.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class VentanaCocinero extends JFrame {

    private final PedidoDAO pedidoDAO = new PedidoDAOImpl();
    private final DetallePedidoDAO detalleDAO = new DetallePedidoDAOImpl();
    private final DetalleIngredienteExcluidoDAO exclusionDAO = new DetalleIngredienteExcluidoDAOImpl();

    private final JPanel rejilla = new JPanel(new GridLayout(0, 3, 14, 14));
    private final JLabel lblPendientes = Estilo.etiqueta("0", new Font("Segoe UI", Font.BOLD, 28), new Color(0xF97316));
    private final JLabel lblPreparacion = Estilo.etiqueta("0", new Font("Segoe UI", Font.BOLD, 28), Estilo.PRIMARIO);
    private final JLabel lblListos = Estilo.etiqueta("0", new Font("Segoe UI", Font.BOLD, 28), Estilo.EXITO);
    private final JLabel lblHora = Estilo.etiqueta("", Estilo.PEQUENA, Estilo.TEXTO_SUAVE);
    private Estilo.Tarjeta tarjetaPendientes;
    private Estilo.Tarjeta tarjetaPreparacion;
    private Estilo.Tarjeta tarjetaListos;
    private Timer temporizador;
    private String filtro = "TODOS";

    public VentanaCocinero(Usuario usuario) {
        super("Cevichería | Cocina");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1000, 640));

        JPanel raiz = new JPanel(new BorderLayout(0, 12));
        raiz.setBackground(Estilo.FONDO);
        raiz.setBorder(new EmptyBorder(16, 22, 18, 22));

        // Cabecera
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setOpaque(false);

        JPanel titulo = new JPanel(new BorderLayout(14, 0));
        titulo.setOpaque(false);
        titulo.add(new Estilo.CajaIcono("\u2668", Estilo.PRIMARIO, Color.WHITE, 60), BorderLayout.WEST);
        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(Box.createVerticalGlue());
        textos.add(Estilo.etiqueta("Pedidos de Cocina", Estilo.TITULO, Estilo.PRIMARIO_OSCURO));
        textos.add(Estilo.etiqueta("Prepara los pedidos y actualiza su estado", Estilo.FUENTE, Estilo.TEXTO_SUAVE));
        textos.add(Box.createVerticalGlue());
        titulo.add(textos, BorderLayout.CENTER);
        cabecera.add(titulo, BorderLayout.WEST);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        acciones.setOpaque(false);
        JButton btnActualizar = Estilo.boton("Actualizar", Estilo.PRIMARIO);
        JButton btnSalir = Estilo.boton("Cerrar sesión", Estilo.PELIGRO);
        JLabel lblUsuario = Estilo.etiqueta("\u263B  " + usuario.getNombreUsuario() + " (Cocinero)",
                Estilo.NEGRITA, Estilo.PRIMARIO_OSCURO);
        acciones.add(lblUsuario);
        acciones.add(lblHora);
        acciones.add(btnActualizar);
        acciones.add(btnSalir);
        JPanel contAcciones = new JPanel(new GridBagLayout());
        contAcciones.setOpaque(false);
        contAcciones.add(acciones);
        cabecera.add(contAcciones, BorderLayout.EAST);

        // Estadisticas (sirven tambien como filtro)
        JPanel estadisticas = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        estadisticas.setOpaque(false);
        tarjetaPendientes = crearTarjetaEstado("\u25F4", new Color(0xFBBF24), "Pendientes", lblPendientes, "PENDIENTE");
        tarjetaPreparacion = crearTarjetaEstado("\u2668", new Color(0x2F80ED), "En preparación", lblPreparacion, "EN PREPARACION");
        tarjetaListos = crearTarjetaEstado("\u2713", new Color(0x22C55E), "Listos", lblListos, "LISTO");
        estadisticas.add(tarjetaPendientes);
        estadisticas.add(tarjetaPreparacion);
        estadisticas.add(tarjetaListos);
        estadisticas.add(Estilo.etiqueta("Toca una tarjeta para filtrar; tócala de nuevo para ver todos.",
                Estilo.PEQUENA, Estilo.TEXTO_SUAVE));

        JPanel norte = new JPanel(new BorderLayout(0, 14));
        norte.setOpaque(false);
        norte.add(cabecera, BorderLayout.NORTH);
        norte.add(estadisticas, BorderLayout.CENTER);

        // Zona de pedidos
        rejilla.setOpaque(false);
        JPanel contenedorRejilla = new JPanel(new BorderLayout());
        contenedorRejilla.setOpaque(false);
        contenedorRejilla.add(rejilla, BorderLayout.NORTH);

        Estilo.Tarjeta zona = new Estilo.Tarjeta(new BorderLayout(), 22);
        zona.setBorder(new EmptyBorder(10, 10, 10, 10));
        JScrollPane sp = new JScrollPane(contenedorRejilla);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setUnitIncrement(18);
        zona.add(sp, BorderLayout.CENTER);

        raiz.add(norte, BorderLayout.NORTH);
        raiz.add(zona, BorderLayout.CENTER);
        setContentPane(raiz);

        btnActualizar.addActionListener(e -> cargar());
        btnSalir.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(this, "¿Desea cerrar la sesión?",
                    "Cerrar sesión", JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) {
                temporizador.stop();
                dispose();
                new VentanaLogin().setVisible(true);
            }
        });

        temporizador = new Timer(15000, e -> cargar());
        temporizador.start();
        cargar();
    }

    private Estilo.Tarjeta crearTarjetaEstado(String glifo, Color color, String titulo, JLabel valor, String estado) {
        Estilo.Tarjeta t = new Estilo.Tarjeta(new BorderLayout(10, 0));
        t.setBorder(new EmptyBorder(10, 14, 10, 22));
        t.setPreferredSize(new Dimension(220, 76));
        t.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        t.add(new Estilo.CajaIcono(glifo, color, Color.WHITE, 46), BorderLayout.WEST);
        JPanel c = new JPanel();
        c.setOpaque(false);
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.add(Estilo.etiqueta(titulo, Estilo.NEGRITA, Estilo.PRIMARIO_OSCURO));
        c.add(valor);
        t.add(c, BorderLayout.CENTER);
        t.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                filtro = filtro.equals(estado) ? "TODOS" : estado;
                cargar();
            }
        });
        return t;
    }

    private void cargar() {
        try {
            List<Pedido> pedidos = pedidoDAO.listar();
            List<Pedido> visibles = new ArrayList<>();
            int pendientes = 0;
            int preparacion = 0;
            int listos = 0;

            for (Pedido p : pedidos) {
                String estado = estadoDe(p);
                if (estado.equals("PENDIENTE")) {
                    pendientes++;
                } else if (estado.equals("EN PREPARACION")) {
                    preparacion++;
                } else if (estado.equals("LISTO")) {
                    listos++;
                } else {
                    continue;
                }
                if (filtro.equals("TODOS") || filtro.equals(estado)) {
                    visibles.add(p);
                }
            }

            lblPendientes.setText(String.valueOf(pendientes));
            lblPreparacion.setText(String.valueOf(preparacion));
            lblListos.setText(String.valueOf(listos));

            rejilla.removeAll();
            for (int i = visibles.size() - 1; i >= 0; i--) {
                rejilla.add(crearTarjetaPedido(visibles.get(i)));
            }
            // Relleno para que las tarjetas no se estiren cuando hay pocas
            while (rejilla.getComponentCount() < 3) {
                JPanel vacio = new JPanel();
                vacio.setOpaque(false);
                rejilla.add(vacio);
            }
            rejilla.revalidate();
            rejilla.repaint();
            lblHora.setText("Actualizado: " + java.time.LocalTime.now().withNano(0));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudieron cargar los pedidos: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String estadoDe(Pedido p) {
        return p.getEstado() == null ? "" : p.getEstado().trim().toUpperCase();
    }

    private JPanel crearTarjetaPedido(Pedido pedido) {
        String estado = estadoDe(pedido);

        Estilo.Tarjeta t = new Estilo.Tarjeta(new BorderLayout(0, 8), 16);
        t.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Encabezado: numero + estado
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);
        encabezado.add(Estilo.etiqueta("\u2637  Pedido N.º " + pedido.getId(), Estilo.SUBTITULO, Estilo.PRIMARIO_OSCURO),
                BorderLayout.WEST);
        encabezado.add(new Estilo.Pastilla(estado), BorderLayout.EAST);
        t.add(encabezado, BorderLayout.NORTH);

        // Cuerpo
        JPanel cuerpo = new JPanel();
        cuerpo.setOpaque(false);
        cuerpo.setLayout(new BoxLayout(cuerpo, BoxLayout.Y_AXIS));

        String cliente = pedido.getCliente() == null ? "" : (pedido.getCliente().getNombres() + " "
                + (pedido.getCliente().getApellidos() == null ? "" : pedido.getCliente().getApellidos()));
        String mesero = pedido.getMesero() == null ? "" : pedido.getMesero().getNombres();
        String fecha = pedido.getFecha() == null ? "" : pedido.getFecha();
        String hora = fecha.length() >= 16 ? fecha.substring(11, 16) : fecha;
        String dia = fecha.length() >= 10 ? fecha.substring(0, 10) : "";

        JPanel info = new JPanel(new GridLayout(2, 2, 6, 2));
        info.setBackground(new Color(0xEEF4FD));
        info.setBorder(new EmptyBorder(6, 10, 6, 10));
        info.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        info.add(Estilo.etiqueta("\u263A  " + cliente.trim(), Estilo.NEGRITA, Estilo.TEXTO));
        info.add(Estilo.etiqueta("\u25F7  " + hora, Estilo.NEGRITA, Estilo.PRIMARIO_OSCURO));
        info.add(Estilo.etiqueta("Atiende: " + mesero, Estilo.PEQUENA, Estilo.TEXTO_SUAVE));
        info.add(Estilo.etiqueta(dia, Estilo.PEQUENA, Estilo.TEXTO_SUAVE));
        cuerpo.add(info);
        cuerpo.add(Box.createVerticalStrut(6));

        try {
            List<DetallePedido> detalles = detalleDAO.listarPorPedido(pedido.getId());
            if (detalles.isEmpty()) {
                cuerpo.add(Estilo.etiqueta("Sin platos registrados", Estilo.PEQUENA, Estilo.TEXTO_SUAVE));
            }
            for (DetallePedido d : detalles) {
                cuerpo.add(crearLineaPlato(d));
                cuerpo.add(Box.createVerticalStrut(4));
            }
        } catch (Exception ex) {
            cuerpo.add(Estilo.etiqueta("No se pudo cargar el detalle", Estilo.PEQUENA, Estilo.PELIGRO));
        }
        t.add(cuerpo, BorderLayout.CENTER);

        // Botones
        JPanel pie = new JPanel(new GridLayout(1, 2, 8, 0));
        pie.setOpaque(false);
        if (estado.equals("PENDIENTE")) {
            JButton iniciar = Estilo.boton("\u25B6  Iniciar preparación", Estilo.PRIMARIO);
            JButton listo = Estilo.boton("\u2713  Marcar como listo", Estilo.EXITO);
            iniciar.addActionListener(e -> cambiarEstado(pedido.getId(), "EN PREPARACION"));
            listo.addActionListener(e -> cambiarEstado(pedido.getId(), "LISTO"));
            pie.add(iniciar);
            pie.add(listo);
        } else if (estado.equals("EN PREPARACION")) {
            JButton enCurso = Estilo.boton("En preparación...", Estilo.GRIS);
            enCurso.setEnabled(false);
            JButton listo = Estilo.boton("\u2713  Marcar como listo", Estilo.EXITO);
            listo.addActionListener(e -> cambiarEstado(pedido.getId(), "LISTO"));
            pie.add(enCurso);
            pie.add(listo);
        } else {
            JLabel completado = Estilo.etiqueta("\u2713  Pedido completado", Estilo.NEGRITA, new Color(0x15803D));
            completado.setHorizontalAlignment(SwingConstants.CENTER);
            completado.setOpaque(true);
            completado.setBackground(new Color(0xDCFCE7));
            completado.setBorder(new EmptyBorder(9, 0, 9, 0));
            pie = new JPanel(new BorderLayout());
            pie.setOpaque(false);
            pie.add(completado, BorderLayout.CENTER);
        }
        t.add(pie, BorderLayout.SOUTH);
        return t;
    }

    private JPanel crearLineaPlato(DetallePedido d) {
        JPanel linea = new JPanel(new BorderLayout(8, 0));
        linea.setOpaque(false);
        linea.setAlignmentX(Component.LEFT_ALIGNMENT);

        linea.add(Estilo.etiqueta("\u2668  " + d.getPlato().getNombre() + " (" + d.getCantidad() + ")",
                Estilo.NEGRITA, Estilo.TEXTO), BorderLayout.CENTER);

        JPanel etiquetas = new JPanel();
        etiquetas.setOpaque(false);
        etiquetas.setLayout(new BoxLayout(etiquetas, BoxLayout.Y_AXIS));

        boolean algo = false;
        try {
            List<Ingrediente> excluidos = exclusionDAO.listarPorDetalle(d.getId());
            if (excluidos != null && !excluidos.isEmpty()) {
                StringBuilder sb = new StringBuilder("Sin ");
                for (int i = 0; i < excluidos.size(); i++) {
                    if (i > 0) {
                        sb.append(", ");
                    }
                    sb.append(excluidos.get(i).getNombre().toLowerCase());
                }
                etiquetas.add(pastillaAlineada("\u2298 " + sb, new Color(0xFEE2E2), new Color(0xDC2626)));
                algo = true;
            }
        } catch (Exception ignorada) {
        }

        String obs = d.getObservacion();
        if (obs != null && !obs.trim().isEmpty()) {
            etiquetas.add(Box.createVerticalStrut(3));
            etiquetas.add(pastillaAlineada("\u270E " + obs.trim(), new Color(0xFEF3C7), new Color(0xB45309)));
            algo = true;
        }
        if (!algo) {
            etiquetas.add(pastillaAlineada("\u2713 Sin observaciones", new Color(0xDCFCE7), new Color(0x15803D)));
        }
        linea.add(etiquetas, BorderLayout.EAST);
        return linea;
    }

    private JComponent pastillaAlineada(String texto, Color fondo, Color letra) {
        String mostrado = texto.length() > 34 ? texto.substring(0, 33) + "…" : texto;
        Estilo.Pastilla p = new Estilo.Pastilla();
        p.poner(mostrado, fondo, letra);
        p.setToolTipText(texto);
        JPanel c = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        c.setOpaque(false);
        c.add(p);
        return c;
    }

    private void cambiarEstado(int idPedido, String nuevoEstado) {
        Pedido actual = PedidosOperaciones.buscarPedidoObjeto(idPedido);
        if (actual == null) {
            JOptionPane.showMessageDialog(this, "El pedido ya no existe.", "Atención", JOptionPane.WARNING_MESSAGE);
            cargar();
            return;
        }

        String texto = nuevoEstado.equals("LISTO") ? "listo" : "en preparación";
        int r = JOptionPane.showConfirmDialog(this,
                "¿Marcar el pedido N.º " + idPedido + " como " + texto + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (r != JOptionPane.YES_OPTION) {
            return;
        }

        // Un pedido pendiente debe pasar primero por "EN PREPARACION" antes de quedar listo.
        if (nuevoEstado.equals("LISTO") && estadoDe(actual).equals("PENDIENTE")) {
            PedidosOperaciones.actualizarEstadoPedido(idPedido, "EN PREPARACION");
        }
        PedidosOperaciones.actualizarEstadoPedido(idPedido, nuevoEstado);

        Pedido despues = PedidosOperaciones.buscarPedidoObjeto(idPedido);
        if (despues == null || !nuevoEstado.equalsIgnoreCase(despues.getEstado())) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo cambiar el estado. Revise que haya stock suficiente de los ingredientes del pedido.",
                    "No se actualizó", JOptionPane.ERROR_MESSAGE);
        }
        cargar();
    }
}
