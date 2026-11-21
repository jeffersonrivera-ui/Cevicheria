
package controlador;

import dao.EmpleadoDAO;
import dao.impl.EmpleadoDAOImpl;
import modelo.Empleado;
import modelo.Mesero;
import modelo.Cocinero;

import java.time.LocalDate;
import java.util.List;

public class EmpleadosOperaciones {

    private static final EmpleadoDAO empleadoDAO = new EmpleadoDAOImpl();

    public static void registrarEmpleado(String nombres, String apellidos, String telefono, String cargo, String direccion, String dni, String contactoEmergencia, String correo, String distrito, String provincia, String departamento, String codigoPostal, String referenciaDireccion,
            double salario, String turnoLaboral, LocalDate fechaContratacion) {

        if (nombres == null || nombres.trim().isEmpty() || apellidos == null || apellidos.trim().isEmpty() || dni == null || dni.trim().isEmpty() || cargo == null || cargo.trim().isEmpty()) {

            System.out.println("Complete los campos obligatorios.");
            return;
        }


        if (!cargo.equalsIgnoreCase("MESERO")
                && !cargo.equalsIgnoreCase("COCINERO")) {

            System.out.println("El cargo ingresado no es valido.");
            return;
        }

        if (!Double.isFinite(salario) || salario < 0) {
            System.out.println("El salario no puede ser negativo.");
            return;
        }

        nombres = nombres.trim();
        apellidos = apellidos.trim();
        dni = dni.trim();
        cargo = cargo.toUpperCase().trim();

        Empleado empleado;

        if (cargo.equals("MESERO")) {

            empleado = new Mesero(0, nombres, telefono, apellidos, direccion, dni, contactoEmergencia, correo, distrito, provincia, departamento, codigoPostal, referenciaDireccion, salario, turnoLaboral, fechaContratacion, "ACTIVO");

        } else {

            empleado = new Cocinero(0, nombres, telefono, apellidos, direccion, dni, contactoEmergencia, correo, distrito, provincia, departamento, codigoPostal, referenciaDireccion, salario, turnoLaboral, fechaContratacion, "ACTIVO");
        }

        int id = empleadoDAO.insertar(empleado);

        if (id > 0) {
            System.out.println("Empleado registrado correctamente.");
            System.out.println("ID generado: " + id);

        } else {

            System.out.println("No se pudo registrar el empleado.");
        }
    }


    public static void listarEmpleados() {

        List<Empleado> empleados = empleadoDAO.listar();

        if (empleados.isEmpty()) {
            System.out.println("No hay empleados registrados.");
            return;
        }

        for (Empleado empleado : empleados) {
            empleado.mostrarInformacion();
            System.out.println("-------------------------");
        }
    }


    public static void buscarEmpleadoPorId(int id) {

        Empleado empleados = empleadoDAO.buscarPorId(id);

        if (empleados == null) {
            System.out.println("No se encontró el empleado.");
            return;
        }

        empleados.mostrarInformacion();
    }

    public static void eliminarEmpleado(int id) {

        Empleado empleado = empleadoDAO.buscarPorId(id);

        if (empleado == null) {
            System.out.println("No se encontró el empleado.");
            return;
        }

        boolean eliminado = empleadoDAO.eliminar(id);

        if (eliminado) {
            System.out.println("Empleado eliminado correctamente.");
        } else {
            System.out.println("No se pudo eliminar el empleado.");
        }
    }

    public static void actualizarEmpleado (int id,
    String nombres,
    String apellidos,
    String cargo,
    String telefono,
    String direccion,
    String dni,
    String contactoEmergencia,
    String correo,
    String distrito,
    String provincia,
    String departamento,
    String codigoPostal,
    String referenciaDireccion,
    double salario,
    String turnoLaboral,
    LocalDate fechaContratacion) {

        Empleado empleadoActual = empleadoDAO.buscarPorId(id);

        if (empleadoActual == null) {
            System.out.println("El empleado no existe.");
            return;
        }

        // Validar datos obligatorios
        if (nombres == null || nombres.trim().isEmpty() || apellidos == null || apellidos.trim().isEmpty() || dni == null || dni.trim().isEmpty()) {
            System.out.println("Complete los campos obligatorios.");
            return;
        }

        // Validar salario
        if (!Double.isFinite(salario) || salario < 0) {
            System.out.println("El salario no puede ser negativo.");
            return;
        }

        nombres = nombres.trim();
        apellidos = apellidos.trim();
        cargo = cargo.toUpperCase().trim();
        dni = dni.trim();
        Empleado empleado;

        if (cargo.equals("MESERO")) {

            empleado = new Mesero(id, nombres, telefono, apellidos, direccion, dni, contactoEmergencia, correo, distrito, provincia, departamento, codigoPostal, referenciaDireccion, salario, turnoLaboral, fechaContratacion, empleadoActual.getEstadoLaboral()
            );

        } else {

            empleado = new Cocinero(id, nombres, telefono, apellidos, direccion, dni, contactoEmergencia, correo, distrito, provincia, departamento, codigoPostal, referenciaDireccion, salario, turnoLaboral, fechaContratacion, empleadoActual.getEstadoLaboral()
            );
        }

        // Actualizar en la base de datos
        if (empleadoDAO.actualizar(empleado)) {

            System.out.println("Empleado actualizado correctamente.");
        } else {

            System.out.println("No se pudo actualizar el empleado.");
        }
    }


    public static void actualizarEstado(String dni, String estado) {

        // Validar DNI
        if (dni == null || dni.trim().isEmpty()) {
            System.out.println("Debe ingresar un DNI.");
            return;
        }

        // Validar estado
        if (estado == null || (!estado.equalsIgnoreCase("ACTIVO") && !estado.equalsIgnoreCase("RENUNCIADO") && !estado.equalsIgnoreCase("DESPEDIDO"))) {

            System.out.println("Estado laboral no valido.");
            return;
        }

        dni = dni.trim();
        estado = estado.toUpperCase().trim();

        boolean actualizado = empleadoDAO.actualizarEstado(dni, estado);

        if (actualizado) {
            System.out.println("Estado laboral actualizado correctamente.");
            System.out.println("Nuevo estado: " + estado);
        } else {
            System.out.println("No se encontro el empleado o no se pudo actualizar.");
        }
    }
    public static boolean existeEmpleado(int id) {

        return empleadoDAO.existe(id);
    }
}


