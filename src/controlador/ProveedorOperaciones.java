package controlador;

import dao.ProveedorDAO;
import dao.impl.ProveedorDAOImpl;
import modelo.Proveedor;

import java.util.List;

public class ProveedorOperaciones {

    private static final ProveedorDAO proveedorDAO = new ProveedorDAOImpl();

    // Resultado de una operacion: la vista muestra el mensaje exacto.

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

    // Devuelve null si los datos son validos, o el motivo del error.
    private static String validar(int idActual, String nombre, String ruc, String telefono, String direccion, String productos, String estado) {

        if (nombre == null || nombre.trim().isEmpty()) {
            return "El nombre del proveedor es obligatorio.";
        }
        if (nombre.trim().length() > 100) {
            return "El nombre admite hasta 100 caracteres.";
        }
        if (ruc != null && !ruc.trim().isEmpty()) {

            if (!ruc.trim().matches("\\d{11}")) {

                return "El RUC debe tener 11 dígitos (o déjelo vacío).";
            }
            if (proveedorDAO.existeRuc(ruc.trim(), idActual)) {
                return "Ya existe un proveedor con ese RUC.";
            }
        }
        if (telefono != null && !telefono.trim().isEmpty() && !telefono.trim().matches("[0-9+ ]{6,15}")) {
            return "El teléfono solo puede tener números (de 6 a 15 dígitos).";
        }
        if (direccion != null && direccion.trim().length() > 200) {
            return "La dirección admite hasta 200 caracteres.";
        }
        if (productos != null && productos.trim().length() > 200) {
            return "Los productos admiten hasta 200 caracteres.";
        }
        if (!Proveedor.ACTIVO.equals(estado) && !Proveedor.INACTIVO.equals(estado)) {
            return "El estado debe ser ACTIVO o INACTIVO.";
        }
        return null;
    }

    public static Resultado registrarProveedor(String nombre, String ruc, String telefono, String direccion, String productos) {

        String error = validar(0, nombre, ruc, telefono, direccion, productos, Proveedor.ACTIVO);

        if (error != null) {
            return fallo(error);
        }

        int id = proveedorDAO.insertar(new Proveedor(0, nombre.trim(), ruc, telefono, direccion, productos, Proveedor.ACTIVO));

        if (id > 0) {
            return exito("Proveedor registrado correctamente. ID generado: " + id, id);
        }

        return fallo("No se pudo registrar el proveedor.");
    }

    public static Resultado actualizarProveedor(int id, String nombre, String ruc, String telefono, String direccion, String productos, String estado) {

        Proveedor p = proveedorDAO.buscarPorId(id);

        if (p == null) {
            return fallo("No se encontró el proveedor.");
        }

        String error = validar(id, nombre, ruc, telefono, direccion, productos, estado);

        if (error != null) {
            return fallo(error);
        }

        p.setNombre(nombre.trim());
        p.setRuc(ruc);
        p.setTelefono(telefono);
        p.setDireccion(direccion);
        p.setProductos(productos);
        p.setEstado(estado);

        if (proveedorDAO.actualizar(p)) {

            return exito("Proveedor actualizado correctamente.", id);
        }
        return fallo("No se pudo actualizar el proveedor.");
    }

    public static Resultado eliminarProveedor(int id) {

        if (!proveedorDAO.existe(id)) {

            return fallo("No se encontró el proveedor.");
        }
        if (proveedorDAO.tieneCompras(id)) {

            return fallo("No se puede eliminar: el proveedor tiene compras registradas."
                    + "Cámbielo a INACTIVO para que ya no aparezca al registrar compras.");
        }
        if (proveedorDAO.eliminar(id)) {

            return exito("Proveedor eliminado correctamente.", id);
        }
        return fallo("No se pudo eliminar el proveedor.");
    }

    public static List<Proveedor> listarProveedores() {
        return proveedorDAO.listar();
    }

    public static Proveedor buscarProveedor(int id) {
        return proveedorDAO.buscarPorId(id);
    }

    public static boolean existeProveedor(int id) {
        return proveedorDAO.existe(id);
    }
}
