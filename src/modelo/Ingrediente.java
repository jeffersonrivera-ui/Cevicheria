package modelo;

public class Ingrediente {

    protected int id;
    protected String nombre;
    protected String unidad;
    protected double stock;

    public Ingrediente(int id, String nombre, String unidad, double stock) {
        this.id = id;
        this.nombre = nombre;
        this.unidad = unidad;
        this.stock = stock;
    }

    public void mostrarInformacion() {
        System.out.println("ID: " + id);
        System.out.println("Ingrediente: " + nombre);
        System.out.println("Unidad: " + unidad);
        System.out.println("Stock disponible: " + stock + " " + unidad);
    }


    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getUnidad() {
        return unidad;
    }

    public double getStock() {
        return stock;
    }


    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public void setStock(double stock) {
        this.stock = stock;
    }
}
