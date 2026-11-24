
package dao.impl;

import dao.ClienteDAO;
import conexion.ConexionBD;
import modelo.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAOImpl implements ClienteDAO {

    // ==========================================
    // INSERTAR CLIENTE
    // ==========================================

    @Override
    public int insertar(Cliente cliente) {

        String sql = "INSERT INTO clientes (nombres, apellidos, telefono, direccion, referencia, distrito) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = ConexionBD.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, cliente.getNombres());
            ps.setString(2, cliente.getApellidos());
            ps.setString(3, cliente.getTelefono());
            ps.setString(4, vacioANulo(cliente.getDireccion()));
            ps.setString(5, vacioANulo(cliente.getReferencia()));
            ps.setString(6, vacioANulo(cliente.getDistrito()));

            int filas = ps.executeUpdate();

            if (filas > 0) {

                try (ResultSet rs = ps.getGeneratedKeys()) {

                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }

        } catch (SQLException e) {

            System.out.println("Error al insertar cliente: " + e.getMessage());
        }

        return -1;
    }

    // ==========================================
    // LISTAR CLIENTES
    // ==========================================

    @Override
    public List<Cliente> listar() {
        List<Cliente> clientes = new ArrayList<>();

        String sql = "SELECT * FROM clientes ORDER BY id_cliente";

        try (Connection connection = ConexionBD.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                clientes.add(mapear(rs));
            }

        } catch (SQLException e) {

            System.out.println("Error al listar clientes: " + e.getMessage());
        }

        return clientes;
    }

    // ==========================================
    // BUSCAR CLIENTE POR ID
    // ==========================================

    @Override
    public Cliente buscarPorId(Integer id) {

        String sql = "SELECT * FROM clientes WHERE id_cliente = ?";

        try (Connection connection = ConexionBD.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }

        } catch (SQLException e) {

            System.out.println("Error al buscar cliente: " + e.getMessage());
        }

        return null;
    }

    // ==========================================
    // ACTUALIZAR CLIENTE
    // ==========================================

    @Override
    public boolean actualizar(Cliente cliente) {

        String sql = "UPDATE clientes SET nombres = ?, apellidos = ?, telefono = ?, direccion = ?, referencia = ?, distrito = ? WHERE id_cliente = ?";

        try (Connection connection = ConexionBD.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, cliente.getNombres());
            ps.setString(2, cliente.getApellidos());
            ps.setString(3, cliente.getTelefono());
            ps.setString(4, vacioANulo(cliente.getDireccion()));
            ps.setString(5, vacioANulo(cliente.getReferencia()));
            ps.setString(6, vacioANulo(cliente.getDistrito()));
            ps.setInt(7, cliente.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {

            System.out.println("Error al actualizar cliente: " + e.getMessage());
        }

        return false;
    }

    // ==========================================
    // ELIMINAR CLIENTE
    // ==========================================

    @Override
    public boolean eliminar(Integer id) {

        String sql = "DELETE FROM clientes WHERE id_cliente = ?";

        try (Connection connection = ConexionBD.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {

            if ("23000".equals(e.getSQLState())) {

                System.out.println("El cliente tiene pedidos relacionados.");

            } else {
                System.out.println("Error al eliminar cliente: " + e.getMessage());
            }
        }

        return false;
    }

    // ==========================================
    // VERIFICAR SI EXISTE CLIENTE
    // ==========================================

    @Override
    public boolean existe(int id) {

        String sql = "SELECT id_cliente FROM clientes WHERE id_cliente = ?";

        try (Connection connection = ConexionBD.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                return rs.next();
            }

        } catch (SQLException e) {

            System.out.println("Error al verificar cliente: " + e.getMessage());
        }

        return false;
    }

    // ==========================================
    // AUXILIARES
    // ==========================================

    private Cliente mapear(ResultSet rs) throws SQLException {
        return new Cliente(
                rs.getInt("id_cliente"),
                rs.getString("nombres"),
                rs.getString("telefono"),
                rs.getString("apellidos"),
                rs.getString("direccion"),
                rs.getString("referencia"),
                rs.getString("distrito")
        );
    }

    private String vacioANulo(String texto) {
        return texto == null || texto.trim().isEmpty() ? null : texto.trim();
    }
}
