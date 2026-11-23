package vista.escritorio;

import controlador.ComprasOperaciones;
import dao.CompraDAO;
import dao.IngredientesDAO;
import dao.impl.CompraDAOImpl;
import dao.impl.IngredientesDAOImpl;
import modelo.Ingrediente;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Sección Compras: registro de compras de ingredientes e historial de abastecimiento (administrador). */
public class PanelCompras extends ModuloBase {

    private static final DateTimeFormatter FECHA_UI =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    // Elemento de combo con id (id 0 = texto "Seleccione...").
    private static final class Opcion {
        final int id;
        final String texto;
        final String unidad;

        Opcion(int id, String texto, String unidad) {
            this.id = id;
            this.texto = texto;
            this.unidad = unidad;
        }

        @Override
        public String toString() {
            return texto;
        }
    }

    private final CompraDAO compraDAO = new CompraDAOImpl();
    private final IngredientesDAO ingredienteDAO = new IngredientesDAOImpl();

    private List<Map<String, Object>> historial = new ArrayList<>();

    private final JLabel lblTotalCompras;
    private final JLabel lblIngredientes;
    private final JLabel lblInvertido;

    private final JTextField txtId = new CampoHint("Ingrese el ID de compra...");
    private final JTextField txtFecha = new CampoHint("dd/mm/aaaa");
    private final TablaPaginada tabla = new TablaPaginada(
            new String[]{"ID compra", "Fecha", "Ingrediente", "Cantidad", "Precio unit. (S/)", "Total (S/)"}, 5, true);

    private final JComboBox<Opcion> cmbIngrediente = new JComboBox<>();
    private final JSpinner spnCantidad = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 1000000.0, 0.5));
    private final JSpinner spnPrecio = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 1000000.0, 0.5));
    private final JLabel lblUnidad = Estilo.etiqueta("", Estilo.PEQUENA, Estilo.TEXTO_SUAVE);
    private final JLabel lblTotal = Estilo.etiqueta("0.00", Estilo.SUBTITULO, new Color(0x15803D));

    public PanelCompras() {
        super("Compras", "Registra compras y consulta el historial de abastecimiento",
                "🛒", Estilo.PRIMARIO, true);

        lblTotalCompras = agregarEstadistica("☷", new Color(0x38A3F5), "Total de compras", Estilo.PRIMARIO_OSCURO);
        lblIngredientes = agregarEstadistica("♨", Estilo.EXITO, "Ingredientes comprados", new Color(0x15803D));
        lblInvertido = agregarEstadistica("S/", Estilo.PELIGRO, "Total invertido", Estilo.PELIGRO);
        setAnchoEstadisticas(650);

        // ----- Izquierda -----
        txtId.setPreferredSize(new Dimension(100, 36));
        txtId.setMinimumSize(new Dimension(100, 36));
        txtFecha.setPreferredSize(new Dimension(115, 36));
        JPanel colFecha = new JPanel(new BorderLayout(0, 4));
        colFecha.setOpaque(false);
        colFecha.add(Estilo.etiqueta("Fecha de compra:", Estilo.PEQUENA_NEGRITA, Estilo.TEXTO), BorderLayout.NORTH);
        colFecha.add(txtFecha, BorderLayout.CENTER);
        colFecha.setPreferredSize(new Dimension(115, 58));
        JPanel extra = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        extra.setOpaque(false);
        extra.add(colFecha);
        extra.setPreferredSize(new Dimension(115, 58));

        JButton btnBuscar = Estilo.boton("Buscar", Estilo.PRIMARIO);
        JPanel arriba = new JPanel(new BorderLayout(0, 10));
        arriba.setOpaque(false);
        arriba.add(barraBusqueda("Buscar por ID de compra:", txtId, extra, btnBuscar), BorderLayout.NORTH);
        arriba.add(tituloDePanel("≡", "Historial de compras"), BorderLayout.CENTER);
        panelIzquierdo.add(arriba, BorderLayout.NORTH);

        tabla.setAnchos(50, 80, 110, 80, 80, 80, 60);
        tabla.setTituloAcciones("Detalle");
        tabla.setAcciones(new TablaPaginada.Accion("◉", new Color(0x1E6FEB), Color.WHITE, this::verDetalle));
        panelIzquierdo.add(tabla, BorderLayout.CENTER);

        // ----- Derecha -----
        cmbIngrediente.setFont(Estilo.FUENTE);
        cmbIngrediente.setPreferredSize(new Dimension(120, 36));
        for (JSpinner s : new JSpinner[]{spnCantidad, spnPrecio}) {
            s.setFont(Estilo.FUENTE);
            s.setPreferredSize(new Dimension(100, 36));
            s.setEditor(new JSpinner.NumberEditor(s, "0.00"));
        }

        JPanel cantidadPrecio = new JPanel(new GridLayout(1, 2, 12, 0));
        cantidadPrecio.setOpaque(false);
        cantidadPrecio.add(conEtiqueta("Cantidad:", true, spnCantidad));
        cantidadPrecio.add(conEtiqueta("Precio unitario (S/):", true, spnPrecio));

        JPanel barraTotal = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8)) {
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
        barraTotal.setOpaque(false);
        barraTotal.add(Estilo.etiqueta("S/", Estilo.SUBTITULO, Estilo.PRIMARIO));
        barraTotal.add(Estilo.etiqueta("Total (S/):", Estilo.NEGRITA, Estilo.PRIMARIO_OSCURO));
        barraTotal.add(lblTotal);
        barraTotal.setPreferredSize(new Dimension(300, 46));

        JButton btnRegistrar = Estilo.boton("+  Registrar compra", Estilo.EXITO);

        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.add(alinear(tituloDePanel("+", "Registrar compra")));
        col.add(Box.createVerticalStrut(10));
        col.add(alinear(conEtiqueta("Ingrediente:", true, cmbIngrediente)));
        col.add(alinear(lblUnidad));
        col.add(Box.createVerticalStrut(6));
        col.add(alinear(cantidadPrecio));
        col.add(Box.createVerticalStrut(10));
        col.add(alinear(barraTotal));
        col.add(Box.createVerticalStrut(12));
        col.add(alinear(btnRegistrar));
        col.add(Box.createVerticalStrut(12));
        col.add(alinear(Estilo.aviso("Al registrar una compra, el stock del ingrediente aumenta automáticamente.",
                "info", 210)));
        col.add(Box.createVerticalStrut(8));
        col.add(alinear(Estilo.aviso("Verifica la cantidad y el precio antes de registrar. Las compras registradas "
                + "no se pueden editar ni eliminar; quedan en el historial.", "alerta", 210)));
        col.add(Box.createVerticalStrut(8));
        col.add(alinear(Estilo.aviso("Este módulo permite registrar una compra de un ingrediente a la vez.",
                "info", 210)));
        panelDerecho.add(col, BorderLayout.NORTH);
        setAnchoDerecho(370);

        // ----- Eventos -----
        btnBuscar.addActionListener(e -> aplicarFiltros());
        txtId.addActionListener(e -> aplicarFiltros());
        txtFecha.addActionListener(e -> aplicarFiltros());
        btnRegistrar.addActionListener(e -> registrar());
        spnCantidad.addChangeListener(e -> recalcular());
        spnPrecio.addChangeListener(e -> recalcular());
        cmbIngrediente.addActionListener(e -> {
            Opcion o = (Opcion) cmbIngrediente.getSelectedItem();
            lblUnidad.setText(o == null || o.id <= 0 || o.unidad.isEmpty() ? " "
                    : "Unidad de medida: " + o.unidad);
        });

        cargarIngredientes();
        cargar();
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

    private static double num(Object o) {
        return o instanceof Number ? ((Number) o).doubleValue() : 0;
    }

    private static int entero(Object o) {
        return o instanceof Number ? ((Number) o).intValue() : 0;
    }

    private static String dos(double v) {
        return String.format(Locale.US, "%.2f", v);
    }

    // La fecha puede venir con hora; solo interesa el día.
    private static String diaIso(Object fecha) {
        String f = fecha == null ? "" : fecha.toString();
        return f.length() >= 10 ? f.substring(0, 10) : f;
    }

    private static String fechaBonita(Object fecha) {
        try {
            return LocalDate.parse(diaIso(fecha)).format(FECHA_UI);
        } catch (Exception e) {
            return fecha == null ? "" : fecha.toString();
        }
    }

    private void cargarIngredientes() {
        cmbIngrediente.removeAllItems();
        cmbIngrediente.addItem(new Opcion(0, "Seleccione un ingrediente...", ""));
        try {
            for (Ingrediente i : ingredienteDAO.listar()) {
                cmbIngrediente.addItem(new Opcion(i.getId(), i.getNombre(), nz(i.getUnidad())));
            }
        } catch (Exception ex) {
            error("No se pudieron cargar los ingredientes: " + ex.getMessage());
        }
    }

    private void cargar() {
        try {
            historial = compraDAO.listarCompras();
        } catch (Exception ex) {
            error("No se pudo cargar el historial de compras: " + ex.getMessage());
            return;
        }
        Set<Integer> compras = new HashSet<>();
        Set<Integer> ingredientes = new HashSet<>();
        double invertido = 0;
        for (Map<String, Object> c : historial) {
            if (compras.add(entero(c.get("id_compra")))) {
                invertido += num(c.get("total"));
            }
            ingredientes.add(entero(c.get("id_ingrediente")));
        }
        lblTotalCompras.setText(String.valueOf(compras.size()));
        lblIngredientes.setText(String.valueOf(ingredientes.size()));
        lblInvertido.setText(Estilo.soles(invertido));
        aplicarFiltros();
    }

    private void aplicarFiltros() {
        String idTxt = txtId.getText().trim();
        String fechaTxt = txtFecha.getText().trim();

        int id = 0;
        if (!idTxt.isEmpty()) {
            try {
                id = Integer.parseInt(idTxt);
                if (id <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                advertencia("El ID de compra debe ser un número entero positivo.");
                return;
            }
        }
        String fechaIso = null;
        if (!fechaTxt.isEmpty()) {
            try {
                fechaIso = LocalDate.parse(fechaTxt, FECHA_UI).toString();
            } catch (Exception e) {
                advertencia("La fecha no es válida. Use el formato dd/mm/aaaa (por ejemplo 26/01/2025).");
                return;
            }
        }

        List<Object[]> filas = new ArrayList<>();
        for (Map<String, Object> c : historial) {
            if (id > 0 && entero(c.get("id_compra")) != id) {
                continue;
            }
            if (fechaIso != null && !fechaIso.equals(diaIso(c.get("fecha")))) {
                continue;
            }
            filas.add(new Object[]{c.get("id_compra"), fechaBonita(c.get("fecha")), c.get("nombre"),
                    dos(num(c.get("cantidad"))) + " " + nz((String) c.get("unidad")),
                    dos(num(c.get("precio_unitario"))), dos(num(c.get("total")))});
        }
        tabla.setFilas(filas);
        if (filas.isEmpty() && (id > 0 || fechaIso != null)) {
            aviso("No se encontraron compras con ese criterio.");
        }
    }

    // ---------- Acciones ----------

    private void verDetalle(Object[] fila) {
        int id = ((Number) fila[0]).intValue();
        List<Map<String, Object>> partes = compraDAO.buscarCompra(id);
        if (partes.isEmpty()) {
            advertencia("No se encontró la compra N.° " + id + ".");
            return;
        }
        StringBuilder sb = new StringBuilder();
        Map<String, Object> primera = partes.get(0);
        sb.append("Compra N.° ").append(id).append("\nFecha: ").append(fechaBonita(primera.get("fecha")));
        for (Map<String, Object> p : partes) {
            sb.append("\n\nIngrediente: ").append(p.get("nombre"))
                    .append("\nCantidad: ").append(dos(num(p.get("cantidad")))).append(" ").append(nz((String) p.get("unidad")))
                    .append("\nPrecio unitario: ").append(Estilo.soles(num(p.get("precio_unitario"))))
                    .append("\nSubtotal: ").append(Estilo.soles(num(p.get("cantidad")) * num(p.get("precio_unitario"))));
        }
        sb.append("\n\nTotal de la compra: ").append(Estilo.soles(num(primera.get("total"))));
        aviso(sb.toString());
    }

    private void recalcular() {
        double cantidad = ((Number) spnCantidad.getValue()).doubleValue();
        double precio = ((Number) spnPrecio.getValue()).doubleValue();
        lblTotal.setText(dos(cantidad * precio));
    }

    private double valor(JSpinner s) {
        try {
            s.commitEdit();
        } catch (java.text.ParseException ignorada) {
        }
        return ((Number) s.getValue()).doubleValue();
    }

    private void registrar() {
        Opcion ing = (Opcion) cmbIngrediente.getSelectedItem();
        if (ing == null || ing.id <= 0) {
            advertencia("Seleccione el ingrediente comprado.");
            return;
        }
        double cantidad = valor(spnCantidad);
        double precio = valor(spnPrecio);
        if (cantidad <= 0) {
            advertencia("La cantidad debe ser mayor que cero.");
            return;
        }
        if (precio < 0) {
            advertencia("El precio unitario no puede ser negativo.");
            return;
        }
        if (precio == 0 && !confirmar("El precio unitario es S/ 0.00. ¿Registrar la compra de todos modos?")) {
            return;
        }
        double total = cantidad * precio;
        if (!confirmar("¿Registrar la compra?\n\nIngrediente: " + ing.texto + "\nCantidad: " + dos(cantidad) + " "
                + ing.unidad + "\nPrecio unitario: " + Estilo.soles(precio) + "\nTotal: " + Estilo.soles(total))) {
            return;
        }

        int id = ComprasOperaciones.registrarCompra(ing.id, cantidad, precio);

        if (id > 0 && !compraDAO.buscarCompra(id).isEmpty()) {
            aviso("Compra N.° " + id + " registrada correctamente.\n"
                    + "El stock de " + ing.texto + " aumentó en " + dos(cantidad) + " " + ing.unidad + ".");
            cmbIngrediente.setSelectedIndex(0);
            spnCantidad.setValue(0.0);
            spnPrecio.setValue(0.0);
            cargar();
        } else {
            error("No se pudo registrar la compra.\nRevise los datos e inténtelo nuevamente.");
        }
    }
}
