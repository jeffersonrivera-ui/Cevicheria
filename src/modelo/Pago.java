package modelo;

public class Pago {

    protected int id;
    protected Pedido pedido;
    protected double monto;
    protected String metodoPago;
    protected String fecha;
    protected String estado;

    public Pago(int id, Pedido pedido, double monto, String metodoPago, String fecha, String estado) {
        this.id = id;
        this.pedido = pedido;
        this.monto = monto;
        this.metodoPago = metodoPago;
        this.fecha = fecha;
        this.estado = estado;
    }

    public void registrarPago() {
        estado = "Pagado";
        System.out.println("modelo.Pago registrado correctamente.");
    }

    public void mostrarInformacion() {
        System.out.println("ID del pago: " + id);
        System.out.println("modelo.Pedido: " + pedido.getId());
        System.out.println("Monto: " + monto + "solsitos");
        System.out.println("Metodo de pago: " + metodoPago);
        System.out.println("Fecha: " + fecha);
        System.out.println("Estado: " + estado);
    }
    public int getId() {
        return id;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public double getMonto() {
        return monto;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public String getFecha() {
        return fecha;
    }

    public String getEstado() {
        return estado;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

}
