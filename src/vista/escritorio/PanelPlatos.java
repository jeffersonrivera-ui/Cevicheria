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
    private final TablaPaginada tabla = new TablaPaginada(
            new String[]{"ID", "Nombre del plato", "Precio (S/)", "Estado"}, 3, false);

    private final JTextField txtId = Estilo.campo();
    private final JTextField txtNombre = Estilo.campo();
    private final JTextField txtPrecio = Estilo.campo();
    private final Estilo.Pastilla pastillaEstado = new Estilo.Pastilla("No disponible");

    public PanelPlatos() {
        super("Platos", "Consulta los platos de la carta de la cevichería", "\u2668", Estilo.PRIMARIO, true);

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
        txtNombre.setEditable(false);
        txtPrecio.setEditable(false);
        txtId.setBackground(new Color(0xEEF2F8));
        txtNombre.setBackground(new Color(0xEEF2F8));
        txtPrecio.setBackground(new Color(0xEEF2F8));

        JPanel contPastilla = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        contPastilla.setOpaque(false);
        contPastilla.add(pastillaEstado);

        Formulario f = new Formulario();
        f.agregar("ID:", false, txtId);
        f.agregar("Nombre del plato:", false, txtNombre);
        f.agregar("Precio (S/):", false, txtPrecio);
        f.agregar("Disponibilidad:", false, contPastilla);

        JPanel avisos = new JPanel();
        avisos.setOpaque(false);
        avisos.setLayout(new BoxLayout(avisos, BoxLayout.Y_AXIS));
        avisos.add(Estilo.aviso("Vista de carta: aquí solo se consultan los platos.", "info"));
        avisos.add(Box.createVerticalStrut(8));
        avisos.add(Estilo.aviso("Un plato está disponible cuando tiene receta y hay stock suficiente de todos sus ingredientes.", "alerta"));

        JPanel centro = new JPanel(new BorderLayout(0, 12));
        centro.setOpaque(false);
        centro.add(f, BorderLayout.NORTH);
        centro.add(avisos, BorderLayout.CENTER);

        JButton btnActualizar = Estilo.boton("Actualizar disponibilidad", Estilo.PRIMARIO);
        panelDerecho.add(tituloDePanel("\u2668", "Detalle del plato"), BorderLayout.NORTH);
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
}
