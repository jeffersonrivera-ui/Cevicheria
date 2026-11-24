package vista.escritorio;

import modelo.Usuario;

public class VentanaMesero extends VentanaConMenu {

    public VentanaMesero(Usuario usuario) {
        super("Cevichería | Atención al cliente", "Mesero", usuario);

        String[][] accesos = {
                {"Tomar pedido", "Tomar pedido", "\u2637", "#DBEAFE", "#1D4ED8"},
                {"Pedidos listos", "Pedidos listos", "\u2713", "#DCFCE7", "#15803D"},
                {"Historial de pedidos", "Historial de pedidos", "\u2630", "#EDE9FE", "#6D28D9"},
                {"Delivery", "Delivery", "\u27A4", "#FFEDD5", "#C2410C"}
        };

        agregarGrupo("General");
        agregarSeccion("Inicio", "\u2302", () -> new PanelInicio(usuario.getIdEmpleado(), this::mostrar, accesos));

        agregarGrupo("Operaciones");
        agregarSeccion("Tomar pedido", "\u2637", () -> new PanelTomarPedido(usuario.getIdEmpleado()));
        agregarSeccion("Pedidos listos", "\u2713", () -> new PanelPedidosListos());
        agregarSeccion("Historial de pedidos", "\u2630", () -> new PanelHistorialPedidos());
        agregarSeccion("Delivery", "\u27A4", () -> new PanelDelivery());

        terminarMenu();
    }
}
