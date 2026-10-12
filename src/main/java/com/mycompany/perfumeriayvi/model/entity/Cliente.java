package com.mycompany.perfumeriayvi.model.entity;

public class Cliente {

    private int idCliente;
    private String nombre;
    private String telefono;
    private String ciudad;

    private String tipoDocumento;
    private String numeroDocumento;
    private String email;
    private String barrio;

    // Dirección estructurada
    private String tipoVia;
    private String numeroVia;
    private String complemento;

    public Cliente() {
    }

    public Cliente(int idCliente, String nombre, String telefono, String ciudad,
                   String tipoDocumento, String numeroDocumento,
                   String email, String barrio,
                   String tipoVia, String numeroVia, String complemento) {

        this.idCliente = idCliente;
        this.nombre = nombre;
        this.telefono = telefono;
        this.ciudad = ciudad;

        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.email = email;
        this.barrio = barrio;

        this.tipoVia = tipoVia;
        this.numeroVia = numeroVia;
        this.complemento = complemento;
    }

    // =====================
    // GETTERS
    // =====================

    public int getIdCliente() {
        return idCliente;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getCiudad() {
        return ciudad;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public String getEmail() {
        return email;
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
    
    public String getDireccion() {
        StringBuilder dir = new StringBuilder();

        if (tipoVia != null) dir.append(tipoVia).append(" ");
        if (numeroVia != null) dir.append(numeroVia).append(" ");
        if (complemento != null) dir.append(complemento);

        return dir.toString().trim();
    }

    // =====================
    // SETTERS
    // =====================

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public void setEmail(String email) {
        this.email = email;
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
}