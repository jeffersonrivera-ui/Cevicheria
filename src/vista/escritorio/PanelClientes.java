package vista.escritorio;

import controlador.ClienteOperaciones;
import dao.ClienteDAO;
import dao.PedidoDAO;
import dao.impl.ClienteDAOImpl;
import dao.impl.PedidoDAOImpl;
import modelo.Cliente;
import modelo.Pedido;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PanelClientes extends ModuloBase {

    private final ClienteDAO clienteDAO = new ClienteDAOImpl();
    private final PedidoDAO pedidoDAO = new PedidoDAOImpl();

    private final JLabel lblTotal;
    private final JLabel lblConPedidos;
    private final JLabel lblSinPedidos;

    private final JTextField txtBuscar = Estilo.campo();
    private final TablaPaginada tabla = new TablaPaginada(
            new String[]{"ID", "Nombres y Apellidos", "Teléfono"}, -1, true);

    private final JTextField txtId = Estilo.campo();
    private final JTextField txtNombres = Estilo.campo();
    private final JTextField txtApellidos = Estilo.campo();
    private final JTextField txtTelefono = Estilo.campo();

    public PanelClientes() {
        super("Clientes", "Administra los clientes de la cevichería", "\u263A", Estilo.PRIMARIO, true);

        lblTotal = agregarEstadistica("\u263A", new Color(0x38A3F5), "Total de clientes", Estilo.PRIMARIO_OSCURO);
        lblConPedidos = agregarEstadistica("\u2713", Estilo.EXITO, "Con pedidos", Estilo.EXITO);
        lblSinPedidos = agregarEstadistica("\u2205", Estilo.PELIGRO, "Sin pedidos", Estilo.PELIGRO);

        // Izquierda: busqueda + tabla
        JButton btnBuscar = Estilo.boton("Buscar", Estilo.PRIMARIO);
        txtBuscar.setToolTipText("ID, nombre o apellido");
        panelIzquierdo.add(barraBusqueda("Buscar por ID, nombre o apellido:", txtBuscar, null, btnBuscar),
                BorderLayout.NORTH);
        panelIzquierdo.add(tabla, BorderLayout.CENTER);

        // Derecha: formulario
        txtId.setEditable(false);
        txtId.setBackground(new Color(0xEEF2F8));

        Formulario f = new Formulario();
        f.agregar("ID:", false, txtId);
        f.agregar("Nombres:", true, txtNombres);
        f.agregar("Apellidos:", true, txtApellidos);
        f.agregar("Teléfono:", true, txtTelefono);

        JButton btnRegistrar = Estilo.boton("Registrar", Estilo.EXITO);
        JButton btnActualizar = Estilo.boton("Actualizar", Estilo.PRIMARIO);
        JButton btnEliminar = Estilo.boton("Eliminar", Estilo.PELIGRO);
        JButton btnLimpiar = Estilo.boton("Limpiar", Estilo.GRIS);

        JPanel centro = new JPanel(new BorderLayout(0, 12));
        centro.setOpaque(false);
        centro.add(f, BorderLayout.NORTH);
        JPanel avisos = new JPanel();
        avisos.setOpaque(false);
        avisos.setLayout(new BoxLayout(avisos, BoxLayout.Y_AXIS));
        avisos.add(Estilo.aviso("Selecciona un cliente de la tabla para editarlo o eliminarlo.", "info"));
        avisos.add(Box.createVerticalStrut(8));
        avisos.add(Estilo.aviso("No se puede eliminar un cliente que ya tiene pedidos registrados.", "alerta"));
        centro.add(avisos, BorderLayout.CENTER);

        panelDerecho.add(tituloDePanel("\u263A", "Registro / Actualización de Cliente"), BorderLayout.NORTH);
        panelDerecho.add(centro, BorderLayout.CENTER);
        panelDerecho.add(filaBotones(btnRegistrar, btnActualizar, btnEliminar, btnLimpiar), BorderLayout.SOUTH);

        // Eventos
        tabla.setEscucha(new TablaPaginada.Escucha() {
            @Override
            public void seleccionar(Object[] fila) {
                cargarEnFormulario(((Number) fila[0]).intValue());
            }

            @Override
            public void eliminar(Object[] fila) {
                cargarEnFormulario(((Number) fila[0]).intValue());
                eliminarCliente();
            }

            @Override
            public void ver(Object[] fila) {
                Cliente c = clienteDAO.buscarPorId(((Number) fila[0]).intValue());
                if (c != null) {
                    aviso("Cliente N.° " + c.getId() + "\nNombres: " + c.getNombres()
                            + "\nApellidos: " + c.getApellidos() + "\nTeléfono: " + c.getTelefono());
                }
            }
        });
        btnBuscar.addActionListener(e -> cargar());
        txtBuscar.addActionListener(e -> cargar());
        btnRegistrar.addActionListener(e -> registrar());
        btnActualizar.addActionListener(e -> actualizar());
        btnEliminar.addActionListener(e -> eliminarCliente());
        btnLimpiar.addActionListener(e -> limpiar());

        cargar();
    }

    private void cargar() {
        try {
            List<Cliente> todos = clienteDAO.listar();
            Set<Integer> conPedido = new HashSet<>();
            for (Pedido p : pedidoDAO.listar()) {
                if (p.getCliente() != null) {
                    conPedido.add(p.getCliente().getId());
                }
            }

            int total = todos == null ? 0 : todos.size();
            int con = 0;
            String filtro = txtBuscar.getText().trim().toLowerCase();
            List<Object[]> filas = new ArrayList<>();

            if (todos != null) {
                for (Cliente c : todos) {
                    if (conPedido.contains(c.getId())) {
                        con++;
                    }
                    String completo = (c.getNombres() + " " + c.getApellidos()).toLowerCase();
                    if (filtro.isEmpty() || String.valueOf(c.getId()).equals(filtro) || completo.contains(filtro)) {
                        filas.add(new Object[]{c.getId(), c.getNombres() + " " + c.getApellidos(), c.getTelefono()});
                    }
                }
            }
            lblTotal.setText(String.valueOf(total));
            lblConPedidos.setText(String.valueOf(con));
            lblSinPedidos.setText(String.valueOf(total - con));
            tabla.setFilas(filas);
        } catch (Exception ex) {
            error("No se pudieron cargar los clientes: " + ex.getMessage());
        }
    }

    private void cargarEnFormulario(int id) {
        Cliente c = clienteDAO.buscarPorId(id);
        if (c == null) {
            return;
        }
        txtId.setText(String.valueOf(c.getId()));
        txtNombres.setText(c.getNombres());
        txtApellidos.setText(c.getApellidos());
        txtTelefono.setText(c.getTelefono());
    }

    private boolean validar() {
        if (txtNombres.getText().trim().isEmpty() || txtApellidos.getText().trim().isEmpty()
                || txtTelefono.getText().trim().isEmpty()) {
            advertencia("Complete nombres, apellidos y teléfono.");
            return false;
        }
        if (!txtTelefono.getText().trim().matches("[0-9+ ]{6,15}")) {
            advertencia("El teléfono solo puede tener números (de 6 a 15 dígitos).");
            return false;
        }
        return true;
    }

    private void registrar() {
        if (!validar()) {
            return;
        }
        int antes = clienteDAO.listar().size();
        ClienteOperaciones.registrarCliente(txtNombres.getText().trim(),
                txtApellidos.getText().trim(), txtTelefono.getText().trim());
        int despues = clienteDAO.listar().size();
        if (despues > antes) {
            aviso("Cliente registrado correctamente.");
            limpiar();
            cargar();
        } else {
            error("No se pudo registrar el cliente.");
        }
    }

    private void actualizar() {
        if (txtId.getText().isEmpty()) {
            advertencia("Seleccione un cliente de la tabla para actualizarlo.");
            return;
        }
        if (!validar()) {
            return;
        }
        int id = Integer.parseInt(txtId.getText());
        ClienteOperaciones.actualizarCliente(id, txtNombres.getText().trim(),
                txtApellidos.getText().trim(), txtTelefono.getText().trim());
        Cliente c = clienteDAO.buscarPorId(id);
        if (c != null && c.getNombres().equals(txtNombres.getText().trim())
                && c.getApellidos().equals(txtApellidos.getText().trim())) {
            aviso("Cliente actualizado correctamente.");
            cargar();
        } else {
            error("No se pudo actualizar el cliente.");
        }
    }

    private void eliminarCliente() {
        if (txtId.getText().isEmpty()) {
            advertencia("Seleccione un cliente de la tabla para eliminarlo.");
            return;
        }
        int id = Integer.parseInt(txtId.getText());
        if (!confirmar("¿Eliminar al cliente N.° " + id + "?")) {
            return;
        }
        ClienteOperaciones.eliminarCliente(id);
        if (!ClienteOperaciones.existeCliente(id)) {
            aviso("Cliente eliminado correctamente.");
            limpiar();
            cargar();
        } else {
            error("No se pudo eliminar el cliente. Verifique si tiene pedidos relacionados.");
        }
    }

    private void limpiar() {
        txtId.setText("");
        txtNombres.setText("");
        txtApellidos.setText("");
        txtTelefono.setText("");
        tabla.limpiarSeleccion();
    }
}
