package modelo;

public class Proveedor {

    public static final String ACTIVO = "ACTIVO";
    public static final String INACTIVO = "INACTIVO";

    protected int id;
    protected String nombre;
    protected String ruc;
    protected String telefono;
    protected String direccion;
    protected String productos;
    protected String estado;

    public Proveedor(int id, String nombre, String ruc, String telefono,
                     String direccion, String productos, String estado) {
        this.id = id;
        this.nombre = nombre;
        this.ruc = ruc;
        this.telefono = telefono;
        this.direccion = direccion;
        this.productos = productos;
        this.estado = estado;
    }

    public void mostrarInformacion() {
        System.out.println("ID: " + id);
        System.out.println("Proveedor: " + nombre);
        System.out.println("RUC: " + ruc);
        System.out.println("Telefono: " + telefono);
        System.out.println("Direccion: " + direccion);
        System.out.println("Productos: " + productos);
        System.out.println("Estado: " + estado);
    }

    public boolean estaActivo() {
        return ACTIVO.equalsIgnoreCase(estado);
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getProductos() {
        return productos;
    }

    public void setProductos(String productos) {
        this.productos = productos;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Proveedor{id=" + id + ", nombre='" + nombre + "', ruc='" + ruc + "', telefono='" + telefono
                + "', direccion='" + direccion + "', productos='" + productos + "', estado='" + estado + "'}";
    }
}
