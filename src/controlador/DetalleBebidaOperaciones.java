package controlador;

import dao.DetalleBebidaDAO;
import dao.impl.DetalleBebidaDAOImpl;
import modelo.Bebida;
import modelo.DetalleBebida;

import java.util.List;

public class DetalleBebidaOperaciones {

    private static final DetalleBebidaDAO detalleBebidaDAO = new DetalleBebidaDAOImpl();

    // REGISTRAR BEBIDA EN UN PEDIDO

    public static int registrarDetalleBebida(int idPedido, int idBebida, int cantidad) {

        if (cantidad <= 0) {
            System.out.println("La cantidad debe ser mayor que cero.");
            return -1;
        }

        if (PedidosOperaciones.buscarPedidoObjeto(idPedido) == null) {
            System.out.println("No se encontró el pedido.");
            return -1;
        }

        Bebida bebida = BebidasOperaciones.buscarBebidaObjeto(idBebida);

        if (bebida == null) {
            System.out.println("No se encontró la bebida.");
            return -1;
        }

        DetalleBebida detalle = new DetalleBebida(0, idPedido, bebida, cantidad, bebida.getPrecio());

        return detalleBebidaDAO.insertar(detalle);
    }

    // LISTAR BEBIDAS DE UN PEDIDO

    public static void listarDetallesBebida(int idPedido) {

        List<DetalleBebida> detalles = detalleBebidaDAO.listarPorPedido(idPedido);

        if (detalles.isEmpty()) {
            System.out.println("El pedido no tiene bebidas registradas.");
            return;
        }

        for (DetalleBebida detalle : detalles) {
            System.out.println("-------------------------");
            detalle.mostrarInformacion();
        }
    }


    public static DetalleBebida buscarDetalleBebida(int idDetalleBebida) {
        return detalleBebidaDAO.buscarPorId(idDetalleBebida);
    }

    // ACTUALIZAR LA CANTIDAD DE UNA BEBIDA DEL PEDIDO

    public static boolean actualizarCantidad(int idDetalleBebida, int cantidad) {

        if (cantidad <= 0) {
            System.out.println("La cantidad debe ser mayor que cero.");
            return false;
        }

        if (detalleBebidaDAO.actualizarCantidad(idDetalleBebida, cantidad)) {
            System.out.println("Cantidad actualizada correctamente.");
            return true;
        }

        System.out.println("No se pudo actualizar la cantidad de la bebida.");
        return false;
    }

    // ELIMINAR BEBIDA DE UN PEDIDO

    public static void eliminarDetalleBebida(int idDetalleBebida) {

        if (idDetalleBebida <= 0) {
            System.out.println("El ID debe ser mayor que cero.");
            return;
        }

        if (detalleBebidaDAO.eliminar(idDetalleBebida)) {
            System.out.println("Bebida eliminada del pedido correctamente.");
        } else {
            System.out.println("No se pudo eliminar la bebida del pedido.");
        }
    }

    // TOTAL DE BEBIDAS DE UN PEDIDO

    public static double calcularTotalBebidas(int idPedido) {
        return detalleBebidaDAO.calcularTotal(idPedido);
    }
}
