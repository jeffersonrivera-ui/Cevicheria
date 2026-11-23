import controlador.UsuarioOperaciones;

public class CrearUsuarios {
    public static void main(String[] args) {
        UsuarioOperaciones op = new UsuarioOperaciones();
        System.out.println(op.registrarUsuario(1, "administrador", "Admin123"));
        System.out.println(op.registrarUsuario(2, "mesero", "Mesero123"));
        System.out.println(op.registrarUsuario(3, "cocinero", "Cocina123"));
    }
}