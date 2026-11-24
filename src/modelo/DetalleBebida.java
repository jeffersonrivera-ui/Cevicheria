package modelo;

public class DetalleBebida {

    protected int id;
    protected int idPedido;
    protected Bebida bebida;
    protected int cantidad;
    protected double precio;

    public DetalleBebida(int id, int idPedido, Bebida bebida, int cantidad, double precio) {
        this.id = id;
        this.idPedido = idPedido;
        this.bebida = bebida;
        this.cantidad = cantidad;
        this.precio = precio;
    }

    public double calcularSubtotal() {
        return cantidad * precio;
    }

    public void mostrarInformacion() {
        System.out.println("ID detalle bebida: " + id);
        System.out.println("Bebida: " + bebida.getNombre());
        System.out.println("Cantidad: " + cantidad);
        System.out.println("Precio unitario: S/ " + precio);
        System.out.println("Subtotal: S/ " + calcularSubtotal());
    }

    public int getId() {
        return id;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public Bebida getBebida() {
        return bebida;
    }

    public int getCantidad() {
        return cantidad;
    }

    public double getPrecio() {
        return precio;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
