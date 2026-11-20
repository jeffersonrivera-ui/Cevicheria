package modelo;

public class Plato extends ProductoMenu {
    public Plato(int id, String nombre,double precio){
        super(id, nombre, precio);
    }
    @Override
    public void mostrarTipo(){
        System.out.println("Tipo de productos: modelo.Plato");
    }
}
