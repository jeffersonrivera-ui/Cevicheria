package dao;

import modelo.DetalleBebida;

import java.util.List;

public interface DetalleBebidaDAO {

    int insertar(DetalleBebida detalle);

    List<DetalleBebida> listarPorPedido(int idPedido);

    DetalleBebida buscarPorId(int idDetalleBebida);

    boolean actualizarCantidad(int idDetalleBebida, int cantidad);

    boolean eliminar(int idDetalleBebida);

    double calcularTotal(int idPedido);
}
