package dao;

import modelo.Cliente;
import modelo.Empleado;

import java.util.List;

public interface GenericDAO <T,K> {
    int insertar(T cliente);
    List<T> listar();
    T buscarPorId(K id);
    boolean actualizar(T cliente);
    boolean eliminar(K id);

}
