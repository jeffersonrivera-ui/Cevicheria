package dao.impl;

import conexion.ConexionBD;
import dao.DeliveryDAO;
import modelo.Delivery;
import modelo.SeguimientoDelivery;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class DeliveryDAOImpl implements DeliveryDAO {

    private static final String SELECT_DELIVERY = "SELECT * FROM deliveries ";


    @Override
    public int registrar(Delivery d, String descripcionInicial) {

        String sqlInsert = "INSERT INTO deliveries (id_pedido, codigo_delivery, plataforma, codigo_externo, "
                + "direccion_entrega, referencia_entrega, costo_delivery, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlCodigo = "UPDATE deliveries SET codigo_delivery = ? WHERE id_delivery = ?";

        try (Connection con = ConexionBD.getConnection()) {

            con.setAutoCommit(false);

            try {
                int id;

                try (PreparedStatement ps = con.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, d.getIdPedido());
                    ps.setString(2, "TMP-" + System.nanoTime());
                    ps.setString(3, d.getPlataforma());
                    ps.setString(4, d.getCodigoExterno());
                    ps.setString(5, d.getDireccionEntrega());
                    ps.setString(6, d.getReferenciaEntrega());
                    ps.setDouble(7, d.getCostoDelivery());
                    ps.setString(8, Delivery.SOLICITADO);
                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) {
                            throw new SQLException("No se genero el id del delivery.");
                        }
                        id = rs.getInt(1);
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(sqlCodigo)) {
                    ps.setString(1, String.format("DEL-%03d", id));
                    ps.setInt(2, id);
                    ps.executeUpdate();
                }

                insertarSeguimiento(con, id, Delivery.SOLICITADO,
                        descripcionInicial == null || descripcionInicial.trim().isEmpty()
                                ? "Delivery solicitado" : descripcionInicial.trim());

                con.commit();
                return id;

            } catch (SQLException e) {
                con.rollback();
                System.out.println("Error al registrar delivery: " + e.getMessage());
            } finally {
                con.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.out.println("Error de conexion al registrar delivery: " + e.getMessage());
        }

        return -1;
    }


    @Override
    public List<Delivery> listar() {
        List<Delivery> lista = new ArrayList<>();
        String sql = SELECT_DELIVERY + "ORDER BY id_delivery DESC";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error al listar deliveries: " + e.getMessage());
        }

        return lista;
    }

    @Override
    public Delivery buscarPorId(int idDelivery) {
        return buscarUno(SELECT_DELIVERY + "WHERE id_delivery = ?", ps -> ps.setInt(1, idDelivery));
    }

    @Override
    public Delivery buscarPorCodigo(String codigo) {
        return buscarUno(SELECT_DELIVERY + "WHERE codigo_delivery = ?", ps -> ps.setString(1, codigo));
    }

    @Override
    public Delivery buscarPorPedido(int idPedido) {
        return buscarUno(SELECT_DELIVERY + "WHERE id_pedido = ?", ps -> ps.setInt(1, idPedido));
    }

    @Override
    public boolean existePorPedido(int idPedido) {
        return buscarPorPedido(idPedido) != null;
    }

    private interface Parametros {
        void poner(PreparedStatement ps) throws SQLException;
    }

    private Delivery buscarUno(String sql, Parametros parametros) {
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            parametros.poner(ps);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al buscar delivery: " + e.getMessage());
        }

        return null;
    }

    // ==========================================
    // ACTUALIZAR ESTADO (delivery + seguimiento)
    // ==========================================

    @Override
    public boolean actualizarEstado(int idDelivery, String nuevoEstado, String descripcion) {

        String sql = "UPDATE deliveries SET estado = ? WHERE id_delivery = ?";

        try (Connection con = ConexionBD.getConnection()) {

            con.setAutoCommit(false);

            try {
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, nuevoEstado);
                    ps.setInt(2, idDelivery);

                    if (ps.executeUpdate() == 0) {
                        con.rollback();
                        return false;
                    }
                }

                insertarSeguimiento(con, idDelivery, nuevoEstado, descripcion == null || descripcion.trim().isEmpty() ? "Cambio de estado a " + nuevoEstado : descripcion.trim());

                con.commit();
                return true;

            } catch (SQLException e) {
                con.rollback();
                System.out.println("Error al actualizar estado del delivery: " + e.getMessage());
            } finally {
                con.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.out.println("Error de conexion al actualizar delivery: " + e.getMessage());
        }

        return false;
    }

    // ==========================================
    // HISTORIAL DE SEGUIMIENTO
    // ==========================================

    @Override
    public List<SeguimientoDelivery> listarSeguimiento(int idDelivery) {
        List<SeguimientoDelivery> lista = new ArrayList<>();

        String sql = "SELECT * FROM seguimiento_delivery WHERE id_delivery = ? ORDER BY fecha, id_seguimiento";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idDelivery);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp f = rs.getTimestamp("fecha");
                    lista.add(new SeguimientoDelivery(
                            rs.getInt("id_seguimiento"),
                            rs.getInt("id_delivery"),
                            rs.getString("estado"),
                            rs.getString("descripcion"),
                            f == null ? null : f.toString()
                    ));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al listar seguimiento: " + e.getMessage());
        }

        return lista;
    }

    // ==========================================
    // AUXILIARES
    // ==========================================

    private void insertarSeguimiento(Connection con, int idDelivery, String estado, String descripcion)
            throws SQLException {

        String sql = "INSERT INTO seguimiento_delivery (id_delivery, estado, descripcion) VALUES (?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idDelivery);
            ps.setString(2, estado);
            ps.setString(3, descripcion);
            ps.executeUpdate();
        }
    }

    private Delivery mapear(ResultSet rs) throws SQLException {
        Timestamp f = rs.getTimestamp("fecha_solicitud");
        return new Delivery(
                rs.getInt("id_delivery"),
                rs.getInt("id_pedido"),
                rs.getString("codigo_delivery"),
                rs.getString("plataforma"),
                rs.getString("codigo_externo"),
                rs.getString("direccion_entrega"),
                rs.getString("referencia_entrega"),
                rs.getDouble("costo_delivery"),
                rs.getString("estado"),
                f == null ? null : f.toString()
        );
    }
}
