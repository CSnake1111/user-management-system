package com.gestor.model;

import java.time.OffsetDateTime;

public class Usuario {

    private int id;
    private String nombre;
    private String apellido;
    private String email;
    private String passwordHash;
    private String rol;
    private boolean activo;
    private String tokenReset;
    private OffsetDateTime tokenExpiry;
    private OffsetDateTime creadoEn;
    private OffsetDateTime actualizadoEn;

    public Usuario() {}

    public Usuario(String nombre, String apellido, String email, String passwordHash, String rol) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.activo = true;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public String getTokenReset() { return tokenReset; }
    public void setTokenReset(String tokenReset) { this.tokenReset = tokenReset; }

    public OffsetDateTime getTokenExpiry() { return tokenExpiry; }
    public void setTokenExpiry(OffsetDateTime tokenExpiry) { this.tokenExpiry = tokenExpiry; }

    public OffsetDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(OffsetDateTime creadoEn) { this.creadoEn = creadoEn; }

    public OffsetDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(OffsetDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }

    @Override
    public String toString() {
        return String.format("ID:%-4d | %-15s %-15s | %-30s | %-7s | %s",
                id, nombre, apellido, email, rol, activo ? "Activo" : "Inactivo");
    }
}
