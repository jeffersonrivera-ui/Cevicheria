package dao;

import modelo.Plato;
import java.util.List;

public interface PlatoDAO extends GenericDAO<Plato,Integer> {
    Boolean existe(int id);
    void actualizarDisponibilidad();
    String obtenerEstado(int id);
}
