package com.gestor.service;

import com.gestor.dao.UsuarioDAO;
import com.gestor.model.Usuario;
import com.gestor.util.PasswordUtil;

import java.sql.SQLException;
import java.util.Optional;

public class AuthService {

    private final UsuarioDAO dao = new UsuarioDAO();
    private Usuario usuarioActual = null;

    public boolean login(String email, String password) throws SQLException {
        Optional<Usuario> opt = dao.buscarPorEmail(email);
        if (opt.isEmpty()) {
            dao.registrarAuditoria(null, "LOGIN_FALLIDO", "Email no encontrado: " + email);
            return false;
        }
        Usuario u = opt.get();
        if (!u.isActivo()) {
            dao.registrarAuditoria(u.getId(), "LOGIN_FALLIDO", "Cuenta inactiva");
            return false;
        }
        if (!PasswordUtil.verify(password, u.getPasswordHash())) {
            dao.registrarAuditoria(u.getId(), "LOGIN_FALLIDO", "Password incorrecto");
            return false;
        }
        usuarioActual = u;
        dao.registrarSesion(u.getId(), "127.0.0.1");
        dao.registrarAuditoria(u.getId(), "LOGIN_EXITOSO", "Sesion iniciada");
        return true;
    }

    public void logout() throws SQLException {
        if (usuarioActual != null) {
            dao.registrarAuditoria(usuarioActual.getId(), "LOGOUT", "Sesion cerrada");
        }
        usuarioActual = null;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public boolean isAdmin() {
        return usuarioActual != null && "ADMIN".equals(usuarioActual.getRol());
    }

    public boolean isLoggedIn() {
        return usuarioActual != null;
    }
}