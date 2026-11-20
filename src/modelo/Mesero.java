package modelo;

import java.time.LocalDate;

public class Mesero extends Empleado {

    public Mesero(int id, String nombres, String telefono, String apellidos, String direccion, String dni, String contactoEmergencia, String correo,String distrito, String provincia, String departamento, String codigoPostal, String referenciaDireccion,
            double salario, String turnoLaboral, LocalDate fechaContratacion, String estadoLaboral) {

        super(id, nombres, telefono, apellidos, "Mesero", direccion, dni, contactoEmergencia, correo, distrito,
                provincia, departamento, codigoPostal, referenciaDireccion, salario, turnoLaboral, fechaContratacion, estadoLaboral);
    }

    public void registrarPedido() {
        System.out.println("El mesero registra el pedido.");
    }

    @Override
    public void realizarFuncion() {
        System.out.println("El mesero registra los pedidos.");
    }
}
