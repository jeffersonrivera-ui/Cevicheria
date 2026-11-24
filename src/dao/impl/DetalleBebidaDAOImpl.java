package dao.impl;

import conexion.ConexionBD;
import dao.DetalleBebidaDAO;
import modelo.Bebida;
import modelo.DetalleBebida;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DetalleBebidaDAOImpl implements DetalleBebidaDAO {

    @Override
    public int insertar(DetalleBebida detalle) {

        if (detalle == null || detalle.getBebida() == null) {
            System.out.println("Los datos del detalle de bebida están incompletos.");
            return -1;
        }

        int idPedido = detalle.getIdPedido();
        int idBebida = detalle.getBebida().getId();
        int cantidad = detalle.getCantidad();

        if (cantidad <= 0) {
            System.out.println("La cantidad debe ser mayor que cero.");
            return -1;
        }

        String sqlPedido = "SELECT estado FROM pedidos WHERE id_pedido = ? FOR UPDATE";
        String sqlPagos = "SELECT COUNT(*) FROM pagos WHERE id_pedido = ?";
        String sqlPrecio = "SELECT precio, stock FROM bebidas WHERE id_bebida = ? FOR UPDATE";
        String sqlDescontar = "UPDATE bebidas SET stock = stock - ? WHERE id_bebida = ?";
        String sqlInsertar = "INSERT INTO detalle_bebida (id_pedido, id_bebida, cantidad, precio) VALUES (?, ?, ?, ?)";

        try (Connection connection = ConexionBD.getConnection()) {

            connection.setAutoCommit(false);

            try {
                String estado;

                try (PreparedStatement ps = connection.prepareStatement(sqlPedido)) {
                    ps.setInt(1, idPedido);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            System.out.println("El pedido no existe.");
                            connection.rollback();
                            return -1;
                        }
                        estado = rs.getString("estado");
                    }
                }

                if (estado == null ||
                        (!estado.equalsIgnoreCase("PENDIENTE")
                                && !estado.equalsIgnoreCase("EN PREPARACION"))) {
                    System.out.println("No se pueden agregar bebidas a un pedido finalizado.");
                    connection.rollback();
                    return -1;
                }

                try (PreparedStatement ps = connection.prepareStatement(sqlPagos)) {
                    ps.setInt(1, idPedido);

                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        if (rs.getInt(1) > 0) {
                            System.out.println("El pedido tiene pagos registrados.");
                            connection.rollback();
                            return -1;
                        }
                    }
                }

                double precio;
                int stock;

                try (PreparedStatement ps = connection.prepareStatement(sqlPrecio)) {
                    ps.setInt(1, idBebida);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            System.out.println("La bebida no existe.");
                            connection.rollback();
                            return -1;
                        }
                        precio = rs.getDouble("precio");
                        stock = rs.getInt("stock");
                    }
                }

                if (stock < cantidad) {
                    System.out.println("Stock insuficiente: solo quedan " + stock + " unidades.");
                    connection.rollback();
                    return -1;
                }

                try (PreparedStatement ps = connection.prepareStatement(sqlDescontar)) {
                    ps.setInt(1, cantidad);
                    ps.setInt(2, idBebida);
                    ps.executeUpdate();
                }

                int idDetalle;

                try (PreparedStatement ps = connection.prepareStatement(sqlInsertar, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, idPedido);
                    ps.setInt(2, idBebida);
                    ps.setInt(3, cantidad);
                    ps.setDouble(4, precio);

                    if (ps.executeUpdate() == 0) {
                        connection.rollback();
                        return -1;
                    }

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) {
                            connection.rollback();
                            return -1;
                        }
                        idDetalle = rs.getInt(1);
                    }
                }

                connection.commit();
                return idDetalle;

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            System.out.println("Error al registrar detalle de bebida: " + e.getMessage());
            return -1;
        }
    }

    @Override
    public List<DetalleBebida> listarPorPedido(int idPedido) {

        List<DetalleBebida> lista = new ArrayList<>();

        String sql = "SELECT db.id_detalle_bebida, db.id_pedido, db.id_bebida, b.nombre, db.cantidad, db.precio " +
                "FROM detalle_bebida db INNER JOIN bebidas b ON db.id_bebida = b.id_bebida " +
                "WHERE db.id_pedido = ? ORDER BY db.id_detalle_bebida";

        try (Connection connection = ConexionBD.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, idPedido);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Bebida bebida = new Bebida(rs.getInt("id_bebida"), rs.getString("nombre"), rs.getDouble("precio"));
                    lista.add(new DetalleBebida(rs.getInt("id_detalle_bebida"), rs.getInt("id_pedido"),
                            bebida, rs.getInt("cantidad"), rs.getDouble("precio")));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al listar bebidas del pedido: " + e.getMessage());
        }

        return lista;
    }

    @Override
    public DetalleBebida buscarPorId(int idDetalleBebida) {

        String sql = "SELECT db.id_detalle_bebida, db.id_pedido, db.id_bebida, b.nombre, db.cantidad, db.precio " +
                "FROM detalle_bebida db INNER JOIN bebidas b ON db.id_bebida = b.id_bebida " +
                "WHERE db.id_detalle_bebida = ?";

        try (Connection connection = ConexionBD.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, idDetalleBebida);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Bebida bebida = new Bebida(rs.getInt("id_bebida"), rs.getString("nombre"), rs.getDouble("precio"));
                    return new DetalleBebida(rs.getInt("id_detalle_bebida"), rs.getInt("id_pedido"),
                            bebida, rs.getInt("cantidad"), rs.getDouble("precio"));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al buscar detalle de bebida: " + e.getMessage());
        }

        return null;
    }

    // Modifica la cantidad pedida y ajusta el stock de la bebida con la diferencia.
    @Override
    public boolean actualizarCantidad(int idDetalleBebida, int cantidad) {

        if (cantidad <= 0) {
            System.out.println("La cantidad debe ser mayor que cero.");
            return false;
        }

        String sqlDetalle = "SELECT db.id_bebida, db.cantidad FROM detalle_bebida db " +
                "INNER JOIN pedidos p ON db.id_pedido = p.id_pedido " +
                "WHERE db.id_detalle_bebida = ? AND p.estado IN ('PENDIENTE', 'EN PREPARACION') " +
                "AND NOT EXISTS (SELECT 1 FROM pagos pg WHERE pg.id_pedido = p.id_pedido) FOR UPDATE";
        String sqlStock = "SELECT stock FROM bebidas WHERE id_bebida = ? FOR UPDATE";

        try (Connection connection = ConexionBD.getConnection()) {

            connection.setAutoCommit(false);

            try {
                int idBebida;
                int anterior;

                try (PreparedStatement ps = connection.prepareStatement(sqlDetalle)) {
                    ps.setInt(1, idDetalleBebida);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            connection.rollback();
                            return false;
                        }
                        idBebida = rs.getInt("id_bebida");
                        anterior = rs.getInt("cantidad");
                    }
                }

                int diferencia = cantidad - anterior;

                int stock;
                try (PreparedStatement ps = connection.prepareStatement(sqlStock)) {
                    ps.setInt(1, idBebida);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            connection.rollback();
                            return false;
                        }
                        stock = rs.getInt("stock");
                    }
                }

                if (diferencia > stock) {
                    System.out.println("Stock insuficiente: solo quedan " + stock + " unidades.");
                    connection.rollback();
                    return false;
                }

                try (PreparedStatement ps = connection.prepareStatement(
                        "UPDATE detalle_bebida SET cantidad = ? WHERE id_detalle_bebida = ?")) {
                    ps.setInt(1, cantidad);
                    ps.setInt(2, idDetalleBebida);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = connection.prepareStatement(
                        "UPDATE bebidas SET stock = stock - ? WHERE id_bebida = ?")) {
                    ps.setInt(1, diferencia);
                    ps.setInt(2, idBebida);
                    ps.executeUpdate();
                }

                connection.commit();
                return true;

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            System.out.println("Error al actualizar detalle de bebida: " + e.getMessage());
        }

        return false;
    }

    // Quita la bebida del pedido y devuelve las unidades al stock.
    @Override
    public boolean eliminar(int idDetalleBebida) {

        String sqlDetalle = "SELECT db.id_bebida, db.cantidad FROM detalle_bebida db " +
                "INNER JOIN pedidos p ON db.id_pedido = p.id_pedido " +
                "WHERE db.id_detalle_bebida = ? AND p.estado IN ('PENDIENTE', 'EN PREPARACION') " +
                "AND NOT EXISTS (SELECT 1 FROM pagos pg WHERE pg.id_pedido = p.id_pedido) FOR UPDATE";

        try (Connection connection = ConexionBD.getConnection()) {

            connection.setAutoCommit(false);

            try {
                int idBebida;
                int cantidad;

                try (PreparedStatement ps = connection.prepareStatement(sqlDetalle)) {
                    ps.setInt(1, idDetalleBebida);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            connection.rollback();
                            return false;
                        }
                        idBebida = rs.getInt("id_bebida");
                        cantidad = rs.getInt("cantidad");
                    }
                }

                try (PreparedStatement ps = connection.prepareStatement(
                        "DELETE FROM detalle_bebida WHERE id_detalle_bebida = ?")) {
                    ps.setInt(1, idDetalleBebida);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = connection.prepareStatement(
                        "UPDATE bebidas SET stock = stock + ? WHERE id_bebida = ?")) {
                    ps.setInt(1, cantidad);
                    ps.setInt(2, idBebida);
                    ps.executeUpdate();
                }

                connection.commit();
                return true;

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            System.out.println("Error al eliminar detalle de bebida: " + e.getMessage());
        }

        return false;
    }

    @Override
    public double calcularTotal(int idPedido) {

        String sql = "SELECT COALESCE(SUM(cantidad * precio), 0) AS total FROM detalle_bebida WHERE id_pedido = ?";

        try (Connection connection = ConexionBD.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, idPedido);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al calcular total de bebidas: " + e.getMessage());
        }

        return 0;
    }
}
