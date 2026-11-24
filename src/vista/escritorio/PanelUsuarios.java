package vista.escritorio;

import controlador.UsuarioOperaciones;
import dao.EmpleadoDAO;
import dao.impl.EmpleadoDAOImpl;
import modelo.Empleado;
import modelo.Usuario;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PanelUsuarios extends ModuloBase {

    private final UsuarioOperaciones usuarioOperaciones = new UsuarioOperaciones();
    private final EmpleadoDAO empleadoDAO = new EmpleadoDAOImpl();

    private final JLabel lblTotal;
    private final JLabel lblActivos;
    private final JLabel lblInactivos;

    private final JTextField txtBuscar = new CampoHint("ID, usuario o empleado...");
    private final TablaPaginada tabla = new TablaPaginada(
            new String[]{"ID", "Usuario", "Empleado", "Cargo", "Estado"}, 4, true);

    private final JTextField txtId = Estilo.campo();
    private final JComboBox<Opcion> cmbEmpleado = new JComboBox<>();
    private final JTextField txtUsuario = Estilo.campo();
    private final JPasswordField txtClave = new JPasswordField();
    private final JPasswordField txtClave2 = new JPasswordField();
    private final JComboBox<String> cmbEstado = new JComboBox<>(new String[]{"ACTIVO", "INACTIVO"});

    private static final class Opcion {
        final int id;
        final String texto;

        Opcion(int id, String texto) {
            this.id = id;
            this.texto = texto;
        }

        @Override
        public String toString() {
            return texto;
        }
    }

    public PanelUsuarios() {
        super("Usuarios", "Crea las cuentas con las que el personal inicia sesión", "☻", Estilo.PRIMARIO, true);

        lblTotal = agregarEstadistica("☻", new Color(0x38A3F5), "Total de usuarios", Estilo.PRIMARIO_OSCURO);
        lblActivos = agregarEstadistica("✓", Estilo.EXITO, "Usuarios activos", new Color(0x15803D));
        lblInactivos = agregarEstadistica("✕", Estilo.PELIGRO, "Usuarios inactivos", Estilo.PELIGRO);

        JButton btnBuscar = Estilo.boton("Buscar", Estilo.PRIMARIO);
        panelIzquierdo.add(barraBusqueda("Buscar por ID, usuario o empleado:", txtBuscar, null, btnBuscar), BorderLayout.NORTH);
        tabla.agregarColumnaPastilla(4);
        tabla.setAnchos(40, 120, 190, 100, 90);
        panelIzquierdo.add(tabla, BorderLayout.CENTER);

        txtId.setEditable(false);
        txtId.setBackground(new Color(0xEEF2F8));
        Estilo.estilizarCampo(txtClave);
        Estilo.estilizarCampo(txtClave2);
        for (JComboBox<?> c : new JComboBox[]{cmbEmpleado, cmbEstado}) {
            c.setFont(Estilo.FUENTE);
            c.setPreferredSize(new Dimension(110, 36));
        }

        Formulario f = new Formulario();
        f.agregar("ID:", false, txtId);
        f.agregar("Empleado:", true, cmbEmpleado);
        f.agregar("Nombre de usuario:", true, txtUsuario);
        f.agregar("Contraseña:", true, txtClave);
        f.agregar("Confirmar contraseña:", true, txtClave2);
        f.agregar("Estado:", true, cmbEstado);

        JButton btnRegistrar = Estilo.boton("Registrar", Estilo.EXITO);
        JButton btnClave = Estilo.boton("Cambiar contraseña", Estilo.PRIMARIO);
        JButton btnEstado = Estilo.boton("Cambiar estado", Estilo.ADVERTENCIA);
        JButton btnEliminar = Estilo.boton("Eliminar", Estilo.PELIGRO);
        JButton btnProbar = Estilo.boton("Probar acceso", Estilo.INFO);
        JButton btnLimpiar = Estilo.boton("Limpiar", Estilo.GRIS);

        JPanel botones = new JPanel(new GridLayout(2, 3, 10, 10));
        botones.setOpaque(false);
        botones.add(btnRegistrar);
        botones.add(btnClave);
        botones.add(btnEstado);
        botones.add(btnEliminar);
        botones.add(btnProbar);
        botones.add(btnLimpiar);

        JPanel avisos = new JPanel();
        avisos.setOpaque(false);
        avisos.setLayout(new BoxLayout(avisos, BoxLayout.Y_AXIS));
        avisos.add(Estilo.aviso("Cada empleado activo puede tener una sola cuenta. El cargo del empleado decide a qué ventana entra.", "info"));
        avisos.add(Box.createVerticalStrut(8));
        avisos.add(Estilo.aviso("Para cambiar la contraseña o el estado, seleccione un usuario de la tabla.", "alerta"));

        JPanel abajo = new JPanel(new BorderLayout(0, 14));
        abajo.setOpaque(false);
        abajo.add(botones, BorderLayout.NORTH);
        abajo.add(avisos, BorderLayout.CENTER);

        JPanel centro = new JPanel(new BorderLayout(0, 14));
        centro.setOpaque(false);
        centro.add(f, BorderLayout.NORTH);
        centro.add(abajo, BorderLayout.CENTER);

        panelDerecho.add(tituloDePanel("☻", "Registro de Usuario"), BorderLayout.NORTH);
        panelDerecho.add(centro, BorderLayout.CENTER);

        tabla.setEscucha(new TablaPaginada.Escucha() {
            @Override
            public void seleccionar(Object[] fila) {
                elegir(fila);
            }

            @Override
            public void eliminar(Object[] fila) {
                elegir(fila);
                PanelUsuarios.this.eliminar();
            }
        });
        btnBuscar.addActionListener(e -> cargar());
        txtBuscar.addActionListener(e -> cargar());
        btnRegistrar.addActionListener(e -> registrar());
        btnClave.addActionListener(e -> cambiarClave());
        btnEstado.addActionListener(e -> cambiarEstado());
        btnEliminar.addActionListener(e -> eliminar());
        btnProbar.addActionListener(e -> probarAcceso());
        btnLimpiar.addActionListener(e -> limpiar());

        cargarEmpleadosLibres();
        cargar();
    }

    // Empleados activos que todavía no tienen usuario
    private void cargarEmpleadosLibres() {
        Set<Integer> conUsuario = new HashSet<>();
        for (Usuario u : usuarioOperaciones.listarUsuarios()) {
            conUsuario.add(u.getIdEmpleado());
        }
        cmbEmpleado.removeAllItems();
        for (Empleado e : empleadoDAO.listar()) {
            if (!conUsuario.contains(e.getId()) && "ACTIVO".equalsIgnoreCase(e.getEstadoLaboral())) {
                cmbEmpleado.addItem(new Opcion(e.getId(),
                        e.getNombres() + " " + e.getApellidos() + " (" + e.getCargo() + ")"));
            }
        }
        cmbEmpleado.setEnabled(true);
    }

    private void cargar() {
        try {
            List<Usuario> todos = usuarioOperaciones.listarUsuarios();
            String filtro = txtBuscar.getText().trim().toLowerCase();
            List<Object[]> filas = new ArrayList<>();
            int activos = 0;
            for (Usuario u : todos) {
                if ("ACTIVO".equalsIgnoreCase(u.getEstado())) {
                    activos++;
                }
                if (filtro.isEmpty() || String.valueOf(u.getIdUsuario()).equals(filtro)
                        || u.getNombreUsuario().toLowerCase().contains(filtro)
                        || u.getNombreEmpleado().toLowerCase().contains(filtro)) {
                    filas.add(new Object[]{u.getIdUsuario(), u.getNombreUsuario(),
                            u.getNombreEmpleado(), u.getCargo(), u.getEstado()});
                }
            }
            lblTotal.setText(String.valueOf(todos.size()));
            lblActivos.setText(String.valueOf(activos));
            lblInactivos.setText(String.valueOf(todos.size() - activos));
            tabla.setFilas(filas);
        } catch (Exception ex) {
            error("No se pudieron cargar los usuarios: " + ex.getMessage());
        }
    }

    private void elegir(Object[] fila) {
        txtId.setText(String.valueOf(fila[0]));
        txtUsuario.setText(String.valueOf(fila[1]));
        cmbEmpleado.removeAllItems();
        cmbEmpleado.addItem(new Opcion(0, fila[2] + " (" + fila[3] + ")"));
        cmbEmpleado.setEnabled(false);
        cmbEstado.setSelectedItem(String.valueOf(fila[4]));
        txtClave.setText("");
        txtClave2.setText("");
    }

    private boolean hayUsuarioElegido() {
        if (txtId.getText().isEmpty()) {
            advertencia("Seleccione un usuario de la tabla.");
            return false;
        }
        return true;
    }

    private boolean clavesValidas() {
        String c1 = new String(txtClave.getPassword());
        String c2 = new String(txtClave2.getPassword());
        if (c1.isEmpty()) {
            advertencia("Ingrese la contraseña.");
            return false;
        }
        if (c1.length() < 6) {
            advertencia("La contraseña debe tener al menos 6 caracteres.");
            return false;
        }
        if (!c1.equals(c2)) {
            advertencia("Las contraseñas no coinciden.");
            return false;
        }
        return true;
    }

    private void registrar() {
        if (!txtId.getText().isEmpty()) {
            advertencia("Hay un usuario seleccionado. Pulse \"Limpiar\" para registrar uno nuevo.");
            return;
        }
        Opcion emp = (Opcion) cmbEmpleado.getSelectedItem();
        if (emp == null) {
            advertencia("No hay empleados activos sin usuario. Registre primero al empleado.");
            return;
        }
        String nombre = txtUsuario.getText().trim();
        if (nombre.isEmpty() || nombre.length() > 50 || nombre.contains(" ")) {
            advertencia("Ingrese un nombre de usuario sin espacios (máximo 50 caracteres).");
            return;
        }
        if (!usuarioOperaciones.nombreDisponible(nombre)) {
            advertencia("El nombre de usuario \"" + nombre + "\" ya existe. Elija otro.");
            return;
        }
        if (!clavesValidas()) {
            return;
        }
        int id = usuarioOperaciones.registrarUsuario(emp.id, nombre, new String(txtClave.getPassword()));
        if (id > 0) {
            if ("INACTIVO".equals(cmbEstado.getSelectedItem())) {
                usuarioOperaciones.cambiarEstado(id, "INACTIVO");
            }
            aviso("Usuario registrado. Ya puede iniciar sesión con \"" + nombre + "\".");
            limpiar();
            cargar();
        } else {
            error("No se pudo registrar el usuario.\nRevise que la tabla \"usuarios\" exista en la base de datos.");
        }
    }

    private void cambiarClave() {
        if (!hayUsuarioElegido() || !clavesValidas()) {
            return;
        }
        int id = Integer.parseInt(txtId.getText());
        if (usuarioOperaciones.cambiarContrasena(id, new String(txtClave.getPassword()))) {
            aviso("Contraseña cambiada correctamente.");
            txtClave.setText("");
            txtClave2.setText("");
        } else {
            error("No se pudo cambiar la contraseña.");
        }
    }

    private void cambiarEstado() {
        if (!hayUsuarioElegido()) {
            return;
        }
        int id = Integer.parseInt(txtId.getText());
        String estado = String.valueOf(cmbEstado.getSelectedItem());
        if (usuarioOperaciones.cambiarEstado(id, estado)) {
            aviso("El usuario ahora está " + estado + ".");
            limpiar();
            cargar();
        } else {
            error("No se pudo cambiar el estado.");
        }
    }

    private void eliminar() {
        if (!hayUsuarioElegido()) {
            return;
        }
        int id = Integer.parseInt(txtId.getText());
        if (!confirmar("¿Eliminar al usuario \"" + txtUsuario.getText().trim() + "\"?\nYa no podrá iniciar sesión.")) {
            return;
        }
        if (usuarioOperaciones.eliminarUsuario(id)) {
            aviso("Usuario eliminado correctamente.");
            limpiar();
            cargar();
        } else {
            error("No se pudo eliminar el usuario.");
        }
    }

    // Usa el mismo método que el login para comprobar usuario y contraseña
    private void probarAcceso() {
        String nombre = txtUsuario.getText().trim();
        String clave = new String(txtClave.getPassword());
        if (nombre.isEmpty() || clave.isEmpty()) {
            advertencia("Escriba el usuario y la contraseña que desea probar.");
            return;
        }
        Usuario u = usuarioOperaciones.autenticar(nombre, clave);
        if (u != null) {
            aviso("Acceso correcto. Entraría como " + u.getCargo() + ".");
        } else {
            advertencia("Acceso denegado. Usuario o contraseña incorrectos,\no el usuario/empleado está inactivo.");
        }
    }

    private void limpiar() {
        txtId.setText("");
        txtUsuario.setText("");
        txtClave.setText("");
        txtClave2.setText("");
        cmbEstado.setSelectedItem("ACTIVO");
        cargarEmpleadosLibres();
        tabla.limpiarSeleccion();
    }
}
