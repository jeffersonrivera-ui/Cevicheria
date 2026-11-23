package vista.escritorio;

import controlador.EmpleadosOperaciones;
import dao.EmpleadoDAO;
import dao.impl.EmpleadoDAOImpl;
import modelo.Empleado;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PanelEmpleados extends ModuloBase {

    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private static final String[] CARGOS = {"Mesero", "Cocinero", "Administrador"};
    private static final String[] TURNOS = {"Mañana", "Tarde", "Noche"};
    private static final String[] ESTADOS = {"Activo", "Renunciado", "Despedido"};
    private static final String[] DISTRITOS = {"", "Ate", "Santa Anita", "La Molina", "Surco", "San Borja", "Miraflores",
            "San Isidro", "Lince", "Jesús María", "Breña", "La Victoria", "San Luis", "El Agustino", "Cercado de Lima",
            "Chorrillos", "Villa El Salvador", "Villa María del Triunfo", "San Juan de Lurigancho",
            "San Juan de Miraflores", "Los Olivos", "Comas", "Independencia", "Rímac", "Chosica", "Callao"};
    private static final String[] PROVINCIAS = {"", "Lima", "Callao", "Huaral", "Cañete", "Huaura"};
    private static final String[] DEPARTAMENTOS = {"", "Lima", "Callao", "Arequipa", "Cusco", "La Libertad", "Piura",
            "Junín", "Lambayeque", "Ica", "Áncash"};

    private final EmpleadoDAO empleadoDAO = new EmpleadoDAOImpl();
    private List<Empleado> cache = new ArrayList<>();
    private int idSeleccionado = 0;

    private final JLabel lblTotal;
    private final JLabel lblMeseros;
    private final JLabel lblCocineros;

    // Busqueda
    private final JTextField txtBuscar = new CampoHint("Ingrese ID, nombre o DNI...");
    private final JComboBox<String> cmbCargoFiltro = new JComboBox<>(new String[]{"Todos", "Mesero", "Cocinero", "Administrador"});
    private final JComboBox<String> cmbEstadoFiltro = new JComboBox<>(new String[]{"Todos", "Activo", "Renunciado", "Despedido"});
    private final TablaPaginada tabla = new TablaPaginada(
            new String[]{"ID", "Nombres y apellidos", "Cargo", "DNI", "Teléfono", "Estado laboral"}, 2, true);

    // Formulario
    private final JTextField txtNombres = new CampoHint("Ej. Carlos");
    private final JTextField txtApellidos = new CampoHint("Ej. Rojas Mendoza");
    private final JTextField txtDni = new CampoHint("Ej. 71234567");
    private final JTextField txtTelefono = new CampoHint("Ej. 987654321");
    private final JComboBox<String> cmbCargo = new JComboBox<>(CARGOS);
    private final JTextField txtCorreo = new CampoHint("Ej. carlos@correo.com");
    private final JTextField txtContacto = new CampoHint("Ej. 987654321");
    private final JTextField txtDireccion = new CampoHint("Ej. Av. Los Pescadores 123");
    private final JComboBox<String> cmbDistrito = new JComboBox<>(DISTRITOS);
    private final JComboBox<String> cmbProvincia = new JComboBox<>(PROVINCIAS);
    private final JComboBox<String> cmbDepartamento = new JComboBox<>(DEPARTAMENTOS);
    private final JTextField txtCodigoPostal = new CampoHint("Ej. 15001");
    private final JTextField txtReferencia = new CampoHint("Ej. Frente al mercado");
    private final JSpinner spnSalario = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 1000000.0, 50.0));
    private final JComboBox<String> cmbTurno = new JComboBox<>(TURNOS);
    private final JTextField txtFecha = new CampoHint("dd/mm/aaaa");

    // Estado laboral
    private final JTextField txtDniEstado = new CampoHint("Ingrese DNI...");
    private final JComboBox<String> cmbNuevoEstado = new JComboBox<>(ESTADOS);

    public PanelEmpleados() {
        super("Empleados", "Administra la información y el estado laboral del personal", "☻", Estilo.PRIMARIO, true);

        lblTotal = agregarEstadistica("☻", new Color(0x38A3F5), "Total de empleados", Estilo.PRIMARIO_OSCURO);
        lblMeseros = agregarEstadistica("☺", Estilo.EXITO, "Meseros", new Color(0x15803D));
        lblCocineros = agregarEstadistica("♨", Estilo.PELIGRO, "Cocineros", Estilo.PELIGRO);

        // ----- Izquierda: busqueda, tabla y estado laboral -----
        for (JComboBox<String> c : new JComboBox[]{cmbCargoFiltro, cmbEstadoFiltro}) {
            c.setFont(Estilo.FUENTE);
            c.setPreferredSize(new Dimension(120, 36));
        }
        JPanel filtros = new JPanel(new GridLayout(1, 2, 10, 0));
        filtros.setOpaque(false);
        filtros.add(conEtiqueta("Cargo:", false, cmbCargoFiltro));
        filtros.add(conEtiqueta("Estado laboral:", false, cmbEstadoFiltro));

        JButton btnBuscar = Estilo.boton("Buscar", Estilo.PRIMARIO);
        panelIzquierdo.add(barraBusqueda("Buscar por ID, nombre o DNI:", txtBuscar, filtros, btnBuscar), BorderLayout.NORTH);
        tabla.agregarColumnaPastilla(5);
        tabla.setAnchos(40, 170, 95, 85, 90, 105, 110);
        panelIzquierdo.add(tabla, BorderLayout.CENTER);
        panelIzquierdo.add(crearBloqueEstado(), BorderLayout.SOUTH);

        // ----- Derecha: formulario por secciones -----
        for (JComboBox<String> c : new JComboBox[]{cmbCargo, cmbDistrito, cmbProvincia, cmbDepartamento, cmbTurno, cmbNuevoEstado}) {
            c.setFont(Estilo.FUENTE);
            c.setPreferredSize(new Dimension(110, 34));
        }
        for (JComboBox<String> c : new JComboBox[]{cmbDistrito, cmbProvincia, cmbDepartamento}) {
            c.setEditable(true);
        }
        spnSalario.setFont(Estilo.FUENTE);
        spnSalario.setPreferredSize(new Dimension(110, 34));
        spnSalario.setEditor(new JSpinner.NumberEditor(spnSalario, "0.00"));

        JPanel datos = new JPanel(new GridBagLayout());
        datos.setOpaque(false);
        fila(datos, 0, 2, conEtiqueta("Nombres:", true, txtNombres), conEtiqueta("Apellidos:", true, txtApellidos));
        fila(datos, 1, 3, conEtiqueta("DNI:", true, txtDni), conEtiqueta("Teléfono:", true, txtTelefono),
                conEtiqueta("Cargo:", true, cmbCargo));

        JPanel contacto = new JPanel(new GridBagLayout());
        contacto.setOpaque(false);
        fila(contacto, 0, 2, conEtiqueta("Correo:", false, txtCorreo), conEtiqueta("Contacto de emergencia:", false, txtContacto));
        fila(contacto, 1, 2, conEtiqueta("Dirección:", false, txtDireccion));
        fila(contacto, 2, 3, conEtiqueta("Distrito:", false, cmbDistrito), conEtiqueta("Provincia:", false, cmbProvincia),
                conEtiqueta("Departamento:", false, cmbDepartamento));
        fila(contacto, 3, 2, conEtiqueta("Código postal:", false, txtCodigoPostal),
                conEtiqueta("Referencia de dirección:", false, txtReferencia));

        JPanel laboral = new JPanel(new GridBagLayout());
        laboral.setOpaque(false);
        fila(laboral, 0, 3, conEtiqueta("Salario (S/):", true, spnSalario), conEtiqueta("Turno:", true, cmbTurno),
                conEtiqueta("Fecha de contratación:", true, txtFecha));

        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        anadir(col, crearSeccion("☻", "Datos personales", datos));
        anadir(col, crearSeccion("⌂", "Contacto y dirección", contacto));
        anadir(col, crearSeccion("⚒", "Datos laborales", laboral));

        JButton btnRegistrar = Estilo.boton("Registrar", Estilo.EXITO);
        JButton btnActualizar = Estilo.boton("Actualizar", Estilo.PRIMARIO);
        JButton btnEliminar = Estilo.boton("Eliminar", Estilo.PELIGRO);
        JButton btnLimpiar = Estilo.boton("Limpiar", Estilo.GRIS);

        panelDerecho.add(tituloDePanel("☻", "Registro / Actualización de Empleado"), BorderLayout.NORTH);
        panelDerecho.add(col, BorderLayout.CENTER);
        panelDerecho.add(filaBotones(btnRegistrar, btnActualizar, btnEliminar, btnLimpiar), BorderLayout.SOUTH);

        // ----- Eventos -----
        tabla.setEscucha(new TablaPaginada.Escucha() {
            @Override
            public void seleccionar(Object[] fila) {
                llenarFormulario(((Number) fila[0]).intValue());
            }

            @Override
            public void eliminar(Object[] fila) {
                llenarFormulario(((Number) fila[0]).intValue());
                eliminarEmpleado();
            }

            @Override
            public void ver(Object[] fila) {
                verDetalle(((Number) fila[0]).intValue());
            }
        });
        btnBuscar.addActionListener(e -> cargar());
        txtBuscar.addActionListener(e -> cargar());
        cmbCargoFiltro.addActionListener(e -> cargar());
        cmbEstadoFiltro.addActionListener(e -> cargar());
        btnRegistrar.addActionListener(e -> registrar());
        btnActualizar.addActionListener(e -> actualizar());
        btnEliminar.addActionListener(e -> eliminarEmpleado());
        btnLimpiar.addActionListener(e -> limpiar());

        cargar();
    }

    // ---------- Construcción ----------

    private JPanel conEtiqueta(String texto, boolean obligatorio, JComponent campo) {
        JPanel p = new JPanel(new BorderLayout(0, 3));
        p.setOpaque(false);
        p.add(Estilo.etiquetaCampo(texto, obligatorio), BorderLayout.NORTH);
        p.add(campo, BorderLayout.CENTER);
        return p;
    }

    private void fila(JPanel destino, int y, int columnas, JComponent... items) {
        for (int i = 0; i < items.length; i++) {
            GridBagConstraints g = new GridBagConstraints();
            g.gridx = i;
            g.gridy = y;
            g.weightx = 1;
            g.fill = GridBagConstraints.HORIZONTAL;
            g.insets = new Insets(3, 4, 5, 4);
            if (i == items.length - 1 && items.length < columnas) {
                g.gridwidth = columnas - i;
            }
            destino.add(items[i], g);
        }
    }

    private JPanel crearSeccion(String glifo, String titulo, JPanel contenido) {
        JPanel s = new JPanel(new BorderLayout(0, 6));
        s.setOpaque(false);
        JPanel cab = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xE3EEFB));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        cab.setOpaque(false);
        JLabel ic = new JLabel(glifo);
        ic.setFont(Estilo.fuenteSimbolo(glifo.codePointAt(0), 15));
        ic.setForeground(Estilo.PRIMARIO);
        cab.add(ic);
        cab.add(Estilo.etiqueta(titulo, Estilo.NEGRITA, Estilo.PRIMARIO_OSCURO));
        s.add(cab, BorderLayout.NORTH);
        s.add(contenido, BorderLayout.CENTER);
        return s;
    }

    private void anadir(JPanel col, JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
        col.add(c);
        col.add(Box.createVerticalStrut(10));
    }

    private JPanel crearBloqueEstado() {
        JPanel bloque = new JPanel(new BorderLayout(0, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xF1F6FD));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        bloque.setOpaque(false);
        bloque.setBorder(new EmptyBorder(10, 12, 10, 12));
        bloque.add(Estilo.etiqueta("⚙  Estado laboral del empleado", Estilo.SUBTITULO, Estilo.PRIMARIO_OSCURO),
                BorderLayout.NORTH);

        cmbNuevoEstado.setPreferredSize(new Dimension(130, 34));
        JButton btnEstado = Estilo.boton("Actualizar estado", Estilo.PRIMARIO);
        btnEstado.addActionListener(e -> cambiarEstado());

        JPanel fila = new JPanel(new GridBagLayout());
        fila.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.SOUTH;
        g.insets = new Insets(0, 0, 0, 10);
        g.gridx = 0;
        g.weightx = 1;
        fila.add(conEtiqueta("DNI del empleado:", false, txtDniEstado), g);
        g.gridx = 1;
        g.weightx = 0;
        fila.add(conEtiqueta("Nuevo estado:", false, cmbNuevoEstado), g);
        g.gridx = 2;
        g.fill = GridBagConstraints.NONE;
        g.insets = new Insets(0, 0, 0, 0);
        fila.add(btnEstado, g);
        bloque.add(fila, BorderLayout.CENTER);
        return bloque;
    }

    // ---------- Datos ----------

    private static String titulo(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        String t = s.toLowerCase(Locale.ROOT);
        return Character.toUpperCase(t.charAt(0)) + t.substring(1);
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private static String valor(JComboBox<String> c) {
        Object o = c.isEditable() ? c.getEditor().getItem() : c.getSelectedItem();
        return o == null ? "" : o.toString().trim();
    }

    private static void poner(JComboBox<String> c, String v) {
        String texto = nz(v);
        if (!texto.isEmpty()) {
            boolean existe = false;
            for (int k = 0; k < c.getItemCount(); k++) {
                if (c.getItemAt(k).equalsIgnoreCase(texto)) {
                    c.setSelectedIndex(k);
                    existe = true;
                    break;
                }
            }
            if (!existe) {
                c.addItem(texto);
                c.setSelectedItem(texto);
            }
        } else if (c.getItemCount() > 0) {
            c.setSelectedIndex(0);
        }
    }

    private double salarioElegido() {
        try {
            spnSalario.commitEdit();
        } catch (java.text.ParseException ignorada) {
        }
        return ((Number) spnSalario.getValue()).doubleValue();
    }

    private Empleado buscarEnCache(int id) {
        for (Empleado e : cache) {
            if (e.getId() == id) {
                return e;
            }
        }
        return null;
    }

    private void cargar() {
        try {
            cache = empleadoDAO.listar();
            String filtro = txtBuscar.getText().trim().toLowerCase();
            String cargoF = (String) cmbCargoFiltro.getSelectedItem();
            String estadoF = (String) cmbEstadoFiltro.getSelectedItem();

            int meseros = 0;
            int cocineros = 0;
            List<Object[]> filas = new ArrayList<>();
            for (Empleado e : cache) {
                if ("MESERO".equalsIgnoreCase(e.getCargo())) {
                    meseros++;
                } else if ("COCINERO".equalsIgnoreCase(e.getCargo())) {
                    cocineros++;
                }
                String completo = (nz(e.getNombres()) + " " + nz(e.getApellidos())).trim();
                boolean texto = filtro.isEmpty() || String.valueOf(e.getId()).equals(filtro)
                        || completo.toLowerCase().contains(filtro) || nz(e.getDni()).contains(filtro);
                boolean cargo = "Todos".equals(cargoF) || cargoF.equalsIgnoreCase(e.getCargo());
                boolean estado = "Todos".equals(estadoF) || estadoF.equalsIgnoreCase(e.getEstadoLaboral());
                if (texto && cargo && estado) {
                    filas.add(new Object[]{e.getId(), completo, titulo(e.getCargo()), nz(e.getDni()),
                            nz(e.getTelefono()), nz(e.getEstadoLaboral()).toUpperCase()});
                }
            }
            lblTotal.setText(String.valueOf(cache.size()));
            lblMeseros.setText(String.valueOf(meseros));
            lblCocineros.setText(String.valueOf(cocineros));
            tabla.setFilas(filas);
        } catch (Exception ex) {
            error("No se pudieron cargar los empleados: " + ex.getMessage());
        }
    }

    private void llenarFormulario(int id) {
        Empleado e = buscarEnCache(id);
        if (e == null) {
            return;
        }
        idSeleccionado = id;
        txtNombres.setText(nz(e.getNombres()));
        txtApellidos.setText(nz(e.getApellidos()));
        txtDni.setText(nz(e.getDni()));
        txtTelefono.setText(nz(e.getTelefono()));
        poner(cmbCargo, titulo(e.getCargo()));
        cmbCargo.setEnabled(false); // el cargo no se modifica al actualizar (el DAO no lo guarda)
        txtCorreo.setText(nz(e.getCorreo()));
        txtContacto.setText(nz(e.getContactoEmergencia()));
        txtDireccion.setText(nz(e.getDireccion()));
        poner(cmbDistrito, e.getDistrito());
        poner(cmbProvincia, e.getProvincia());
        poner(cmbDepartamento, e.getDepartamento());
        txtCodigoPostal.setText(nz(e.getCodigoPostal()));
        txtReferencia.setText(nz(e.getReferenciaDireccion()));
        spnSalario.setValue(e.getSalario());
        poner(cmbTurno, titulo(e.getTurnoLaboral()));
        txtFecha.setText(e.getFechaContratacion() == null ? "" : e.getFechaContratacion().format(FECHA));
        txtDniEstado.setText(nz(e.getDni()));
        poner(cmbNuevoEstado, titulo(e.getEstadoLaboral()));
    }

    private void limpiar() {
        idSeleccionado = 0;
        for (JTextField t : new JTextField[]{txtNombres, txtApellidos, txtDni, txtTelefono, txtCorreo, txtContacto,
                txtDireccion, txtCodigoPostal, txtReferencia, txtFecha, txtDniEstado}) {
            t.setText("");
        }
        cmbCargo.setEnabled(true);
        cmbCargo.setSelectedIndex(0);
        for (JComboBox<String> c : new JComboBox[]{cmbDistrito, cmbProvincia, cmbDepartamento}) {
            c.setSelectedIndex(0);
        }
        cmbTurno.setSelectedIndex(0);
        cmbNuevoEstado.setSelectedIndex(0);
        spnSalario.setValue(0.0);
        tabla.limpiarSeleccion();
    }

    // ---------- Validación ----------

    private LocalDate fechaElegida() {
        try {
            return LocalDate.parse(txtFecha.getText().trim(), FECHA);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean validar() {
        if (txtNombres.getText().trim().isEmpty() || txtApellidos.getText().trim().isEmpty()
                || txtDni.getText().trim().isEmpty() || txtTelefono.getText().trim().isEmpty()
                || txtFecha.getText().trim().isEmpty()) {
            advertencia("Complete los campos obligatorios: nombres, apellidos, DNI, teléfono y fecha de contratación.");
            return false;
        }
        if (!txtDni.getText().trim().matches("\\d{8}")) {
            advertencia("El DNI debe tener 8 dígitos numéricos.");
            return false;
        }
        if (!txtTelefono.getText().trim().matches("\\d{9}")) {
            advertencia("El teléfono debe tener 9 dígitos numéricos.");
            return false;
        }
        String contacto = txtContacto.getText().trim();
        if (!contacto.isEmpty() && !contacto.matches("\\d{9}")) {
            advertencia("El contacto de emergencia debe tener 9 dígitos numéricos.");
            return false;
        }
        String correo = txtCorreo.getText().trim();
        if (!correo.isEmpty() && !correo.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            advertencia("El correo no tiene un formato válido.");
            return false;
        }
        if (salarioElegido() < 0) {
            advertencia("El salario no puede ser negativo.");
            return false;
        }
        if (fechaElegida() == null) {
            advertencia("La fecha de contratación no es válida. Use el formato dd/mm/aaaa (por ejemplo 15/03/2025).");
            return false;
        }
        return true;
    }

    private boolean dniRepetido(String dni, int idPropio) {
        for (Empleado e : empleadoDAO.listar()) {
            if (e.getId() != idPropio && dni.equals(e.getDni())) {
                return true;
            }
        }
        return false;
    }

    // ---------- Acciones ----------

    private void registrar() {
        if (!validar()) {
            return;
        }
        String dni = txtDni.getText().trim();
        if (dniRepetido(dni, 0)) {
            advertencia("Ya existe un empleado con ese DNI.");
            return;
        }
        int antes = empleadoDAO.listar().size();

        EmpleadosOperaciones.registrarEmpleado(txtNombres.getText().trim(), txtApellidos.getText().trim(),
                txtTelefono.getText().trim(), valor(cmbCargo).toUpperCase(Locale.ROOT), txtDireccion.getText().trim(), dni,
                txtContacto.getText().trim(), txtCorreo.getText().trim(), valor(cmbDistrito), valor(cmbProvincia),
                valor(cmbDepartamento), txtCodigoPostal.getText().trim(), txtReferencia.getText().trim(),
                salarioElegido(), valor(cmbTurno).toUpperCase(Locale.ROOT), fechaElegida());

        int despues = empleadoDAO.listar().size();
        if (despues > antes) {
            aviso("Empleado registrado correctamente.");
            limpiar();
            cargar();
        } else {
            error("No se pudo registrar el empleado.");
        }
    }

    private void actualizar() {
        if (idSeleccionado <= 0) {
            advertencia("Seleccione un empleado de la tabla para actualizarlo.");
            return;
        }
        if (!validar()) {
            return;
        }
        Empleado actual = buscarEnCache(idSeleccionado);
        if (actual == null) {
            advertencia("El empleado ya no está en la lista. Actualice la búsqueda.");
            return;
        }
        String dni = txtDni.getText().trim();
        if (dniRepetido(dni, idSeleccionado)) {
            advertencia("Otro empleado ya tiene ese DNI.");
            return;
        }
        String nombres = txtNombres.getText().trim();
        String apellidos = txtApellidos.getText().trim();
        double salario = salarioElegido();

        EmpleadosOperaciones.actualizarEmpleado(idSeleccionado, nombres, apellidos, actual.getCargo(),
                txtTelefono.getText().trim(), txtDireccion.getText().trim(), dni, txtContacto.getText().trim(),
                txtCorreo.getText().trim(), valor(cmbDistrito), valor(cmbProvincia), valor(cmbDepartamento),
                txtCodigoPostal.getText().trim(), txtReferencia.getText().trim(), salario,
                valor(cmbTurno).toUpperCase(Locale.ROOT), fechaElegida());

        Empleado nuevo = null;
        for (Empleado e : empleadoDAO.listar()) {
            if (e.getId() == idSeleccionado) {
                nuevo = e;
            }
        }
        boolean ok = nuevo != null && nombres.equals(nuevo.getNombres()) && apellidos.equals(nuevo.getApellidos())
                && dni.equals(nuevo.getDni()) && Math.abs(nuevo.getSalario() - salario) < 0.01
                && fechaElegida().equals(nuevo.getFechaContratacion());
        if (ok) {
            aviso("Empleado actualizado correctamente.");
            cargar();
        } else {
            error("No se pudo actualizar el empleado.");
        }
    }

    private void eliminarEmpleado() {
        if (idSeleccionado <= 0) {
            advertencia("Seleccione un empleado de la tabla para eliminarlo.");
            return;
        }
        String nombre = (txtNombres.getText().trim() + " " + txtApellidos.getText().trim()).trim();
        if (!confirmar("¿Eliminar al empleado N.° " + idSeleccionado + " (" + nombre + ")?\n"
                + "Esta acción no se puede deshacer.")) {
            return;
        }
        int id = idSeleccionado;
        EmpleadosOperaciones.eliminarEmpleado(id);

        if (!EmpleadosOperaciones.existeEmpleado(id)) {
            aviso("Empleado eliminado correctamente.");
            limpiar();
            cargar();
        } else {
            error("No se pudo eliminar el empleado.\n"
                    + "Puede tener un usuario, pedidos u otros registros asociados. "
                    + "En ese caso cambie su estado laboral en lugar de eliminarlo.");
        }
    }

    private void cambiarEstado() {
        String dni = txtDniEstado.getText().trim();
        if (dni.isEmpty()) {
            advertencia("Ingrese el DNI del empleado.");
            return;
        }
        String estado = valor(cmbNuevoEstado).toUpperCase(Locale.ROOT);
        Empleado objetivo = null;
        for (Empleado e : empleadoDAO.listar()) {
            if (dni.equals(e.getDni())) {
                objetivo = e;
            }
        }
        if (objetivo == null) {
            advertencia("No existe un empleado con el DNI " + dni + ".");
            return;
        }
        if (!confirmar("¿Cambiar el estado laboral de " + objetivo.getNombres() + " " + objetivo.getApellidos()
                + " a " + estado + "?\nUn empleado que no está ACTIVO no puede iniciar sesión.")) {
            return;
        }

        EmpleadosOperaciones.actualizarEstado(dni, estado);

        boolean ok = false;
        for (Empleado e : empleadoDAO.listar()) {
            if (dni.equals(e.getDni()) && estado.equalsIgnoreCase(e.getEstadoLaboral())) {
                ok = true;
            }
        }
        if (ok) {
            aviso("Estado laboral actualizado correctamente.\nNuevo estado: " + estado);
            cargar();
        } else {
            error("No se pudo actualizar el estado laboral.");
        }
    }

    private void verDetalle(int id) {
        Empleado e = buscarEnCache(id);
        if (e == null) {
            return;
        }
        aviso("Empleado N.° " + e.getId()
                + "\nNombre: " + nz(e.getNombres()) + " " + nz(e.getApellidos())
                + "\nCargo: " + titulo(e.getCargo()) + "  |  Estado: " + nz(e.getEstadoLaboral())
                + "\nDNI: " + nz(e.getDni()) + "  |  Teléfono: " + nz(e.getTelefono())
                + "\nCorreo: " + nz(e.getCorreo())
                + "\nContacto de emergencia: " + nz(e.getContactoEmergencia())
                + "\nDirección: " + nz(e.getDireccion()) + ", " + nz(e.getDistrito()) + ", " + nz(e.getProvincia())
                + ", " + nz(e.getDepartamento())
                + "\nCódigo postal: " + nz(e.getCodigoPostal()) + "  |  Referencia: " + nz(e.getReferenciaDireccion())
                + "\nSalario: " + Estilo.soles(e.getSalario()) + "  |  Turno: " + titulo(e.getTurnoLaboral())
                + "\nFecha de contratación: " + (e.getFechaContratacion() == null ? "" : e.getFechaContratacion().format(FECHA)));
    }
}
