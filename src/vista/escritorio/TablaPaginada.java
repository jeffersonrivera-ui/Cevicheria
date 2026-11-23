package vista.escritorio;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TablaPaginada extends JPanel {

    public interface Escucha {
        void seleccionar(Object[] fila);

        default void editar(Object[] fila) {
            seleccionar(fila);
        }

        default void eliminar(Object[] fila) {
        }

        default void ver(Object[] fila) {
        }
    }

    // Acción personalizada de la columna "Acciones" (icono + qué hacer al pulsarlo).
    public static final class Accion {
        final String glifo;
        final Color fondo;
        final Color letra;
        final java.util.function.Consumer<Object[]> alPulsar;

        public Accion(String glifo, Color fondo, Color letra, java.util.function.Consumer<Object[]> alPulsar) {
            this.glifo = glifo;
            this.fondo = fondo;
            this.letra = letra;
            this.alPulsar = alPulsar;
        }
    }

    private static final int TAMANO = 8;
    private static final int ICONO = 28;
    private static final int SEPARACION = 6;

    private final DefaultTableModel modelo;
    private final JTable tabla;
    private final boolean conAcciones;
    private final boolean conVer;
    private String formatoInfo = null;
    private boolean ocultarBotones = false;
    private final int colAcciones;
    private final JLabel lblInfo = Estilo.etiqueta("", Estilo.PEQUENA, Estilo.PRIMARIO_OSCURO);
    private final JPanel botonesPagina = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));

    private List<Object[]> filas = new ArrayList<>();
    private int pagina = 0;
    private boolean cargando = false;
    private Escucha escucha;
    private List<Accion> acciones = null;
    private java.util.function.Function<String, Color[]> colorPastilla = Estilo::coloresPastilla;

    public TablaPaginada(String[] columnas, int colPastilla, boolean conAcciones) {
        this(columnas, colPastilla, conAcciones, true);
    }

    public TablaPaginada(String[] columnas, int colPastilla, boolean conAcciones, boolean conVer) {
        super(new BorderLayout(0, 8));
        this.conVer = conVer;
        setOpaque(false);
        this.conAcciones = conAcciones;

        String[] todas = columnas;
        if (conAcciones) {
            todas = Arrays.copyOf(columnas, columnas.length + 1);
            todas[columnas.length] = "Acciones";
        }
        this.colAcciones = conAcciones ? columnas.length : -1;

        modelo = Estilo.modeloNoEditable(todas);
        tabla = Estilo.tabla(modelo);

        if (colPastilla >= 0) {
            tabla.getColumnModel().getColumn(colPastilla).setCellRenderer(new RenderizadorPastilla());
        }
        if (conAcciones) {
            tabla.getColumnModel().getColumn(colAcciones).setCellRenderer(new RenderizadorAcciones(conVer));
            tabla.getColumnModel().getColumn(colAcciones).setPreferredWidth(120);
        }

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || cargando || escucha == null) {
                return;
            }
            Object[] f = filaSeleccionada();
            if (f != null) {
                escucha.seleccionar(f);
            }
        });

        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!TablaPaginada.this.conAcciones || (escucha == null && acciones == null)) {
                    return;
                }
                int col = tabla.columnAtPoint(e.getPoint());
                int row = tabla.rowAtPoint(e.getPoint());
                if (col != colAcciones || row < 0) {
                    return;
                }
                int idx = pagina * TAMANO + row;
                if (idx >= filas.size()) {
                    return;
                }
                Rectangle r = tabla.getCellRect(row, col, false);
                int n = acciones != null ? acciones.size() : (TablaPaginada.this.conVer ? 3 : 2);
                int ancho = n * ICONO + (n - 1) * SEPARACION;
                int inicio = r.x + (r.width - ancho) / 2;
                int rel = e.getX() - inicio;
                if (rel < 0 || rel > ancho) {
                    return;
                }
                int cual = rel / (ICONO + SEPARACION);
                Object[] f = filas.get(idx);
                if (acciones != null) {
                    if (cual >= 0 && cual < acciones.size()) {
                        acciones.get(cual).alPulsar.accept(f);
                    }
                    return;
                }
                if (cual == 0) {
                    escucha.editar(f);
                } else if (cual == 1) {
                    escucha.eliminar(f);
                } else if (TablaPaginada.this.conVer) {
                    escucha.ver(f);
                }
            }
        });

        JScrollPane sp = Estilo.scroll(tabla);
        sp.getViewport().setBackground(Color.WHITE);

        JPanel pie = new JPanel(new BorderLayout());
        pie.setOpaque(false);
        pie.setBorder(new EmptyBorder(4, 2, 0, 0));
        botonesPagina.setOpaque(false);
        pie.add(lblInfo, BorderLayout.WEST);
        pie.add(botonesPagina, BorderLayout.EAST);

        add(sp, BorderLayout.CENTER);
        add(pie, BorderLayout.SOUTH);
        mostrarPagina();
    }

    public void setEscucha(Escucha escucha) {
        this.escucha = escucha;
    }

    // Reemplaza los iconos de la columna "Acciones" por unos propios.
    public void setAcciones(Accion... lista) {
        this.acciones = Arrays.asList(lista);
        if (conAcciones) {
            tabla.getColumnModel().getColumn(colAcciones).setCellRenderer(new RenderizadorAcciones(lista));
        }
    }

    // Cambia el título de la columna de acciones (por ejemplo "Detalle").
    public void setTituloAcciones(String titulo) {
        if (conAcciones) {
            tabla.getColumnModel().getColumn(colAcciones).setHeaderValue(titulo);
            tabla.getTableHeader().repaint();
        }
    }

    // Aplica el estilo de pastilla (cargo, estado...) a otra columna.
    public void agregarColumnaPastilla(int columna) {
        tabla.getColumnModel().getColumn(columna).setCellRenderer(new RenderizadorPastilla());
    }

    // Ancho preferido de cada columna, en el orden de la tabla.
    public void setAnchos(int... anchos) {
        for (int i = 0; i < anchos.length && i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
    }

    // Texto del pie, por ejemplo "%d ingredientes registrados"; si se usa, también se oculta la paginación.
    public void setTextoPie(String formato) {
        this.formatoInfo = formato;
        this.ocultarBotones = formato != null;
        mostrarPagina();
    }

    // Permite definir los colores {fondo, letra} de la pastilla según el texto de la celda.
    public void setColorPastilla(java.util.function.Function<String, Color[]> f) {
        this.colorPastilla = f;
    }

    public void setFilas(List<Object[]> nuevas) {
        this.filas = nuevas == null ? new ArrayList<>() : nuevas;
        int total = totalPaginas();
        if (pagina >= total) {
            pagina = total - 1;
        }
        if (pagina < 0) {
            pagina = 0;
        }
        mostrarPagina();
    }

    public Object[] filaSeleccionada() {
        int row = tabla.getSelectedRow();
        if (row < 0) {
            return null;
        }
        int idx = pagina * TAMANO + row;
        return idx < filas.size() ? filas.get(idx) : null;
    }

    public void limpiarSeleccion() {
        tabla.clearSelection();
    }

    public int totalFilas() {
        return filas.size();
    }

    private int totalPaginas() {
        return Math.max(1, (int) Math.ceil(filas.size() / (double) TAMANO));
    }

    private void mostrarPagina() {
        cargando = true;
        modelo.setRowCount(0);
        int desde = pagina * TAMANO;
        int hasta = Math.min(desde + TAMANO, filas.size());
        for (int i = desde; i < hasta; i++) {
            Object[] r = filas.get(i);
            modelo.addRow(conAcciones ? Arrays.copyOf(r, r.length + 1) : r);
        }
        cargando = false;

        lblInfo.setText(formatoInfo != null ? String.format(formatoInfo, filas.size())
                : "Mostrando " + (hasta - desde) + " de " + filas.size() + " registros");
        construirBotones();
    }

    private void construirBotones() {
        botonesPagina.removeAll();
        if (ocultarBotones) {
            botonesPagina.revalidate();
            botonesPagina.repaint();
            return;
        }
        int total = totalPaginas();

        botonesPagina.add(botonPagina("\u00AB", pagina > 0, false, 0));
        botonesPagina.add(botonPagina("\u2039", pagina > 0, false, pagina - 1));

        int hasta = Math.min(total - 1, Math.max(pagina + 1, 2));
        int desde = Math.max(0, hasta - 2);
        for (int p = desde; p <= hasta; p++) {
            botonesPagina.add(botonPagina(String.valueOf(p + 1), true, p == pagina, p));
        }

        botonesPagina.add(botonPagina("\u203A", pagina < total - 1, false, pagina + 1));
        botonesPagina.add(botonPagina("\u00BB", pagina < total - 1, false, total - 1));
        botonesPagina.revalidate();
        botonesPagina.repaint();
    }

    private JButton botonPagina(String texto, boolean habilitado, boolean activo, int destino) {
        Estilo.BotonRedondo b = new Estilo.BotonRedondo(texto, activo ? Estilo.PRIMARIO : new Color(0xE3ECFB));
        b.setForeground(activo ? Color.WHITE : Estilo.PRIMARIO_OSCURO);
        b.setBorder(new EmptyBorder(5, 10, 5, 10));
        b.setEnabled(habilitado);
        b.addActionListener(e -> {
            pagina = destino;
            mostrarPagina();
        });
        return b;
    }

    // ---------- Renderizadores ----------

    private class RenderizadorPastilla extends JPanel implements TableCellRenderer {
        private final Estilo.Pastilla pastilla = new Estilo.Pastilla();

        RenderizadorPastilla() {
            super(new GridBagLayout());
            add(pastilla);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foco, int r, int c) {
            setBackground(sel ? t.getSelectionBackground() : Color.WHITE);
            String texto = v == null ? "" : v.toString();
            Color[] colores = colorPastilla.apply(texto);
            pastilla.poner(texto, colores[0], colores[1]);
            return this;
        }
    }

    private static class RenderizadorAcciones extends JPanel implements TableCellRenderer {
        RenderizadorAcciones(Accion[] lista) {
            super(new FlowLayout(FlowLayout.CENTER, SEPARACION, 6));
            for (Accion a : lista) {
                add(new Estilo.CajaIcono(a.glifo, a.fondo, a.letra, ICONO));
            }
        }

        RenderizadorAcciones(boolean conVer) {
            super(new FlowLayout(FlowLayout.CENTER, SEPARACION, 6));
            add(new Estilo.CajaIcono("\u270E", new Color(0x1E6FEB), Color.WHITE, ICONO));
            add(new Estilo.CajaIcono("\u2716", new Color(0xEF4444), Color.WHITE, ICONO));
            if (conVer) {
                add(new Estilo.CajaIcono("\u25C9", new Color(0xE5E7EB), new Color(0x4B5563), ICONO));
            }
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foco, int r, int c) {
            setBackground(sel ? t.getSelectionBackground() : Color.WHITE);
            return this;
        }
    }
}
