package com.mycompany.perfumeriayvi.model.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Cabecera de una venta (tabla venta) con sus líneas de detalle.
 */
public class Venta {

    private int id;
    private String fecha;
    private int idCliente;
    private String cliente;
    private double total;
    private int idUser;
    private String vendedor;

    private List<DetalleVenta> detalles = new ArrayList<>();

    public Venta() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }

    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public int getIdUser() { return idUser; }
    public void setIdUser(int idUser) { this.idUser = idUser; }

    public String getVendedor() { return vendedor; }
    public void setVendedor(String vendedor) { this.vendedor = vendedor; }

    public List<DetalleVenta> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleVenta> detalles) { this.detalles = detalles; }
}
