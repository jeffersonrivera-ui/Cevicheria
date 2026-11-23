package vista.escritorio;

import controlador.IngredientesOperaciones;
import dao.IngredientesDAO;
import dao.impl.IngredientesDAOImpl;
import modelo.Ingrediente;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PanelIngredientes extends ModuloBase {

    private static final double STOCK_BAJO = 5.0;
    private static final String[] UNIDADES = {"kg", "g", "litro", "ml", "unidad", "atado", "paquete", "lata"};

    private final IngredientesDAO ingredienteDAO = new IngredientesDAOImpl();

    private final JLabel lblTotal;
    private final JLabel lblConStock;
    private final JLabel lblStockBajo;

    private final JTextField txtBuscar = Estilo.campo();
    private final JComboBox<String> cmbEstadoStock = new JComboBox<>(
            new String[]{"Todos", "Con stock", "Stock bajo", "Sin stock"});
    private final TablaPaginada tabla = new TablaPaginada(
            new String[]{"ID", "Nombre del ingrediente", "Unidad", "Stock"}, 3, true);

    private final JTextField txtId = Estilo.campo();
    private final JTextField txtNombre = Estilo.campo();
    private final JComboBox<String> cmbUnidad = new JComboBox<>(UNIDADES);
    private final JSpinner spnStock = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 1000000.0, 0.5));

    public PanelIngredientes() {
        super("Ingredientes", "Administra los ingredientes y el stock de la cevichería", "❦",
                Estilo.PRIMARIO, true);

        lblTotal = agregarEstadistica("≡", new Color(0x38A3F5), "Total de ingredientes", Estilo.PRIMARIO_OSCURO);
        lblConStock = agregarEstadistica("✓", Estilo.EXITO, "Ingredientes con stock", new Color(0x15803D));
        lblStockBajo = agregarEstadistica("⚠", Estilo.PELIGRO, "Stock bajo", Estilo.PELIGRO);

        // Izquierda: busqueda + tabla
        cmbEstadoStock.setFont(Estilo.FUENTE);
        cmbEstadoStock.setPreferredSize(new Dimension(150, 36));
        JPanel extra = new JPanel(new BorderLayout(0, 4));
        extra.setOpaque(false);
        extra.add(Estilo.etiqueta("Estado del stock:", Estilo.PEQUENA_NEGRITA, Estilo.TEXTO), BorderLayout.NORTH);
        extra.add(cmbEstadoStock, BorderLayout.CENTER);

        JButton btnBuscar = Estilo.boton("Buscar", Estilo.PRIMARIO);
        panelIzquierdo.add(barraBusqueda("Buscar por ID o nombre:", txtBuscar, extra, btnBuscar), BorderLayout.NORTH);
        panelIzquierdo.add(tabla, BorderLayout.CENTER);

        tabla.setColorPastilla(texto -> {
            double v = numero(texto);
            return v < STOCK_BAJO
                    ? new Color[]{new Color(0xFEE2E2), new Color(0xDC2626)}
                    : new Color[]{new Color(0xDCFCE7), new Color(0x15803D)};
        });

        // Derecha: formulario
        txtId.setEditable(false);
        txtId.setBackground(new Color(0xEEF2F8));
        cmbUnidad.setEditable(true);
        cmbUnidad.setFont(Estilo.FUENTE);
        cmbUnidad.setPreferredSize(new Dimension(160, 36));
        spnStock.setFont(Estilo.FUENTE);
        spnStock.setPreferredSize(new Dimension(160, 36));
        spnStock.setEditor(new JSpinner.NumberEditor(spnStock, "0.00"));

        Formulario f = new Formulario();
        f.agregar("ID:", false, txtId);
        f.agregar("Nombre del ingrediente:", true, txtNombre);
        f.agregar("Unidad de medida:", true, cmbUnidad);
        f.agregar("Stock inicial / disponible:", true, spnStock);

        JButton btnRegistrar = Estilo.boton("Registrar", Estilo.EXITO);
        JButton btnActualizar = Estilo.boton("Actualizar", Estilo.PRIMARIO);
        JButton btnEliminar = Estilo.boton("Eliminar", Estilo.PELIGRO);
        JButton btnLimpiar = Estilo.boton("Limpiar", Estilo.GRIS);

        JPanel avisos = new JPanel();
        avisos.setOpaque(false);
        avisos.setLayout(new BoxLayout(avisos, BoxLayout.Y_AXIS));
        avisos.add(Estilo.aviso("La unidad no se puede cambiar si el ingrediente pertenece a una receta.", "info"));
        avisos.add(Box.createVerticalStrut(8));
        avisos.add(Estilo.aviso("No se puede eliminar un ingrediente si está asociado a una o más recetas o si tiene historial de compras.", "alerta"));

        JPanel centro = new JPanel(new BorderLayout(0, 14));
        centro.setOpaque(false);
        centro.add(f, BorderLayout.NORTH);
        JPanel botonesYAvisos = new JPanel(new BorderLayout(0, 14));
        botonesYAvisos.setOpaque(false);
        botonesYAvisos.add(filaBotones(btnRegistrar, btnActualizar, btnEliminar, btnLimpiar), BorderLayout.NORTH);
        botonesYAvisos.add(avisos, BorderLayout.CENTER);
        centro.add(botonesYAvisos, BorderLayout.CENTER);

        panelDerecho.add(tituloDePanel("❦", "Registro / Actualización de Ingrediente"), BorderLayout.NORTH);
        panelDerecho.add(centro, BorderLayout.CENTER);

        // Eventos
        tabla.setEscucha(new TablaPaginada.Escucha() {
            @Override
            public void seleccionar(Object[] fila) {
                cargarEnFormulario(((Number) fila[0]).intValue());
            }

            @Override
            public void eliminar(Object[] fila) {
                cargarEnFormulario(((Number) fila[0]).intValue());
                eliminarIngrediente();
            }

            @Override
            public void ver(Object[] fila) {
                verDetalle(((Number) fila[0]).intValue());
            }
        });
        btnBuscar.addActionListener(e -> cargar());
        txtBuscar.addActionListener(e -> cargar());
        cmbEstadoStock.addActionListener(e -> cargar());
        btnRegistrar.addActionListener(e -> registrar());
        btnActualizar.addActionListener(e -> actualizar());
        btnEliminar.addActionListener(e -> eliminarIngrediente());
        btnLimpiar.addActionListener(e -> limpiar());

        cargar();
    }

    private static double numero(String texto) {
        try {
            return Double.parseDouble(texto.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private static String formato(double v) {
        return String.format(Locale.US, "%.2f", v);
    }

    private void cargar() {
        try {
            List<Ingrediente> todos = ingredienteDAO.listar();
            String filtro = txtBuscar.getText().trim().toLowerCase();
            String estado = (String) cmbEstadoStock.getSelectedItem();

            int conStock = 0;
            int bajos = 0;
            List<Object[]> filas = new ArrayList<>();

            for (Ingrediente i : todos) {
                double s = i.getStock();
                if (s > 0) {
                    conStock++;
                }
                if (s < STOCK_BAJO) {
                    bajos++;
                }

                boolean coincideTexto = filtro.isEmpty() || String.valueOf(i.getId()).equals(filtro)
                        || i.getNombre().toLowerCase().contains(filtro);
                boolean coincideEstado = "Todos".equals(estado)
                        || ("Con stock".equals(estado) && s > 0)
                        || ("Stock bajo".equals(estado) && s < STOCK_BAJO)
                        || ("Sin stock".equals(estado) && s <= 0);
                if (coincideTexto && coincideEstado) {
                    filas.add(new Object[]{i.getId(), i.getNombre(), i.getUnidad(), formato(s)});
                }
            }

            lblTotal.setText(String.valueOf(todos.size()));
            lblConStock.setText(String.valueOf(conStock));
            lblStockBajo.setText(String.valueOf(bajos));
            tabla.setFilas(filas);
        } catch (Exception ex) {
            error("No se pudieron cargar los ingredientes: " + ex.getMessage());
        }
    }

    private void cargarEnFormulario(int id) {
        Ingrediente i = ingredienteDAO.buscarPorId(id);
        if (i == null) {
            return;
        }
        txtId.setText(String.valueOf(i.getId()));
        txtNombre.setText(i.getNombre());
        asegurarUnidad(i.getUnidad());
        cmbUnidad.setSelectedItem(i.getUnidad());
        spnStock.setValue(i.getStock());
    }

    private void asegurarUnidad(String unidad) {
        if (unidad == null) {
            return;
        }
        for (int k = 0; k < cmbUnidad.getItemCount(); k++) {
            if (cmbUnidad.getItemAt(k).equalsIgnoreCase(unidad)) {
                return;
            }
        }
        cmbUnidad.addItem(unidad);
    }

    private String unidadElegida() {
        Object u = cmbUnidad.getEditor().getItem();
        return u == null ? "" : u.toString().trim();
    }

    private double stockElegido() {
        try {
            spnStock.commitEdit();
        } catch (java.text.ParseException ignorada) {
        }
        return ((Number) spnStock.getValue()).doubleValue();
    }

    private boolean validar() {
        if (txtNombre.getText().trim().isEmpty()) {
            advertencia("Ingrese el nombre del ingrediente.");
            return false;
        }
        if (unidadElegida().isEmpty()) {
            advertencia("Ingrese o seleccione la unidad de medida.");
            return false;
        }
        if (stockElegido() < 0) {
            advertencia("El stock no puede ser negativo.");
            return false;
        }
        return true;
    }

    private void registrar() {
        if (!validar()) {
            return;
        }
        int antes = ingredienteDAO.listar().size();
        IngredientesOperaciones.registrarIngrediente(txtNombre.getText().trim(), unidadElegida(), stockElegido());
        int despues = ingredienteDAO.listar().size();
        if (despues > antes) {
            aviso("Ingrediente registrado correctamente.");
            limpiar();
            cargar();
        } else {
            error("No se pudo registrar el ingrediente.");
        }
    }

    private void actualizar() {
        if (txtId.getText().isEmpty()) {
            advertencia("Seleccione un ingrediente de la tabla para actualizarlo.");
            return;
        }
        if (!validar()) {
            return;
        }
        int id = Integer.parseInt(txtId.getText());
        String nombre = txtNombre.getText().trim();
        String unidad = unidadElegida();
        double stock = stockElegido();

        IngredientesOperaciones.actualizarIngrediente(id, nombre, unidad, stock);

        Ingrediente i = ingredienteDAO.buscarPorId(id);
        boolean ok = i != null && i.getNombre().equals(nombre) && i.getUnidad().equals(unidad)
                && Math.abs(i.getStock() - stock) < 0.0001;
        if (ok) {
            aviso("Ingrediente actualizado correctamente.");
            cargar();
        } else {
            error("No se pudo actualizar el ingrediente.\n"
                    + "Recuerde que la unidad no se puede cambiar si el ingrediente pertenece a una receta.");
        }
    }

    private void eliminarIngrediente() {
        if (txtId.getText().isEmpty()) {
            advertencia("Seleccione un ingrediente de la tabla para eliminarlo.");
            return;
        }
        int id = Integer.parseInt(txtId.getText());
        if (!confirmar("¿Eliminar el ingrediente N.° " + id + " (" + txtNombre.getText().trim() + ")?")) {
            return;
        }
        IngredientesOperaciones.eliminarIngrediente(id);
        if (!IngredientesOperaciones.existeIngrediente(id)) {
            aviso("Ingrediente eliminado correctamente.");
            limpiar();
            cargar();
        } else {
            error("No se pudo eliminar el ingrediente.\n"
                    + "Pertenece a una o más recetas o tiene compras registradas en el historial.");
        }
    }

    private void verDetalle(int id) {
        Ingrediente i = ingredienteDAO.buscarPorId(id);
        if (i == null) {
            return;
        }
        String situacion = i.getStock() <= 0 ? "Sin stock" : (i.getStock() < STOCK_BAJO ? "Stock bajo" : "Stock suficiente");
        aviso("Ingrediente N.° " + i.getId()
                + "\nNombre: " + i.getNombre()
                + "\nUnidad: " + i.getUnidad()
                + "\nStock disponible: " + formato(i.getStock()) + " " + i.getUnidad()
                + "\nSituación: " + situacion);
    }

    private void limpiar() {
        txtId.setText("");
        txtNombre.setText("");
        cmbUnidad.setSelectedIndex(0);
        spnStock.setValue(0.0);
        tabla.limpiarSeleccion();
    }
}
