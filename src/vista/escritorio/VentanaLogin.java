package vista.escritorio;

import controlador.UsuarioOperaciones;
import modelo.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class VentanaLogin extends JFrame {

    private final UsuarioOperaciones usuarioOperaciones = new UsuarioOperaciones();
    private final JTextField txtUsuario = new JTextField();
    private final JPasswordField txtClave = new JPasswordField();
    private final JButton btnIngresar = Estilo.boton("Ingresar", Estilo.PRIMARIO);

    public VentanaLogin() {
        super("Cevichería - Iniciar sesión");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(460, 520);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel fondo = new JPanel(new GridBagLayout());
        fondo.setBackground(Estilo.PRIMARIO_OSCURO);

        JPanel tarjeta = Estilo.tarjeta();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBorder(new EmptyBorder(28, 32, 28, 32));
        tarjeta.setPreferredSize(new Dimension(390, 440));

        JLabel titulo = Estilo.etiqueta("Cevichería", Estilo.TITULO, Estilo.PRIMARIO_OSCURO);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel sub = Estilo.etiqueta("Inicia sesión para continuar", Estilo.FUENTE, Estilo.TEXTO_SUAVE);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        for (JTextField campo : new JTextField[]{txtUsuario, txtClave}) {
            campo.setFont(Estilo.FUENTE);
            campo.setPreferredSize(new Dimension(300, 38));
            campo.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0xCBD5E8)), new EmptyBorder(4, 10, 4, 10)));
        }
        btnIngresar.setPreferredSize(new Dimension(300, 42));

        // Cada bloque (etiqueta + campo) ocupa el mismo ancho y queda centrado en la tarjeta.
        JPanel bloqueUsuario = bloque("Usuario", txtUsuario);
        // Campo de contraseña con el ojo a la derecha para ver u ocultar lo escrito.
        final char caracterOculto = txtClave.getEchoChar();
        BotonOjo ojo = new BotonOjo();
        txtClave.setBorder(new EmptyBorder(4, 10, 4, 4));
        JPanel cajaClave = new JPanel(new BorderLayout());
        cajaClave.setBackground(Color.WHITE);
        cajaClave.setBorder(BorderFactory.createLineBorder(new Color(0xCBD5E8)));
        cajaClave.setPreferredSize(new Dimension(300, 38));
        cajaClave.add(txtClave, BorderLayout.CENTER);
        cajaClave.add(ojo, BorderLayout.EAST);
        ojo.addActionListener(e -> {
            boolean ver = !ojo.isMostrando();
            ojo.setMostrando(ver);
            txtClave.setEchoChar(ver ? (char) 0 : caracterOculto);
            txtClave.requestFocusInWindow();
        });
        JPanel bloqueClave = bloque("Contraseña", cajaClave);
        JPanel bloqueBoton = new JPanel(new BorderLayout());
        bloqueBoton.setOpaque(false);
        bloqueBoton.add(btnIngresar, BorderLayout.CENTER);
        bloqueBoton.setMaximumSize(new Dimension(300, 42));
        bloqueBoton.setAlignmentX(Component.CENTER_ALIGNMENT);

        tarjeta.add(Box.createVerticalGlue());
        tarjeta.add(titulo);
        tarjeta.add(Box.createVerticalStrut(6));
        tarjeta.add(sub);
        tarjeta.add(Box.createVerticalStrut(30));
        tarjeta.add(bloqueUsuario);
        tarjeta.add(Box.createVerticalStrut(16));
        tarjeta.add(bloqueClave);
        tarjeta.add(Box.createVerticalStrut(28));
        tarjeta.add(bloqueBoton);
        tarjeta.add(Box.createVerticalGlue());

        fondo.add(tarjeta);
        setContentPane(fondo);
        getRootPane().setDefaultButton(btnIngresar);

        btnIngresar.addActionListener(e -> iniciarSesion());
    }

    // Botón con un ojo dibujado (tachado cuando la contraseña está oculta).
    private static class BotonOjo extends JButton {
        private boolean mostrando = false;

        BotonOjo() {
            setPreferredSize(new Dimension(40, 36));
            setFocusable(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText("Mostrar u ocultar la contraseña");
        }

        boolean isMostrando() {
            return mostrando;
        }

        void setMostrando(boolean valor) {
            mostrando = valor;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            g2.setColor(mostrando ? Estilo.PRIMARIO : new Color(0x7B879E));
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            // Contorno del ojo (dos arcos) y pupila.
            java.awt.geom.Path2D ojo = new java.awt.geom.Path2D.Double();
            ojo.moveTo(cx - 10, cy);
            ojo.quadTo(cx, cy - 11, cx + 10, cy);
            ojo.quadTo(cx, cy + 11, cx - 10, cy);
            g2.draw(ojo);
            g2.fillOval(cx - 3, cy - 3, 6, 6);
            if (!mostrando) {
                g2.setColor(new Color(0x7B879E));
                g2.drawLine(cx - 9, cy + 8, cx + 9, cy - 8);
            }
            g2.dispose();
        }
    }

    // Etiqueta arriba y campo debajo, ambos del mismo ancho y alineados a la izquierda entre sí.
    private JPanel bloque(String texto, JComponent campo) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        JLabel etiqueta = Estilo.etiqueta(texto, Estilo.NEGRITA, Estilo.TEXTO);
        p.add(etiqueta, BorderLayout.NORTH);
        p.add(campo, BorderLayout.CENTER);
        p.setMaximumSize(new Dimension(300, 70));
        p.setPreferredSize(new Dimension(300, 70));
        p.setAlignmentX(Component.CENTER_ALIGNMENT);
        return p;
    }

    private void iniciarSesion() {
        String nombre = txtUsuario.getText().trim();
        String clave = new String(txtClave.getPassword());

        if (nombre.isEmpty() || clave.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese usuario y contraseña.",
                    "Campos vacíos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        btnIngresar.setEnabled(false);
        try {
            Usuario usuario = usuarioOperaciones.autenticar(nombre, clave);

            if (usuario == null) {
                JOptionPane.showMessageDialog(this,
                        "Usuario o contraseña incorrectos, o el usuario está inactivo.",
                        "Acceso denegado", JOptionPane.ERROR_MESSAGE);
                txtClave.setText("");
                return;
            }

            String rol = decidirRol(usuario);
            JFrame siguiente;
            switch (rol) {
                case "ADMINISTRADOR":
                    siguiente = new VentanaAdministrador(usuario);
                    break;
                case "MESERO":
                    siguiente = new VentanaMesero(usuario);
                    break;
                case "COCINERO":
                    siguiente = new VentanaCocinero(usuario);
                    break;
                default:
                    JOptionPane.showMessageDialog(this,
                            "El usuario \"" + usuario.getNombreUsuario() + "\" no tiene una ventana asignada.",
                            "Rol no soportado", JOptionPane.ERROR_MESSAGE);
                    return;
            }

            dispose();
            siguiente.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo iniciar sesión: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            btnIngresar.setEnabled(true);
        }
    }

    // Decide a qué panel entra el usuario: primero por su nombre de usuario
    // (administrador, mesero, cocinero) y, si no coincide, por el cargo de su empleado.
    private String decidirRol(Usuario usuario) {
        String nombre = usuario.getNombreUsuario() == null ? "" : usuario.getNombreUsuario().trim().toLowerCase();
        switch (nombre) {
            case "administrador":
            case "admin":
                return "ADMINISTRADOR";
            case "mesero":
                return "MESERO";
            case "cocinero":
                return "COCINERO";
            default:
                return usuario.getCargo() == null ? "" : usuario.getCargo().trim().toUpperCase();
        }
    }
}