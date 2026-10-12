package com.mycompany.perfumeriayvi.model.entity;

public class Producto {

    private int id;

    private String nombre;
    private String marca;
    private String tipo;
    private String categoria;
    private String descripcion;

    private double precioCompra;
    private double precioVenta;

    private int stock;
    private int stockMinimo;

    private int idProveedor;
    private String nombreProveedor;

    public Producto() {
    }

    public Producto(int id,
                    String nombre,
                    String marca,
                    String tipo,
                    String categoria,
                    String descripcion,
                    double precioCompra,
                    double precioVenta,
                    int stock,
                    int stockMinimo,
                    int idProveedor) {

        this.id = id;
        this.nombre = nombre;
        this.marca = marca;
        this.tipo = tipo;
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.precioCompra = precioCompra;
        this.precioVenta = precioVenta;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
        this.idProveedor = idProveedor;
    }

    // =========================
    // GETTERS
    // =========================

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getMarca() {
        return marca;
    }

    public String getTipo() {
        return tipo;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public double getPrecioCompra() {
        return precioCompra;
    }

    public double getPrecioVenta() {
        return precioVenta;
    }

    public int getStock() {
        return stock;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public int getIdProveedor() {
        return idProveedor;
    }
    
    public String getNombreProveedor() {
        return nombreProveedor;
    }

    // =========================
    // SETTERS
    // =========================

    public void setId(int id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setPrecioCompra(double precioCompra) {
        this.precioCompra = precioCompra;
    }

    public void setPrecioVenta(double precioVenta) {
        this.precioVenta = precioVenta;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public void setIdProveedor(int idProveedor) {
        this.idProveedor = idProveedor;
    }
    
    public void setNombreProveedor(String nombreProveedor) {
        this.nombreProveedor = nombreProveedor;
    }

    // =========================
    // UTILIDAD
    // =========================

    public double getUtilidad() {
        return precioVenta - precioCompra;
    }
}