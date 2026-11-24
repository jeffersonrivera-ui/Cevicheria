package dao;

import modelo.Bebida;

public interface BebidaDAO extends GenericDAO<Bebida, Integer> {
    Boolean existe(int id);
}
