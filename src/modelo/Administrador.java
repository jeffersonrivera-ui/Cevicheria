package modelo;

import java.time.LocalDate;

public class Administrador extends Empleado {

    public Administrador(int id, String nombres, String telefono,
                         String apellidos, String direccion, String dni,
                         String contactoEmergencia, String correo,
                         String distrito, String provincia,
                         String departamento, String codigoPostal,
                         String referenciaDireccion, double salario,
                         String turnoLaboral, LocalDate fechaContratacion,
                         String estadoLaboral) {

        super(id, nombres, telefono, apellidos, "ADMINISTRADOR", direccion, dni, contactoEmergencia, correo,
                distrito, provincia, departamento, codigoPostal, referenciaDireccion, salario, turnoLaboral, fechaContratacion, estadoLaboral);
    }

    @Override
    public void realizarFuncion() {
        System.out.println("El administrador gestiona el sistema de la cevichería.");
    }
}
