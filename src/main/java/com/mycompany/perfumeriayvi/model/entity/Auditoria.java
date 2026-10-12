package com.mycompany.perfumeriayvi.model.entity;



import java.sql.Timestamp;

public class Auditoria {
    private int idAuditoria;
    private Timestamp fecha;
    private String tablaAfectada;
    private String operacion;
    private int idRegistro;
    private String usuario;
    private String detalle;

    public Auditoria() {} // constructor vacío (útil para frameworks)

    public Auditoria(int idAuditoria, Timestamp fecha, String tablaAfectada,
                     String operacion, int idRegistro, String usuario, String detalle) {
        this.idAuditoria = idAuditoria;
        this.fecha = fecha;
        this.tablaAfectada = tablaAfectada;
        this.operacion = operacion;
        this.idRegistro = idRegistro;
        this.usuario = usuario;
        this.detalle = detalle;
    }

    // Getters
    public int getIdAuditoria() { return idAuditoria; }
    public Timestamp getFecha() { return fecha; }
    public String getTablaAfectada() { return tablaAfectada; }
    public String getOperacion() { return operacion; }
    public int getIdRegistro() { return idRegistro; }
    public String getUsuario() { return usuario; }
    public String getDetalle() { return detalle; }

    // Setters
    public void setIdAuditoria(int idAuditoria) { this.idAuditoria = idAuditoria; }
    public void setFecha(Timestamp fecha) { this.fecha = fecha; }
    public void setTablaAfectada(String tablaAfectada) { this.tablaAfectada = tablaAfectada; }
    public void setOperacion(String operacion) { this.operacion = operacion; }
    public void setIdRegistro(int idRegistro) { this.idRegistro = idRegistro; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
}
