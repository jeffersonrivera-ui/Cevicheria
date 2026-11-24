package dao.impl;

import conexion.ConexionBD;
import dao.ProveedorDAO;
import modelo.Proveedor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProveedorDAOImpl implements ProveedorDAO {

    @Override
    public int insertar(Proveedor p) {

        String sql = "INSERT INTO proveedores (nombre, ruc, telefono, direccion, productos, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getNombre());
            ps.setString(2, vacioANulo(p.getRuc()));
            ps.setString(3, vacioANulo(p.getTelefono()));
            ps.setString(4, vacioANulo(p.getDireccion()));
            ps.setString(5, vacioANulo(p.getProductos()));
            ps.setString(6, p.getEstado() == null ? Proveedor.ACTIVO : p.getEstado());

            if (ps.executeUpdate() > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al insertar proveedor: " + e.getMessage());
        }

        return -1;
    }

    @Override
    public List<Proveedor> listar() {
        List<Proveedor> lista = new ArrayList<>();
        String sql = "SELECT * FROM proveedores ORDER BY id_proveedor";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error al listar proveedores: " + e.getMessage());
        }

        return lista;
    }

    @Override
    public Proveedor buscarPorId(Integer id) {
        String sql = "SELECT * FROM proveedores WHERE id_proveedor = ?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al buscar proveedor: " + e.getMessage());
        }

        return null;
    }

    @Override
    public boolean actualizar(Proveedor p) {

        String sql = "UPDATE proveedores SET nombre = ?, ruc = ?, telefono = ?, direccion = ?, "
                + "productos = ?, estado = ? WHERE id_proveedor = ?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getNombre());
            ps.setString(2, vacioANulo(p.getRuc()));
            ps.setString(3, vacioANulo(p.getTelefono()));
            ps.setString(4, vacioANulo(p.getDireccion()));
            ps.setString(5, vacioANulo(p.getProductos()));
            ps.setString(6, p.getEstado());
            ps.setInt(7, p.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al actualizar proveedor: " + e.getMessage());
        }

        return false;
    }

    @Override
    public boolean eliminar(Integer id) {
        String sql = "DELETE FROM proveedores WHERE id_proveedor = ?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            if ("23000".equals(e.getSQLState())) {
                System.out.println("El proveedor tiene compras relacionadas.");
            } else {
                System.out.println("Error al eliminar proveedor: " + e.getMessage());
            }
        }

        return false;
    }

    @Override
    public boolean existe(int id) {
        return consultarExiste("SELECT 1 FROM proveedores WHERE id_proveedor = ?", id, null);
    }

    @Override
    public boolean existeRuc(String ruc, int idExcluido) {
        if (ruc == null || ruc.trim().isEmpty()) {
            return false;
        }
        return consultarExiste("SELECT 1 FROM proveedores WHERE ruc = ? AND id_proveedor <> ?", idExcluido, ruc.trim());
    }

    @Override
    public boolean tieneCompras(int id) {
        return consultarExiste("SELECT 1 FROM compras WHERE id_proveedor = ? LIMIT 1", id, null);
    }

    // Con ruc != null el primer parametro es el ruc y el segundo el id.
    private boolean consultarExiste(String sql, int id, String ruc) {
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (ruc != null) {
                ps.setString(1, ruc);
                ps.setInt(2, id);
            } else {
                ps.setInt(1, id);
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            System.out.println("Error al consultar proveedor: " + e.getMessage());
        }

        return false;
    }

    private Proveedor mapear(ResultSet rs) throws SQLException {
        return new Proveedor(
                rs.getInt("id_proveedor"),
                rs.getString("nombre"),
                rs.getString("ruc"),
                rs.getString("telefono"),
                rs.getString("direccion"),
                rs.getString("productos"),
                rs.getString("estado")
        );
    }

    private String vacioANulo(String s) {
        return s == null || s.trim().isEmpty() ? null : s.trim();
    }
}
