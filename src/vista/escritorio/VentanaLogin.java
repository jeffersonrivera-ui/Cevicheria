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
        setSize(420, 460);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel fondo = new JPanel(new GridBagLayout());
        fondo.setBackground(Estilo.PRIMARIO_OSCURO);

        JPanel tarjeta = Estilo.tarjeta();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBorder(new EmptyBorder(28, 32, 28, 32));
        tarjeta.setPreferredSize(new Dimension(340, 380));

        JLabel titulo = Estilo.etiqueta("Cevichería", Estilo.TITULO, Estilo.PRIMARIO_OSCURO);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel sub = Estilo.etiqueta("Inicia sesión para continuar", Estilo.FUENTE, Estilo.TEXTO_SUAVE);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblUsuario = Estilo.etiqueta("Usuario", Estilo.NEGRITA, Estilo.TEXTO);
        lblUsuario.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblClave = Estilo.etiqueta("Contraseña", Estilo.NEGRITA, Estilo.TEXTO);
        lblClave.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (JTextField campo : new JTextField[]{txtUsuario, txtClave}) {
            campo.setFont(Estilo.FUENTE);
            campo.setAlignmentX(Component.LEFT_ALIGNMENT);
            campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            campo.setPreferredSize(new Dimension(260, 36));
        }

        btnIngresar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnIngresar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        tarjeta.add(titulo);
        tarjeta.add(Box.createVerticalStrut(4));
        tarjeta.add(sub);
        tarjeta.add(Box.createVerticalStrut(24));
        tarjeta.add(lblUsuario);
        tarjeta.add(Box.createVerticalStrut(4));
        tarjeta.add(txtUsuario);
        tarjeta.add(Box.createVerticalStrut(14));
        tarjeta.add(lblClave);
        tarjeta.add(Box.createVerticalStrut(4));
        tarjeta.add(txtClave);
        tarjeta.add(Box.createVerticalStrut(24));
        tarjeta.add(btnIngresar);

        fondo.add(tarjeta);
        setContentPane(fondo);
        getRootPane().setDefaultButton(btnIngresar);

        btnIngresar.addActionListener(e -> iniciarSesion());
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
