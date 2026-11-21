package dao;

import java.util.List;
import modelo.Empleado;

public interface EmpleadoDAO extends GenericDAO<Empleado,Integer> {

    boolean actualizarEstado(String dni, String estado);
    boolean existe(int id);

}
