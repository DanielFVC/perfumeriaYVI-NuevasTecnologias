package com.mycompany.perfumeriayvi.model.entity;

public class Proveedor {

    private int idProveedor;
    private String nombre;
    private String nit;

    private String ciudad;
    private String barrio;

    private String tipoVia;
    private String numeroVia;
    private String complemento;

    private String tipoProveedor;

    private String telefono;
    private String email;

    public Proveedor() {
    }

    public Proveedor(int idProveedor, String nombre, String nit,
                     String ciudad, String barrio,
                     String tipoVia, String numeroVia, String complemento,
                     String tipoProveedor,
                     String telefono, String email) {

        this.idProveedor = idProveedor;
        this.nombre = nombre;
        this.nit = nit;
        this.ciudad = ciudad;
        this.barrio = barrio;
        this.tipoVia = tipoVia;
        this.numeroVia = numeroVia;
        this.complemento = complemento;
        this.tipoProveedor = tipoProveedor;
        this.telefono = telefono;
        this.email = email;
    }

    // =========================
    // GETTERS
    // =========================

    public int getIdProveedor() {
        return idProveedor;
    }

    public String getNombre() {
        return nombre;
    }

    public String getNit() {
        return nit;
    }

    public String getCiudad() {
        return ciudad;
    }

    public String getBarrio() {
        return barrio;
    }

    public String getTipoVia() {
        return tipoVia;
    }

    public String getNumeroVia() {
        return numeroVia;
    }

    public String getComplemento() {
        return complemento;
    }

    public String getTipoProveedor() {
        return tipoProveedor;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getEmail() {
        return email;
    }

    // =========================
    // SETTERS
    // =========================

    public void setIdProveedor(int idProveedor) {
        this.idProveedor = idProveedor;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public void setBarrio(String barrio) {
        this.barrio = barrio;
    }

    public void setTipoVia(String tipoVia) {
        this.tipoVia = tipoVia;
    }

    public void setNumeroVia(String numeroVia) {
        this.numeroVia = numeroVia;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public void setTipoProveedor(String tipoProveedor) {
        this.tipoProveedor = tipoProveedor;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // =========================
    // DIRECCIÓN COMPLETA
    // =========================

    public String getDireccion() {

        StringBuilder direccion = new StringBuilder();

        if (tipoVia != null) {
            direccion.append(tipoVia);
        }

        if (numeroVia != null && !numeroVia.isBlank()) {
            direccion.append(" ").append(numeroVia);
        }

        if (complemento != null && !complemento.isBlank()) {
            direccion.append(" ").append(complemento);
        }

        return direccion.toString().trim();
    }
}
