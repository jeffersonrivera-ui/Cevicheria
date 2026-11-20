package modelo;

public class MenuDiario {

    protected int id;
    protected String fecha;
    protected String descripcion;
    protected boolean activo;

    public MenuDiario(int id, String fecha, String descripcion, boolean activo) {
        this.id = id;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.activo = activo;
    }

    public void activarMenu() {
        activo = true;
        System.out.println("El menu esta activado.");
    }

    public void desactivarMenu() {
        activo = false;
        System.out.println("El menu esta desactivado.");
    }

    public void mostrarInformacion() {
        System.out.println("ID: " + id);
        System.out.println("Fecha: " + fecha);
        System.out.println("Descripcion: " + descripcion);

        if (activo) {
            System.out.println("Estado: Activo");
        } else {
            System.out.println("Estado: Inactivo");
        }
    }
    public int getId() {
        return id;
    }

    public String getFecha() {
        return fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

}
