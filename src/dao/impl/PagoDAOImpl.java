package dao.impl;

import conexion.ConexionBD;
import dao.PagoDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PagoDAOImpl implements PagoDAO {

    @Override
    public Map<String, Object> registrarPago(
            int idPedido, double monto, String metodoPago) {

        String sqlPedido = "SELECT estado FROM pedidos WHERE id_pedido = ? FOR UPDATE";

        String sqlTotal = "SELECT COALESCE(SUM(t.cantidad * t.precio), 0) AS total FROM (SELECT id_pedido, cantidad, precio FROM detalle_pedido UNION ALL SELECT id_pedido, cantidad, precio FROM detalle_bebida) t WHERE t.id_pedido = ?";

        String sqlPagado = "SELECT COALESCE(SUM(monto), 0) AS total_pagado FROM pagos WHERE id_pedido = ? AND estado IN ('Pagado', 'Adelanto')";

        // Un pedido usa UN solo registro de pago: si ya existe se reutiliza (mismo id) y se actualiza.
        String sqlExistente = "SELECT id_pago FROM pagos WHERE id_pedido = ? AND estado IN ('Pagado', 'Adelanto') ORDER BY id_pago LIMIT 1 FOR UPDATE";

        String sqlInsertar = "INSERT INTO pagos (id_pedido, monto, metodo_pago, estado) VALUES (?, ?, ?, ?)";

        String sqlActualizar = "UPDATE pagos SET monto = monto + ?, metodo_pago = ?, fecha = NOW(), estado = ? WHERE id_pago = ?";

        try (Connection connection = ConexionBD.getConnection()) {

            connection.setAutoCommit(false);

            try {
                // Verificar y bloquear el pedido
                String estadoPedido;

                try (PreparedStatement ps =
                             connection.prepareStatement(sqlPedido)) {

                    ps.setInt(1, idPedido);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {
                            connection.rollback();
                            System.out.println("El pedido no existe.");
                            return null;
                        }

                        estadoPedido = rs.getString("estado");
                    }
                }

                if ("CANCELADO".equalsIgnoreCase(estadoPedido)) {
                    connection.rollback();
                    System.out.println("No se puede pagar un pedido cancelado.");
                    return null;
                }

                // Obtener el total del pedido.
                double totalPedido;

                try (PreparedStatement ps =
                             connection.prepareStatement(sqlTotal)) {

                    ps.setInt(1, idPedido);

                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        totalPedido = rs.getDouble("total");
                    }
                }

                if (totalPedido <= 0) {
                    connection.rollback();
                    System.out.println(
                            "El pedido no tiene detalles registrados.");
                    return null;
                }

                // Obtener el total pagado anteriormente/
                double totalPagado;

                try (PreparedStatement ps = connection.prepareStatement(sqlPagado)) {

                    ps.setInt(1, idPedido);

                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        totalPagado = rs.getDouble("total_pagado");
                    }
                }

                // Calcular el saldo pendiente/
                double saldoPendiente = totalPedido - totalPagado;

                if (saldoPendiente <= 0.01) {
                    connection.rollback();
                    System.out.println("El pedido ya esta pagado completamente.");
                    return null;
                }

                if (monto > saldoPendiente + 0.01) {
                    connection.rollback();
                    System.out.println("El monto supera el saldo pendiente.");
                    System.out.println("Saldo pendiente: S/ " + saldoPendiente);
                    return null;
                }

                // Registrar el pago: reutilizar el registro del pedido o crear el primero.
                boolean completo = saldoPendiente - monto <= 0.01;
                String nuevoEstado = completo ? "Pagado" : "Adelanto";
                int idPago = 0;

                try (PreparedStatement ps = connection.prepareStatement(sqlExistente)) {
                    ps.setInt(1, idPedido);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            idPago = rs.getInt("id_pago");
                        }
                    }
                }

                if (idPago > 0) {
                    try (PreparedStatement ps = connection.prepareStatement(sqlActualizar)) {
                        ps.setDouble(1, monto);
                        ps.setString(2, metodoPago);
                        ps.setString(3, nuevoEstado);
                        ps.setInt(4, idPago);

                        if (ps.executeUpdate() == 0) {
                            connection.rollback();
                            System.out.println("No se pudo actualizar el pago.");
                            return null;
                        }
                    }
                } else {
                    try (PreparedStatement ps = connection.prepareStatement(sqlInsertar, PreparedStatement.RETURN_GENERATED_KEYS)) {
                        ps.setInt(1, idPedido);
                        ps.setDouble(2, monto);
                        ps.setString(3, metodoPago);
                        ps.setString(4, nuevoEstado);

                        if (ps.executeUpdate() == 0) {
                            connection.rollback();
                            System.out.println("No se pudo registrar el pago.");
                            return null;
                        }

                        try (ResultSet rs = ps.getGeneratedKeys()) {
                            if (!rs.next()) {
                                connection.rollback();
                                return null;
                            }
                            idPago = rs.getInt(1);
                        }
                    }
                }

                // 6. Confirmar la transacción

                connection.commit();

                totalPagado += monto;
                saldoPendiente = totalPedido - totalPagado;

                Map<String, Object> pago = new LinkedHashMap<>();

                pago.put("id_pago", idPago);
                pago.put("id_pedido", idPedido);
                pago.put("monto", monto);
                pago.put("metodo_pago", metodoPago);
                pago.put("total_pedido", totalPedido);
                pago.put("total_pagado", totalPagado);
                pago.put("saldo_pendiente", saldoPendiente);
                pago.put("estado", nuevoEstado);

                return pago;

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            System.out.println("Error al registrar el pago: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<Map<String, Object>> listarPagos() {

        List<Map<String, Object>> pagos = new ArrayList<>();

        String sql = "SELECT id_pago, id_pedido, monto, metodo_pago, fecha, estado FROM pagos ORDER BY id_pago";

        try (Connection connection = ConexionBD.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Map<String, Object> pago = new LinkedHashMap<>();

                pago.put("id_pago", rs.getInt("id_pago"));
                pago.put("id_pedido", rs.getInt("id_pedido"));
                pago.put("monto", rs.getDouble("monto"));
                pago.put("metodo_pago", rs.getString("metodo_pago"));
                pago.put("fecha", rs.getString("fecha"));
                pago.put("estado", rs.getString("estado"));

                pagos.add(pago);
            }

        } catch (SQLException e) {
            System.out.println(
                    "Error al listar pagos: " + e.getMessage());
        }

        return pagos;
    }

    @Override
    public Map<String, Object> buscarPago(int idPago) {

        String sql = "SELECT id_pago, id_pedido, monto, metodo_pago, fecha, estado FROM pagos WHERE id_pago = ?";

        try (Connection connection = ConexionBD.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, idPago);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    Map<String, Object> pago = new LinkedHashMap<>();

                    pago.put("id_pago", rs.getInt("id_pago"));
                    pago.put("id_pedido", rs.getInt("id_pedido"));
                    pago.put("monto", rs.getDouble("monto"));
                    pago.put("metodo_pago", rs.getString("metodo_pago"));
                    pago.put("fecha", rs.getString("fecha"));
                    pago.put("estado", rs.getString("estado"));

                    return pago;
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al buscar pago: " + e.getMessage());
        }

        return null;
    }
}

