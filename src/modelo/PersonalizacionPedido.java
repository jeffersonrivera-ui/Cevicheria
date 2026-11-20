package modelo;

public class PersonalizacionPedido {

    protected int id;
    protected DetallePedido detallePedido;
    protected String descripcion;

    public PersonalizacionPedido(int id, DetallePedido detallePedido, String descripcion) {
        this.id = id;
        this.detallePedido = detallePedido;
        this.descripcion = descripcion;
    }

    public void mostrarInformacion() {
        System.out.println("ID: " + id);
        System.out.println("modelo.Plato: " + detallePedido.getPlato().getNombre());
        System.out.println("Personalizacion: " + descripcion);
    }
    public int getId() {
        return id;
    }

    public DetallePedido getDetallePedido() {
        return detallePedido;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

}
