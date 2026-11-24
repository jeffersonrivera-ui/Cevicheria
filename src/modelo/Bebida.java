package modelo;

public class Bebida extends ProductoMenu {

    private int stock;

    public Bebida(int id, String nombre, double precio) {
        super(id, nombre, precio);
    }

    public Bebida(int id, String nombre, double precio, int stock) {
        super(id, nombre, precio);
        this.stock = stock;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    @Override
    public void mostrarTipo() {
        System.out.println("Tipo de productos: modelo.Bebida");
    }
}
