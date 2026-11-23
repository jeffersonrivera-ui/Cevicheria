package vista.escritorio;

import modelo.Usuario;

public class VentanaAdministrador extends VentanaConMenu {

    public VentanaAdministrador(Usuario usuario) {
        super("Cevichería | Sistema Administrativo", "Administrador", usuario);

        String[][] accesos = {
                {"Registrar pedido", "Pedidos", "☷", "#DBEAFE", "#1D4ED8"},
                {"Registrar compra", "Compras", "☆", "#DCFCE7", "#15803D"},
                {"Registrar pago", "Pagos", "☰", "#EDE9FE", "#6D28D9"},
                {"Registrar venta", "Ventas", "≡", "#FFEDD5", "#C2410C"}
        };

        agregarGrupo("General");
        agregarSeccion("Inicio", "⌂", new PanelInicio(null, this::mostrar, accesos));

        agregarGrupo("Gestión Comercial");
        agregarSeccion("Platos", "♨", new PanelPlatos());
        agregarSeccion("Clientes", "☺", new PanelClientes());
        agregarSeccion("Ingredientes", "\u2766", new PanelIngredientes());
        agregarSeccion("Recetas", "☰", new PanelRecetas(false));

        agregarGrupo("Operaciones");
        agregarSeccion("Empleados", "☻", new PanelEmpleados());
        agregarSeccion("Pedidos", "☷", new PanelPedidos());
        agregarSeccion("Detalles de pedidos", "≡", new PanelDetallesPedido());
        agregarSeccion("Exclusiones", "⊘", new PanelExclusiones());

        agregarGrupo("Administración Financiera");
        agregarSeccion("Compras", "☆", new PanelCompras());
        agregarSeccion("Pagos", "☰", new PanelPagos());
        agregarSeccion("Ventas", "≡", new PanelVentas());

        terminarMenu();
    }
}
