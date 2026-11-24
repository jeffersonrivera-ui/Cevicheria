package vista.escritorio;

import controlador.ProveedorOperaciones;
import controlador.ProveedorOperaciones.Resultado;
import modelo.Proveedor;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** Sección Proveedores: a quién se le compran los ingredientes (administrador). */
public class PanelProveedores extends ModuloBase {

    private List<Proveedor> proveedores = new ArrayList<>();

    private final JLabel lblTotal;
    private final JLabel lblActivos;
    private final JLabel lblInactivos;

    private final JTextField txtBuscar = Estilo.campo();
    private final TablaPaginada tabla = new TablaPaginada(
            new String[]{"ID", "Proveedor", "RUC", "Teléfono", "Productos", "Estado"}, 5, true);

    private final JTextField txtId = Estilo.campo();
    private final JTextField txtNombre = Estilo.campo();
    private final JTextField txtRuc = Estilo.campo();
    private final JTextField txtTelefono = Estilo.campo();
    private final JTextField txtDireccion = Estilo.campo();
    private final JTextField txtProductos = Estilo.campo();
    private final JComboBox<String> cmbEstado = new JComboBox<>(new String[]{Proveedor.ACTIVO, Proveedor.INACTIVO});

    public PanelProveedores() {
        super("Proveedores", "Administra a quién se le compran los ingredientes", "☖", Estilo.PRIMARIO, true);

        lblTotal = agregarEstadistica("☖", new Color(0x38A3F5), "Total de proveedores", Estilo.PRIMARIO_OSCURO);
        lblActivos = agregarEstadistica("✓", Estilo.EXITO, "Activos", Estilo.EXITO);
        lblInactivos = agregarEstadistica("∅", Estilo.PELIGRO, "Inactivos", Estilo.PELIGRO);

        // ----- Izquierda -----
        JButton btnBuscar = Estilo.boton("Buscar", Estilo.PRIMARIO);
        txtBuscar.setToolTipText("ID, nombre, RUC o productos");
        panelIzquierdo.add(barraBusqueda("Buscar por ID, nombre, RUC o productos:", txtBuscar, null, btnBuscar),
                BorderLayout.NORTH);
        tabla.setAnchos(30, 130, 90, 80, 130, 75, 55);
        panelIzquierdo.add(tabla, BorderLayout.CENTER);

        // ----- Derecha -----
        txtId.setEditable(false);
        txtId.setBackground(new Color(0xEEF2F8));
        cmbEstado.setFont(Estilo.FUENTE);
        cmbEstado.setPreferredSize(new Dimension(120, 36));
        txtRuc.setToolTipText("Opcional: 11 dígitos");

        Formulario f = new Formulario();
        f.agregar("ID:", false, txtId);
        f.agregar("Proveedor:", true, txtNombre);
        f.agregar("RUC:", false, txtRuc);
        f.agregar("Teléfono:", false, txtTelefono);
        f.agregar("Dirección:", false, txtDireccion);
        f.agregar("Productos:", false, txtProductos);
        f.agregar("Estado:", true, cmbEstado);

        JButton btnRegistrar = Estilo.boton("Registrar", Estilo.EXITO);
        JButton btnActualizar = Estilo.boton("Actualizar", Estilo.PRIMARIO);
        JButton btnEliminar = Estilo.boton("Eliminar", Estilo.PELIGRO);
        JButton btnLimpiar = Estilo.boton("Limpiar", Estilo.GRIS);

        JPanel avisos = new JPanel();
        avisos.setOpaque(false);
        avisos.setLayout(new BoxLayout(avisos, BoxLayout.Y_AXIS));
        avisos.add(Estilo.aviso("El RUC es opcional (por ejemplo, para vendedores del mercado). "
                + "En Productos escriba qué vende: pescado, limones, ajíes...", "info", 210));
        avisos.add(Box.createVerticalStrut(8));
        avisos.add(Estilo.aviso("Un proveedor con compras no se puede eliminar: cámbielo a INACTIVO para que "
                + "no aparezca al registrar compras.", "alerta", 210));

        JPanel centro = new JPanel(new BorderLayout(0, 12));
        centro.setOpaque(false);
        centro.add(f, BorderLayout.NORTH);
        centro.add(avisos, BorderLayout.CENTER);

        JPanel botones = new JPanel(new GridLayout(2, 2, 10, 10));
        botones.setOpaque(false);
        botones.add(btnRegistrar);
        botones.add(btnActualizar);
        botones.add(btnEliminar);
        botones.add(btnLimpiar);

        panelDerecho.add(tituloDePanel("☖", "Registro / Actualización de Proveedor"), BorderLayout.NORTH);
        panelDerecho.add(centro, BorderLayout.CENTER);
        panelDerecho.add(botones, BorderLayout.SOUTH);
        setAnchoDerecho(340);

        // ----- Eventos -----
        tabla.setEscucha(new TablaPaginada.Escucha() {
            @Override
            public void seleccionar(Object[] fila) {
                cargarEnFormulario(((Number) fila[0]).intValue());
            }

            @Override
            public void eliminar(Object[] fila) {
                cargarEnFormulario(((Number) fila[0]).intValue());
                eliminarProveedor();
            }

            @Override
            public void ver(Object[] fila) {
                Proveedor p = ProveedorOperaciones.buscarProveedor(((Number) fila[0]).intValue());
                if (p != null) {
                    aviso("Proveedor N.° " + p.getId() + "\nNombre: " + p.getNombre()
                            + "\nRUC: " + nz(p.getRuc()) + "\nTeléfono: " + nz(p.getTelefono())
                            + "\nDirección: " + nz(p.getDireccion()) + "\nProductos: " + nz(p.getProductos())
                            + "\nEstado: " + p.getEstado());
                }
            }
        });
        btnBuscar.addActionListener(e -> filtrar());
        txtBuscar.addActionListener(e -> filtrar());
        btnRegistrar.addActionListener(e -> registrar());
        btnActualizar.addActionListener(e -> actualizar());
        btnEliminar.addActionListener(e -> eliminarProveedor());
        btnLimpiar.addActionListener(e -> limpiar());

        cargar();
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private void cargar() {
        try {
            proveedores = ProveedorOperaciones.listarProveedores();
        } catch (Exception ex) {
            error("No se pudieron cargar los proveedores: " + ex.getMessage());
            return;
        }
        int activos = 0;
        for (Proveedor p : proveedores) {
            if (p.estaActivo()) {
                activos++;
            }
        }
        lblTotal.setText(String.valueOf(proveedores.size()));
        lblActivos.setText(String.valueOf(activos));
        lblInactivos.setText(String.valueOf(proveedores.size() - activos));
        filtrar();
    }

    private void filtrar() {
        String filtro = txtBuscar.getText().trim().toLowerCase();
        List<Object[]> filas = new ArrayList<>();
        for (Proveedor p : proveedores) {
            String texto = (nz(p.getNombre()) + " " + nz(p.getRuc()) + " " + nz(p.getProductos())).toLowerCase();
            if (filtro.isEmpty() || String.valueOf(p.getId()).equals(filtro) || texto.contains(filtro)) {
                filas.add(new Object[]{p.getId(), p.getNombre(), nz(p.getRuc()), nz(p.getTelefono()),
                        nz(p.getProductos()), p.getEstado()});
            }
        }
        tabla.setFilas(filas);
    }

    private void cargarEnFormulario(int id) {
        Proveedor p = ProveedorOperaciones.buscarProveedor(id);
        if (p == null) {
            return;
        }
        txtId.setText(String.valueOf(p.getId()));
        txtNombre.setText(p.getNombre());
        txtRuc.setText(nz(p.getRuc()));
        txtTelefono.setText(nz(p.getTelefono()));
        txtDireccion.setText(nz(p.getDireccion()));
        txtProductos.setText(nz(p.getProductos()));
        cmbEstado.setSelectedItem(p.getEstado());
    }

    private void registrar() {
        Resultado r = ProveedorOperaciones.registrarProveedor(txtNombre.getText(), txtRuc.getText(),
                txtTelefono.getText(), txtDireccion.getText(), txtProductos.getText());
        if (r.ok) {
            aviso(r.mensaje);
            limpiar();
            cargar();
        } else {
            advertencia(r.mensaje);
        }
    }

    private void actualizar() {
        if (txtId.getText().isEmpty()) {
            advertencia("Seleccione un proveedor de la tabla para actualizarlo.");
            return;
        }
        Resultado r = ProveedorOperaciones.actualizarProveedor(Integer.parseInt(txtId.getText()),
                txtNombre.getText(), txtRuc.getText(), txtTelefono.getText(), txtDireccion.getText(),
                txtProductos.getText(), (String) cmbEstado.getSelectedItem());
        if (r.ok) {
            aviso(r.mensaje);
            cargar();
        } else {
            advertencia(r.mensaje);
        }
    }

    private void eliminarProveedor() {
        if (txtId.getText().isEmpty()) {
            advertencia("Seleccione un proveedor de la tabla para eliminarlo.");
            return;
        }
        int id = Integer.parseInt(txtId.getText());
        if (!confirmar("¿Eliminar al proveedor N.° " + id + "?")) {
            return;
        }
        Resultado r = ProveedorOperaciones.eliminarProveedor(id);
        if (r.ok) {
            aviso(r.mensaje);
            limpiar();
            cargar();
        } else {
            error(r.mensaje);
        }
    }

    private void limpiar() {
        txtId.setText("");
        txtNombre.setText("");
        txtRuc.setText("");
        txtTelefono.setText("");
        txtDireccion.setText("");
        txtProductos.setText("");
        cmbEstado.setSelectedIndex(0);
        tabla.limpiarSeleccion();
    }
}
