package dao.impl;

import conexion.ConexionBD;
import dao.PedidoDAO;
import modelo.Pedido;
import modelo.Cliente;
import modelo.Mesero;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class  PedidoDAOImpl implements PedidoDAO {

    @Override
    public int insertar(int idCliente, int idMesero) {

        String sql = "INSERT INTO pedidos (id_cliente, id_mesero) VALUES (?, ?)";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Verificar que el cliente exista
            if (!existeCliente(con, idCliente)) {

                System.out.println("El cliente no existe.");
                return -1;
            }

            // Verificar que el empleado sea un mesero activo
            String sqlMesero = "SELECT COUNT(*) FROM empleados WHERE id_empleado = ? AND cargo = 'MESERO' AND estado_laboral = 'ACTIVO'";

            try (PreparedStatement validar = con.prepareStatement(sqlMesero)) {
                validar.setInt(1, idMesero);

                try (ResultSet rs = validar.executeQuery()) {
                    rs.next();

                    if (rs.getInt(1) == 0) {

                        System.out.println("El mesero no existe o no está activo.");
                        return -1;
                    }
                }
            }

            ps.setInt(1, idCliente);
            ps.setInt(2, idMesero);

            int filas = ps.executeUpdate();

            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al registrar pedido: " + e.getMessage());
        }

        return -1;
    }

    @Override
    public List<Pedido> listar() {

        List<Pedido> lista = new ArrayList<>();

        String sql = "SELECT p.id_pedido, p.fecha, p.estado, c.id_cliente, c.nombres AS nombres_cliente, " +
                "c.apellidos AS apellidos_cliente, c.telefono AS telefono_cliente, e.id_empleado, e.nombres, e.apellidos, e.telefono, " +
                "e.direccion, e.dni, e.contacto_emergencia, e.correo, e.distrito, e.provincia, e.departamento, e.codigo_postal, " +
                "e.referencia_direccion, e.salario, e.turno_laboral, e.fecha_contratacion, e.estado_laboral " +
                "FROM pedidos p INNER JOIN clientes c ON p.id_cliente = c.id_cliente " +
                "INNER JOIN empleados e ON p.id_mesero = e.id_empleado ORDER BY p.id_pedido";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                lista.add(crearPedido(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error al listar pedidos: " + e.getMessage());
        }

        return lista;
    }

    @Override
    public Pedido buscarPorId(int id) {

        String sql = "SELECT p.id_pedido, p.fecha, p.estado, c.id_cliente, c.nombres AS nombres_cliente, " +
                "c.apellidos AS apellidos_cliente, c.telefono AS telefono_cliente,e.id_empleado, e.nombres, e.apellidos, e.telefono, " +
                "e.direccion, e.dni, e.contacto_emergencia, e.correo,e.distrito, e.provincia, e.departamento, e.codigo_postal, " +
                "e.referencia_direccion, e.salario, e.turno_laboral,e.fecha_contratacion, e.estado_laboral " +
                "FROM pedidos p INNER JOIN clientes c ON p.id_cliente = c.id_cliente " +
                "INNER JOIN empleados e ON p.id_mesero = e.id_empleado WHERE p.id_pedido = ?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {

                    return crearPedido(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al buscar pedido: " + e.getMessage());
        }

        return null;
    }

    private boolean existeCliente(Connection con, int idCliente) throws SQLException {

        String sql = "SELECT COUNT(*) FROM clientes WHERE id_cliente = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCliente);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    private Pedido crearPedido(ResultSet rs) throws SQLException {

        Cliente cliente = new Cliente(
                rs.getInt("id_cliente"),
                rs.getString("nombres_cliente"),
                rs.getString("telefono_cliente"),
                rs.getString("apellidos_cliente")
        );

        Date fechaSQL = rs.getDate("fecha");
        String fecha = fechaSQL != null ? fechaSQL.toString() : null;

        Date fechaContratacionSQL = rs.getDate("fecha_contratacion");
        LocalDate fechaContratacion = fechaContratacionSQL != null
                ? fechaContratacionSQL.toLocalDate()
                : null;

        Mesero mesero = new Mesero(
                rs.getInt("id_empleado"),
                rs.getString("nombres"),
                rs.getString("telefono"),
                rs.getString("apellidos"),
                rs.getString("direccion"),
                rs.getString("dni"),
                rs.getString("contacto_emergencia"),
                rs.getString("correo"),
                rs.getString("distrito"),
                rs.getString("provincia"),
                rs.getString("departamento"),
                rs.getString("codigo_postal"),
                rs.getString("referencia_direccion"),
                rs.getDouble("salario"),
                rs.getString("turno_laboral"),
                fechaContratacion,
                rs.getString("estado_laboral")
        );

        return new Pedido(
                rs.getInt("id_pedido"),
                cliente,
                mesero,
                fecha,
                rs.getString("estado")
        );
    }

    // Los demás métodos se agregarán en el siguiente paso.


    @Override
    public boolean actualizarEstado(int id, String nuevoEstado) {

        if (nuevoEstado == null) {
            System.out.println("El estado no es válido.");
            return false;
        }

        nuevoEstado = nuevoEstado.trim().toUpperCase();

        String[] estadosPermitidos = {
                "PENDIENTE", "EN PREPARACION", "LISTO",
                "ENTREGADO", "CANCELADO"
        };

        boolean valido = false;

        for (String estado : estadosPermitidos) {
            if (estado.equals(nuevoEstado)) {
                valido = true;
                break;
            }
        }

        if (!valido) {
            System.out.println("Estado no permitido.");
            return false;
        }

        try (Connection con = ConexionBD.getConnection()) {

            con.setAutoCommit(false);

            String estadoActual;
            boolean inventarioDescontado;

            // 1. Buscar y bloquear el pedido
            String sqlBuscar = "SELECT estado, inventario_descontado FROM pedidos WHERE id_pedido = ? FOR UPDATE";

            try (PreparedStatement ps = con.prepareStatement(sqlBuscar)) {

                ps.setInt(1, id);

                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {
                        con.rollback();
                        System.out.println("El pedido no existe.");
                        return false;
                    }

                    estadoActual = rs.getString("estado");
                    inventarioDescontado = rs.getBoolean("inventario_descontado");
                }
            }

            // 2. Validar transición de estado
            boolean transicionPermitida = ("PENDIENTE".equals(estadoActual) && ("EN PREPARACION".equals(nuevoEstado) || "CANCELADO".equals(nuevoEstado)))
                            || ("EN PREPARACION".equals(estadoActual) && ("LISTO".equals(nuevoEstado) || "CANCELADO".equals(nuevoEstado)))
                            || ("LISTO".equals(estadoActual) && "ENTREGADO".equals(nuevoEstado));

            // Permitir reintentar el descuento si ya está LISTO
            boolean reintentarDescuento = "LISTO".equals(estadoActual) && "LISTO".equals(nuevoEstado) && !inventarioDescontado;

            if (!transicionPermitida && !reintentarDescuento) {
                con.rollback();
                System.out.println("No se permite cambiar de " + estadoActual + " a " + nuevoEstado + ".");
                return false;
            }

            // 3. Descontar inventario cuando corresponda
            boolean descontar = "LISTO".equals(nuevoEstado) && !inventarioDescontado;

            if (descontar) {

                if (!descontarInventarioPedido(con, id)) {
                    con.rollback();
                    System.out.println("No se pudo descontar el inventario. " + "Verifique los ingredientes y sus cantidades.");
                    return false;
                }

                String sqlInventario = "UPDATE pedidos SET inventario_descontado = TRUE WHERE id_pedido = ?";

                try (PreparedStatement ps = con.prepareStatement(sqlInventario)) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
            }

            // 3.1 Si se cancela, las bebidas vuelven al stock
            if ("CANCELADO".equals(nuevoEstado) && !"CANCELADO".equals(estadoActual)) {
                devolverStockBebidas(con, id);
            }

            // 4. Actualizar estado solamente si cambió
            if (!estadoActual.equals(nuevoEstado)) {

                String sqlActualizar = "UPDATE pedidos SET estado = ? WHERE id_pedido = ?";

                try (PreparedStatement ps = con.prepareStatement(sqlActualizar)) {

                    ps.setString(1, nuevoEstado);
                    ps.setInt(2, id);

                    if (ps.executeUpdate() == 0) {
                        con.rollback();
                        return false;
                    }
                }
            }

            // 5. Confirmar transacción
            con.commit();

            System.out.println("Estado del pedido actualizado correctamente.");

            // La actualización de disponibilidad es posterior al commit.
            if (descontar) {
                try {
                    actualizarDisponibilidadPlatos();
                } catch (SQLException e) {
                    System.out.println(
                            "Aviso: no se pudo actualizar la disponibilidad " +
                                    "de los platos: " + e.getMessage());
                }
            }

            return true;

        } catch (SQLException e) {
            System.out.println(
                    "Error al actualizar pedido: " + e.getMessage());
            return false;
        }
    }

    // Suma de nuevo al stock las bebidas que tenia el pedido.
    private void devolverStockBebidas(Connection con, int idPedido) throws SQLException {

        String sql = "UPDATE bebidas b SET b.stock = b.stock + (SELECT COALESCE(SUM(d.cantidad), 0) FROM detalle_bebida d "
                + "WHERE d.id_pedido = ? AND d.id_bebida = b.id_bebida) "
                + "WHERE b.id_bebida IN (SELECT id_bebida FROM detalle_bebida WHERE id_pedido = ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            ps.setInt(2, idPedido);
            ps.executeUpdate();
        }
    }

    private boolean descontarInventarioPedido(
            Connection con, int idPedido) throws SQLException {

        String sqlRecetas =
                "SELECT COUNT(*) FROM detalle_pedido dp " +
                        "WHERE dp.id_pedido = ? AND NOT EXISTS (" +
                        "SELECT 1 FROM plato_ingrediente pi " +
                        "WHERE pi.id_plato = dp.id_plato)";

        try (PreparedStatement ps = con.prepareStatement(sqlRecetas)) {
            ps.setInt(1, idPedido);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();

                if (rs.getInt(1) > 0) {
                    System.out.println(
                            "Hay platos del pedido sin ingredientes registrados.");
                    return false;
                }
            }
        }

        // La consulta de ingredientes (JOIN + NOT EXISTS + GROUP BY) esta en el
        // procedimiento sp_ingredientes_requeridos
        String sqlIngredientes = "{CALL sp_ingredientes_requeridos(?)}";

        try (CallableStatement cs = con.prepareCall(sqlIngredientes)) {

            cs.setInt(1, idPedido);

            try (ResultSet rs = cs.executeQuery()) {

                while (rs.next()) {

                    int idIngrediente = rs.getInt("id_ingrediente");
                    double cantidad = rs.getDouble("cantidad_requerida");
                    double stock = rs.getDouble("stock");

                    if (stock < cantidad) {
                        System.out.println("Stock insuficiente para el ingrediente ID: " + idIngrediente);
                        return false;
                    }

                    String sqlDescontar = "UPDATE ingredientes SET stock = stock - ? WHERE id_ingrediente = ? AND stock >= ?";

                    try (PreparedStatement descontar = con.prepareStatement(sqlDescontar)) {

                        descontar.setDouble(1, cantidad);
                        descontar.setInt(2, idIngrediente);
                        descontar.setDouble(3, cantidad);

                        if (descontar.executeUpdate() == 0) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    private void actualizarDisponibilidadPlatos() throws SQLException {

        String sql = "UPDATE platos p SET p.activo = (" +
                        "EXISTS (SELECT 1 FROM plato_ingrediente pi " +
                        "WHERE pi.id_plato = p.id_plato) " +
                        "AND NOT EXISTS (" +
                        "SELECT 1 FROM plato_ingrediente pi " +
                        "INNER JOIN ingredientes i " +
                        "ON pi.id_ingrediente = i.id_ingrediente " +
                        "WHERE pi.id_plato = p.id_plato " +
                        "AND i.stock < pi.cantidad))";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }

    @Override
    public boolean eliminar(int id) {

        try (Connection con = ConexionBD.getConnection()) {

            con.setAutoCommit(false);

            boolean inventarioDescontado;
            String estado;

            // Buscar y bloquear el pedido
            String sqlBuscar = "SELECT estado, inventario_descontado FROM pedidos WHERE id_pedido = ? FOR UPDATE";

            try (PreparedStatement ps = con.prepareStatement(sqlBuscar)) {

                ps.setInt(1, id);

                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {

                        con.rollback();
                        System.out.println("El pedido no existe.");
                        return false;
                    }

                    estado = rs.getString("estado");
                    inventarioDescontado = rs.getBoolean("inventario_descontado");
                }
            }

            // No permitir eliminar pedidos entregados
            if ("ENTREGADO".equalsIgnoreCase(estado)) {
                con.rollback();
                System.out.println("No se puede eliminar un pedido entregado.");
                return false;
            }

            // Verificar pagos
            String sqlPagos = "SELECT COUNT(*) FROM pagos WHERE id_pedido = ?";

            try (PreparedStatement ps = con.prepareStatement(sqlPagos)) {

                ps.setInt(1, id);

                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();

                    if (rs.getInt(1) > 0) {
                        con.rollback();
                        System.out.println("No se puede eliminar: el pedido tiene pagos registrados.");
                        return false;
                    }
                }
            }

            // Verificar ventas
            String sqlVentas = "SELECT COUNT(*) FROM ventas WHERE id_pedido = ?";

            try (PreparedStatement ps = con.prepareStatement(sqlVentas)) {

                ps.setInt(1, id);

                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();

                    if (rs.getInt(1) > 0) {
                        con.rollback();
                        System.out.println("No se puede eliminar: el pedido tiene una venta registrada.");
                        return false;
                    }
                }
            }

            // Devolver ingredientes al inventario si fueron descontados
            if (inventarioDescontado) {

                //////////////// procedimiento///////////////

                String sqlIngredientes = "{CALL sp_ingredientes_devolver(?)}";

                try (CallableStatement cs = con.prepareCall(sqlIngredientes)) {

                    cs.setInt(1, id);

                    try (ResultSet rs = cs.executeQuery()) {

                        while (rs.next()) {

                            int idIngrediente = rs.getInt("id_ingrediente");

                            double cantidad = rs.getDouble("cantidad");

                            String sqlDevolver = "UPDATE ingredientes SET stock = stock + ? WHERE id_ingrediente = ?";

                            try (PreparedStatement devolver = con.prepareStatement(sqlDevolver)) {

                                devolver.setDouble(1, cantidad);
                                devolver.setInt(2, idIngrediente);
                                devolver.executeUpdate();
                            }
                        }
                    }
                }
            }

            // 6. Eliminar personalizaciones de ingredientes
            String sqlExclusiones = "DELETE ex FROM detalle_ingrediente_excluido ex INNER JOIN detalle_pedido dp " +
                            "ON ex.id_detalle = dp.id_detalle WHERE dp.id_pedido = ?";

            try (PreparedStatement ps = con.prepareStatement(sqlExclusiones)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }

            // 6.0 Devolver al stock las bebidas del pedido
            devolverStockBebidas(con, id);

            // 6.1 Eliminar bebidas del pedido
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM detalle_bebida WHERE id_pedido = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }

            // 7. Eliminar detalles del pedido
            String sqlDetalles = "DELETE FROM detalle_pedido WHERE id_pedido = ?";

            try (PreparedStatement ps = con.prepareStatement(sqlDetalles)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }

            // 8. Eliminar pedido
            String sqlEliminar = "DELETE FROM pedidos WHERE id_pedido = ?";

            try (PreparedStatement ps = con.prepareStatement(sqlEliminar)) {

                ps.setInt(1, id);

                if (ps.executeUpdate() == 0) {
                    con.rollback();
                    return false;
                }
            }

            con.commit();

            System.out.println("Pedido eliminado correctamente.");

            if (inventarioDescontado) {
                actualizarDisponibilidadPlatos();
            }

            return true;

        } catch (SQLException e) {
            System.out.println("Error al eliminar pedido: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Boolean existe(int id) {

        String sql = "SELECT COUNT(*) FROM pedidos WHERE id_pedido = ?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.out.println("Error al verificar pedido: " + e.getMessage());
            return false;
        }
    }
}
