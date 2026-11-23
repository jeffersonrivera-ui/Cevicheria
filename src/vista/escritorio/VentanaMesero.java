package vista.escritorio;

import modelo.Usuario;

public class VentanaMesero extends VentanaConMenu {

    public VentanaMesero(Usuario usuario) {
        super("Cevichería | Atención al cliente", "Mesero", usuario);

        String[][] accesos = {
                {"Registrar pedido", "Pedidos", "\u2637", "#DBEAFE", "#1D4ED8"},
                {"Clientes", "Clientes", "\u263A", "#DCFCE7", "#15803D"},
                {"Ver platos", "Platos", "\u2668", "#EDE9FE", "#6D28D9"},
                {"Detalles de pedidos", "Detalles de pedidos", "\u2261", "#FFEDD5", "#C2410C"}
        };

        agregarGrupo("General");
        agregarSeccion("Inicio", "\u2302", new PanelInicio(usuario.getIdEmpleado(), this::mostrar, accesos));

        agregarGrupo("Gestión Comercial");
        agregarSeccion("Platos", "\u2668", new PanelPlatos());
        agregarSeccion("Clientes", "\u263A", new PanelClientes());
        agregarSeccion("Recetas", "\u2630", new PanelRecetas(true));

        agregarGrupo("Operaciones");
        agregarSeccion("Pedidos", "\u2637", new PanelPedidos(usuario.getIdEmpleado()));
        agregarSeccion("Detalles de pedidos", "\u2261", new PanelDetallesPedido(usuario.getIdEmpleado()));

        terminarMenu();
    }
}
