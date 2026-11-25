package dao.impl;

import conexion.ConexionBD;
import dao.UsuarioDAO;
import modelo.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class UsuarioDAOImpl implements UsuarioDAO {

    @Override
    public int insertar(Usuario usuario) {

        String sql = "INSERT INTO usuarios (id_empleado, nombre_usuario, password_hash) VALUES (?, ?, ?)";

        try (Connection cn = ConexionBD.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, usuario.getIdEmpleado());
            ps.setString(2, usuario.getNombreUsuario());
            ps.setString(3, usuario.getPasswordHash());

            int filas = ps.executeUpdate();

            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("Error al registrar usuario: " + e.getMessage());
        }

        return 0;
    }

    @Override
    public Usuario buscarPorNombreUsuario(String nombreUsuario) {

        String sql = "SELECT u.id_usuario, u.id_empleado," +
                "u.nombre_usuario, u.password_hash, u.estado, " +
                "e.cargo, e.estado_laboral " +
                "FROM usuarios u " +
                "INNER JOIN empleados e ON u.id_empleado = e.id_empleado " +
                "WHERE u.nombre_usuario = ?";

        try (Connection cn = ConexionBD.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, nombreUsuario);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    Usuario usuario = new Usuario(
                            rs.getInt("id_usuario"),
                            rs.getInt("id_empleado"),
                            rs.getString("nombre_usuario"),
                            rs.getString("password_hash"),
                            rs.getString("estado")
                    );

                    usuario.setCargo(rs.getString("cargo"));
                    usuario.setEstadoLaboral(rs.getString("estado_laboral"));

                    return usuario;
                }
            }

        } catch (Exception e) {
            System.out.println("Error al buscar usuario: " + e.getMessage());
        }

        return null;
    }

    private static final String SELECT_USUARIO = "SELECT u.id_usuario, u.id_empleado, u.nombre_usuario, u.password_hash, u.estado, e.cargo, e.estado_laboral, CONCAT(e.nombres, ' ', e.apellidos) AS empleado " +
            "FROM usuarios u INNER JOIN empleados e ON u.id_empleado = e.id_empleado ";

    private Usuario crearUsuario(ResultSet rs) throws Exception {

        Usuario u = new Usuario(
                rs.getInt("id_usuario"),
                rs.getInt("id_empleado"),
                rs.getString("nombre_usuario"),
                rs.getString("password_hash"),
                rs.getString("estado"));
        u.setCargo(rs.getString("cargo"));
        u.setEstadoLaboral(rs.getString("estado_laboral"));
        u.setNombreEmpleado(rs.getString("empleado"));
        return u;
    }

    @Override
    public Usuario buscarPorId(int idUsuario) {

        String sql = SELECT_USUARIO + "WHERE u.id_usuario = ?";

        try (Connection cn = ConexionBD.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return crearUsuario(rs);
                }
            }
        } catch (Exception e) {
            System.out.println("Error al buscar usuario: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Usuario> listar() {

        List<Usuario> lista = new ArrayList<>();

        String sql = SELECT_USUARIO + "ORDER BY u.id_usuario";

        try (Connection cn = ConexionBD.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(crearUsuario(rs));
            }

        } catch (Exception e) {
            System.out.println("Error al listar usuarios: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public boolean actualizarPassword(int idUsuario, String passwordHash) {

        String sql = "UPDATE usuarios SET password_hash = ? WHERE id_usuario = ?";

        try (Connection cn = ConexionBD.getConnection();

             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error al cambiar contraseña: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean actualizarEstado(int idUsuario, String estado) {

        String sql = "UPDATE usuarios SET estado = ? WHERE id_usuario = ?";

        try (Connection cn = ConexionBD.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error al cambiar estado del usuario: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean eliminar(int idUsuario) {

        String sql = "DELETE FROM usuarios WHERE id_usuario = ?";

        try (Connection cn = ConexionBD.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error al eliminar usuario: " + e.getMessage());
        }
        return false;
    }
}
