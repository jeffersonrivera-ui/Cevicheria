package controlador;

import dao.ClienteDAO;
import dao.DeliveryDAO;
import dao.PedidoDAO;
import dao.impl.ClienteDAOImpl;
import dao.impl.DeliveryDAOImpl;
import dao.impl.PedidoDAOImpl;
import modelo.Delivery;
import modelo.Pedido;
import modelo.SeguimientoDelivery;

import java.util.List;

public class DeliveryOperaciones {

    private static final DeliveryDAO deliveryDAO = new DeliveryDAOImpl();
    private static final PedidoDAO pedidoDAO = new PedidoDAOImpl();
    private static final ClienteDAO clienteDAO = new ClienteDAOImpl();


    // Resultado de una operacion: la vista lo usa para mostrar el mensaje exacto.
    public static class Resultado {
        public final boolean ok;
        public final String mensaje;
        public final int id;

        Resultado(boolean ok, String mensaje, int id) {
            this.ok = ok;
            this.mensaje = mensaje;
            this.id = id;
        }
    }

    private static Resultado fallo(String mensaje) {
        System.out.println(mensaje);

        return new Resultado(false, mensaje, -1);
    }

    private static Resultado exito(String mensaje, int id) {
        System.out.println(mensaje);

        return new Resultado(true, mensaje, id);
    }


    public static Resultado registrarDelivery(int idPedido, String plataforma, String codigoExterno, String direccionEntrega, String referenciaEntrega, double costoDelivery) {

        Pedido pedido = pedidoDAO.buscarPorId(idPedido);

        if (pedido == null) {
            return fallo("El pedido N.° " + idPedido + " no existe.");
        }

        if ("CANCELADO".equalsIgnoreCase(pedido.getEstado())) {
            return fallo("No se puede registrar un delivery para un pedido cancelado.");
        }

        if (deliveryDAO.existePorPedido(idPedido)) {
            return fallo("El pedido N.° " + idPedido + " ya tiene un delivery registrado.");
        }

        if (plataforma == null || !plataformaValida(plataforma)) {
            return fallo("La plataforma debe ser inDrive, Rappi u Otro.");
        }

        if (direccionEntrega == null || direccionEntrega.trim().isEmpty()) {
            return fallo("La dirección de entrega es obligatoria.");
        }

        if (costoDelivery < 0) {
            return fallo("El costo del delivery no puede ser negativo.");
        }

        Delivery d = new Delivery(0, idPedido, null, canonica(plataforma), vacioANulo(codigoExterno), direccionEntrega.trim(),
                vacioANulo(referenciaEntrega), costoDelivery, Delivery.SOLICITADO, null);

        int id = deliveryDAO.registrar(d, "Delivery solicitado por " + canonica(plataforma));

        if (id > 0) {

            return exito("Delivery registrado correctamente. Código: " + String.format("DEL-%03d", id), id);
        }
        return fallo("No se pudo registrar el delivery.");
    }


    public static List<Delivery> listarDeliveries() {
        return deliveryDAO.listar();
    }

    public static Delivery buscarPorId(int idDelivery) {
        return deliveryDAO.buscarPorId(idDelivery);
    }

    public static Delivery buscarPorCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return null;
        }
        return deliveryDAO.buscarPorCodigo(codigo.trim().toUpperCase());
    }

    public static Delivery buscarPorPedido(int idPedido) {
        return deliveryDAO.buscarPorPedido(idPedido);
    }

    public static boolean pedidoTieneDelivery(int idPedido) {
        return deliveryDAO.existePorPedido(idPedido);
    }

    public static List<SeguimientoDelivery> listarSeguimiento(int idDelivery) {
        return deliveryDAO.listarSeguimiento(idDelivery);
    }


    public static Resultado actualizarEstado(int idDelivery, String nuevoEstado, String descripcion) {

        Delivery d = deliveryDAO.buscarPorId(idDelivery);

        if (d == null) {
            return fallo("No se encontró el delivery N.° " + idDelivery + ".");
        }

        String error = validarTransicion(d.getEstado(), nuevoEstado);
        if (error != null) {
            return fallo(error);
        }

        if (deliveryDAO.actualizarEstado(idDelivery, nuevoEstado, descripcion)) {
            return exito("Estado actualizado a " + nuevoEstado + ".", idDelivery);
        }
        return fallo("No se pudo actualizar el estado del delivery.");
    }


    // Devuelve null si el cambio es valido, o el motivo por el que no lo es.

    public static String validarTransicion(String actual, String nuevo) {

        if (nuevo == null || indice(nuevo) < 0 && !Delivery.CANCELADO.equals(nuevo)) {
            return "El estado '" + nuevo + "' no es válido.";
        }

        if (Delivery.ENTREGADO.equals(actual) || Delivery.CANCELADO.equals(actual)) {
            return "El delivery ya está " + actual + " y no admite más cambios.";
        }

        if (Delivery.CANCELADO.equals(nuevo)) {
            return null;
        }

        int i = indice(actual);
        int j = indice(nuevo);

        if (j == i) {
            return "El delivery ya está en estado " + actual + ".";
        }
        if (j < i) {
            return "No se puede retroceder de " + actual + " a " + nuevo + ".";
        }
        if (j > i + 1) {
            return "No se puede saltar de " + actual + " a " + nuevo
                    + ". El siguiente estado es " + Delivery.FLUJO[i + 1] + ".";
        }
        return null;
    }

    // Estados a los que se puede pasar desde el actual (el siguiente del flujo y CANCELADO).

    public static String[] estadosSiguientes(String actual) {

        if (Delivery.ENTREGADO.equals(actual) || Delivery.CANCELADO.equals(actual) || indice(actual) < 0) {
            return new String[0];
        }
        return new String[]{Delivery.FLUJO[indice(actual) + 1], Delivery.CANCELADO};
    }


    private static int indice(String estado) {
        for (int i = 0; i < Delivery.FLUJO.length; i++) {
            if (Delivery.FLUJO[i].equals(estado)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean plataformaValida(String p) {
        return canonica(p) != null;
    }

    private static String canonica(String p) {
        for (String valida : Delivery.PLATAFORMAS) {
            if (valida.equalsIgnoreCase(p.trim())) {
                return valida;
            }
        }
        return null;
    }

    private static String vacioANulo(String s) {
        return s == null || s.trim().isEmpty() ? null : s.trim();
    }

    // Datos del cliente de un pedido para precargar la direccion de entrega.

    public static modelo.Cliente clienteDelPedido(Pedido pedido) {
        if (pedido == null || pedido.getCliente() == null) {
            return null;
        }
        return clienteDAO.buscarPorId(pedido.getCliente().getId());
    }
}
