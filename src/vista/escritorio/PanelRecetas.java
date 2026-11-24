package vista.escritorio;

import controlador.PlatoIngredienteOperaciones;
import dao.IngredientesDAO;
import dao.PlatoDAO;
import dao.PlatoIngredienteDAO;
import dao.impl.IngredientesDAOImpl;
import dao.impl.PlatoDAOImpl;
import dao.impl.PlatoIngredienteDAOImpl;
import modelo.Ingrediente;
import modelo.Plato;
import modelo.PlatoIngrediente;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PanelRecetas extends ModuloBase {

    private static final double STOCK_BAJO = 5.0;

    private static class Item {
        final int id;
        final String texto;

        Item(int id, String texto) {
            this.id = id;
            this.texto = texto;
        }

        @Override
        public String toString() {
            return texto;
        }
    }

    private final PlatoDAO platoDAO = new PlatoDAOImpl();
    private final IngredientesDAO ingredienteDAO = new IngredientesDAOImpl();
    private final PlatoIngredienteDAO recetaDAO = new PlatoIngredienteDAOImpl();
    private final boolean soloConsulta;

    private final JLabel lblPlato;
    private final JLabel lblCantidad;
    private final JLabel lblEstado;

    private final JComboBox<Item> cmbPlato = new JComboBox<>();
    private final TablaPaginada tabla;

    private final JTextField txtIdPlato = Estilo.campo();
    private final JComboBox<Item> cmbIngrediente = new JComboBox<>();
    private final JSpinner spnCantidad = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100000.0, 0.05));
    private final JLabel lblUnidad = Estilo.etiqueta("-", new Font("Segoe UI", Font.BOLD, 16), Estilo.PRIMARIO_OSCURO);
    private final JLabel lblStock = Estilo.etiqueta("-", new Font("Segoe UI", Font.BOLD, 16), new Color(0x15803D));

    private boolean cargando = false;

    // Ingredientes adicionales (botón "+ Agregar otro ingrediente").
    private final JPanel panelExtras = new JPanel() {
        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }
    };
    private final List<FilaExtra> extras = new ArrayList<>();

    private final class FilaExtra extends JPanel {
        final JComboBox<Item> cmb = new JComboBox<>();
        final JSpinner spn = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100000.0, 0.05));
        final JLabel info = Estilo.etiqueta(" ", Estilo.PEQUENA, Estilo.TEXTO_SUAVE);

        FilaExtra(int numero) {
            super(new GridBagLayout());
            setOpaque(false);
            for (int i = 0; i < cmbIngrediente.getItemCount(); i++) {
                cmb.addItem(cmbIngrediente.getItemAt(i));
            }
            cmb.setFont(Estilo.FUENTE);
            cmb.setPreferredSize(new Dimension(120, 36));
            spn.setFont(Estilo.FUENTE);
            spn.setPreferredSize(new Dimension(120, 36));
            spn.setEditor(new JSpinner.NumberEditor(spn, "0.00"));

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
            cab.add(Estilo.etiquetaCampo("Ingrediente " + numero + ":", true), BorderLayout.WEST);
            cab.add(quitar, BorderLayout.EAST);

            GridBagConstraints g = new GridBagConstraints();
            g.fill = GridBagConstraints.HORIZONTAL;
            g.weightx = 1;
            g.gridx = 0;
            g.gridy = 0;
            g.insets = new Insets(0, 0, 4, 0);
            add(cab, g);
            g.gridy = 1;
            g.insets = new Insets(0, 0, 2, 0);
            add(cmb, g);
            g.gridy = 2;
            g.insets = new Insets(0, 0, 6, 0);
            add(info, g);
            g.gridy = 3;
            g.insets = new Insets(0, 0, 4, 0);
            JPanel cant = new JPanel(new BorderLayout(12, 0));
            cant.setOpaque(false);
            cant.add(Estilo.etiquetaCampo("Cantidad necesaria:", true), BorderLayout.WEST);
            cant.add(spn, BorderLayout.CENTER);
            add(cant, g);
            g.gridy = 4;
            add(Box.createVerticalStrut(8), g);

            cmb.addActionListener(e -> {
                Item it = (Item) cmb.getSelectedItem();
                Ingrediente ing = it == null || it.id <= 0 ? null : ingredienteDAO.buscarPorId(it.id);
                info.setText(ing == null ? " " : "Unidad: " + ing.getUnidad() + "   |   Stock: "
                        + formato(ing.getStock()) + " " + ing.getUnidad());
            });
        }

        int idIngrediente() {
            Item it = (Item) cmb.getSelectedItem();
            return it == null ? 0 : it.id;
        }

        double cantidad() {
            try {
                spn.commitEdit();
            } catch (java.text.ParseException ignorada) {
            }
            return ((Number) spn.getValue()).doubleValue();
        }
    }

    public PanelRecetas(boolean soloConsulta) {
        super("Recetas",
                soloConsulta ? "Consulta los ingredientes que lleva cada plato"
                        : "Administra los ingredientes y cantidades de cada plato",
                "☰", Estilo.PRIMARIO, true);
        this.soloConsulta = soloConsulta;

        lblPlato = agregarEstadistica("♨", new Color(0x38A3F5), "Plato seleccionado", Estilo.PRIMARIO);
        lblPlato.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblCantidad = agregarEstadistica("❦", Estilo.EXITO, "Ingredientes en receta", new Color(0x15803D));
        lblCantidad.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblEstado = agregarEstadistica("✓", Estilo.PELIGRO, "Estado de receta", Estilo.PELIGRO);
        lblEstado.setFont(new Font("Segoe UI", Font.BOLD, 17));

        // Fila superior: selector de plato + nota informativa
        cmbPlato.setFont(Estilo.FUENTE);
        cmbPlato.setPreferredSize(new Dimension(300, 36));
        JButton btnBuscarPlato = Estilo.boton("Buscar plato", Estilo.PRIMARIO);

        Estilo.Tarjeta selector = new Estilo.Tarjeta(new FlowLayout(FlowLayout.LEFT, 12, 0));
        selector.setBorder(new EmptyBorder(10, 14, 10, 14));
        selector.add(Estilo.etiqueta("Seleccionar plato:", Estilo.NEGRITA, Estilo.TEXTO));
        selector.add(cmbPlato);
        selector.add(btnBuscarPlato);

        JPanel fila = new JPanel(new GridBagLayout());
        fila.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0;
        g.fill = GridBagConstraints.BOTH;
        g.weighty = 1;
        g.gridx = 0;
        g.weightx = 0.6;
        fila.add(selector, g);
        g.gridx = 1;
        g.weightx = 0.4;
        g.insets = new Insets(0, 14, 0, 0);
        fila.add(Estilo.aviso("Una receta es la relación entre un plato y los ingredientes que lo componen, "
                + "indicando la cantidad necesaria de cada uno.", "info"), g);
        setFilaSuperior(fila);

        // Izquierda: tabla de la receta
        tabla = new TablaPaginada(
                new String[]{"ID ingrediente", "Ingrediente", "Cantidad necesaria", "Unidad", "Stock disponible"},
                4, !soloConsulta, false);
        tabla.setTextoPie("%d ingredientes registrados");
        tabla.setColorPastilla(texto -> {
            double v = primerNumero(texto);
            return v < STOCK_BAJO
                    ? new Color[]{new Color(0xFEE2E2), new Color(0xDC2626)}
                    : new Color[]{new Color(0xDCFCE7), new Color(0x15803D)};
        });
        panelIzquierdo.add(tituloDePanel("≡", "Ingredientes de la receta"), BorderLayout.NORTH);
        panelIzquierdo.add(tabla, BorderLayout.CENTER);

        // Derecha: formulario
        txtIdPlato.setEditable(false);
        txtIdPlato.setBackground(new Color(0xEEF2F8));
        cmbIngrediente.setFont(Estilo.FUENTE);
        cmbIngrediente.setPreferredSize(new Dimension(160, 36));
        spnCantidad.setFont(Estilo.FUENTE);
        spnCantidad.setPreferredSize(new Dimension(160, 36));
        spnCantidad.setEditor(new JSpinner.NumberEditor(spnCantidad, "0.00"));
        if (soloConsulta) {
            cmbIngrediente.setEnabled(false);
            spnCantidad.setEnabled(false);
        }

        Formulario f = new Formulario();
        f.agregar("ID del plato:", false, txtIdPlato);
        f.agregar("ID del ingrediente:", !soloConsulta, cmbIngrediente);
        f.agregar("Cantidad necesaria:", !soloConsulta, spnCantidad);

        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        anadir(col, f);
        anadir(col, crearCajaUnidadStock());

        if (!soloConsulta) {
            panelExtras.setOpaque(false);
            panelExtras.setLayout(new BoxLayout(panelExtras, BoxLayout.Y_AXIS));
            panelExtras.setAlignmentX(Component.LEFT_ALIGNMENT);
            col.add(panelExtras);
            JButton btnMas = Estilo.boton("+  Agregar otro ingrediente", Estilo.PRIMARIO);
            btnMas.setFont(Estilo.PEQUENA_NEGRITA);
            btnMas.setPreferredSize(new Dimension(120, 34));
            btnMas.addActionListener(e -> agregarFila());
            anadir(col, btnMas);

            JButton btnAgregar = Estilo.boton("<html><center>Agregar<br>ingrediente</center></html>", Estilo.EXITO);
            JButton btnActualizar = Estilo.boton("<html><center>Actualizar<br>cantidad</center></html>", Estilo.PRIMARIO);
            JButton btnEliminar = Estilo.boton("<html><center>Eliminar<br>de receta</center></html>", Estilo.PELIGRO);
            JButton btnLimpiar = Estilo.boton("Limpiar", Estilo.GRIS);
            for (JButton b : new JButton[]{btnAgregar, btnActualizar, btnEliminar, btnLimpiar}) {
                b.setFont(Estilo.PEQUENA_NEGRITA);
            }
            anadir(col, filaBotones(btnAgregar, btnActualizar, btnEliminar, btnLimpiar));
            anadir(col, Estilo.aviso("La cantidad debe ser mayor que cero.", "info"));
            anadir(col, Estilo.aviso("No se puede modificar o eliminar esta receta si existen pedidos con inventario "
                    + "descontado o personalizaciones que impidan el cambio.", "alerta"));

            btnAgregar.addActionListener(e -> agregar());
            btnActualizar.addActionListener(e -> actualizarCantidad());
            btnEliminar.addActionListener(e -> eliminar());
            btnLimpiar.addActionListener(e -> limpiar());
        } else {
            anadir(col, Estilo.aviso("Vista de consulta: seleccione un ingrediente de la tabla para ver su unidad y su stock.", "info"));
        }

        panelDerecho.add(tituloDePanel("+", soloConsulta ? "Detalle del ingrediente" : "Agregar / Actualizar ingrediente"),
                BorderLayout.NORTH);
        panelDerecho.add(col, BorderLayout.CENTER);

        // Eventos
        tabla.setEscucha(new TablaPaginada.Escucha() {
            @Override
            public void seleccionar(Object[] fila) {
                seleccionarDeTabla(fila);
            }

            @Override
            public void eliminar(Object[] fila) {
                seleccionarDeTabla(fila);
                PanelRecetas.this.eliminar();
            }
        });
        btnBuscarPlato.addActionListener(e -> cargarReceta());
        cmbPlato.addActionListener(e -> {
            if (!cargando) {
                limpiar();
                cargarReceta();
            }
        });
        cmbIngrediente.addActionListener(e -> {
            if (!cargando) {
                actualizarCajaIngrediente();
            }
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                recargarCombos();
                cargarReceta();
            }
        });

        recargarCombos();
        cargarReceta();
    }

    // ---------- Construcción ----------

    private void anadir(JPanel col, JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
        col.add(c);
        col.add(Box.createVerticalStrut(12));
    }

    private JPanel crearCajaUnidadStock() {
        JPanel caja = new JPanel(new GridLayout(1, 2, 0, 0)) {
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
        caja.setOpaque(false);
        caja.setBorder(new EmptyBorder(8, 12, 8, 12));
        caja.add(celdaDato("⚖", "Unidad del ingrediente:", lblUnidad));
        caja.add(celdaDato("▣", "Stock disponible:", lblStock));
        return caja;
    }

    private JPanel celdaDato(String glifo, String titulo, JLabel valor) {
        JPanel c = new JPanel(new BorderLayout(8, 0));
        c.setOpaque(false);
        JLabel ic = new JLabel(glifo);
        ic.setFont(Estilo.fuenteSimbolo(glifo.codePointAt(0), 22));
        ic.setForeground(Estilo.PRIMARIO);
        c.add(ic, BorderLayout.WEST);
        JPanel t = new JPanel();
        t.setOpaque(false);
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        t.add(Estilo.etiqueta(titulo, Estilo.PEQUENA, Estilo.TEXTO_SUAVE));
        t.add(valor);
        c.add(t, BorderLayout.CENTER);
        return c;
    }

    // ---------- Datos ----------

    private static double primerNumero(String texto) {
        try {
            return Double.parseDouble(texto.trim().split("\\s+")[0]);
        } catch (Exception e) {
            return 0;
        }
    }

    private static String formato(double v) {
        return String.format(Locale.US, "%.2f", v);
    }

    private int idPlatoActual() {
        Item it = (Item) cmbPlato.getSelectedItem();
        return it == null ? 0 : it.id;
    }

    private int idIngredienteActual() {
        Item it = (Item) cmbIngrediente.getSelectedItem();
        return it == null ? 0 : it.id;
    }

    private void recargarCombos() {
        quitarFilas();
        cargando = true;
        try {
            int platoPrevio = idPlatoActual();
            int ingPrevio = idIngredienteActual();

            cmbPlato.removeAllItems();
            Item elegidoPlato = null;
            for (Plato p : platoDAO.listar()) {
                Item it = new Item(p.getId(), p.getNombre() + " (ID: " + p.getId() + ")");
                cmbPlato.addItem(it);
                if (p.getId() == platoPrevio) {
                    elegidoPlato = it;
                }
            }
            if (elegidoPlato != null) {
                cmbPlato.setSelectedItem(elegidoPlato);
            }

            cmbIngrediente.removeAllItems();
            cmbIngrediente.addItem(new Item(0, "Seleccione un ingrediente..."));
            Item elegidoIng = null;
            for (Ingrediente i : ingredienteDAO.listar()) {
                Item it = new Item(i.getId(), i.getNombre() + " (ID: " + i.getId() + ")");
                cmbIngrediente.addItem(it);
                if (i.getId() == ingPrevio) {
                    elegidoIng = it;
                }
            }
            if (elegidoIng != null) {
                cmbIngrediente.setSelectedItem(elegidoIng);
            }
        } catch (Exception ex) {
            error("No se pudieron cargar los platos o ingredientes: " + ex.getMessage());
        } finally {
            cargando = false;
        }
        actualizarCajaIngrediente();
    }

    private void cargarReceta() {
        int idPlato = idPlatoActual();
        if (idPlato <= 0) {
            txtIdPlato.setText("");
            lblPlato.setText("Sin platos");
            lblCantidad.setText("0 ingredientes");
            lblEstado.setText("Sin receta");
            lblEstado.setForeground(Estilo.PELIGRO);
            tabla.setFilas(new ArrayList<>());
            return;
        }
        try {
            List<PlatoIngrediente> receta = recetaDAO.listarPorPlato(idPlato);
            List<Object[]> filas = new ArrayList<>();
            for (PlatoIngrediente r : receta) {
                Ingrediente i = r.getIngrediente();
                filas.add(new Object[]{i.getId(), i.getNombre(), formato(r.getCantidad()), i.getUnidad(),
                        formato(i.getStock()) + " " + i.getUnidad()});
            }
            tabla.setFilas(filas);

            Plato plato = platoDAO.buscarPorId(idPlato);
            txtIdPlato.setText(String.valueOf(idPlato));
            lblPlato.setText(plato == null ? "-" : plato.getNombre());
            lblCantidad.setText(receta.size() + (receta.size() == 1 ? " ingrediente" : " ingredientes"));
            if (receta.isEmpty()) {
                lblEstado.setText("Sin receta");
                lblEstado.setForeground(Estilo.PELIGRO);
            } else {
                lblEstado.setText("Configurada");
                lblEstado.setForeground(new Color(0x15803D));
            }
            actualizarCajaIngrediente();
        } catch (Exception ex) {
            error("No se pudo cargar la receta: " + ex.getMessage());
        }
    }

    private void actualizarCajaIngrediente() {
        int idIng = idIngredienteActual();
        Ingrediente i = idIng > 0 ? ingredienteDAO.buscarPorId(idIng) : null;
        if (i == null) {
            lblUnidad.setText("-");
            lblStock.setText("-");
            lblStock.setForeground(Estilo.PRIMARIO_OSCURO);
            return;
        }
        lblUnidad.setText(i.getUnidad());
        lblStock.setText(formato(i.getStock()) + " " + i.getUnidad());
        lblStock.setForeground(i.getStock() < STOCK_BAJO ? Estilo.PELIGRO : new Color(0x15803D));
    }

    private void seleccionarDeTabla(Object[] fila) {
        int idIng = ((Number) fila[0]).intValue();
        cargando = true;
        for (int k = 0; k < cmbIngrediente.getItemCount(); k++) {
            if (cmbIngrediente.getItemAt(k).id == idIng) {
                cmbIngrediente.setSelectedIndex(k);
                break;
            }
        }
        cargando = false;
        spnCantidad.setValue(primerNumero(String.valueOf(fila[2])));
        actualizarCajaIngrediente();
    }

    private PlatoIngrediente buscarEnReceta(int idPlato, int idIng) {
        for (PlatoIngrediente r : recetaDAO.listarPorPlato(idPlato)) {
            if (r.getIngrediente().getId() == idIng) {
                return r;
            }
        }
        return null;
    }

    private double cantidadElegida() {
        try {
            spnCantidad.commitEdit();
        } catch (java.text.ParseException ignorada) {
        }
        return ((Number) spnCantidad.getValue()).doubleValue();
    }

    private boolean validar(boolean exigirIngrediente) {
        if (idPlatoActual() <= 0) {
            advertencia("Seleccione un plato.");
            return false;
        }
        if (exigirIngrediente && idIngredienteActual() <= 0) {
            advertencia("Seleccione un ingrediente.");
            return false;
        }
        return true;
    }

    // ---------- Acciones ----------

    private void agregarFila() {
        if (extras.size() >= 14) {
            advertencia("Puede agregar hasta 15 ingredientes a la vez.");
            return;
        }
        FilaExtra f = new FilaExtra(extras.size() + 2);
        extras.add(f);
        panelExtras.add(f);
        refrescarFilas();
    }

    private void quitarFila(FilaExtra f) {
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
        Container c = panelExtras.getParent();
        while (c != null) {
            c.revalidate();
            c = c.getParent();
        }
    }

    private void agregar() {
        if (!validar(true)) {
            return;
        }
        int idPlato = idPlatoActual();

        // Lista: el ingrediente del formulario + los adicionales.
        List<int[]> ids = new ArrayList<>();
        List<Double> cantidades = new ArrayList<>();
        ids.add(new int[]{idIngredienteActual()});
        cantidades.add(cantidadElegida());
        for (int k = 0; k < extras.size(); k++) {
            FilaExtra f = extras.get(k);
            if (f.idIngrediente() <= 0) {
                advertencia("Seleccione el ingrediente " + (k + 2) + " o quítelo.");
                return;
            }
            ids.add(new int[]{f.idIngrediente()});
            cantidades.add(f.cantidad());
        }

        java.util.Set<Integer> vistos = new java.util.HashSet<>();
        for (int k = 0; k < ids.size(); k++) {
            int idIng = ids.get(k)[0];
            if (cantidades.get(k) <= 0) {
                advertencia("La cantidad del ingrediente " + (k + 1) + " debe ser mayor que cero.");
                return;
            }
            if (!vistos.add(idIng)) {
                advertencia("El ingrediente " + (k + 1) + " está repetido en el formulario.");
                return;
            }
            if (buscarEnReceta(idPlato, idIng) != null) {
                advertencia("El ingrediente " + (k + 1) + " ya pertenece a la receta.\n"
                        + "Use \"Actualizar cantidad\" para cambiarlo.");
                return;
            }
        }

        int agregados = 0;
        for (int k = 0; k < ids.size(); k++) {
            PlatoIngredienteOperaciones.registrarIngredientePlato(idPlato, ids.get(k)[0], cantidades.get(k));
            if (buscarEnReceta(idPlato, ids.get(k)[0]) != null) {
                agregados++;
            }
        }

        if (agregados == ids.size()) {
            aviso(agregados == 1 ? "Ingrediente agregado a la receta correctamente."
                    : agregados + " ingredientes agregados a la receta correctamente.");
        } else {
            error("Se agregaron " + agregados + " de " + ids.size() + " ingredientes a la receta.\n"
                    + "Revise la receta y vuelva a intentar con los que faltan.");
        }
        limpiar();
        cargarReceta();
    }

    private void actualizarCantidad() {
        if (!validar(true)) {
            return;
        }
        double cantidad = cantidadElegida();
        if (cantidad <= 0) {
            advertencia("La cantidad debe ser mayor que cero.");
            return;
        }
        int idPlato = idPlatoActual();
        int idIng = idIngredienteActual();
        if (buscarEnReceta(idPlato, idIng) == null) {
            advertencia("Ese ingrediente no está en la receta. Selecciónelo de la tabla o use \"Agregar ingrediente\".");
            return;
        }

        PlatoIngredienteOperaciones.actualizarCantidadIngrediente(idPlato, idIng, cantidad);

        PlatoIngrediente r = buscarEnReceta(idPlato, idIng);
        if (r != null && Math.abs(r.getCantidad() - cantidad) < 0.01) {
            aviso("Cantidad actualizada correctamente.");
            cargarReceta();
        } else {
            error("No se pudo actualizar la cantidad.\n"
                    + "Verifique si el plato tiene pedidos con inventario descontado.");
        }
    }

    private void eliminar() {
        if (!validar(true)) {
            return;
        }
        int idPlato = idPlatoActual();
        int idIng = idIngredienteActual();
        if (buscarEnReceta(idPlato, idIng) == null) {
            advertencia("Ese ingrediente no está en la receta. Selecciónelo de la tabla.");
            return;
        }
        if (!confirmar("¿Eliminar este ingrediente de la receta?")) {
            return;
        }

        PlatoIngredienteOperaciones.eliminarIngredientePlato(idPlato, idIng);

        if (buscarEnReceta(idPlato, idIng) == null) {
            aviso("Ingrediente eliminado de la receta correctamente.");
            limpiar();
            cargarReceta();
        } else {
            error("No se pudo eliminar el ingrediente.\n"
                    + "Verifique los pedidos con inventario descontado y las personalizaciones.");
        }
    }

    private void limpiar() {
        quitarFilas();
        cargando = true;
        if (cmbIngrediente.getItemCount() > 0) {
            cmbIngrediente.setSelectedIndex(0);
        }
        cargando = false;
        spnCantidad.setValue(0.0);
        tabla.limpiarSeleccion();
        actualizarCajaIngrediente();
    }
}
