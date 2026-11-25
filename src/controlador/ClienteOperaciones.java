
package controlador;

import dao.ClienteDAO;
import dao.impl.ClienteDAOImpl;

import java.util.List;

import modelo.Cliente;

public class ClienteOperaciones {

    /*conexion con la interfaz clienteDAO*/

    private static final ClienteDAO clienteDAO = new ClienteDAOImpl();

    public static void registrarCliente(String nombres, String apellidos, String telefono, String correo, String direccion, String referencia, String distrito) {

                           //o                         //o                 //o

        if (nombres == null || nombres.trim().isEmpty() || apellidos == null || apellidos.trim().isEmpty()) {

            System.out.println("Los nombres y apellidos son obligatorios.");
            return;
        }

        Cliente cliente = new Cliente(0, nombres.trim(), telefono, apellidos.trim(), limpiar(direccion), limpiar(referencia), limpiar(distrito), limpiar(correo));

        int id = clienteDAO.insertar(cliente);

        if (id > 0) {
            System.out.println("Cliente registrado correctamente.");
            System.out.println("ID generado: " + id);

        } else {
            System.out.println("No se pudo registrar el cliente.");
        }
    }

    public static void listarClientes() {

        List<Cliente> clientes = clienteDAO.listar();

        if (clientes == null || clientes.isEmpty()) {

            System.out.println("No hay clientes registrados.");

        } else {

            for (Cliente cliente : clientes) {

                cliente.mostrarInformacion();
                System.out.println("-------------------------");
            }
        }
    }


    public static void buscarCliente(int id) {

        Cliente cliente = clienteDAO.buscarPorId(id);

        if (cliente != null) {

            cliente.mostrarInformacion();

        } else {

            System.out.println("No se encontro el cliente.");
        }
    }


    public static void actualizarCliente(int id, String nombres, String apellidos, String telefono, String correo, String direccion, String referencia, String distrito) {

        if (nombres == null || nombres.trim().isEmpty() || apellidos == null || apellidos.trim().isEmpty()) {

            System.out.println("Los nombres y apellidos son obligatorios.");
            return;
        }

        // Primero buscamos el cliente
        Cliente cliente = clienteDAO.buscarPorId(id);

        if (cliente == null) {

            System.out.println("No se encontro el cliente.");
            return;
        }

        // Modificamos sus atributos

        cliente.setNombres(nombres.trim());
        cliente.setApellidos(apellidos.trim());
        cliente.setTelefono(telefono);
        cliente.setCorreo(limpiar(correo));
        cliente.setDireccion(limpiar(direccion));
        cliente.setReferencia(limpiar(referencia));
        cliente.setDistrito(limpiar(distrito));

        // Enviamos el objeto actualizado al DAO

        if (clienteDAO.actualizar(cliente)) {

            System.out.println("Cliente actualizado correctamente.");

        } else {

            System.out.println("No se pudo actualizar el cliente.");
        }
    }

    public static void eliminarCliente(int id) {

        if (clienteDAO.eliminar(id)) {

            System.out.println("Cliente eliminado correctamente.");

        } else {

            System.out.println("No se pudo eliminar el cliente. " + "Verifique si tiene pedidos relacionados.");
        }
    }



    public static boolean existeCliente(int id) {

        return clienteDAO.existe(id);
    }

    private static String limpiar(String texto) {
                                                       //si    //no
        return texto == null || texto.trim().isEmpty() ? null : texto.trim();
                               //el texto, después de quitarle los espacios de los lados, quedó vacío"
    }
}
