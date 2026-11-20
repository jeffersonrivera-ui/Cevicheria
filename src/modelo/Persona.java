package modelo;

public abstract class Persona {

    protected int id;
    protected String nombres;
    protected String apellidos;
    protected String telefono;

    public Persona(int id, String nombres, String telefono, String apellidos) {
        this.id = id;
        this.nombres = nombres;
        this.telefono = telefono;
        this.apellidos = apellidos;
    }

    public void mostrarInformacion() {
        System.out.println("ID: " + id);
        System.out.println("Nombres: " + nombres);
        System.out.println("Apellidos: " + apellidos);
        System.out.println("Telefono: " + telefono);
    }

    public int getId() {
        return id;
    }

    public String getNombres() {
        return nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
}
