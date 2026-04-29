package com.gestor.service;

import com.gestor.dao.UsuarioDAO;
import com.gestor.email.EmailService;
import com.gestor.model.Usuario;
import com.gestor.util.PasswordUtil;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public class UsuarioService {

    private final UsuarioDAO dao = new UsuarioDAO();
    private final EmailService emailService = new EmailService();

    public Usuario crearUsuario(String nombre, String apellido, String email,
                                 String passwordPlano, String rol) throws SQLException {
        if (!PasswordUtil.isStrong(passwordPlano)) {
            throw new IllegalArgumentException(
                "La contrasena debe tener al menos 8 caracteres, mayuscula, minuscula, numero y caracter especial.");
        }
        if (dao.buscarPorEmail(email).isPresent()) {
            throw new IllegalArgumentException("El email ya esta registrado: " + email);
        }
        String hash = PasswordUtil.hash(passwordPlano);
        Usuario u = new Usuario(nombre, apellido, email, hash, rol.toUpperCase());
        dao.crear(u);
        dao.registrarAuditoria(u.getId(), "CREAR_USUARIO", "Usuario creado: " + email);
        emailService.enviarBienvenida(nombre, email, passwordPlano);
        return u;
    }

    public Usuario crearUsuarioConPasswordAuto(String nombre, String apellido,
                                                String email, String rol) throws SQLException {
        String password = PasswordUtil.generateSecurePassword(12);
        return crearUsuario(nombre, apellido, email, password, rol);
    }

    public List<Usuario> listarTodos() throws SQLException {
        return dao.listarTodos();
    }

    public List<Usuario> listarActivos() throws SQLException {
        return dao.listarActivos();
    }

    public Optional<Usuario> buscarPorId(int id) throws SQLException {
        return dao.buscarPorId(id);
    }

    public boolean actualizarUsuario(int id, String nombre, String apellido,
                                      String email, String rol, boolean activo,
                                      int editorId) throws SQLException {
        Optional<Usuario> opt = dao.buscarPorId(id);
        if (opt.isEmpty()) return false;

        Optional<Usuario> existente = dao.buscarPorEmail(email);
        if (existente.isPresent() && existente.get().getId() != id) {
            throw new IllegalArgumentException("El email ya esta en uso por otro usuario.");
        }

        Usuario u = opt.get();
        u.setNombre(nombre);
        u.setApellido(apellido);
        u.setEmail(email);
        u.setRol(rol.toUpperCase());
        u.setActivo(activo);
        boolean ok = dao.actualizar(u);
        if (ok) {
            dao.registrarAuditoria(editorId, "EDITAR_USUARIO", "Usuario ID " + id + " actualizado");
            emailService.enviarNotificacionCambio(u.getNombre(), u.getEmail(), "Tu perfil fue actualizado");
        }
        return ok;
    }

    public boolean cambiarPassword(int id, String passwordActual,
                                    String nuevoPassword) throws SQLException {
        Optional<Usuario> opt = dao.buscarPorId(id);
        if (opt.isEmpty()) return false;
        Usuario u = opt.get();
        if (!PasswordUtil.verify(passwordActual, u.getPasswordHash())) {
            return false;
        }
        if (!PasswordUtil.isStrong(nuevoPassword)) {
            throw new IllegalArgumentException(
                "La nueva contrasena no cumple los requisitos de seguridad.");
        }
        String nuevoHash = PasswordUtil.hash(nuevoPassword);
        boolean ok = dao.actualizarPassword(id, nuevoHash);
        if (ok) {
            dao.registrarAuditoria(id, "CAMBIO_PASSWORD", "Contrasena actualizada");
            emailService.enviarNotificacionCambio(u.getNombre(), u.getEmail(), "Tu contrasena fue cambiada");
        }
        return ok;
    }

    public String solicitarResetPassword(String email) throws SQLException {
        Optional<Usuario> opt = dao.buscarPorEmail(email);
        if (opt.isEmpty()) throw new IllegalArgumentException("Email no registrado.");
        Usuario u = opt.get();
        String token = PasswordUtil.generateResetToken();
        OffsetDateTime expiry = OffsetDateTime.now().plusHours(1);
        dao.guardarTokenReset(u.getId(), token, expiry);
        dao.registrarAuditoria(u.getId(), "RESET_SOLICITADO", "Token enviado a " + email);
        emailService.enviarResetPassword(u.getNombre(), email, token);
        return token;
    }

    public boolean resetearPassword(String email, String token, String nuevoPassword) throws SQLException {
        Optional<Usuario> opt = dao.buscarPorEmail(email);
        if (opt.isEmpty()) return false;
        Usuario u = opt.get();
        if (u.getTokenReset() == null || !u.getTokenReset().equals(token)) return false;
        if (u.getTokenExpiry() == null || OffsetDateTime.now().isAfter(u.getTokenExpiry())) {
            throw new IllegalArgumentException("El token ha expirado.");
        }
        if (!PasswordUtil.isStrong(nuevoPassword)) {
            throw new IllegalArgumentException("La contrasena no cumple los requisitos.");
        }
        dao.actualizarPassword(u.getId(), PasswordUtil.hash(nuevoPassword));
        dao.limpiarTokenReset(u.getId());
        dao.registrarAuditoria(u.getId(), "RESET_COMPLETADO", "Contrasena restablecida");
        return true;
    }

    public boolean eliminarUsuario(int id, int adminId) throws SQLException {
        Optional<Usuario> opt = dao.buscarPorId(id);
        if (opt.isEmpty()) return false;
        if (id == adminId) throw new IllegalArgumentException("No puedes eliminarte a ti mismo.");
        boolean ok = dao.eliminar(id);
        if (ok) dao.registrarAuditoria(adminId, "ELIMINAR_USUARIO", "Usuario ID " + id + " eliminado");
        return ok;
    }

    public boolean desactivarUsuario(int id, int adminId) throws SQLException {
        if (id == adminId) throw new IllegalArgumentException("No puedes desactivarte a ti mismo.");
        boolean ok = dao.desactivar(id);
        if (ok) {
            dao.registrarAuditoria(adminId, "DESACTIVAR_USUARIO", "Usuario ID " + id + " desactivado");
            Optional<Usuario> opt = dao.buscarPorId(id);
            opt.ifPresent(u -> emailService.enviarNotificacionCambio(
                    u.getNombre(), u.getEmail(), "Tu cuenta ha sido desactivada"));
        }
        return ok;
    }
}
