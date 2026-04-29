package com.gestor;

import com.gestor.model.Usuario;
import com.gestor.service.AuthService;
import com.gestor.service.UsuarioService;
import com.gestor.util.PasswordUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {


    private static final Scanner sc = new Scanner(System.in);
    private static final AuthService auth = new AuthService();
    private static final UsuarioService service = new UsuarioService();

    public static void main(String[] args) {
        System.out.println("=== Gestor de Usuarios ===");
        System.out.println("Conectando a Supabase...");

        if (!iniciarSesion()) {
            System.out.println("Demasiados intentos fallidos. Saliendo.");
            return;
        }

        boolean corriendo = true;
        while (corriendo) {
            mostrarMenu();
            int opcion = leerInt("Opcion: ");
            try {
                if (auth.isAdmin()) {
                    corriendo = manejarMenuAdmin(opcion);
                } else {
                    corriendo = manejarMenuUsuario(opcion);
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        try { auth.logout(); } catch (Exception ignored) {}
        System.out.println("Sesion cerrada. Hasta pronto.");
    }

    private static boolean iniciarSesion() {
        for (int intento = 1; intento <= 3; intento++) {
            System.out.println("\n--- Inicio de Sesion (intento " + intento + "/3) ---");
            String email = leerTexto("Email: ");
            String password = leerTexto("Contrasena: ");
            try {
                if (auth.login(email, password)) {
                    Usuario u = auth.getUsuarioActual();
                    System.out.println("Bienvenido, " + u.getNombre() + " [" + u.getRol() + "]");
                    return true;
                } else {
                    System.out.println("Credenciales incorrectas o cuenta inactiva.");
                }
            } catch (SQLException e) {
                System.out.println("Error de conexion: " + e.getMessage());
            }
        }
        return false;
    }

    private static void mostrarMenu() {
        System.out.println("\n" + "=".repeat(40));
        if (auth.isAdmin()) {
            System.out.println("  MENU ADMINISTRADOR");
            System.out.println("  1. Crear usuario");
            System.out.println("  2. Listar todos los usuarios");
            System.out.println("  3. Buscar usuario por ID");
            System.out.println("  4. Editar usuario");
            System.out.println("  5. Desactivar usuario");
            System.out.println("  6. Eliminar usuario");
            System.out.println("  7. Cambiar mi contrasena");
            System.out.println("  8. Salir");
        } else {
            System.out.println("  MENU USUARIO");
            System.out.println("  1. Ver mi perfil");
            System.out.println("  2. Cambiar mi contrasena");
            System.out.println("  3. Solicitar reset de contrasena");
            System.out.println("  4. Salir");
        }
        System.out.println("=".repeat(40));
    }

    private static boolean manejarMenuAdmin(int opcion) throws SQLException {
        switch (opcion) {
            case 1 -> crearUsuario();
            case 2 -> listarUsuarios();
            case 3 -> buscarUsuario();
            case 4 -> editarUsuario();
            case 5 -> desactivarUsuario();
            case 6 -> eliminarUsuario();
            case 7 -> cambiarMiPassword();
            case 8 -> { return false; }
            default -> System.out.println("Opcion invalida.");
        }
        return true;
    }

    private static boolean manejarMenuUsuario(int opcion) throws SQLException {
        switch (opcion) {
            case 1 -> verMiPerfil();
            case 2 -> cambiarMiPassword();
            case 3 -> solicitarReset();
            case 4 -> { return false; }
            default -> System.out.println("Opcion invalida.");
        }
        return true;
    }

    private static void crearUsuario() throws SQLException {
        System.out.println("\n--- Crear Usuario ---");
        String nombre   = leerTexto("Nombre: ");
        String apellido = leerTexto("Apellido: ");
        String email    = leerTexto("Email: ");
        System.out.println("Rol: 1. ADMIN  2. USUARIO");
        int rolOp = leerInt("Seleccionar: ");
        String rol = (rolOp == 1) ? "ADMIN" : "USUARIO";

        System.out.println("Contrasena: 1. Ingresar manualmente  2. Generar automaticamente");
        int passOp = leerInt("Seleccionar: ");

        Usuario creado;
        if (passOp == 1) {
            System.out.println("Requisitos: min 8 chars, mayuscula, minuscula, numero, especial");
            String pass = leerTexto("Contrasena: ");
            creado = service.crearUsuario(nombre, apellido, email, pass, rol);
        } else {
            creado = service.crearUsuarioConPasswordAuto(nombre, apellido, email, rol);
        }
        System.out.println("Usuario creado con ID: " + creado.getId());
        System.out.println("Se envio correo de bienvenida a: " + email);
    }

    private static void listarUsuarios() throws SQLException {
        System.out.println("\n--- Lista de Usuarios ---");
        List<Usuario> lista = service.listarTodos();
        if (lista.isEmpty()) {
            System.out.println("No hay usuarios registrados.");
            return;
        }
        System.out.printf("%-5s | %-15s %-15s | %-30s | %-7s | %s%n",
                "ID", "Nombre", "Apellido", "Email", "Rol", "Estado");
        System.out.println("-".repeat(85));
        lista.forEach(System.out::println);
        System.out.println("Total: " + lista.size());
    }

    private static void buscarUsuario() throws SQLException {
        int id = leerInt("ID del usuario: ");
        Optional<Usuario> opt = service.buscarPorId(id);
        if (opt.isEmpty()) {
            System.out.println("Usuario no encontrado.");
            return;
        }
        Usuario u = opt.get();
        System.out.println("\n--- Detalle ---");
        System.out.println("ID:          " + u.getId());
        System.out.println("Nombre:      " + u.getNombre() + " " + u.getApellido());
        System.out.println("Email:       " + u.getEmail());
        System.out.println("Rol:         " + u.getRol());
        System.out.println("Estado:      " + (u.isActivo() ? "Activo" : "Inactivo"));
        System.out.println("Creado:      " + u.getCreadoEn());
        System.out.println("Actualizado: " + u.getActualizadoEn());
    }

    private static void editarUsuario() throws SQLException {
        int id = leerInt("ID del usuario a editar: ");
        Optional<Usuario> opt = service.buscarPorId(id);
        if (opt.isEmpty()) {
            System.out.println("Usuario no encontrado.");
            return;
        }
        Usuario u = opt.get();
        System.out.println("Editando: " + u.getNombre() + " " + u.getApellido());
        System.out.println("(Enter para mantener valor actual)");

        String nombre   = leerOpcional("Nombre [" + u.getNombre() + "]: ", u.getNombre());
        String apellido = leerOpcional("Apellido [" + u.getApellido() + "]: ", u.getApellido());
        String email    = leerOpcional("Email [" + u.getEmail() + "]: ", u.getEmail());
        System.out.println("Rol: 1. ADMIN  2. USUARIO (actual: " + u.getRol() + ")");
        int rolOp = leerInt("Seleccionar (0 mantener): ");
        String rol = (rolOp == 1) ? "ADMIN" : (rolOp == 2) ? "USUARIO" : u.getRol();
        System.out.println("Estado: 1. Activo  2. Inactivo (actual: " + (u.isActivo() ? "Activo" : "Inactivo") + ")");
        int estadoOp = leerInt("Seleccionar (0 mantener): ");
        boolean activo = (estadoOp == 0) ? u.isActivo() : (estadoOp == 1);

        boolean ok = service.actualizarUsuario(id, nombre, apellido, email, rol, activo,
                auth.getUsuarioActual().getId());
        System.out.println(ok ? "Usuario actualizado." : "No se pudo actualizar.");
    }

    private static void desactivarUsuario() throws SQLException {
        int id = leerInt("ID del usuario a desactivar: ");
        System.out.print("Confirmar desactivacion (s/n): ");
        if (!"s".equalsIgnoreCase(sc.nextLine().trim())) return;
        boolean ok = service.desactivarUsuario(id, auth.getUsuarioActual().getId());
        System.out.println(ok ? "Usuario desactivado." : "No se pudo desactivar.");
    }

    private static void eliminarUsuario() throws SQLException {
        int id = leerInt("ID del usuario a eliminar: ");
        System.out.print("Esta accion es IRREVERSIBLE. Confirmar (s/n): ");
        if (!"s".equalsIgnoreCase(sc.nextLine().trim())) return;
        boolean ok = service.eliminarUsuario(id, auth.getUsuarioActual().getId());
        System.out.println(ok ? "Usuario eliminado." : "No se pudo eliminar.");
    }

    private static void cambiarMiPassword() throws SQLException {
        System.out.println("\n--- Cambiar Contrasena ---");
        String actual = leerTexto("Contrasena actual: ");
        String nueva  = leerTexto("Nueva contrasena: ");
        boolean ok = service.cambiarPassword(auth.getUsuarioActual().getId(), actual, nueva);
        System.out.println(ok ? "Contrasena actualizada." : "Contrasena actual incorrecta.");
    }

    private static void verMiPerfil() {
        Usuario u = auth.getUsuarioActual();
        System.out.println("\n--- Mi Perfil ---");
        System.out.println("ID:      " + u.getId());
        System.out.println("Nombre:  " + u.getNombre() + " " + u.getApellido());
        System.out.println("Email:   " + u.getEmail());
        System.out.println("Rol:     " + u.getRol());
    }

    private static void solicitarReset() throws SQLException {
        String email = leerTexto("Email para reset: ");
        String token = service.solicitarResetPassword(email);
        System.out.println("Token enviado al correo registrado.");
        System.out.println("Introduce el token recibido:");
        String tokenIngresado = leerTexto("Token: ");
        String nueva = leerTexto("Nueva contrasena: ");
        boolean ok = service.resetearPassword(email, tokenIngresado, nueva);
        System.out.println(ok ? "Contrasena restablecida. Por favor inicia sesion nuevamente." : "Token invalido.");
    }

    private static String leerTexto(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    private static int leerInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int val = Integer.parseInt(sc.nextLine().trim());
                return val;
            } catch (NumberFormatException e) {
                System.out.println("Ingresa un numero valido.");
            }
        }
    }

    private static String leerOpcional(String prompt, String valorActual) {
        System.out.print(prompt);
        String val = sc.nextLine().trim();
        return val.isEmpty() ? valorActual : val;
    }
}
