
package dao;

import modelo.DetallePedido;
import modelo.Ingrediente;

import java.util.List;

public interface DetallePedidoDAO {

    int insertar(DetallePedido detalle);

    List<DetallePedido> listarPorPedido(int idPedido);

    DetallePedido buscarPorId(int idDetalle);

    boolean actualizar(DetallePedido detalle);

    boolean eliminar(int idDetalle);

    double calcularTotal(int idPedido);
}
