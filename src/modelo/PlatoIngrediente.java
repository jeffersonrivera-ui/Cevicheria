package modelo;

public class PlatoIngrediente {

    protected Plato plato;
    protected Ingrediente ingrediente;
    protected double cantidad;

    public PlatoIngrediente(Plato plato, Ingrediente ingrediente, double cantidad) {
        this.plato = plato;
        this.ingrediente = ingrediente;
        this.cantidad = cantidad;
    }


    public void mostrarInformacion() {
        System.out.println("Plato: " + plato.getNombre());
        System.out.println("Ingrediente: " + ingrediente.getNombre());
        System.out.println("Cantidad necesaria: " + cantidad + " " + ingrediente.getUnidad());
    }


    public Plato getPlato() {
        return plato;
    }

    public Ingrediente getIngrediente() {
        return ingrediente;
    }

    public double getCantidad() {
        return cantidad;
    }


    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }
}

