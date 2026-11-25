package dao;

import java.util.List;

import modelo.Usuario;

public interface UsuarioDAO {

    int insertar(Usuario usuario);

    Usuario buscarPorNombreUsuario(String nombreUsuario);

    Usuario buscarPorId(int idUsuario);

    List<Usuario> listar();

    boolean actualizarPassword(int idUsuario, String passwordHash);

    boolean actualizarEstado(int idUsuario, String estado);

    boolean eliminar(int idUsuario);
}
