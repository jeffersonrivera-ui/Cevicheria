package modelo;

public class SeguimientoDelivery {

    protected int idSeguimiento;
    protected int idDelivery;
    protected String estado;
    protected String descripcion;
    protected String fecha;

    public SeguimientoDelivery(int idSeguimiento, int idDelivery, String estado,
                               String descripcion, String fecha) {
        this.idSeguimiento = idSeguimiento;
        this.idDelivery = idDelivery;
        this.estado = estado;
        this.descripcion = descripcion;
        this.fecha = fecha;
    }

    public int getIdSeguimiento() {
        return idSeguimiento;
    }

    public void setIdSeguimiento(int idSeguimiento) {
        this.idSeguimiento = idSeguimiento;
    }

    public int getIdDelivery() {
        return idDelivery;
    }

    public void setIdDelivery(int idDelivery) {
        this.idDelivery = idDelivery;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    @Override
    public String toString() {
        return "SeguimientoDelivery{" + "idSeguimiento=" + idSeguimiento +
                ", idDelivery=" + idDelivery +
                ", estado='" + estado + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", fecha='" + fecha + '\'' +
                '}';
    }
}
