package dao.impl;

import conexion.ConexionBD;
import dao.CompraDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CompraDAOImpl implements CompraDAO {

    @Override
    public int registrarCompra(int idIngrediente, double cantidad, double precioUnitario) {
        return registrarCompra(idIngrediente, cantidad, precioUnitario, null);
    }

    @Override
    public int registrarCompra(int idIngrediente, double cantidad, double precioUnitario, Integer idProveedor) {

        double total = cantidad * precioUnitario;

        String sqlVerificar = "SELECT id_ingrediente FROM ingredientes WHERE id_ingrediente = ? FOR UPDATE";
        //«Buscar el ingrediente por su ID y bloquear su registro mientras se realiza la transacción».

        String sqlCompra = idProveedor == null
                ? "INSERT INTO compras (total) VALUES (?)"
                : "INSERT INTO compras (total, id_proveedor) VALUES (?, ?)";
        //«Insertar una nueva compra en la tabla compras, guardando su total».

        String sqlDetalle = "INSERT INTO detalle_compra (id_compra, id_ingrediente, cantidad, precio_unitario) VALUES (?, ?, ?, ?)";
        //«Registrar en detalle_compra qué ingrediente se compró, en qué cantidad, a qué precio y a qué compra pertenece».

        String sqlStock = "UPDATE ingredientes SET stock = stock + ? WHERE id_ingrediente = ?";
        //«Actualizar el stock del ingrediente, sumándole la cantidad que acabamos de comprar».

        try (Connection connection = ConexionBD.getConnection()) {

            connection.setAutoCommit(false);

            try {
                // 1. Verificar y bloquear el ingrediente.

                PreparedStatement verificar = connection.prepareStatement(sqlVerificar);
                verificar.setInt(1, idIngrediente);

                try (ResultSet resultado = verificar.executeQuery()) {

                    if (!resultado.next()) {

                        connection.rollback();
                        System.out.println("El ingrediente no existe.");
                        return -1;
                    }
                }

                // 2. Registrar la compra.
                int idCompra;

                try (PreparedStatement registrar = connection.prepareStatement(sqlCompra, Statement.RETURN_GENERATED_KEYS)) {

                    registrar.setDouble(1, total);
                    if (idProveedor != null) {
                        registrar.setInt(2, idProveedor);
                    }

                    if (registrar.executeUpdate() == 0) {
                        connection.rollback();
                        return -1;
                    }

                    try (ResultSet claves = registrar.getGeneratedKeys()) {
                        if (!claves.next()) {
                            connection.rollback();
                            return -1;
                        }

                        idCompra = claves.getInt(1);
                    }
                }

                // 3. Registrar el detalle de la compra.
                try (PreparedStatement detalle = connection.prepareStatement(sqlDetalle)) {

                    detalle.setInt(1, idCompra);
                    detalle.setInt(2, idIngrediente);
                    detalle.setDouble(3, cantidad);
                    detalle.setDouble(4, precioUnitario);

                    if (detalle.executeUpdate() == 0) {
                        connection.rollback();
                        return -1;
                    }
                }

                // 4. Aumentar el stock del ingrediente.
                try (PreparedStatement actualizar = connection.prepareStatement(sqlStock)) {

                    actualizar.setDouble(1, cantidad);
                    actualizar.setInt(2, idIngrediente);

                    if (actualizar.executeUpdate() == 0) {
                        connection.rollback();
                        return -1;
                    }
                }

                // 5. Confirmar la transacción.
                connection.commit();

                return idCompra;

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            System.out.println(
                    "Error al registrar compra: " + e.getMessage());
            return -1;
        }
    }

    @Override
    public List<Map<String, Object>> listarCompras() {

        List<Map<String, Object>> compras = new ArrayList<>();

        String sql = "SELECT c.id_compra, c.fecha, c.total,dc.id_ingrediente, i.nombre, i.unidad, " +
                "dc.cantidad, dc.precio_unitario, c.id_proveedor, pr.nombre AS proveedor FROM compras c INNER JOIN detalle_compra dc " +
                "ON c.id_compra = dc.id_compra INNER JOIN ingredientes i ON dc.id_ingrediente = i.id_ingrediente " +
                "LEFT JOIN proveedores pr ON c.id_proveedor = pr.id_proveedor " +
                "ORDER BY c.id_compra DESC";

        try (Connection connection = ConexionBD.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> compra = new LinkedHashMap<>();

                compra.put("id_compra", rs.getInt("id_compra"));
                compra.put("fecha", rs.getString("fecha"));
                compra.put("total", rs.getDouble("total"));
                compra.put("id_ingrediente", rs.getInt("id_ingrediente"));
                compra.put("nombre", rs.getString("nombre"));
                compra.put("unidad", rs.getString("unidad"));
                compra.put("cantidad", rs.getDouble("cantidad"));
                compra.put("precio_unitario", rs.getDouble("precio_unitario"));
                compra.put("id_proveedor", rs.getObject("id_proveedor"));
                compra.put("proveedor", rs.getString("proveedor"));

                compras.add(compra);
            }

        } catch (SQLException e) {
            System.out.println("Error al listar compras: " + e.getMessage());
        }

        return compras;
    }

    @Override
    public List<Map<String, Object>> buscarCompra(int idCompra) {

        List<Map<String, Object>> compras = new ArrayList<>();

        String sql = "SELECT c.id_compra, c.fecha, c.total,dc.id_ingrediente, i.nombre, i.unidad, " +
                "dc.cantidad, dc.precio_unitario, c.id_proveedor, pr.nombre AS proveedor FROM compras c " +
                "INNER JOIN detalle_compra dc ON c.id_compra = dc.id_compra " +
                "INNER JOIN ingredientes i ON dc.id_ingrediente = i.id_ingrediente " +
                "LEFT JOIN proveedores pr ON c.id_proveedor = pr.id_proveedor " +
                "WHERE c.id_compra = ?";

        try (Connection connection = ConexionBD.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, idCompra);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    Map<String, Object> compra = new LinkedHashMap<>();

                    compra.put("id_compra", rs.getInt("id_compra"));
                    compra.put("fecha", rs.getString("fecha"));
                    compra.put("total", rs.getDouble("total"));
                    compra.put("id_ingrediente", rs.getInt("id_ingrediente"));
                    compra.put("nombre", rs.getString("nombre"));
                    compra.put("unidad", rs.getString("unidad"));
                    compra.put("cantidad", rs.getDouble("cantidad"));
                    compra.put("precio_unitario", rs.getDouble("precio_unitario"));
                    compra.put("id_proveedor", rs.getObject("id_proveedor"));
                    compra.put("proveedor", rs.getString("proveedor"));
                compra.put("id_proveedor", rs.getObject("id_proveedor"));
                compra.put("proveedor", rs.getString("proveedor"));

                    compras.add(compra);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al buscar compra: " + e.getMessage());
        }

        return compras;
    }
}

