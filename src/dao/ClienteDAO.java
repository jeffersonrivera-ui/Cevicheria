package dao;

import modelo.Cliente;

public interface ClienteDAO extends GenericDAO<Cliente,Integer>{
    boolean existe(int id);


}
