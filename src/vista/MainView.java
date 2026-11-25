package vista;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

import controlador.BebidasOperaciones;
import controlador.ClienteOperaciones;
import controlador.ComprasOperaciones;
import controlador.DeliveryOperaciones;
import controlador.DetalleBebidaOperaciones;
import controlador.DetalleIngredienteExcluidoOperaciones;
import controlador.DetallePedidoOperacione;
import controlador.EmpleadosOperaciones;
import controlador.IngredientesOperaciones;
import controlador.PagoOperaciones;
import controlador.PedidosOperaciones;
import controlador.PlatoIngredienteOperaciones;
import controlador.PlatosOperaciones;
import controlador.ProveedorOperaciones;
import controlador.UsuarioOperaciones;
import controlador.VentaOperaciones;
import modelo.Delivery;
import modelo.Proveedor;
import modelo.SeguimientoDelivery;
import modelo.Usuario;

public class MainView {

    public static Scanner teclado = new Scanner(System.in);

    private final UsuarioOperaciones usuarioOperaciones = new UsuarioOperaciones();

    // ==================================================
    // MENU PRINCIPAL
    // ==================================================

    public void iniciar() {

        int opcion;

        do {
            System.out.println("================================");
            System.out.println("      SISTEMA DE CEVICHERIA");
            System.out.println("================================");
            System.out.println("--- CARTA E INSUMOS ---");
            System.out.println("1.  Gestionar Platos");
            System.out.println("2.  Gestionar Bebidas");
            System.out.println("3.  Gestionar Ingredientes");
            System.out.println("4.  Gestionar Proveedores");
            System.out.println("--- PERSONAS ---");
            System.out.println("5.  Gestionar Clientes");
            System.out.println("6.  Gestionar Empleados");
            System.out.println("7.  Gestionar Usuarios");
            System.out.println("--- PEDIDOS ---");
            System.out.println("8.  Gestionar Pedidos");
            System.out.println("9.  Gestionar Platos del Pedido");
            System.out.println("10. Gestionar Bebidas del Pedido");
            System.out.println("11. Gestionar Delivery");
            System.out.println("--- CAJA ---");
            System.out.println("12. Gestionar Compras");
            System.out.println("13. Gestionar Pagos");
            System.out.println("14. Gestionar Ventas");
            System.out.println("0.  Salir");
            System.out.print("Seleccione una opcion: ");

            opcion = leerEntero();

            switch (opcion) {
                case 1:
                    menuPlatos();
                    break;
                case 2:
                    menuBebidas();
                    break;
                case 3:
                    menuIngredientes();
                    break;
                case 4:
                    menuProveedores();
                    break;
                case 5:
                    menuClientes();
                    break;
                case 6:
                    menuEmpleados();
                    break;
                case 7:
                    menuUsuarios();
                    break;
                case 8:
                    menuPedidos();
                    break;
                case 9:
                    menuDetallesPedidos();
                    break;
                case 10:
                    menuBebidasPedido();
                    break;
                case 11:
                    menuDelivery();
                    break;
                case 12:
                    menuCompras();
                    break;
                case 13:
                    menuPagos();
                    break;
                case 14:
                    menuVentas();
                    break;
                case 0:
                    System.out.println("Saliendo del sistema.");
                    break;
                default:
                    System.out.println("Opcion no valida.");
            }

        } while (opcion != 0);
    }

    // ==================================================
    // 1. PLATOS
    // ==================================================

    private void menuPlatos() {

        int opcion;

        do {
            System.out.println("================================");
            System.out.println("         MENU DE PLATOS");
            System.out.println("================================");
            System.out.println("1. Registrar plato");
            System.out.println("2. Consultar platos");
            System.out.println("3. Buscar plato por ID");
            System.out.println("4. Modificar plato");
            System.out.println("5. Eliminar plato");
            System.out.println("6. Gestionar receta de un plato");
            System.out.println("0. Volver al menu principal");
            System.out.print("Seleccione una opcion: ");

            opcion = leerEntero();

            switch (opcion) {
                case 1:
                    registrarPlatoView();
                    break;
                case 2:
                    PlatosOperaciones.listarPlatos();
                    break;
                case 3:
                    buscarPlatoView();
                    break;
                case 4:
                    actualizarPlatoView();
                    break;
                case 5:
                    eliminarPlatoView();
                    break;
                case 6:
                    menuRecetaPlato();
                    break;
                case 0:
                    System.out.println("Volviendo al menu principal.");
                    break;
                default:
                    System.out.println("Opcion no valida.");
            }

        } while (opcion != 0);
    }

    private void menuRecetaPlato() {

        System.out.println("========== GESTIONAR RECETA ==========");

        PlatosOperaciones.listarPlatos();

        System.out.print("Ingrese el ID del plato: ");
        int idPlato = leerEntero();

        Boolean existe = PlatosOperaciones.existePlato(idPlato);

        if (existe == null) {
            return;
        }

        if (!existe) {
            System.out.println("No existe el plato.");
            return;
        }

        int opcion;

        do {
            System.out.println("================================");
            System.out.println("       GESTION DE RECETA");
            System.out.println("================================");
            System.out.println("1. Agregar ingrediente");
            System.out.println("2. Consultar receta");
            System.out.println("3. Modificar cantidad");
            System.out.println("4. Eliminar ingrediente");
            System.out.println("0. Volver");
            System.out.print("Seleccione una opcion: ");

            opcion = leerEntero();

            switch (opcion) {
                case 1:
                    agregarIngredienteReceta(idPlato);
                    break;
                case 2:
                    PlatoIngredienteOperaciones.listarIngredientesPlato(idPlato);
                    break;
                case 3:
                    modificarCantidadReceta(idPlato);
                    break;
                case 4:
                    eliminarIngredienteReceta(idPlato);
                    break;
                case 0:
                    System.out.println("Volviendo al menu de platos.");
                    break;
                default:
                    System.out.println("Opcion no valida.");
            }

        } while (opcion != 0);
    }

    private void registrarPlatoView() {

        System.out.print("Ingrese el nombre del plato: ");
        String nombre = teclado.nextLine();

        System.out.print("Ingrese el precio: ");
        double precio = leerDecimal();

        PlatosOperaciones.registrarPlato(nombre, precio);
    }

    private void buscarPlatoView() {

        System.out.print("Ingrese el ID del plato: ");
        int id = leerEntero();

        PlatosOperaciones.buscarPlato(id);
    }

    private void actualizarPlatoView() {

        int id;

        while (true) {

            System.out.print("Ingrese el ID del plato que desea actualizar: ");
            id = leerEntero();

            Boolean existe = PlatosOperaciones.existePlato(id);

            if (existe == null) {
                return;
            }

            if (existe) {
                break;
            }

            System.out.println("No existe el plato. Intente nuevamente.");
        }

        System.out.print("Ingrese el nuevo nombre: ");
        String nombre = teclado.nextLine();

        System.out.print("Ingrese el nuevo precio: ");
        double precio = leerDecimal();

        PlatosOperaciones.actualizarPlato(id, nombre, precio);
    }

    private void eliminarPlatoView() {

        System.out.print("Ingrese el ID del plato: ");
        int id = leerEntero();

        PlatosOperaciones.eliminarPlato(id);
    }

    private void agregarIngredienteReceta(int idPlato) {

        System.out.println("--- AGREGAR INGREDIENTE A LA RECETA ---");

        IngredientesOperaciones.listarIngredientes();

        System.out.print("Ingrese el ID del ingrediente: ");
        int idIngrediente = leerEntero();

        Boolean existe = IngredientesOperaciones.existeIngrediente(idIngrediente);

        if (existe == null) {
            return;
        }

        if (!existe) {
            System.out.println("No existe el ingrediente.");
            return;
        }

        System.out.print("Ingrese la cantidad necesaria para una porcion: ");
        double cantidad = leerDecimal();

        PlatoIngredienteOperaciones.registrarIngredientePlato(idPlato, idIngrediente, cantidad);
    }

    private void modificarCantidadReceta(int idPlato) {

        System.out.println("--- MODIFICAR CANTIDAD DE INGREDIENTE ---");

        PlatoIngredienteOperaciones.listarIngredientesPlato(idPlato);

        System.out.print("Ingrese el ID del ingrediente: ");
        int idIngrediente = leerEntero();

        System.out.print("Ingrese la nueva cantidad: ");
        double cantidad = leerDecimal();

        PlatoIngredienteOperaciones.actualizarCantidadIngrediente(idPlato, idIngrediente, cantidad);
    }

    private void eliminarIngredienteReceta(int idPlato) {

        System.out.println("--- ELIMINAR INGREDIENTE DE LA RECETA ---");

        PlatoIngredienteOperaciones.listarIngredientesPlato(idPlato);

        System.out.print("Ingrese el ID del ingrediente que desea eliminar: ");
        int idIngrediente = leerEntero();

        PlatoIngredienteOperaciones.eliminarIngredientePlato(idPlato, idIngrediente);
    }

    // ==================================================
    // 2. BEBIDAS
    // ==================================================

    private void menuBebidas() {

        int opcion;

        do {
            System.out.println("================================");
            System.out.println("        MENU DE BEBIDAS");
            System.out.println("================================");
            System.out.println("1. Registrar bebida");
            System.out.println("2. Consultar bebidas");
            System.out.println("3. Buscar bebida por ID");
            System.out.println("4. Modificar bebida");
            System.out.println("5. Eliminar bebida");
            System.out.println("0. Volver al menu principal");
            System.out.print("Seleccione una opcion: ");

            opcion = leerEntero();

            switch (opcion) {
                case 1:
                    registrarBebidaView();
                    break;
                case 2:
                    BebidasOperaciones.listarBebidas();
                    break;
                case 3:
                    buscarBebidaView();
                    break;
                case 4:
                    actualizarBebidaView();
                    break;
                case 5:
                    eliminarBebidaView();
                    break;
                case 0:
                    System.out.println("Volviendo al menu principal.");
                    break;
                default:
                    System.out.println("Opcion no valida.");
            }

        } while (opcion != 0);
    }

    private void registrarBebidaView() {

        System.out.println("--- REGISTRAR BEBIDA ---");

        System.out.print("Ingrese el nombre de la bebida: ");
        String nombre = teclado.nextLine();

        System.out.print("Ingrese el precio: ");
        double precio = leerDecimal();

        System.out.print("Ingrese el stock inicial (unidades): ");
        int stock = leerEntero();

        BebidasOperaciones.registrarBebida(nombre, precio, stock);
    }

    private void buscarBebidaView() {

        System.out.print("Ingrese el ID de la bebida: ");
        int id = leerEntero();

        BebidasOperaciones.buscarBebida(id);
    }

    private void actualizarBebidaView() {

        System.out.println("--- MODIFICAR BEBIDA ---");

        System.out.print("Ingrese el ID de la bebida: ");
        int id = leerEntero();

        Boolean existe = BebidasOperaciones.existeBebida(id);

        if (existe == null) {
            return;
        }

        if (!existe) {
            System.out.println("No existe la bebida.");
            return;
        }

        System.out.print("Ingrese el nuevo nombre: ");
        String nombre = teclado.nextLine();

        System.out.print("Ingrese el nuevo precio: ");
        double precio = leerDecimal();

        System.out.print("Ingrese el nuevo stock (unidades): ");
        int stock = leerEntero();

        BebidasOperaciones.actualizarBebida(id, nombre, precio, stock);
    }

    private void eliminarBebidaView() {

        System.out.print("Ingrese el ID de la bebida: ");
        int id = leerEntero();

        BebidasOperaciones.eliminarBebida(id);
    }

    // ==================================================
    // 3. INGREDIENTES
    // ==================================================

    private void menuIngredientes() {

        int opcion;

        do {
            System.out.println("================================");
            System.out.println("      MENU DE INGREDIENTES");
            System.out.println("================================");
            System.out.println("1. Registrar ingrediente");
            System.out.println("2. Consultar ingredientes");
            System.out.println("3. Buscar ingrediente por ID");
            System.out.println("4. Modificar ingrediente");
            System.out.println("5. Eliminar ingrediente");
            System.out.println("0. Volver al menu principal");
            System.out.print("Seleccione una opcion: ");

            opcion = leerEntero();

            switch (opcion) {
                case 1:
                    registrarIngredienteView();
                    break;
                case 2:
                    IngredientesOperaciones.listarIngredientes();
                    break;
                case 3:
                    buscarIngredienteView();
                    break;
                case 4:
                    actualizarIngredienteView();
                    break;
                case 5:
                    eliminarIngredienteView();
                    break;
                case 0:
                    System.out.println("Volviendo al menu principal.");
                    break;
                default:
                    System.out.println("Opcion no valida.");
            }

        } while (opcion != 0);
    }

    private void registrarIngredienteView() {

        System.out.println("--- REGISTRAR INGREDIENTE ---");

        System.out.print("Ingrese el nombre del ingrediente: ");
        String nombre = teclado.nextLine();

        System.out.print("Ingrese la unidad de medida: ");
        String unidad = teclado.nextLine();

        System.out.print("Ingrese el stock inicial: ");
        double stock = leerDecimal();

        IngredientesOperaciones.registrarIngrediente(nombre, unidad, stock);
    }

    private void buscarIngredienteView() {

        System.out.print("Ingrese el ID del ingrediente: ");
        int id = leerEntero();

        IngredientesOperaciones.buscarIngrediente(id);
    }

    private void actualizarIngredienteView() {

        int id;

        while (true) {

            System.out.print("Ingrese el ID del ingrediente que desea actualizar: ");
            id = leerEntero();

            Boolean existe = IngredientesOperaciones.existeIngrediente(id);

            if (existe == null) {
                return;
            }

            if (existe) {
                break;
            }

            System.out.println("No existe el ingrediente. Intente nuevamente.");
        }

        System.out.print("Ingrese el nuevo nombre: ");
        String nombre = teclado.nextLine();

        System.out.print("Ingrese la nueva unidad de medida: ");
        String unidad = teclado.nextLine();

        System.out.print("Ingrese el nuevo stock: ");
        double stock = leerDecimal();

        IngredientesOperaciones.actualizarIngrediente(id, nombre, unidad, stock);
    }

    private void eliminarIngredienteView() {

        System.out.print("Ingrese el ID del ingrediente: ");
        int id = leerEntero();

        IngredientesOperaciones.eliminarIngrediente(id);
    }

    // ==================================================
    // 4. PROVEEDORES
    // ==================================================

    private void menuProveedores() {

        int opcion;

        do {
            System.out.println("================================");
            System.out.println("      MENU DE PROVEEDORES");
            System.out.println("================================");
            System.out.println("1. Registrar proveedor");
            System.out.println("2. Consultar proveedores");
            System.out.println("3. Buscar proveedor por ID");
            System.out.println("4. Modificar proveedor");
            System.out.println("5. Eliminar proveedor");
            System.out.println("0. Volver al menu principal");
            System.out.print("Seleccione una opcion: ");

            opcion = leerEntero();

            switch (opcion) {
                case 1:
                    registrarProveedorView();
                    break;
                case 2:
                    listarProveedoresView();
                    break;
                case 3:
                    buscarProveedorView();
                    break;
                case 4:
                    actualizarProveedorView();
                    break;
                case 5:
                    eliminarProveedorView();
                    break;
                case 0:
                    System.out.println("Volviendo al menu principal.");
                    break;
                default:
                    System.out.println("Opcion no valida.");
            }

        } while (opcion != 0);
    }

    private void registrarProveedorView() {

        System.out.println("--- REGISTRAR PROVEEDOR ---");

        System.out.print("Ingrese el nombre: ");
        String nombre = teclado.nextLine();

        System.out.print("Ingrese el RUC (11 digitos, opcional): ");
        String ruc = teclado.nextLine();

        System.out.print("Ingrese el telefono (opcional): ");
        String telefono = teclado.nextLine();

        System.out.print("Ingrese la direccion (opcional): ");
        String direccion = teclado.nextLine();

        System.out.print("Ingrese los productos que provee (opcional): ");
        String productos = teclado.nextLine();

        ProveedorOperaciones.registrarProveedor(nombre, ruc, telefono, direccion, productos);
    }

    private void listarProveedoresView() {

        List<Proveedor> proveedores = ProveedorOperaciones.listarProveedores();

        if (proveedores.isEmpty()) {
            System.out.println("No hay proveedores registrados.");
            return;
        }

        for (Proveedor proveedor : proveedores) {
            System.out.println("-------------------------");
            proveedor.mostrarInformacion();
        }
    }

    private void buscarProveedorView() {

        System.out.print("Ingrese el ID del proveedor: ");
        int id = leerEntero();

        Proveedor proveedor = ProveedorOperaciones.buscarProveedor(id);

        if (proveedor == null) {
            System.out.println("No se encontro el proveedor.");
            return;
        }

        proveedor.mostrarInformacion();
    }

    private void actualizarProveedorView() {

        System.out.println("--- MODIFICAR PROVEEDOR ---");

        System.out.print("Ingrese el ID del proveedor: ");
        int id = leerEntero();

        Proveedor actual = ProveedorOperaciones.buscarProveedor(id);

        if (actual == null) {
            System.out.println("No se encontro el proveedor.");
            return;
        }

        System.out.print("Ingrese el nuevo nombre: ");
        String nombre = teclado.nextLine();

        System.out.print("Ingrese el nuevo RUC (11 digitos, opcional): ");
        String ruc = teclado.nextLine();

        System.out.print("Ingrese el nuevo telefono (opcional): ");
        String telefono = teclado.nextLine();

        System.out.print("Ingrese la nueva direccion (opcional): ");
        String direccion = teclado.nextLine();

        System.out.print("Ingrese los nuevos productos (opcional): ");
        String productos = teclado.nextLine();

        System.out.println("1. ACTIVO");
        System.out.println("2. INACTIVO");
        System.out.print("Seleccione el estado: ");
        int opcionEstado = leerEntero();

        String estado;

        switch (opcionEstado) {
            case 1:
                estado = Proveedor.ACTIVO;
                break;
            case 2:
                estado = Proveedor.INACTIVO;
                break;
            default:
                System.out.println("Opcion no valida.");
                return;
        }

        ProveedorOperaciones.actualizarProveedor(id, nombre, ruc, telefono, direccion, productos, estado);
    }

    private void eliminarProveedorView() {

        System.out.print("Ingrese el ID del proveedor: ");
        int id = leerEntero();

        ProveedorOperaciones.eliminarProveedor(id);
    }

    // ==================================================
    // 5. CLIENTES
    // ==================================================

    private void menuClientes() {

        int opcion;

        do {
            System.out.println("================================");
            System.out.println("        MENU DE CLIENTES");
            System.out.println("================================");
            System.out.println("1. Registrar cliente");
            System.out.println("2. Consultar clientes");
            System.out.println("3. Buscar cliente por ID");
            System.out.println("4. Modificar cliente");
            System.out.println("5. Eliminar cliente");
            System.out.println("0. Volver al menu principal");
            System.out.print("Seleccione una opcion: ");

            opcion = leerEntero();

            switch (opcion) {
                case 1:
                    registrarClienteView();
                    break;
                case 2:
                    ClienteOperaciones.listarClientes();
                    break;
                case 3:
                    buscarClienteView();
                    break;
                case 4:
                    actualizarClienteView();
                    break;
                case 5:
                    eliminarClienteView();
                    break;
                case 0:
                    System.out.println("Volviendo al menu principal.");
                    break;
                default:
                    System.out.println("Opcion no valida.");
            }

        } while (opcion != 0);
    }

    private void registrarClienteView() {

        System.out.println("--- REGISTRAR CLIENTE ---");

        System.out.print("Ingrese los nombres del cliente: ");
        String nombres = teclado.nextLine();

        System.out.print("Ingrese los apellidos del cliente: ");
        String apellidos = teclado.nextLine();

        System.out.print("Ingrese el telefono del cliente: ");
        String telefono = teclado.nextLine();

        System.out.print("Ingrese el correo (opcional): ");
        String correo = teclado.nextLine();

        System.out.print("Ingrese la direccion (opcional, para delivery): ");
        String direccion = teclado.nextLine();

        System.out.print("Ingrese la referencia (opcional): ");
        String referencia = teclado.nextLine();

        System.out.print("Ingrese el distrito (opcional): ");
        String distrito = teclado.nextLine();

        ClienteOperaciones.registrarCliente(nombres, apellidos, telefono, correo, direccion, referencia, distrito);
    }

    private void buscarClienteView() {

        System.out.print("Ingrese el ID del cliente: ");
        int id = leerEntero();

        ClienteOperaciones.buscarCliente(id);
    }

    private void actualizarClienteView() {

        System.out.println("--- ACTUALIZAR CLIENTE ---");

        System.out.print("Ingrese el ID del cliente: ");
        int id = leerEntero();

        if (!ClienteOperaciones.existeCliente(id)) {
            System.out.println("No se encontro el cliente.");
            return;
        }

        System.out.print("Ingrese los nuevos nombres: ");
        String nombres = teclado.nextLine();

        System.out.print("Ingrese los nuevos apellidos: ");
        String apellidos = teclado.nextLine();

        System.out.print("Ingrese el nuevo telefono: ");
        String telefono = teclado.nextLine();

        System.out.print("Ingrese el nuevo correo (opcional): ");
        String correo = teclado.nextLine();

        System.out.print("Ingrese la nueva direccion (opcional): ");
        String direccion = teclado.nextLine();

        System.out.print("Ingrese la nueva referencia (opcional): ");
        String referencia = teclado.nextLine();

        System.out.print("Ingrese el nuevo distrito (opcional): ");
        String distrito = teclado.nextLine();

        ClienteOperaciones.actualizarCliente(id, nombres, apellidos, telefono, correo, direccion, referencia, distrito);
    }

    private void eliminarClienteView() {

        System.out.print("Ingrese el ID del cliente: ");
        int id = leerEntero();

        ClienteOperaciones.eliminarCliente(id);
    }

    // ==================================================
    // 6. EMPLEADOS
    // ==================================================

    private void menuEmpleados() {

        int opcion;

        do {
            System.out.println("================================");
            System.out.println("        MENU DE EMPLEADOS");
            System.out.println("================================");
            System.out.println("1. Registrar empleado");
            System.out.println("2. Listar empleados");
            System.out.println("3. Buscar empleado por ID");
            System.out.println("4. Modificar empleado");
            System.out.println("5. Actualizar estado laboral");
            System.out.println("6. Eliminar empleado");
            System.out.println("0. Volver al menu principal");
            System.out.print("Seleccione una opcion: ");

            opcion = leerEntero();

            switch (opcion) {
                case 1:
                    registrarEmpleadoView();
                    break;
                case 2:
                    EmpleadosOperaciones.listarEmpleados();
                    break;
                case 3:
                    buscarEmpleadoView();
                    break;
                case 4:
                    actualizarEmpleadoView();
                    break;
                case 5:
                    actualizarEstadoEmpleadoView();
                    break;
                case 6:
                    eliminarEmpleadoView();
                    break;
                case 0:
                    System.out.println("Regresando al menu principal.");
                    break;
                default:
                    System.out.println("Opcion no valida.");
            }

        } while (opcion != 0);
    }

    private void registrarEmpleadoView() {

        System.out.println("========== REGISTRAR EMPLEADO ==========");

        System.out.print("Ingrese los nombres: ");
        String nombres = teclado.nextLine();

        System.out.print("Ingrese los apellidos: ");
        String apellidos = teclado.nextLine();

        System.out.print("Ingrese el telefono: ");
        String telefono = teclado.nextLine();

        System.out.print("Ingrese el cargo (Mesero/Cocinero/Administrador): ");
        String cargo = teclado.nextLine();

        System.out.print("Ingrese la direccion: ");
        String direccion = teclado.nextLine();

        System.out.print("Ingrese el DNI: ");
        String dni = teclado.nextLine();

        System.out.print("Ingrese el contacto de emergencia: ");
        String contactoEmergencia = teclado.nextLine();

        System.out.print("Ingrese el correo: ");
        String correo = teclado.nextLine();

        System.out.print("Ingrese el distrito: ");
        String distrito = teclado.nextLine();

        System.out.print("Ingrese la provincia: ");
        String provincia = teclado.nextLine();

        System.out.print("Ingrese el departamento: ");
        String departamento = teclado.nextLine();

        System.out.print("Ingrese el codigo postal: ");
        String codigoPostal = teclado.nextLine();

        System.out.print("Ingrese la referencia de direccion: ");
        String referenciaDireccion = teclado.nextLine();

        System.out.print("Ingrese el salario mensual: ");
        double salario = leerDecimal();

        String turnoLaboral = leerTurno();

        if (turnoLaboral == null) {
            return;
        }

        LocalDate fechaContratacion = leerFecha("Ingrese la fecha de contratacion (AAAA-MM-DD): ");

        EmpleadosOperaciones.registrarEmpleado(nombres, apellidos, telefono, cargo, direccion,
                dni, contactoEmergencia, correo, distrito, provincia, departamento, codigoPostal,
                referenciaDireccion, salario, turnoLaboral, fechaContratacion
        );
    }

    private void buscarEmpleadoView() {

        System.out.println("========== BUSCAR EMPLEADO ==========");

        System.out.print("Ingrese el ID del empleado: ");
        int id = leerEntero();

        EmpleadosOperaciones.buscarEmpleadoPorId(id);
    }

    private void actualizarEmpleadoView() {

        System.out.println("========== MODIFICAR EMPLEADO ==========");

        System.out.print("Ingrese el ID del empleado: ");
        int id = leerEntero();

        if (!EmpleadosOperaciones.existeEmpleado(id)) {
            System.out.println("No se encontro el empleado.");
            return;
        }

        System.out.print("Ingrese los nuevos nombres: ");
        String nombres = teclado.nextLine();

        System.out.print("Ingrese los nuevos apellidos: ");
        String apellidos = teclado.nextLine();

        System.out.print("Ingrese el nuevo cargo (Mesero/Cocinero/Administrador): ");
        String cargo = teclado.nextLine();

        if (!cargo.equalsIgnoreCase("MESERO") && !cargo.equalsIgnoreCase("COCINERO")
                && !cargo.equalsIgnoreCase("ADMINISTRADOR")) {
            System.out.println("Cargo no valido.");
            return;
        }
        cargo = cargo.toUpperCase();

        System.out.print("Ingrese el nuevo telefono: ");
        String telefono = teclado.nextLine();

        System.out.print("Ingrese la nueva direccion: ");
        String direccion = teclado.nextLine();

        System.out.print("Ingrese el nuevo DNI: ");
        String dni = teclado.nextLine();

        System.out.print("Ingrese el nuevo contacto de emergencia: ");
        String contactoEmergencia = teclado.nextLine();

        System.out.print("Ingrese el nuevo correo: ");
        String correo = teclado.nextLine();

        System.out.print("Ingrese el nuevo distrito: ");
        String distrito = teclado.nextLine();

        System.out.print("Ingrese la nueva provincia: ");
        String provincia = teclado.nextLine();

        System.out.print("Ingrese el nuevo departamento: ");
        String departamento = teclado.nextLine();

        System.out.print("Ingrese el nuevo codigo postal: ");
        String codigoPostal = teclado.nextLine();

        System.out.print("Ingrese la nueva referencia de direccion: ");
        String referenciaDireccion = teclado.nextLine();

        System.out.print("Ingrese el nuevo salario: ");
        double salario = leerDecimal();

        if (salario < 0) {
            System.out.println("El salario no puede ser negativo.");
            return;
        }

        String turnoLaboral = leerTurno();

        if (turnoLaboral == null) {
            return;
        }

        LocalDate fechaContratacion = leerFecha("Ingrese la nueva fecha de contratacion (AAAA-MM-DD): ");

        EmpleadosOperaciones.actualizarEmpleado(id, nombres,
                apellidos, cargo, telefono, direccion, dni,
                contactoEmergencia, correo, distrito, provincia, departamento,
                codigoPostal, referenciaDireccion, salario, turnoLaboral, fechaContratacion);
    }

    private void actualizarEstadoEmpleadoView() {

        System.out.print("Ingrese el DNI del empleado: ");
        String dni = teclado.nextLine();

        System.out.println("1. ACTIVO");
        System.out.println("2. RENUNCIADO");
        System.out.println("3. DESPEDIDO");

        System.out.print("Seleccione el nuevo estado: ");
        int opcion = leerEntero();

        String estado;

        switch (opcion) {
            case 1:
                estado = "ACTIVO";
                break;
            case 2:
                estado = "RENUNCIADO";
                break;
            case 3:
                estado = "DESPEDIDO";
                break;
            default:
                System.out.println("Opcion no valida.");
                return;
        }

        EmpleadosOperaciones.actualizarEstado(dni, estado);
    }

    private void eliminarEmpleadoView() {

        System.out.println("========== ELIMINAR EMPLEADO ==========");

        System.out.print("Ingrese el ID del empleado: ");
        int id = leerEntero();

        EmpleadosOperaciones.eliminarEmpleado(id);
    }

    // Devuelve el turno elegido, o null si la opcion no es valida.
    private String leerTurno() {

        System.out.println("Seleccione el turno laboral:");
        System.out.println("1. Mañana");
        System.out.println("2. Tarde");
        System.out.println("3. Noche");
        System.out.print("Seleccione una opcion: ");

        int opcionTurno = leerEntero();

        switch (opcionTurno) {
            case 1:
                return "MAÑANA";
            case 2:
                return "TARDE";
            case 3:
                return "NOCHE";
            default:
                System.out.println("Turno no valido.");
                return null;
        }
    }

    // ==================================================
    // 7. USUARIOS
    // ==================================================

    private void menuUsuarios() {

        int opcion;

        do {
            System.out.println("================================");
            System.out.println("        MENU DE USUARIOS");
            System.out.println("================================");
            System.out.println("1. Registrar usuario");
            System.out.println("2. Listar usuarios");
            System.out.println("3. Buscar usuario por ID");
            System.out.println("4. Cambiar contraseña");
            System.out.println("5. Cambiar estado (ACTIVO/INACTIVO)");
            System.out.println("6. Eliminar usuario");
            System.out.println("0. Volver al menu principal");
            System.out.print("Seleccione una opcion: ");

            opcion = leerEntero();

            switch (opcion) {
                case 1:
                    registrarUsuarioView();
                    break;
                case 2:
                    listarUsuariosView();
                    break;
                case 3:
                    buscarUsuarioView();
                    break;
                case 4:
                    cambiarContrasenaView();
                    break;
                case 5:
                    cambiarEstadoUsuarioView();
                    break;
                case 6:
                    eliminarUsuarioView();
                    break;
                case 0:
                    System.out.println("Volviendo al menu principal.");
                    break;
                default:
                    System.out.println("Opcion no valida.");
            }

        } while (opcion != 0);
    }

    private void registrarUsuarioView() {

        System.out.println("--- REGISTRAR USUARIO ---");

        EmpleadosOperaciones.listarEmpleados();

        System.out.print("Ingrese el ID del empleado: ");
        int idEmpleado = leerEntero();

        if (!EmpleadosOperaciones.existeEmpleado(idEmpleado)) {
            System.out.println("No se encontro el empleado.");
            return;
        }

        System.out.print("Ingrese el nombre de usuario: ");
        String nombreUsuario = teclado.nextLine();

        if (nombreUsuario.trim().isEmpty()) {
            System.out.println("El nombre de usuario es obligatorio.");
            return;
        }

        if (!usuarioOperaciones.nombreDisponible(nombreUsuario)) {
            System.out.println("Ese nombre de usuario ya esta en uso.");
            return;
        }

        System.out.print("Ingrese la contraseña: ");
        String contrasena = teclado.nextLine();

        int id = usuarioOperaciones.registrarUsuario(idEmpleado, nombreUsuario, contrasena);

        if (id > 0) {
            System.out.println("Usuario registrado correctamente. ID generado: " + id);
        } else {
            System.out.println("No se pudo registrar el usuario.");
        }
    }

    private void listarUsuariosView() {

        List<Usuario> usuarios = usuarioOperaciones.listarUsuarios();

        if (usuarios.isEmpty()) {
            System.out.println("No hay usuarios registrados.");
            return;
        }

        for (Usuario u : usuarios) {
            mostrarUsuario(u);
        }
    }

    private void buscarUsuarioView() {

        System.out.print("Ingrese el ID del usuario: ");
        int id = leerEntero();

        Usuario usuario = usuarioOperaciones.buscarUsuario(id);

        if (usuario == null) {
            System.out.println("No se encontro el usuario.");
            return;
        }

        mostrarUsuario(usuario);
    }

    private void mostrarUsuario(Usuario u) {
        System.out.println("-------------------------");
        System.out.println("ID usuario: " + u.getIdUsuario());
        System.out.println("Usuario: " + u.getNombreUsuario());
        System.out.println("Empleado: " + u.getNombreEmpleado() + " (ID " + u.getIdEmpleado() + ")");
        System.out.println("Cargo: " + u.getCargo());
        System.out.println("Estado: " + u.getEstado());
    }

    private void cambiarContrasenaView() {

        System.out.print("Ingrese el ID del usuario: ");
        int id = leerEntero();

        if (usuarioOperaciones.buscarUsuario(id) == null) {
            System.out.println("No se encontro el usuario.");
            return;
        }

        System.out.print("Ingrese la nueva contraseña: ");
        String contrasena = teclado.nextLine();

        if (usuarioOperaciones.cambiarContrasena(id, contrasena)) {
            System.out.println("Contraseña actualizada correctamente.");
        } else {
            System.out.println("No se pudo cambiar la contraseña.");
        }
    }

    private void cambiarEstadoUsuarioView() {

        System.out.print("Ingrese el ID del usuario: ");
        int id = leerEntero();

        if (usuarioOperaciones.buscarUsuario(id) == null) {
            System.out.println("No se encontro el usuario.");
            return;
        }

        System.out.println("1. ACTIVO");
        System.out.println("2. INACTIVO");
        System.out.print("Seleccione el nuevo estado: ");
        int opcion = leerEntero();

        String estado;

        switch (opcion) {
            case 1:
                estado = "ACTIVO";
                break;
            case 2:
                estado = "INACTIVO";
                break;
            default:
                System.out.println("Opcion no valida.");
                return;
        }

        if (usuarioOperaciones.cambiarEstado(id, estado)) {
            System.out.println("Estado actualizado a " + estado + ".");
        } else {
            System.out.println("No se pudo cambiar el estado.");
        }
    }

    private void eliminarUsuarioView() {

        System.out.print("Ingrese el ID del usuario: ");
        int id = leerEntero();

        if (usuarioOperaciones.buscarUsuario(id) == null) {
            System.out.println("No se encontro el usuario.");
            return;
        }

        if (usuarioOperaciones.eliminarUsuario(id)) {
            System.out.println("Usuario eliminado correctamente.");
        } else {
            System.out.println("No se pudo eliminar el usuario.");
        }
    }

    // ==================================================
    // 8. PEDIDOS
    // ==================================================

    private void menuPedidos() {

        int opcion;

        do {
            System.out.println("================================");
            System.out.println("         MENU DE PEDIDOS");
            System.out.println("================================");
            System.out.println("1. Registrar pedido completo");
            System.out.println("2. Consultar pedidos");
            System.out.println("3. Buscar pedido por ID");
            System.out.println("4. Actualizar estado del pedido");
            System.out.println("5. Eliminar pedido");
            System.out.println("0. Volver al menu principal");
            System.out.print("Seleccione una opcion: ");

            opcion = leerEntero();

            switch (opcion) {
                case 1:
                    registrarPedidoView();
                    break;
                case 2:
                    PedidosOperaciones.listarPedidos();
                    break;
                case 3:
                    buscarPedidoView();
                    break;
                case 4:
                    actualizarEstadoPedidoView();
                    break;
                case 5:
                    eliminarPedidoView();
                    break;
                case 0:
                    System.out.println("Volviendo al menu principal.");
                    break;
                default:
                    System.out.println("Opcion no valida.");
            }

        } while (opcion != 0);
    }

    private void registrarPedidoView() {

        System.out.println("=== REGISTRAR PEDIDO ===");

        System.out.print("Ingrese el ID del cliente: ");
        int idCliente = leerEntero();

        System.out.print("Ingrese el ID del mesero: ");
        int idMesero = leerEntero();

        int idPedido = PedidosOperaciones.registrarPedido(idCliente, idMesero);

        if (idPedido <= 0) {
            return;
        }

        // Platos del pedido
        String continuar;

        do {
            System.out.println("=== AGREGAR PLATO AL PEDIDO ===");

            registrarDetalleView(idPedido);

            System.out.print("¿Desea agregar otro plato? (S/N): ");
            continuar = teclado.nextLine();

        } while (continuar.equalsIgnoreCase("S"));

        // Bebidas del pedido (opcional)
        System.out.print("¿Desea agregar bebidas al pedido? (S/N): ");

        if (teclado.nextLine().equalsIgnoreCase("S")) {

            do {
                System.out.println("=== AGREGAR BEBIDA AL PEDIDO ===");

                registrarDetalleBebidaView(idPedido);

                System.out.print("¿Desea agregar otra bebida? (S/N): ");
                continuar = teclado.nextLine();

            } while (continuar.equalsIgnoreCase("S"));
        }

        // Delivery (opcional)
        System.out.print("¿El pedido es para delivery? (S/N): ");

        if (teclado.nextLine().equalsIgnoreCase("S")) {
            registrarDeliveryView(idPedido);
        }

        // Resumen
        System.out.println("================================");
        System.out.println("       PEDIDO COMPLETADO");
        System.out.println("================================");
        System.out.println("ID del pedido: " + idPedido);
        System.out.println("RESUMEN DEL PEDIDO:");

        System.out.println("--- Platos ---");
        DetallePedidoOperacione.listarDetalles(idPedido);

        System.out.println("--- Bebidas ---");
        DetalleBebidaOperaciones.listarDetallesBebida(idPedido);

        mostrarTotales(idPedido);
    }

    private void mostrarTotales(int idPedido) {

        double platos = DetallePedidoOperacione.calcularTotalPedido(idPedido);
        double bebidas = DetalleBebidaOperaciones.calcularTotalBebidas(idPedido);

        if (platos >= 0 && bebidas >= 0) {
            System.out.println("Total de bebidas: S/ " + bebidas);
            System.out.println("TOTAL (platos + bebidas): S/ " + (platos + bebidas));
        }
    }

    private void buscarPedidoView() {

        System.out.print("Ingrese el ID del pedido: ");
        int id = leerEntero();

        PedidosOperaciones.buscarPedido(id);
    }

    private void actualizarEstadoPedidoView() {

        System.out.print("Ingrese el ID del pedido: ");
        int id = leerEntero();

        System.out.println("1. PENDIENTE");
        System.out.println("2. EN PREPARACION");
        System.out.println("3. LISTO");
        System.out.println("4. ENTREGADO");
        System.out.println("5. CANCELADO");

        System.out.print("Seleccione el nuevo estado: ");
        int opcion = leerEntero();

        String estado;

        switch (opcion) {
            case 1:
                estado = "PENDIENTE";
                break;
            case 2:
                estado = "EN PREPARACION";
                break;
            case 3:
                estado = "LISTO";
                break;
            case 4:
                estado = "ENTREGADO";
                break;
            case 5:
                estado = "CANCELADO";
                break;
            default:
                System.out.println("Opcion no valida.");
                return;
        }

        PedidosOperaciones.actualizarEstadoPedido(id, estado);
    }

    private void eliminarPedidoView() {

        System.out.print("Ingrese el ID del pedido: ");
        int id = leerEntero();

        PedidosOperaciones.eliminarPedido(id);
    }

    // ==================================================
    // 9. PLATOS DEL PEDIDO (DETALLE_PEDIDO)
    // ==================================================

    private void menuDetallesPedidos() {

        int opcion;

        do {
            System.out.println("================================");
            System.out.println("    MENU DE PLATOS DEL PEDIDO");
            System.out.println("================================");
            System.out.println("1. Registrar detalle");
            System.out.println("2. Listar detalles de un pedido");
            System.out.println("3. Buscar detalle");
            System.out.println("4. Actualizar detalle");
            System.out.println("5. Eliminar detalle");
            System.out.println("6. Calcular total de platos del pedido");
            System.out.println("7. Gestionar ingredientes excluidos de un detalle");
            System.out.println("0. Volver al menu principal");
            System.out.print("Seleccione una opcion: ");

            opcion = leerEntero();

            switch (opcion) {
                case 1:
                    registrarDetalleView();
                    break;
                case 2:
                    listarDetallesView();
                    break;
                case 3:
                    buscarDetalleView();
                    break;
                case 4:
                    actualizarDetalleView();
                    break;
                case 5:
                    eliminarDetalleView();
                    break;
                case 6:
                    calcularTotalPedidoView();
                    break;
                case 7:
                    menuExclusiones();
                    break;
                case 0:
                    System.out.println("Volviendo al menu principal.");
                    break;
                default:
                    System.out.println("Opcion no valida.");
            }

        } while (opcion != 0);
    }

    private void registrarDetalleView() {

        System.out.print("Ingrese el ID del pedido: ");
        int idPedido = leerEntero();

        registrarDetalleView(idPedido);
    }

    private void registrarDetalleView(int idPedido) {

        System.out.print("Ingrese el ID del plato: ");
        int idPlato = leerEntero();

        System.out.print("Ingrese la cantidad: ");
        int cantidad = leerEntero();

        System.out.print("Ingrese la observacion: ");
        String observacion = teclado.nextLine();

        int idDetalle = DetallePedidoOperacione.registrarDetalle(idPedido, idPlato, cantidad, observacion);

        if (idDetalle > 0) {

            System.out.print("¿Desea excluir algun ingrediente? (S/N): ");
            String respuesta = teclado.nextLine();

            if (respuesta.equalsIgnoreCase("S")) {

                System.out.println("=== INGREDIENTES DE LA RECETA ===");

                PlatoIngredienteOperaciones.listarIngredientesPlato(idPlato);

                String continuar;

                do {

                    System.out.print("Ingrese el ID del ingrediente que desea excluir: ");
                    int idIngrediente = leerEntero();

                    DetalleIngredienteExcluidoOperaciones.registrarExclusion(idDetalle, idIngrediente);

                    System.out.print("¿Desea excluir otro ingrediente? (S/N): ");
                    continuar = teclado.nextLine();

                } while (continuar.equalsIgnoreCase("S"));
            }
        }
    }

    private void listarDetallesView() {

        System.out.print("Ingrese el ID del pedido: ");
        int idPedido = leerEntero();

        DetallePedidoOperacione.listarDetalles(idPedido);
    }

    private void buscarDetalleView() {

        System.out.print("Ingrese el ID del detalle: ");
        int idDetalle = leerEntero();

        DetallePedidoOperacione.buscarDetalle(idDetalle);
    }

    private void actualizarDetalleView() {

        System.out.print("Ingrese el ID del detalle: ");
        int idDetalle = leerEntero();

        System.out.print("Ingrese la nueva cantidad: ");
        int cantidad = leerEntero();

        System.out.print("Ingrese la nueva observacion: ");
        String observacion = teclado.nextLine();

        DetallePedidoOperacione.actualizarDetalle(idDetalle, cantidad, observacion);
    }

    private void eliminarDetalleView() {

        System.out.print("Ingrese el ID del detalle: ");
        int idDetalle = leerEntero();

        DetallePedidoOperacione.eliminarDetalle(idDetalle);
    }

    private void calcularTotalPedidoView() {

        System.out.print("Ingrese el ID del pedido: ");
        int idPedido = leerEntero();

        DetallePedidoOperacione.calcularTotalPedido(idPedido);
    }

    private void menuExclusiones() {

        System.out.print("Ingrese el ID del detalle: ");
        int idDetalle = leerEntero();

        int opcion;

        do {
            System.out.println("================================");
            System.out.println("   INGREDIENTES EXCLUIDOS");
            System.out.println("================================");
            System.out.println("1. Excluir ingrediente");
            System.out.println("2. Listar exclusiones");
            System.out.println("3. Quitar exclusion");
            System.out.println("0. Volver");
            System.out.print("Seleccione una opcion: ");

            opcion = leerEntero();

            switch (opcion) {
                case 1:
                    System.out.print("Ingrese el ID del ingrediente: ");
                    DetalleIngredienteExcluidoOperaciones.registrarExclusion(idDetalle, leerEntero());
                    break;
                case 2:
                    DetalleIngredienteExcluidoOperaciones.listarExclusiones(idDetalle);
                    break;
                case 3:
                    System.out.print("Ingrese el ID del ingrediente: ");
                    DetalleIngredienteExcluidoOperaciones.eliminarExclusion(idDetalle, leerEntero());
                    break;
                case 0:
                    System.out.println("Volviendo.");
                    break;
                default:
                    System.out.println("Opcion no valida.");
            }

        } while (opcion != 0);
    }

    // ==================================================
    // 10. BEBIDAS DEL PEDIDO (DETALLE_BEBIDA)
    // ==================================================

    private void menuBebidasPedido() {

        int opcion;

        do {
            System.out.println("================================");
            System.out.println("   MENU DE BEBIDAS DEL PEDIDO");
            System.out.println("================================");
            System.out.println("1. Agregar bebida a un pedido");
            System.out.println("2. Listar bebidas de un pedido");
            System.out.println("3. Modificar cantidad");
            System.out.println("4. Quitar bebida del pedido");
            System.out.println("5. Calcular total de bebidas del pedido");
            System.out.println("0. Volver al menu principal");
            System.out.print("Seleccione una opcion: ");

            opcion = leerEntero();

            switch (opcion) {
                case 1:
                    System.out.print("Ingrese el ID del pedido: ");
                    registrarDetalleBebidaView(leerEntero());
                    break;
                case 2:
                    System.out.print("Ingrese el ID del pedido: ");
                    DetalleBebidaOperaciones.listarDetallesBebida(leerEntero());
                    break;
                case 3:
                    actualizarDetalleBebidaView();
                    break;
                case 4:
                    System.out.print("Ingrese el ID del detalle de bebida: ");
                    DetalleBebidaOperaciones.eliminarDetalleBebida(leerEntero());
                    break;
                case 5:
                    System.out.print("Ingrese el ID del pedido: ");
                    double total = DetalleBebidaOperaciones.calcularTotalBebidas(leerEntero());
                    System.out.println("Total de bebidas: S/ " + total);
                    break;
                case 0:
                    System.out.println("Volviendo al menu principal.");
                    break;
                default:
                    System.out.println("Opcion no valida.");
            }

        } while (opcion != 0);
    }

    private void registrarDetalleBebidaView(int idPedido) {

        BebidasOperaciones.listarBebidas();

        System.out.print("Ingrese el ID de la bebida: ");
        int idBebida = leerEntero();

        System.out.print("Ingrese la cantidad: ");
        int cantidad = leerEntero();

        DetalleBebidaOperaciones.registrarDetalleBebida(idPedido, idBebida, cantidad);
    }

    private void actualizarDetalleBebidaView() {

        System.out.print("Ingrese el ID del detalle de bebida: ");
        int idDetalleBebida = leerEntero();

        System.out.print("Ingrese la nueva cantidad: ");
        int cantidad = leerEntero();

        DetalleBebidaOperaciones.actualizarCantidad(idDetalleBebida, cantidad);
    }

    // ==================================================
    // 11. DELIVERY
    // ==================================================

    private void menuDelivery() {

        int opcion;

        do {
            System.out.println("================================");
            System.out.println("        MENU DE DELIVERY");
            System.out.println("================================");
            System.out.println("1. Registrar delivery");
            System.out.println("2. Listar deliveries");
            System.out.println("3. Buscar delivery por ID");
            System.out.println("4. Buscar delivery por pedido");
            System.out.println("5. Actualizar estado del delivery");
            System.out.println("6. Ver seguimiento de un delivery");
            System.out.println("0. Volver al menu principal");
            System.out.print("Seleccione una opcion: ");

            opcion = leerEntero();

            switch (opcion) {
                case 1:
                    System.out.print("Ingrese el ID del pedido: ");
                    registrarDeliveryView(leerEntero());
                    break;
                case 2:
                    listarDeliveriesView();
                    break;
                case 3:
                    buscarDeliveryView();
                    break;
                case 4:
                    buscarDeliveryPorPedidoView();
                    break;
                case 5:
                    actualizarEstadoDeliveryView();
                    break;
                case 6:
                    verSeguimientoView();
                    break;
                case 0:
                    System.out.println("Volviendo al menu principal.");
                    break;
                default:
                    System.out.println("Opcion no valida.");
            }

        } while (opcion != 0);
    }

    private void registrarDeliveryView(int idPedido) {

        System.out.println("--- REGISTRAR DELIVERY (pedido N.° " + idPedido + ") ---");

        System.out.println("Plataformas: inDrive, Rappi, Otro");
        System.out.print("Ingrese la plataforma: ");
        String plataforma = teclado.nextLine();

        System.out.print("Ingrese el codigo externo (opcional): ");
        String codigoExterno = teclado.nextLine();

        System.out.print("Ingrese la direccion de entrega: ");
        String direccion = teclado.nextLine();

        System.out.print("Ingrese la referencia de entrega (opcional): ");
        String referencia = teclado.nextLine();

        System.out.print("Ingrese el costo del delivery: ");
        double costo = leerDecimal();

        DeliveryOperaciones.registrarDelivery(idPedido, plataforma, codigoExterno,
                direccion, referencia, costo);
    }

    private void listarDeliveriesView() {

        List<Delivery> deliveries = DeliveryOperaciones.listarDeliveries();

        if (deliveries.isEmpty()) {
            System.out.println("No hay deliveries registrados.");
            return;
        }

        for (Delivery d : deliveries) {
            System.out.println("-------------------------");
            d.mostrarInformacion();
        }
    }

    private void buscarDeliveryView() {

        System.out.print("Ingrese el ID del delivery: ");
        int id = leerEntero();

        Delivery delivery = DeliveryOperaciones.buscarPorId(id);

        if (delivery == null) {
            System.out.println("No se encontro el delivery.");
            return;
        }

        delivery.mostrarInformacion();
    }

    private void buscarDeliveryPorPedidoView() {

        System.out.print("Ingrese el ID del pedido: ");
        int idPedido = leerEntero();

        Delivery delivery = DeliveryOperaciones.buscarPorPedido(idPedido);

        if (delivery == null) {
            System.out.println("El pedido no tiene delivery.");
            return;
        }

        delivery.mostrarInformacion();
    }

    private void actualizarEstadoDeliveryView() {

        System.out.print("Ingrese el ID del delivery: ");
        int id = leerEntero();

        Delivery delivery = DeliveryOperaciones.buscarPorId(id);

        if (delivery == null) {
            System.out.println("No se encontro el delivery.");
            return;
        }

        String[] siguientes = DeliveryOperaciones.estadosSiguientes(delivery.getEstado());

        if (siguientes.length == 0) {
            System.out.println("El delivery ya esta " + delivery.getEstado() + " y no admite mas cambios.");
            return;
        }

        System.out.println("Estado actual: " + delivery.getEstado());

        for (int i = 0; i < siguientes.length; i++) {
            System.out.println((i + 1) + ". " + siguientes[i]);
        }

        System.out.print("Seleccione el nuevo estado: ");
        int opcion = leerEntero();

        if (opcion < 1 || opcion > siguientes.length) {
            System.out.println("Opcion no valida.");
            return;
        }

        System.out.print("Ingrese una descripcion (opcional): ");
        String descripcion = teclado.nextLine();

        DeliveryOperaciones.actualizarEstado(id, siguientes[opcion - 1], descripcion);
    }

    private void verSeguimientoView() {

        System.out.print("Ingrese el ID del delivery: ");
        int id = leerEntero();

        List<SeguimientoDelivery> seguimiento = DeliveryOperaciones.listarSeguimiento(id);

        if (seguimiento.isEmpty()) {
            System.out.println("El delivery no tiene seguimiento registrado.");
            return;
        }

        for (SeguimientoDelivery s : seguimiento) {
            System.out.println("-------------------------");
            System.out.println(s);
        }
    }

    // ==================================================
    // 12. COMPRAS
    // ==================================================

    public void menuCompras() {

        int opcion;

        do {
            System.out.println("================================");
            System.out.println("       GESTION DE COMPRAS");
            System.out.println("================================");
            System.out.println("1. Registrar compra");
            System.out.println("2. Listar compras");
            System.out.println("3. Buscar compra");
            System.out.println("0. Volver al menu principal");
            System.out.print("Seleccione una opcion: ");

            opcion = leerEntero();

            switch (opcion) {
                case 1:
                    registrarCompraView();
                    break;
                case 2:
                    ComprasOperaciones.listarCompras();
                    break;
                case 3:
                    buscarCompraView();
                    break;
                case 0:
                    System.out.println("Regresando al menu principal...");
                    break;
                default:
                    System.out.println("Opcion no valida.");
            }

        } while (opcion != 0);
    }

    private void registrarCompraView() {

        System.out.println("================================");
        System.out.println("       REGISTRAR COMPRA");
        System.out.println("================================");

        Integer idProveedor = null;

        System.out.print("¿Desea indicar el proveedor? (S/N): ");

        if (teclado.nextLine().equalsIgnoreCase("S")) {

            listarProveedoresView();

            System.out.print("Ingrese el ID del proveedor: ");
            idProveedor = leerEntero();
        }

        String continuar;

        do {
            IngredientesOperaciones.listarIngredientes();

            System.out.print("Ingrese el ID del ingrediente: ");
            int idIngrediente = leerEntero();

            System.out.print("Ingrese la cantidad comprada: ");
            double cantidad = leerDecimal();

            System.out.print("Ingrese el precio unitario: ");
            double precio = leerDecimal();

            ComprasOperaciones.registrarCompra(idIngrediente, cantidad, precio, idProveedor);

            System.out.print("¿Desea registrar otra compra con el mismo proveedor? (S/N): ");
            continuar = teclado.nextLine();

        } while (continuar.equalsIgnoreCase("S"));
    }

    private void buscarCompraView() {

        System.out.println("================================");
        System.out.println("         BUSCAR COMPRA");
        System.out.println("================================");

        System.out.print("Ingrese el ID de la compra: ");
        int idCompra = leerEntero();

        ComprasOperaciones.buscarCompra(idCompra);
    }

    // ==================================================
    // 13. PAGOS
    // ==================================================

    private void menuPagos() {

        int opcion;

        do {
            System.out.println("================================");
            System.out.println("        GESTIONAR PAGOS");
            System.out.println("================================");
            System.out.println("1. Registrar pago");
            System.out.println("2. Listar pagos");
            System.out.println("3. Buscar pago");
            System.out.println("0. Regresar");
            System.out.print("Seleccione una opcion: ");

            opcion = leerEntero();

            switch (opcion) {
                case 1:
                    registrarPagoView();
                    break;
                case 2:
                    PagoOperaciones.listarPagos();
                    break;
                case 3:
                    buscarPagoView();
                    break;
                case 0:
                    System.out.println("Regresando al menu principal...");
                    break;
                default:
                    System.out.println("Opcion no valida.");
            }

        } while (opcion != 0);
    }

    private void registrarPagoView() {

        System.out.println("========== REGISTRAR PAGO ==========");

        System.out.print("Ingrese ID del pedido: ");
        int idPedido = leerEntero();

        System.out.print("Ingrese monto del pago: ");
        double monto = leerDecimal();

        System.out.print("Ingrese metodo de pago: ");
        String metodoPago = teclado.nextLine();

        PagoOperaciones.registrarPago(idPedido, monto, metodoPago);
    }

    private void buscarPagoView() {

        System.out.println("========== BUSCAR PAGO ==========");

        System.out.print("Ingrese ID del pago: ");
        int idPago = leerEntero();

        PagoOperaciones.buscarPago(idPago);
    }

    // ==================================================
    // 14. VENTAS
    // ==================================================

    private void menuVentas() {

        int opcion;

        do {
            System.out.println("================================");
            System.out.println("        GESTIONAR VENTAS");
            System.out.println("================================");
            System.out.println("1. Registrar venta");
            System.out.println("2. Listar ventas");
            System.out.println("3. Buscar venta");
            System.out.println("0. Regresar");
            System.out.print("Seleccione una opcion: ");

            opcion = leerEntero();

            switch (opcion) {
                case 1:
                    registrarVentaView();
                    break;
                case 2:
                    VentaOperaciones.listarVentas();
                    break;
                case 3:
                    buscarVentaView();
                    break;
                case 0:
                    System.out.println("Regresando al menu principal...");
                    break;
                default:
                    System.out.println("Opcion no valida.");
            }

        } while (opcion != 0);
    }

    private void registrarVentaView() {

        System.out.println("========== REGISTRAR VENTA ==========");

        System.out.print("Ingrese el ID del pedido: ");
        int idPedido = leerEntero();

        VentaOperaciones.registrarVenta(idPedido);
    }

    private void buscarVentaView() {

        System.out.println("========== BUSCAR VENTA ==========");

        System.out.print("Ingrese el ID de la venta: ");
        int idVenta = leerEntero();

        VentaOperaciones.buscarVenta(idVenta);
    }

    // ==================================================
    // LECTURA DE DATOS
    // ==================================================

    private LocalDate leerFecha(String mensaje) {

        while (true) {
            System.out.print(mensaje);
            String fecha = teclado.nextLine();

            try {
                return LocalDate.parse(fecha);
            } catch (DateTimeParseException e) {
                System.out.println("Fecha no valida. Ejemplo: 2026-11-16");
            }
        }
    }

    public static int leerEntero() {

        while (true) {

            try {
                return Integer.parseInt(teclado.nextLine().trim());

            } catch (NumberFormatException e) {

                System.out.print("Ingrese un numero entero valido: ");
            }
        }
    }

    public static double leerDecimal() {

        while (true) {

            try {
                String entrada = teclado.nextLine().trim();

                entrada = entrada.replace(",", ".");

                double numero = Double.parseDouble(entrada);

                if (!Double.isFinite(numero)) {
                    System.out.print("Ingrese un numero decimal valido: ");
                    continue;
                }

                return numero;

            } catch (NumberFormatException e) {

                System.out.print("Ingrese un numero decimal valido: ");
            }
        }
    }
}