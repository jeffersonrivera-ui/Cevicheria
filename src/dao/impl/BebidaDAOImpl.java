package dao.impl;

import conexion.ConexionBD;
import dao.BebidaDAO;
import modelo.Bebida;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BebidaDAOImpl implements BebidaDAO {

    @Override
    public int insertar(Bebida bebida) {

        String sql = "INSERT INTO bebidas (nombre, precio, stock) VALUES (?, ?, ?)";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, bebida.getNombre());
            ps.setDouble(2, bebida.getPrecio());
            ps.setInt(3, bebida.getStock());

            int filas = ps.executeUpdate();

            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {

                    if (rs.next()) {

                        return rs.getInt(1);
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al registrar bebida: " + e.getMessage());
        }

        return 0;
    }

    @Override
    public List<Bebida> listar() {

        List<Bebida> lista = new ArrayList<>();

        String sql = "SELECT id_bebida, nombre, precio, stock FROM bebidas ORDER BY id_bebida";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Bebida bebida = new Bebida(
                        rs.getInt("id_bebida"),
                        rs.getString("nombre"),
                        rs.getDouble("precio"),
                        rs.getInt("stock")
                );

                lista.add(bebida);
            }

        } catch (SQLException e) {
            System.out.println("Error al listar bebidas: " + e.getMessage());
        }

        return lista;
    }

    @Override
    public Bebida buscarPorId(Integer id) {

        String sql = "SELECT id_bebida, nombre, precio, stock FROM bebidas WHERE id_bebida = ?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    return new Bebida(
                            rs.getInt("id_bebida"),
                            rs.getString("nombre"),
                            rs.getDouble("precio"),
                            rs.getInt("stock")
                    );
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al buscar bebida: " + e.getMessage());
        }

        return null;
    }

    @Override
    public boolean actualizar(Bebida bebida) {

        String sql = "UPDATE bebidas SET nombre = ?, precio = ?, stock = ? WHERE id_bebida = ?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, bebida.getNombre());
            ps.setDouble(2, bebida.getPrecio());
            ps.setInt(3, bebida.getStock());
            ps.setInt(4, bebida.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al actualizar bebida: " + e.getMessage());
        }

        return false;
    }

    @Override
    public boolean eliminar(Integer id) {

        String sql = "DELETE FROM bebidas WHERE id_bebida = ?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al eliminar bebida: " + e.getMessage());
        }

        return false;
    }

    @Override
    public Boolean existe(int id) {

        String sql = "SELECT id_bebida FROM bebidas WHERE id_bebida = ?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            System.out.println("Error al verificar bebida: " + e.getMessage());
        }

        return null;
    }
}
