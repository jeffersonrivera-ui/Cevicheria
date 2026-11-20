package modelo;

public class Pedido {

    protected int id;
    protected Cliente cliente;
    protected Mesero mesero;
    protected String fecha;
    protected String estado;


    public Pedido(int id, Cliente cliente, Mesero mesero,
                  String fecha, String estado) {
        this.id = id;
        this.cliente = cliente;
        this.mesero = mesero;
        this.fecha = fecha;
        this.estado = estado;
    }

    public void mostrarInformacion() {
        System.out.println("Pedido: " + id);
        System.out.println("Cliente: " + cliente.getNombres());
        System.out.println("Mesero: " +  mesero.getNombres());
        System.out.println("Fecha: " + fecha);
        System.out.println("Estado: " + estado);
    }

    public void cambiarEstado(String nuevoEstado) {
        estado = nuevoEstado;
        System.out.println("Estado del pedido: " + estado);
    }
    public int getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Mesero getMesero() {
        return mesero;
    }

    public String getFecha() {
        return fecha;
    }

    public String getEstado() {
        return estado;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

}
