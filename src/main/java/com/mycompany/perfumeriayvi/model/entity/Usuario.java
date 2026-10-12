package com.mycompany.perfumeriayvi.model.entity;

import java.time.LocalDateTime;

/**
 * Usuario del sistema (tabla users).
 * Roles: ADMIN o VENDEDOR. Los permisos por módulo solo aplican al VENDEDOR.
 *
 * Requisitos: RF01 (CU01) autenticación, RF03 (CU03) autorización,
 * RF09 (CU09) gestión de vendedores, RF10 (CU10) permisos,
 * RNF-S2 (intentosFallidos / bloqueadoHasta).
 */
public class Usuario {

    public static final String ROL_ADMIN = "ADMIN";
    public static final String ROL_VENDEDOR = "VENDEDOR";

    private int id;
    private String username;
    private String fullName;
    private String role;
    private boolean active;
    private String passwordHash;

    private boolean accesoClientes;
    private boolean accesoProductos;
    private boolean accesoProveedores;
    private boolean accesoVentas;

    // RNF-S2: control de intentos fallidos y bloqueo temporal
    private int intentosFallidos;
    private LocalDateTime bloqueadoHasta;
    private LocalDateTime ultimoAcceso;

    public Usuario() {
    }

    public boolean isAdmin() {
        return ROL_ADMIN.equals(role);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public boolean isAccesoClientes() { return accesoClientes; }
    public void setAccesoClientes(boolean accesoClientes) { this.accesoClientes = accesoClientes; }

    public boolean isAccesoProductos() { return accesoProductos; }
    public void setAccesoProductos(boolean accesoProductos) { this.accesoProductos = accesoProductos; }

    public boolean isAccesoProveedores() { return accesoProveedores; }
    public void setAccesoProveedores(boolean accesoProveedores) { this.accesoProveedores = accesoProveedores; }

    public boolean isAccesoVentas() { return accesoVentas; }
    public void setAccesoVentas(boolean accesoVentas) { this.accesoVentas = accesoVentas; }

    public int getIntentosFallidos() { return intentosFallidos; }
    public void setIntentosFallidos(int intentosFallidos) { this.intentosFallidos = intentosFallidos; }

    public LocalDateTime getBloqueadoHasta() { return bloqueadoHasta; }
    public void setBloqueadoHasta(LocalDateTime bloqueadoHasta) { this.bloqueadoHasta = bloqueadoHasta; }

    public LocalDateTime getUltimoAcceso() { return ultimoAcceso; }
    public void setUltimoAcceso(LocalDateTime ultimoAcceso) { this.ultimoAcceso = ultimoAcceso; }

    /** RNF-S2: true si la cuenta sigue bloqueada temporalmente en el instante indicado. */
    public boolean estaBloqueado(LocalDateTime ahora) {
        return bloqueadoHasta != null && bloqueadoHasta.isAfter(ahora);
    }
}
