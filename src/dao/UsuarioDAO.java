package dao;

import modelo.Usuario;

public interface UsuarioDAO {


    int insertar(Usuario usuario);

    Usuario buscarPorNombreUsuario(String nombreUsuario);
}