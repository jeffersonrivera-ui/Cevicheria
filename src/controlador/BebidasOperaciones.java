package controlador;

import dao.BebidaDAO;
import dao.impl.BebidaDAOImpl;
import modelo.Bebida;

import java.util.List;

public class BebidasOperaciones {

    private static final BebidaDAO bebidaDAO = new BebidaDAOImpl();

    // REGISTRAR BEBIDA
    public static void registrarBebida(String nombre, double precio) {
        registrarBebida(nombre, precio, 0);
    }

    public static void registrarBebida(String nombre, double precio, int stock) {

        if (stock < 0) {
            System.out.println("La cantidad no puede ser negativa.");
            return;
        }

        if (nombre == null || nombre.trim().isEmpty()) {
            System.out.println("El nombre de la bebida es obligatorio.");
            return;
        }

        if (!Double.isFinite(precio) || precio <= 0) {
            System.out.println("El precio debe ser mayor que cero.");
            return;
        }

        Bebida bebida = new Bebida(0, nombre.trim(), precio, stock);

        int id = bebidaDAO.insertar(bebida);

        if (id > 0) {

            System.out.println("Bebida registrada correctamente.");
            System.out.println("ID generado: " + id);

        } else {
            System.out.println("No se pudo registrar la bebida.");

        }
    }

    // LISTAR BEBIDAS
    public static void listarBebidas() {

        List<Bebida> bebidas = bebidaDAO.listar();

        if (bebidas.isEmpty()) {
            System.out.println("No hay bebidas registradas.");
            return;
        }

        for (Bebida bebida : bebidas) {

            System.out.println("-------------------------");
            System.out.println("ID: " + bebida.getId());
            System.out.println("Nombre: " + bebida.getNombre());
            System.out.println("Precio: S/ " + bebida.getPrecio());
            System.out.println("Cantidad disponible: " + bebida.getStock());
        }
    }

    // BUSCAR BEBIDA
    public static void buscarBebida(int id) {

        if (id <= 0) {
            System.out.println("El ID debe ser mayor que cero.");
            return;
        }

        Bebida bebida = bebidaDAO.buscarPorId(id);

        if (bebida == null) {
            System.out.println("No se encontro la bebida.");
            return;
        }

        System.out.println("-------------------------");
        System.out.println("ID: " + bebida.getId());
        System.out.println("Nombre: " + bebida.getNombre());
        System.out.println("Precio: S/ " + bebida.getPrecio());
            System.out.println("Cantidad disponible: " + bebida.getStock());
    }

    // BUSCAR BEBIDA Y DEVOLVER OBJETO
    public static Bebida buscarBebidaObjeto(int idBebida) {

        if (idBebida <= 0) {
            return null;
        }

        return bebidaDAO.buscarPorId(idBebida);
    }

    // ACTUALIZAR BEBIDA
    public static void actualizarBebida(int id, String nombre, double precio) {
        Bebida actual = bebidaDAO.buscarPorId(id);
        actualizarBebida(id, nombre, precio, actual == null ? 0 : actual.getStock());
    }

    public static void actualizarBebida(int id, String nombre, double precio, int stock) {

        if (stock < 0) {
            System.out.println("La cantidad no puede ser negativa.");
            return;
        }

        if (id <= 0) {
            System.out.println("El ID debe ser mayor que cero.");
            return;
        }

        if (nombre == null || nombre.trim().isEmpty()) {
            System.out.println("El nombre de la bebida es obligatorio.");
            return;
        }

        if (!Double.isFinite(precio) || precio <= 0) {
            System.out.println("El precio debe ser mayor que cero.");
            return;
        }

        Bebida bebida = new Bebida(id, nombre.trim(), precio, stock);

        if (bebidaDAO.actualizar(bebida)) {
            System.out.println("Bebida actualizada correctamente.");

        } else {
            System.out.println("No se encontro la bebida o no se pudo actualizar.");

        }
    }

    // ELIMINAR BEBIDA
    public static void eliminarBebida(int id) {

        if (id <= 0) {
            System.out.println("El ID debe ser mayor que cero.");

            return;
        }

        if (bebidaDAO.eliminar(id)) {
            System.out.println("Bebida eliminada correctamente.");

        } else {
            System.out.println("No se pudo eliminar la bebida.");

        }
    }

    // EXISTE BEBIDA
    public static Boolean existeBebida(int id) {

        if (id <= 0) {
            return false;
        }

        return bebidaDAO.existe(id);
    }
}
