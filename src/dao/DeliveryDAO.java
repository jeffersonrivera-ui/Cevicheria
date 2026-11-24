package dao;

import modelo.Delivery;
import modelo.SeguimientoDelivery;

import java.util.List;

public interface DeliveryDAO {
    int registrar(Delivery delivery, String descripcionInicial);
    List<Delivery> listar();
    Delivery buscarPorId(int idDelivery);
    Delivery buscarPorCodigo(String codigo);
    Delivery buscarPorPedido(int idPedido);
    boolean existePorPedido(int idPedido);
    boolean actualizarEstado(int idDelivery, String nuevoEstado, String descripcion);
    List<SeguimientoDelivery> listarSeguimiento(int idDelivery);
}
