package vista.escritorio;

import controlador.PlatosOperaciones;
import dao.PlatoDAO;
import dao.impl.PlatoDAOImpl;
import modelo.Plato;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PanelPlatos extends ModuloBase {

    private final PlatoDAO platoDAO = new PlatoDAOImpl();

    private final JLabel lblTotal;
    private final JLabel lblDisponibles;
    private final JLabel lblNoDisponibles;

    private final JTextField txtBuscar = Estilo.campo();
    private final JComboBox<String> cmbEstado = new JComboBox<>(new String[]{"Todos", "Disponible", "No disponible"});
    private final boolean admin;
    private final TablaPaginada tabla;

    private final JTextField txtId = Estilo.campo();
    private final JTextField txtNombre = Estilo.campo();
    private final JTextField txtPrecio = Estilo.campo();
    private final Estilo.Pastilla pastillaEstado = new Estilo.Pastilla("No disponible");

    // Vista de solo consulta (mesero).
    public PanelPlatos() {
        this(false);
    }

    // admin = true: permite registrar, actualizar y eliminar platos.
    public PanelPlatos(boolean admin) {
        super("Platos", admin ? "Administra los platos de la carta de la cevichería"
                : "Consulta los platos de la carta de la cevichería", "\u2668", Estilo.PRIMARIO, true);
        this.admin = admin;
        tabla = new TablaPaginada(new String[]{"ID", "Nombre del plato", "Precio (S/)", "Estado"}, 3, admin);

        lblTotal = agregarEstadistica("\u2668", new Color(0x38A3F5), "Total de platos", Estilo.PRIMARIO_OSCURO);
        lblDisponibles = agregarEstadistica("\u2713", Estilo.EXITO, "Disponibles", Estilo.EXITO);
        lblNoDisponibles = agregarEstadistica("\u2716", Estilo.PELIGRO, "No disponibles", Estilo.PELIGRO);

        cmbEstado.setFont(Estilo.FUENTE);
        cmbEstado.setPreferredSize(new Dimension(150, 36));
        JPanel extra = new JPanel(new BorderLayout(0, 4));
        extra.setOpaque(false);
        extra.add(Estilo.etiqueta("Estado:", Estilo.PEQUENA_NEGRITA, Estilo.TEXTO), BorderLayout.NORTH);
        extra.add(cmbEstado, BorderLayout.CENTER);

        JButton btnBuscar = Estilo.boton("Buscar", Estilo.PRIMARIO);
        panelIzquierdo.add(barraBusqueda("Buscar por ID o nombre:", txtBuscar, extra, btnBuscar), BorderLayout.NORTH);
        panelIzquierdo.add(tabla, BorderLayout.CENTER);

        txtId.setEditable(false);
        txtId.setBackground(new Color(0xEEF2F8));
        if (!admin) {
            txtNombre.setEditable(false);
            txtPrecio.setEditable(false);
            txtNombre.setBackground(new Color(0xEEF2F8));
            txtPrecio.setBackground(new Color(0xEEF2F8));
        }

        JPanel contPastilla = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        contPastilla.setOpaque(false);
        contPastilla.add(pastillaEstado);

        Formulario f = new Formulario();
        f.agregar("ID:", false, txtId);
        f.agregar("Nombre del plato:", admin, txtNombre);
        f.agregar("Precio (S/):", admin, txtPrecio);
        f.agregar("Disponibilidad:", false, contPastilla);

        JPanel avisos = new JPanel();
        avisos.setOpaque(false);
        avisos.setLayout(new BoxLayout(avisos, BoxLayout.Y_AXIS));
        avisos.add(Estilo.aviso(admin ? "Un plato nuevo queda \"No disponible\" hasta que le registres su receta en la sección Recetas."
                : "Vista de carta: aquí solo se consultan los platos.", "info"));
        avisos.add(Box.createVerticalStrut(8));
        avisos.add(Estilo.aviso("Un plato está disponible cuando tiene receta y hay stock suficiente de todos sus ingredientes.", "alerta"));

        JPanel centro = new JPanel(new BorderLayout(0, 12));
        centro.setOpaque(false);
        centro.add(f, BorderLayout.NORTH);
        centro.add(avisos, BorderLayout.CENTER);

        JButton btnActualizar = Estilo.boton("Actualizar disponibilidad", Estilo.PRIMARIO);
        panelDerecho.add(tituloDePanel("\u2668", admin ? "Registro / Actualización de Plato" : "Detalle del plato"),
                BorderLayout.NORTH);
        if (admin) {
            JButton btnRegistrar = Estilo.boton("Registrar", Estilo.EXITO);
            JButton btnEditar = Estilo.boton("Actualizar", Estilo.PRIMARIO);
            JButton btnEliminar = Estilo.boton("Eliminar", Estilo.PELIGRO);
            JButton btnLimpiar = Estilo.boton("Limpiar", Estilo.GRIS);
            JPanel abajo = new JPanel(new BorderLayout(0, 12));
            abajo.setOpaque(false);
            abajo.add(filaBotones(btnRegistrar, btnEditar, btnEliminar, btnLimpiar), BorderLayout.NORTH);
            abajo.add(avisos, BorderLayout.CENTER);
            centro.remove(avisos);
            centro.add(abajo, BorderLayout.CENTER);
            btnRegistrar.addActionListener(e -> registrar());
            btnEditar.addActionListener(e -> actualizar());
            btnEliminar.addActionListener(e -> eliminar());
            btnLimpiar.addActionListener(e -> limpiar());
        }
        panelDerecho.add(centro, BorderLayout.CENTER);
        panelDerecho.add(btnActualizar, BorderLayout.SOUTH);

        tabla.setEscucha(new TablaPaginada.Escucha() {
            @Override
            public void seleccionar(Object[] fila) {
                txtId.setText(String.valueOf(fila[0]));
                txtNombre.setText(String.valueOf(fila[1]));
                txtPrecio.setText(String.valueOf(fila[2]));
                pastillaEstado.poner(String.valueOf(fila[3]));
            }

            @Override
            public void eliminar(Object[] fila) {
                seleccionar(fila);
                if (admin) {
                    PanelPlatos.this.eliminar();
                }
            }
        });

        btnBuscar.addActionListener(e -> cargar());
        txtBuscar.addActionListener(e -> cargar());
        cmbEstado.addActionListener(e -> cargar());
        btnActualizar.addActionListener(e -> {
            PlatosOperaciones.actualizarDisponibilidadPlatos();
            cargar();
        });

        PlatosOperaciones.actualizarDisponibilidadPlatos();
        cargar();
    }

    private void cargar() {
        try {
            List<Plato> platos = platoDAO.listar();
            String filtro = txtBuscar.getText().trim().toLowerCase();
            String estadoFiltro = (String) cmbEstado.getSelectedItem();

            int disponibles = 0;
            List<Object[]> filas = new ArrayList<>();
            for (Plato p : platos) {
                boolean disponible = "ACTIVO".equals(platoDAO.obtenerEstado(p.getId()));
                if (disponible) {
                    disponibles++;
                }
                String estado = disponible ? "Disponible" : "No disponible";
                boolean coincideTexto = filtro.isEmpty() || String.valueOf(p.getId()).equals(filtro)
                        || p.getNombre().toLowerCase().contains(filtro);
                boolean coincideEstado = "Todos".equals(estadoFiltro) || estado.equals(estadoFiltro);
                if (coincideTexto && coincideEstado) {
                    filas.add(new Object[]{p.getId(), p.getNombre(),
                            String.format(java.util.Locale.US, "%.2f", p.getPrecio()), estado});
                }
            }
            lblTotal.setText(String.valueOf(platos.size()));
            lblDisponibles.setText(String.valueOf(disponibles));
            lblNoDisponibles.setText(String.valueOf(platos.size() - disponibles));
            tabla.setFilas(filas);
        } catch (Exception ex) {
            error("No se pudieron cargar los platos: " + ex.getMessage());
        }
    }

    private double precioEscrito() {
        try {
            return Double.parseDouble(txtPrecio.getText().trim().replace(',', '.'));
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    private boolean validar() {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            advertencia("Ingrese el nombre del plato.");
            return false;
        }
        if (nombre.length() > 100) {
            advertencia("El nombre del plato no puede pasar de 100 caracteres.");
            return false;
        }
        double precio = precioEscrito();
        if (Double.isNaN(precio) || precio <= 0) {
            advertencia("Ingrese un precio válido mayor que cero (por ejemplo 28.50).");
            return false;
        }
        return true;
    }

    private void registrar() {
        if (!validar()) {
            return;
        }
        int antes = platoDAO.listar().size();
        PlatosOperaciones.registrarPlato(txtNombre.getText().trim(), precioEscrito());
        if (platoDAO.listar().size() > antes) {
            aviso("Plato registrado correctamente.\nQuedará \"No disponible\" hasta que registres su receta.");
            limpiar();
            cargar();
        } else {
            error("No se pudo registrar el plato.");
        }
    }

    private void actualizar() {
        if (txtId.getText().isEmpty()) {
            advertencia("Seleccione un plato de la tabla para actualizarlo.");
            return;
        }
        if (!validar()) {
            return;
        }
        int id = Integer.parseInt(txtId.getText());
        PlatosOperaciones.actualizarPlato(id, txtNombre.getText().trim(), precioEscrito());
        Plato p = platoDAO.buscarPorId(id);
        if (p != null && p.getNombre().equals(txtNombre.getText().trim())
                && Math.abs(p.getPrecio() - precioEscrito()) < 0.001) {
            aviso("Plato actualizado correctamente.");
            cargar();
        } else {
            error("No se pudo actualizar el plato.");
        }
    }

    private void eliminar() {
        if (txtId.getText().isEmpty()) {
            advertencia("Seleccione un plato de la tabla para eliminarlo.");
            return;
        }
        int id = Integer.parseInt(txtId.getText());
        if (!confirmar("¿Eliminar el plato N.° " + id + " (" + txtNombre.getText().trim() + ")?")) {
            return;
        }
        PlatosOperaciones.eliminarPlato(id);
        if (platoDAO.buscarPorId(id) == null) {
            aviso("Plato eliminado correctamente.");
            limpiar();
            cargar();
        } else {
            error("No se pudo eliminar el plato.\nTiene una receta registrada o aparece en pedidos.");
        }
    }

    private void limpiar() {
        txtId.setText("");
        txtNombre.setText("");
        txtPrecio.setText("");
        pastillaEstado.poner("No disponible");
        tabla.limpiarSeleccion();
    }
}
