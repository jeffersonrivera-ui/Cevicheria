
package dao;

import java.util.List;
import modelo.Ingrediente;

public interface IngredientesDAO extends GenericDAO< Ingrediente,Integer> {

    boolean existe(int id);
}
