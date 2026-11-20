package modelo;

public class Venta {

    protected int id;
    protected Pedido pedido;
    protected double total;
    protected String fecha;

    public Venta(int id, Pedido pedido, double total, String fecha) {
        this.id = id;
        this.pedido = pedido;
        this.total = total;
        this.fecha = fecha;
    }

    public void mostrarInformacion() {
        System.out.println("ID de venta: " + id);
        System.out.println("modelo.Pedido: " + pedido.getId());
        System.out.println("Total: " + total + "soles" );
        System.out.println("Fecha: " + fecha);
    }
    public int getId() {
        return id;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public double getTotal() {
        return total;
    }

    public String getFecha() {
        return fecha;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

}
