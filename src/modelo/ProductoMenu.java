package modelo;

public abstract class ProductoMenu implements Vendible {
    protected int id;
    protected String nombre;
    protected double precio;
    public ProductoMenu(int id, String nombre, double precio){
        this.id=id;
        this.nombre=nombre;
        this.precio=precio;
    }
    public void mostrarInformacion(){
        System.out.println("ID: " + id);
        System.out.println("Nombre: " + nombre);
        mostrarPrecio();
    }
    @Override
    public void mostrarPrecio(){
        System.out.println("Precio: "+ precio + "soles. ");
    }
    public abstract void mostrarTipo();
    public  int getId(){
        return id;

    }
    public String getNombre(){
        return nombre;

    }
    public double getPrecio(){
        return precio;

    }
    public void setPrecio(double precio){
        this.precio=precio;
    }


}
