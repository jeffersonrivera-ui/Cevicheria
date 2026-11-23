package controlador;

import dao.UsuarioDAO;
import dao.impl.UsuarioDAOImpl;
import modelo.Usuario;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.util.Base64;

public class UsuarioOperaciones {

    private final UsuarioDAO usuarioDAO = new UsuarioDAOImpl();

    private static final int ITERACIONES = 65536;
    private static final int LONGITUD_HASH = 256;

    public int registrarUsuario(int idEmpleado,
                                String nombreUsuario,
                                String contrasena) {

        if (nombreUsuario == null || nombreUsuario.trim().isEmpty()
                || contrasena == null || contrasena.isEmpty()) {
            System.out.println("Complete todos los campos.");
            return 0;
        }

        String hash = generarHash(contrasena);

        if (hash == null) {
            return 0;
        }

        Usuario usuario = new Usuario(
                idEmpleado,
                nombreUsuario.trim(),
                hash
        );

        return usuarioDAO.insertar(usuario);
    }

    // Iniciar sesión
    public Usuario autenticar(String nombreUsuario, String contrasena) {

        if (nombreUsuario == null || contrasena == null) {
            return null;
        }

        Usuario usuario = usuarioDAO.buscarPorNombreUsuario(nombreUsuario.trim());

        if (usuario == null) {
            return null;
        }

        if (!"ACTIVO".equalsIgnoreCase(usuario.getEstado())) {
            return null;
        }
        if (!"ACTIVO".equalsIgnoreCase(usuario.getEstadoLaboral())) {
            return null;
        }

        if (verificarContrasena(contrasena, usuario.getPasswordHash())) {
            return usuario;
        }

        return null;
    }

    // Generar contraseña segura
    private String generarHash(String contrasena) {

        try {
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);

            PBEKeySpec spec = new PBEKeySpec(contrasena.toCharArray(), salt, ITERACIONES, LONGITUD_HASH);

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            byte[] hash = factory.generateSecret(spec).getEncoded();
            spec.clearPassword();

            return ITERACIONES + ":" + Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);

        } catch (Exception e) {
            System.out.println("Error al generar contraseña: " + e.getMessage());
            return null;
        }
    }

    // Verificar contraseña
    private boolean verificarContrasena(String contrasena, String hashGuardado) {

        try {
            String[] partes = hashGuardado.split(":");

            int iteraciones = Integer.parseInt(partes[0]);
            byte[] salt = Base64.getDecoder().decode(partes[1]);
            byte[] hashOriginal = Base64.getDecoder().decode(partes[2]);

            PBEKeySpec spec = new PBEKeySpec(contrasena.toCharArray(), salt, iteraciones, hashOriginal.length * 8);

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            byte[] hashIngresado = factory.generateSecret(spec).getEncoded();
            spec.clearPassword();

            return MessageDigest.isEqual(hashOriginal, hashIngresado);

        } catch (Exception e) {
            System.out.println("Error al verificar contraseña: " + e.getMessage());
            return false;
        }
    }
}
