
package controlador;

import dao.DetallePedidoDAO;
import dao.impl.DetallePedidoDAOImpl;
import modelo.DetallePedido;
import modelo.Pedido;
import modelo.Plato;

import java.util.List;

public class DetallePedidoOperacione {

    private static final DetallePedidoDAO detalleDAO = new DetallePedidoDAOImpl();

    public static int registrarDetalle(int idPedido, int idPlato, int cantidad, String observacion) {

        if (cantidad <= 0) {
            System.out.println("La cantidad debe ser mayor que cero.");
            return -1;
        }

        Pedido pedido = PedidosOperaciones.buscarPedidoObjeto(idPedido);
        Plato plato = PlatosOperaciones.buscarPlatoObjeto(idPlato);

        if (pedido == null || plato == null) {
            System.out.println("No se encontró el pedido o el plato.");
            return -1;
        }

        DetallePedido detalle = new DetallePedido(0, pedido, plato, cantidad, plato.getPrecio(), observacion);

        return detalleDAO.insertar(detalle);
    }


    public static void listarDetalles(int idPedido) {

        List<DetallePedido> detalles = detalleDAO.listarPorPedido(idPedido);

        System.out.println("=== DETALLES DEL PEDIDO ===");

        if (detalles.isEmpty()) {
            System.out.println("Este pedido no tiene platos registrados.");
            return;
        }

        for (DetallePedido detalle : detalles) {

            System.out.println("-------------------------");
            System.out.println("ID detalle: " + detalle.getId());
            System.out.println("Plato: " + detalle.getPlato().getNombre());
            System.out.println("Cantidad: " + detalle.getCantidad());
            System.out.println("Precio: S/ " + detalle.getPrecio());

            String observacion = detalle.getObservacion();

            System.out.println("Observacion: " + (observacion == null || observacion.isEmpty() ? "Sin observaciones" : observacion));

            System.out.println("Subtotal: S/ " + detalle.calcularSubtotal());
        }
    }

    public static void buscarDetalle(int idDetalle) {

        DetallePedido detalle = detalleDAO.buscarPorId(idDetalle);

        if (detalle == null) {
            System.out.println("No se encontro el detalle.");
            return;
        }

        System.out.println("=== DETALLE ENCONTRADO ===");
        System.out.println("ID: " + detalle.getId());
        System.out.println("Pedido: " + detalle.getPedido().getId());
        System.out.println("Plato: " + detalle.getPlato().getNombre());
        System.out.println("Cantidad: " + detalle.getCantidad());
        System.out.println("Precio: S/ " + detalle.getPrecio());

        String observacion = detalle.getObservacion();

        System.out.println("Observacion: " + (observacion == null || observacion.isEmpty() ? "Sin observaciones" : observacion));

        System.out.println("Subtotal: S/ " + detalle.calcularSubtotal());
    }

    public static void actualizarDetalle(int idDetalle, int cantidad, String observacion) {

        if (cantidad <= 0) {
            System.out.println("La cantidad debe ser mayor que cero.");
            return;
        }

        // Buscar el detalle existente

        DetallePedido detalle = detalleDAO.buscarPorId(idDetalle);

        if (detalle == null) {
            System.out.println("No se encontró el detalle.");
            return;
        }
        detalle.setCantidad(cantidad);
        detalle.setObservacion(observacion);

        detalleDAO.actualizar(detalle);
    }


    public static void eliminarDetalle(int idDetalle) {

        detalleDAO.eliminar(idDetalle);
    }


    public static double calcularTotalPedido(int idPedido) {

        double total = detalleDAO.calcularTotal(idPedido);

        if (total >= 0) {
            System.out.println("Total del pedido: S/ " + total);
        }

        return total;
    }
}
