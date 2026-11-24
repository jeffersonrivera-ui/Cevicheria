package dao;

import modelo.Proveedor;

public interface ProveedorDAO extends GenericDAO<Proveedor, Integer> {
    boolean existe(int id);
    boolean existeRuc(String ruc, int idExcluido);
    boolean tieneCompras(int id);
}
