package modelo;

public class Delivery {

    public static final String SOLICITADO = "SOLICITADO";
    public static final String REPARTIDOR_ASIGNADO = "REPARTIDOR_ASIGNADO";
    public static final String PEDIDO_LISTO = "PEDIDO_LISTO";
    public static final String EN_CAMINO = "EN_CAMINO";
    public static final String ENTREGADO = "ENTREGADO";
    public static final String CANCELADO = "CANCELADO";

    public static final String[] FLUJO = {SOLICITADO, REPARTIDOR_ASIGNADO, PEDIDO_LISTO, EN_CAMINO, ENTREGADO};
    public static final String[] PLATAFORMAS = {"inDrive", "Rappi", "Otro"};

    protected int idDelivery;
    protected int idPedido;
    protected String codigoDelivery;
    protected String plataforma;
    protected String codigoExterno;
    protected String direccionEntrega;
    protected String referenciaEntrega;
    protected double costoDelivery;
    protected String estado;
    protected String fechaSolicitud;

    public Delivery(int idDelivery, int idPedido, String codigoDelivery, String plataforma,
                    String codigoExterno, String direccionEntrega, String referenciaEntrega,
                    double costoDelivery, String estado, String fechaSolicitud) {
        this.idDelivery = idDelivery;
        this.idPedido = idPedido;
        this.codigoDelivery = codigoDelivery;
        this.plataforma = plataforma;
        this.codigoExterno = codigoExterno;
        this.direccionEntrega = direccionEntrega;
        this.referenciaEntrega = referenciaEntrega;
        this.costoDelivery = costoDelivery;
        this.estado = estado;
        this.fechaSolicitud = fechaSolicitud;
    }

    public boolean estaFinalizado() {
        return ENTREGADO.equals(estado) || CANCELADO.equals(estado);
    }

    public int getIdDelivery() {
        return idDelivery;
    }

    public void setIdDelivery(int idDelivery) {
        this.idDelivery = idDelivery;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public String getCodigoDelivery() {
        return codigoDelivery;
    }

    public void setCodigoDelivery(String codigoDelivery) {
        this.codigoDelivery = codigoDelivery;
    }

    public String getPlataforma() {
        return plataforma;
    }

    public void setPlataforma(String plataforma) {
        this.plataforma = plataforma;
    }

    public String getCodigoExterno() {
        return codigoExterno;
    }

    public void setCodigoExterno(String codigoExterno) {
        this.codigoExterno = codigoExterno;
    }

    public String getDireccionEntrega() {
        return direccionEntrega;
    }

    public void setDireccionEntrega(String direccionEntrega) {
        this.direccionEntrega = direccionEntrega;
    }

    public String getReferenciaEntrega() {
        return referenciaEntrega;
    }

    public void setReferenciaEntrega(String referenciaEntrega) {
        this.referenciaEntrega = referenciaEntrega;
    }

    public double getCostoDelivery() {
        return costoDelivery;
    }

    public void setCostoDelivery(double costoDelivery) {
        this.costoDelivery = costoDelivery;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(String fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public void mostrarInformacion() {
        System.out.println(this);
    }

    @Override
    public String toString() {
        return "Delivery{" +
                "idDelivery=" + idDelivery +
                ", idPedido=" + idPedido +
                ", codigoDelivery='" + codigoDelivery + '\'' +
                ", plataforma='" + plataforma + '\'' +
                ", codigoExterno='" + codigoExterno + '\'' +
                ", direccionEntrega='" + direccionEntrega + '\'' +
                ", referenciaEntrega='" + referenciaEntrega + '\'' +
                ", costoDelivery=" + costoDelivery +
                ", estado='" + estado + '\'' +
                ", fechaSolicitud='" + fechaSolicitud + '\'' +
                '}';
    }
}
