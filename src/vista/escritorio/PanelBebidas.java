package vista.escritorio;

import controlador.BebidasOperaciones;
import dao.BebidaDAO;
import dao.impl.BebidaDAOImpl;
import modelo.Bebida;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PanelBebidas extends ModuloBase {

    private final BebidaDAO bebidaDAO = new BebidaDAOImpl();

    private final JLabel lblTotal;
    private final JLabel lblPromedio;
    private final JLabel lblMasCara;

    private final JTextField txtBuscar = new CampoHint("ID o nombre de la bebida...");
    private final TablaPaginada tabla = new TablaPaginada(
            new String[]{"ID", "Nombre de la bebida", "Precio (S/)", "Cantidad"}, -1, true);

    private final JTextField txtId = Estilo.campo();
    private final JTextField txtNombre = Estilo.campo();
    private final JTextField txtPrecio = Estilo.campo();
    private final JTextField txtCantidad = Estilo.campo();

    public PanelBebidas() {
        super("Bebidas", "Administra las bebidas de la carta de la cevichería", "☕", Estilo.PRIMARIO, true);

        lblTotal = agregarEstadistica("☕", new Color(0x38A3F5), "Total de bebidas", Estilo.PRIMARIO_OSCURO);
        lblPromedio = agregarEstadistica("S", Estilo.EXITO, "Precio promedio", new Color(0x15803D));
        lblMasCara = agregarEstadistica("↑", new Color(0xF97316), "Precio más alto", new Color(0xC2410C));

        JButton btnBuscar = Estilo.boton("Buscar", Estilo.PRIMARIO);
        panelIzquierdo.add(barraBusqueda("Buscar por ID o nombre:", txtBuscar, null, btnBuscar), BorderLayout.NORTH);
        panelIzquierdo.add(tabla, BorderLayout.CENTER);

        txtId.setEditable(false);
        txtId.setBackground(new Color(0xEEF2F8));

        Formulario f = new Formulario();
        f.agregar("ID:", false, txtId);
        f.agregar("Nombre de la bebida:", true, txtNombre);
        f.agregar("Precio (S/):", true, txtPrecio);
        f.agregar("Cantidad:", true, txtCantidad);

        JButton btnRegistrar = Estilo.boton("Registrar", Estilo.EXITO);
        JButton btnActualizar = Estilo.boton("Actualizar", Estilo.PRIMARIO);
        JButton btnEliminar = Estilo.boton("Eliminar", Estilo.PELIGRO);
        JButton btnLimpiar = Estilo.boton("Limpiar", Estilo.GRIS);

        JPanel avisos = new JPanel();
        avisos.setOpaque(false);
        avisos.setLayout(new BoxLayout(avisos, BoxLayout.Y_AXIS));
        avisos.add(Estilo.aviso("Cantidad = unidades disponibles. Cada pedido la descuenta y se devuelve si se quita la bebida.", "info"));
        avisos.add(Box.createVerticalStrut(8));
        avisos.add(Estilo.aviso("No se puede eliminar una bebida que ya aparece en pedidos.", "alerta"));

        JPanel abajo = new JPanel(new BorderLayout(0, 14));
        abajo.setOpaque(false);
        abajo.add(filaBotones(btnRegistrar, btnActualizar, btnEliminar, btnLimpiar), BorderLayout.NORTH);
        abajo.add(avisos, BorderLayout.CENTER);

        JPanel centro = new JPanel(new BorderLayout(0, 14));
        centro.setOpaque(false);
        centro.add(f, BorderLayout.NORTH);
        centro.add(abajo, BorderLayout.CENTER);

        panelDerecho.add(tituloDePanel("☕", "Registro / Actualización de Bebida"), BorderLayout.NORTH);
        panelDerecho.add(centro, BorderLayout.CENTER);

        tabla.setEscucha(new TablaPaginada.Escucha() {
            @Override
            public void seleccionar(Object[] fila) {
                txtId.setText(String.valueOf(fila[0]));
                txtNombre.setText(String.valueOf(fila[1]));
                txtPrecio.setText(String.valueOf(fila[2]));
                txtCantidad.setText(String.valueOf(fila[3]));
            }

            @Override
            public void eliminar(Object[] fila) {
                seleccionar(fila);
                PanelBebidas.this.eliminar();
            }
        });
        btnBuscar.addActionListener(e -> cargar());
        txtBuscar.addActionListener(e -> cargar());
        btnRegistrar.addActionListener(e -> registrar());
        btnActualizar.addActionListener(e -> actualizar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiar());

        cargar();
    }

    private static String dos(double v) {
        return String.format(Locale.US, "%.2f", v);
    }

    private void cargar() {
        try {
            List<Bebida> todas = bebidaDAO.listar();
            String filtro = txtBuscar.getText().trim().toLowerCase();
            List<Object[]> filas = new ArrayList<>();
            double suma = 0;
            double max = 0;
            for (Bebida b : todas) {
                suma += b.getPrecio();
                max = Math.max(max, b.getPrecio());
                if (filtro.isEmpty() || String.valueOf(b.getId()).equals(filtro)
                        || b.getNombre().toLowerCase().contains(filtro)) {
                    filas.add(new Object[]{b.getId(), b.getNombre(), dos(b.getPrecio()), b.getStock()});
                }
            }
            lblTotal.setText(String.valueOf(todas.size()));
            lblPromedio.setText(Estilo.soles(todas.isEmpty() ? 0 : suma / todas.size()));
            lblMasCara.setText(Estilo.soles(max));
            tabla.setFilas(filas);
        } catch (Exception ex) {
            error("No se pudieron cargar las bebidas: " + ex.getMessage());
        }
    }

    private double precioEscrito() {
        try {
            return Double.parseDouble(txtPrecio.getText().trim().replace(',', '.'));
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    private int cantidadEscrita() {
        try {
            return Integer.parseInt(txtCantidad.getText().trim());
        } catch (Exception e) {
            return -1;
        }
    }

    private boolean validar() {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            advertencia("Ingrese el nombre de la bebida.");
            return false;
        }
        if (nombre.length() > 100) {
            advertencia("El nombre no puede pasar de 100 caracteres.");
            return false;
        }
        double precio = precioEscrito();
        if (Double.isNaN(precio) || precio <= 0) {
            advertencia("Ingrese un precio válido mayor que cero (por ejemplo 6.50).");
            return false;
        }
        if (cantidadEscrita() < 0 || cantidadEscrita() > 100000) {
            advertencia("Ingrese una cantidad válida (número entero, 0 o más).");
            return false;
        }
        return true;
    }

    private void registrar() {
        if (!validar()) {
            return;
        }
        int antes = bebidaDAO.listar().size();
        BebidasOperaciones.registrarBebida(txtNombre.getText().trim(), precioEscrito(), cantidadEscrita());
        if (bebidaDAO.listar().size() > antes) {
            aviso("Bebida registrada correctamente.");
            limpiar();
            cargar();
        } else {
            error("No se pudo registrar la bebida.\nRevise que la tabla \"bebidas\" exista en la base de datos.");
        }
    }

    private void actualizar() {
        if (txtId.getText().isEmpty()) {
            advertencia("Seleccione una bebida de la tabla para actualizarla.");
            return;
        }
        if (!validar()) {
            return;
        }
        int id = Integer.parseInt(txtId.getText());
        BebidasOperaciones.actualizarBebida(id, txtNombre.getText().trim(), precioEscrito(), cantidadEscrita());
        Bebida b = bebidaDAO.buscarPorId(id);
        if (b != null && b.getNombre().equals(txtNombre.getText().trim())
                && Math.abs(b.getPrecio() - precioEscrito()) < 0.001 && b.getStock() == cantidadEscrita()) {
            aviso("Bebida actualizada correctamente.");
            cargar();
        } else {
            error("No se pudo actualizar la bebida.");
        }
    }

    private void eliminar() {
        if (txtId.getText().isEmpty()) {
            advertencia("Seleccione una bebida de la tabla para eliminarla.");
            return;
        }
        int id = Integer.parseInt(txtId.getText());
        if (!confirmar("¿Eliminar la bebida N.° " + id + " (" + txtNombre.getText().trim() + ")?")) {
            return;
        }
        BebidasOperaciones.eliminarBebida(id);
        if (bebidaDAO.buscarPorId(id) == null) {
            aviso("Bebida eliminada correctamente.");
            limpiar();
            cargar();
        } else {
            error("No se pudo eliminar la bebida.\nAparece en pedidos registrados.");
        }
    }

    private void limpiar() {
        txtId.setText("");
        txtNombre.setText("");
        txtPrecio.setText("");
        txtCantidad.setText("");
        tabla.limpiarSeleccion();
    }
}
