package vista;

import java.util.Scanner;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import controlador.ClienteOperaciones;
import controlador.PlatosOperaciones;
import controlador.IngredientesOperaciones;
import controlador.EmpleadosOperaciones;
import controlador.PedidosOperaciones;
import controlador.DetallePedidoOperacione;
import controlador.PlatoIngredienteOperaciones;
import controlador.DetalleIngredienteExcluidoOperaciones;
import controlador.ComprasOperaciones;
import controlador.PagoOperaciones;
import controlador.VentaOperaciones;

public class MainView {

    public static Scanner teclado = new Scanner(System.in);

    // ==================================================
    // MENUS DEL SISTEMA
    // ==================================================

    public void iniciar() {

        int opcion;

        do {
            System.out.println("================================");
            System.out.println("      SISTEMA DE CEVICHERIA");
            System.out.println("================================");
            System.out.println("1. Gestionar Platos");
            System.out.println("2. Gestionar Clientes");
            System.out.println("3. Gestionar Ingredientes");
            System.out.println("4. Gestionar Empleados");
            System.out.println("5. Gestionar Pedidos");
            System.out.println("6. Gestionar Detalles de Pedidos");
            System.out.println("7. Gestionar Compras");
            System.out.println("8. Gestionar Pagos");
            System.out.println("9. Gestionar Ventas");
            System.out.println("0. Salir");
            System.out.print("Seleccione una opcion: ");

            opcion = leerEntero();

            switch (opcion) {

                case 1:
                    menuPlatos();
                    break;

                case 2:
                    menuClientes();
                    break;

                case 3:
                    menuIngredientes();
                    break;

                case 4:
                    menuEmpleados();
                    break;

                case 5:
                    menuPedidos();
                    break;

                case 6:
                    menuDetallesPedidos();
                    break;

                case 7:
                    menuCompras();
                    break;
                case 8:
                    menuPagos();
                    break;
                case 9:
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
                    actualizarPlato();
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
                    actualizarCliente();
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
                    actualizarIngrediente();
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

    private void menuDetallesPedidos() {

        int opcion;

        do {
            System.out.println("================================");
            System.out.println("   MENU DE DETALLES DE PEDIDOS");
            System.out.println("================================");
            System.out.println("1. Registrar detalle");
            System.out.println("2. Listar detalles de un pedido");
            System.out.println("3. Buscar detalle");
            System.out.println("4. Actualizar detalle");
            System.out.println("5. Eliminar detalle");
            System.out.println("6. Calcular total del pedido");
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

                case 0:
                    System.out.println("Volviendo al menu principal.");
                    break;

                default:
                    System.out.println("Opcion no valida.");
            }

        } while (opcion != 0);
    }

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
            System.out.println("================================");
            System.out.print("Seleccione una opcion: ");

            opcion = leerEntero();

            switch (opcion) {

                case 1:
                    registrarCompra();
                    break;

                case 2:
                    ComprasOperaciones.listarCompras();
                    break;

                case 3:
                    buscarCompra();
                    break;

                case 0:
                    System.out.println("Regresando al menu principal...");
                    break;

                default:
                    System.out.println("Opcion no valida.");
            }
        } while (opcion != 0);


    }
    // =====================================
// MENU DE PAGOS
// =====================================

    private void menuPagos() {

        int opcion;

        do {
            System.out.println("========== GESTIONAR PAGOS ==========");
            System.out.println("1. Registrar pago");
            System.out.println("2. Listar pagos");
            System.out.println("3. Buscar pago");
            System.out.println("0. Regresar");
            System.out.print("Seleccione una opcion: ");

            opcion = leerEntero();

            switch (opcion) {

                case 1:
                    registrarPago();
                    break;

                case 2:
                    PagoOperaciones.listarPagos();
                    break;

                case 3:
                    buscarPago();
                    break;

                case 0:
                    System.out.println("Regresando al menu principal...");
                    break;

                default:
                    System.out.println("Opcion no valida.");
            }

        } while (opcion != 0);
    }

    private void menuVentas() {

        int opcion;

        do {
            System.out.println("========== GESTIONAR VENTAS ==========");
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


    private void registrarPago() {

        System.out.println("========== REGISTRAR PAGO ==========");

        System.out.print("Ingrese ID del pedido: ");
        int idPedido = leerEntero();

        System.out.print("Ingrese monto del pago: ");
        double monto = leerDecimal();

        System.out.print("Ingrese metodo de pago: ");
        String metodoPago = teclado.nextLine();

        PagoOperaciones.registrarPago(idPedido, monto, metodoPago);
    }

    private void buscarPago() {

        System.out.println("========== BUSCAR PAGO ==========");

        System.out.print("Ingrese ID del pago: ");
        int idPago = leerEntero();

        PagoOperaciones.buscarPago(idPago);
    }

    private void registrarCompra() {


        System.out.println("================================");
        System.out.println("       REGISTRAR COMPRA");
        System.out.println("================================");

        System.out.print("Ingrese ID del ingrediente: ");
        int idIngrediente = Integer.parseInt(teclado.nextLine());

        System.out.print("Ingrese cantidad comprada: ");
        double cantidad = Double.parseDouble(teclado.nextLine());

        System.out.print("Ingrese precio unitario: ");
        double precio = Double.parseDouble(teclado.nextLine());

        ComprasOperaciones.registrarCompra(idIngrediente, cantidad, precio);


    }

    private void buscarCompra() {

        System.out.println("================================");
        System.out.println("         BUSCAR COMPRA");
        System.out.println("================================");

        System.out.print("Ingrese ID de la compra: ");

        int idCompra =
                Integer.parseInt(teclado.nextLine());

        ComprasOperaciones.buscarCompra(idCompra);


    }

    //metododos

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

    private void actualizarPlato() {

        int id;

        while (true) {

            System.out.print(
                    "Ingrese el ID del plato que desea actualizar: "
            );

            id = leerEntero();

            Boolean existe = PlatosOperaciones.existePlato(id);

            if (existe == null) {
                return;
            }

            if (existe) {
                break;
            }

            System.out.println(
                    "No existe el plato. Intente nuevamente."
            );
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


    private void registrarClienteView() {
        System.out.println("--- REGISTRAR CLIENTE ---");

        System.out.print("Ingrese los nombres del cliente: ");
        String nombres = teclado.nextLine();

        System.out.print("Ingrese los apellidos del cliente: ");
        String apellidos = teclado.nextLine();

        System.out.print("Ingrese el teléfono del cliente: ");
        String telefono = teclado.nextLine();

        ClienteOperaciones.registrarCliente(nombres, apellidos, telefono);
    }

    private void buscarClienteView() {

        System.out.print("Ingrese el ID del cliente: ");
        int id = leerEntero();

        ClienteOperaciones.buscarCliente(id);
    }

    private void actualizarCliente() {
        System.out.println("--- ACTUALIZAR CLIENTE ---");

        System.out.print("Ingrese el ID del cliente: ");
        int id = leerEntero();

        if (!ClienteOperaciones.existeCliente(id)) {
            System.out.println("No se encontró el cliente.");
            return;
        }
        System.out.print("Ingrese los nuevos nombres: ");
        String nombres = teclado.nextLine();

        System.out.print("Ingrese los nuevos apellidos: ");
        String apellidos = teclado.nextLine();

        System.out.print("Ingrese el nuevo teléfono: ");
        String telefono = teclado.nextLine();

        ClienteOperaciones.actualizarCliente(id, nombres, apellidos, telefono);
    }

    private void eliminarClienteView() {

        System.out.print("Ingrese el ID del cliente: ");
        int id = leerEntero();

        ClienteOperaciones.eliminarCliente(id);
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

    private void actualizarIngrediente() {

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

        System.out.println("Seleccione el turno laboral:");
        System.out.println("1. Mañana");
        System.out.println("2. Tarde");
        System.out.println("3. Noche");
        System.out.print("Seleccione una opcion: ");

        int opcionTurno = leerEntero();

        String turnoLaboral;

        switch (opcionTurno) {
            case 1:
                turnoLaboral = "MAÑANA";
                break;
            case 2:
                turnoLaboral = "TARDE";
                break;
            case 3:
                turnoLaboral = "NOCHE";
                break;
            default:
                System.out.println("Turno no valido.");
                return;
        }

        LocalDate fechaContratacion;

        while (true) {
            System.out.print("Ingrese la fecha de contratacion (AAAA-MM-DD): ");
            String fecha = teclado.nextLine();

            try {
                fechaContratacion = LocalDate.parse(fecha);
                break;
            } catch (DateTimeParseException e) {
                System.out.println("Fecha no valida. Ejemplo: 2026-11-16");
            }
        }

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

    private void eliminarEmpleadoView() {

        System.out.println("========== ELIMINAR EMPLEADO ==========");

        System.out.print("Ingrese el ID del empleado: ");
        int id = leerEntero();

        EmpleadosOperaciones.eliminarEmpleado(id);
    }

    private void actualizarEmpleadoView() {

        System.out.println("========== MODIFICAR EMPLEADO ==========");

        System.out.print("Ingrese el ID del empleado: ");
        int id = leerEntero();

        if (!EmpleadosOperaciones.existeEmpleado(id)) {
            System.out.println("No se encontró el empleado.");
            return;
        }

        System.out.print("Ingrese los nuevos nombres: ");
        String nombres = teclado.nextLine();

        System.out.print("Ingrese los nuevos apellidos: ");
        String apellidos = teclado.nextLine();

        System.out.print("Ingrese el nuevo cargo (Mesero/Cocinero): ");
        String cargo = teclado.nextLine();

        if (!cargo.equalsIgnoreCase("MESERO") && !cargo.equalsIgnoreCase("COCINERO")&& !cargo.equalsIgnoreCase("ADMINISTRADOR")) {
            System.out.println("Cargo no válido.");
            return;
        }
        cargo = cargo.toUpperCase();

        System.out.print("Ingrese el nuevo teléfono: ");
        String telefono = teclado.nextLine();

        System.out.print("Ingrese la nueva dirección: ");
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

        System.out.print("Ingrese el nuevo código postal: ");
        String codigoPostal = teclado.nextLine();

        System.out.print("Ingrese la nueva referencia de dirección: ");
        String referenciaDireccion = teclado.nextLine();

        System.out.print("Ingrese el nuevo salario: ");
        double salario = leerDecimal();

        if (salario < 0) {
            System.out.println("El salario no puede ser negativo.");
            return;
        }

        System.out.println("Seleccione el nuevo turno laboral:");
        System.out.println("1. Mañana");
        System.out.println("2. Tarde");
        System.out.println("3. Noche");
        System.out.print("Seleccione una opción: ");

        int opcionTurno = leerEntero();

        String turnoLaboral;

        switch (opcionTurno) {
            case 1:
                turnoLaboral = "MAÑANA";
                break;

            case 2:
                turnoLaboral = "TARDE";
                break;

            case 3:
                turnoLaboral = "NOCHE";
                break;

            default:
                System.out.println("Turno no válido.");
                return;
        }

        LocalDate fechaContratacion;

        while (true) {

            System.out.print("Ingrese la nueva fecha de contratación (AAAA-MM-DD): ");
            String fecha = teclado.nextLine();

            try {
                fechaContratacion = LocalDate.parse(fecha);
                break;

            } catch (DateTimeParseException e) {
                System.out.println("Fecha no válida. Ejemplo: 2026-11-21");
            }
        }

        EmpleadosOperaciones.actualizarEmpleado(id, nombres,
                apellidos, cargo, telefono, direccion, dni,
                contactoEmergencia, correo, distrito, provincia, departamento, codigoPostal, referenciaDireccion, salario, turnoLaboral, fechaContratacion);
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

    private void registrarPedidoView() {

        System.out.println("=== REGISTRAR PEDIDO ===");

        System.out.print("Ingrese el ID del cliente: ");
        int idCliente = leerEntero();

        System.out.print("Ingrese el ID del mesero: ");
        int idMesero = leerEntero();

        int idPedido = PedidosOperaciones.registrarPedido(
                idCliente, idMesero
        );

        if (idPedido != -1) {

            String continuar;

            do {

                System.out.println("=== AGREGAR PLATO AL PEDIDO ===");

                registrarDetalleView(idPedido);

                System.out.print("¿Desea agregar otro plato? (S/N): ");

                continuar = teclado.nextLine();

            } while (continuar.equalsIgnoreCase("S"));

            System.out.println("================================");
            System.out.println("       PEDIDO COMPLETADO");
            System.out.println("================================");
            System.out.println("ID del pedido: " + idPedido);

            System.out.println("RESUMEN DEL PEDIDO:");

            DetallePedidoOperacione.listarDetalles(idPedido);
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

                    DetalleIngredienteExcluidoOperaciones.registrarExclusion(
                            idDetalle, idIngrediente
                    );

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


    public static int leerEntero() {

        while (true) {

            try {
                return Integer.parseInt(teclado.nextLine());

            } catch (NumberFormatException e) {

                System.out.print(
                        "Ingrese un numero entero valido: "
                );
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



