package modelo;

public class Cliente extends Persona {

    private String direccion;
    private String referencia;
    private String distrito;

    // Constructor original (se mantiene para no romper PedidoDAOImpl ni MainView)
    public Cliente(int id, String nombres, String telefono, String apellidos){
        super(id,nombres,telefono,apellidos);
    }

    // Constructor completo con los datos de entrega a domicilio
    public Cliente(int id, String nombres, String telefono, String apellidos,
                   String direccion, String referencia, String distrito) {
        super(id, nombres, telefono, apellidos);
        this.direccion = direccion;
        this.referencia = referencia;
        this.distrito = distrito;
    }

    @Override
    public void mostrarInformacion() {
        super.mostrarInformacion();
        System.out.println("Direccion: " + direccion);
        System.out.println("Referencia: " + referencia);
        System.out.println("Distrito: " + distrito);
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getDistrito() {
        return distrito;
    }

    public void setDistrito(String distrito) {
        this.distrito = distrito;
    }
}
