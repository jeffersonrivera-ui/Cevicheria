package modelo;


public class Usuario {

    private int idUsuario;
    private int idEmpleado;
    private String nombreUsuario;
    private String passwordHash;
    private String estado;
    private String cargo;
    private String estadoLaboral;

    public Usuario() {
    }

    public Usuario(int idUsuario, int idEmpleado, String nombreUsuario, String passwordHash, String estado) {
        this.idUsuario = idUsuario;
        this.idEmpleado = idEmpleado;
        this.nombreUsuario = nombreUsuario;
        this.passwordHash = passwordHash;
        this.estado = estado;
    }

    public Usuario(int idEmpleado, String nombreUsuario, String passwordHash) {
        this.idEmpleado = idEmpleado;
        this.nombreUsuario = nombreUsuario;
        this.passwordHash = passwordHash;
        this.estado = "ACTIVO";
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(int idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getEstadoLaboral() {
        return estadoLaboral;
    }

    public void setEstadoLaboral(String estadoLaboral) {
        this.estadoLaboral = estadoLaboral;
    }
}

