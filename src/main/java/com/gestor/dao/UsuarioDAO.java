package com.gestor.dao;

import com.gestor.config.DatabaseConfig;
import com.gestor.model.Usuario;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioDAO {

    public Usuario crear(Usuario u) throws SQLException {
        String sql = """
                INSERT INTO usuarios (nombre, apellido, email, password_hash, rol, activo)
                VALUES (?, ?, ?, ?, ?::rol_usuario, ?)
                RETURNING id, creado_en, actualizado_en
                """;
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getApellido());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getPasswordHash());
            ps.setString(5, u.getRol());
            ps.setBoolean(6, u.isActivo());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    u.setId(rs.getInt("id"));
                    u.setCreadoEn(rs.getObject("creado_en", OffsetDateTime.class));
                    u.setActualizadoEn(rs.getObject("actualizado_en", OffsetDateTime.class));
                }
            }
        }
        return u;
    }

    public List<Usuario> listarTodos() throws SQLException {
        String sql = "SELECT * FROM usuarios ORDER BY id";
        List<Usuario> lista = new ArrayList<>();
        try (Connection con = DatabaseConfig.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<Usuario> listarActivos() throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE activo = TRUE ORDER BY id";
        List<Usuario> lista = new ArrayList<>();
        try (Connection con = DatabaseConfig.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public Optional<Usuario> buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE id = ?";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapear(rs));
            }
        }
        return Optional.empty();
    }

    public Optional<Usuario> buscarPorEmail(String email) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE email = ?";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapear(rs));
            }
        }
        return Optional.empty();
    }

    public boolean actualizar(Usuario u) throws SQLException {
        String sql = """
                UPDATE usuarios
                SET nombre = ?, apellido = ?, email = ?, rol = ?::rol_usuario, activo = ?
                WHERE id = ?
                """;
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getApellido());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getRol());
            ps.setBoolean(5, u.isActivo());
            ps.setInt(6, u.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean actualizarPassword(int id, String nuevoHash) throws SQLException {
        String sql = "UPDATE usuarios SET password_hash = ? WHERE id = ?";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoHash);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM usuarios WHERE id = ?";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean desactivar(int id) throws SQLException {
        String sql = "UPDATE usuarios SET activo = FALSE WHERE id = ?";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean guardarTokenReset(int id, String token, OffsetDateTime expiry) throws SQLException {
        String sql = "UPDATE usuarios SET token_reset = ?, token_expiry = ? WHERE id = ?";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, token);
            ps.setObject(2, expiry);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean limpiarTokenReset(int id) throws SQLException {
        String sql = "UPDATE usuarios SET token_reset = NULL, token_expiry = NULL WHERE id = ?";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public void registrarAuditoria(Integer usuarioId, String accion, String detalle) throws SQLException {
        String sql = "INSERT INTO auditoria (usuario_id, accion, detalle) VALUES (?, ?, ?)";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (usuarioId != null) ps.setInt(1, usuarioId);
            else ps.setNull(1, Types.INTEGER);
            ps.setString(2, accion);
            ps.setString(3, detalle);
            ps.executeUpdate();
        }
    }

    public void registrarSesion(int usuarioId, String ip) throws SQLException {
        String sql = "INSERT INTO sesiones (usuario_id, ip) VALUES (?, ?)";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ps.setString(2, ip);
            ps.executeUpdate();
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id"));
        u.setNombre(rs.getString("nombre"));
        u.setApellido(rs.getString("apellido"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRol(rs.getString("rol"));
        u.setActivo(rs.getBoolean("activo"));
        u.setTokenReset(rs.getString("token_reset"));
        u.setTokenExpiry(rs.getObject("token_expiry", OffsetDateTime.class));
        u.setCreadoEn(rs.getObject("creado_en", OffsetDateTime.class));
        u.setActualizadoEn(rs.getObject("actualizado_en", OffsetDateTime.class));
        return u;
    }
}
