package controlador;

import dao.PedidoDAO;
import dao.impl.PedidoDAOImpl;
import modelo.Pedido;

import java.util.List;

public class PedidosOperaciones {

    private static final PedidoDAO pedidoDAO = new PedidoDAOImpl();

    public static int registrarPedido(int idCliente, int idMesero) {

        int id = pedidoDAO.insertar(idCliente, idMesero);

        if (id > 0) {

            Pedido pedido = pedidoDAO.buscarPorId(id);

            System.out.println("================================");
            System.out.println("PEDIDO REGISTRADO CORRECTAMENTE");
            System.out.println("================================");
            System.out.println("ID del pedido: " + id);
            System.out.println("ID del cliente: " + idCliente);
            System.out.println("ID del mesero: " + idMesero);

            if (pedido == null) {
                System.out.println("El pedido se registró, pero no se pudieron recuperar sus datos.");
            }
        }

        return id;
    }

    public static void listarPedidos() {

        List<Pedido> pedidos = pedidoDAO.listar();

        if (pedidos.isEmpty()) {
            System.out.println("No hay pedidos registrados.");
            return;
        }

        for (Pedido pedido : pedidos) {

            System.out.println("================================");
            System.out.println("PEDIDO N.° " + pedido.getId());
            System.out.println("================================");

            pedido.mostrarInformacion();

            System.out.println("DETALLE DEL PEDIDO");
            System.out.println("--------------------------------");

            DetallePedidoOperacione.listarDetalles(pedido.getId());

            DetallePedidoOperacione.calcularTotalPedido(pedido.getId());

            System.out.println("================================");
        }
    }

    public static void buscarPedido(int id) {

        Pedido pedido = pedidoDAO.buscarPorId(id);

        if (pedido != null) {

            System.out.println("================================");
            System.out.println("PEDIDO N.° " + pedido.getId());
            System.out.println("================================");

            pedido.mostrarInformacion();

            System.out.println("DETALLE DEL PEDIDO");

            DetallePedidoOperacione.listarDetalles(id);

            DetallePedidoOperacione.calcularTotalPedido(id);

        } else {

            System.out.println("No se encontró el pedido.");
        }
    }

    // BUSCAR PEDIDO Y DEVOLVER OBJETO
    public static Pedido buscarPedidoObjeto(int idPedido) {

        if (idPedido <= 0) {
            return null;
        }

        return pedidoDAO.buscarPorId(idPedido);
    }


    public static void actualizarEstadoPedido(int id, String estado) {

        if (estado == null || estado.trim().isEmpty()) {
            System.out.println("Debe indicar un estado válido.");
            return;
        }

        pedidoDAO.actualizarEstado(id, estado);
    }


    public static void eliminarPedido(int id) {

        pedidoDAO.eliminar(id);
    }


    public static Boolean existePedido(int id) {

        return pedidoDAO.existe(id);
    }
}
