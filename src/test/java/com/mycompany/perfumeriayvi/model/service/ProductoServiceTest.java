package com.mycompany.perfumeriayvi.model.service;

import com.mycompany.perfumeriayvi.model.entity.Producto;
import com.mycompany.perfumeriayvi.model.repository.ProductoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Pruebas de RF06 (CU06): validaciones de productos y alerta de stock bajo. */
class ProductoServiceTest {

    private ProductoRepository repositorio;
    private ProductoService servicio;

    @BeforeEach
    void preparar() {
        repositorio = mock(ProductoRepository.class);
        servicio = new ProductoService(repositorio);
    }

    static Producto producto(int stock, int minimo) {
        Producto p = new Producto();
        p.setNombre("Dior Sauvage");
        p.setMarca("Dior");
        p.setTipo("Perfume");
        p.setCategoria("Original");
        p.setDescripcion("Eau de toilette 100 ml");
        p.setPrecioCompra(200000);
        p.setPrecioVenta(320000);
        p.setStock(stock);
        p.setStockMinimo(minimo);
        p.setIdProveedor(1);
        return p;
    }

    @Test
    void registraProductoValido() {
        Producto p = producto(10, 3);
        servicio.registrar(p, "admin");
        verify(repositorio).registrar(p, "admin");
    }

    @Test
    void precioVentaNoPuedeSerMenorAlDeCompra() {
        Producto p = producto(10, 3);
        p.setPrecioVenta(100000);
        ReglaNegocioException e = assertThrows(ReglaNegocioException.class, () -> servicio.registrar(p, "admin"));
        assertTrue(e.getMessage().contains("precio de venta"));
    }

    @Test
    void stockNegativoNoPermitido() {
        assertThrows(ReglaNegocioException.class, () -> servicio.registrar(producto(-1, 3), "admin"));
    }

    @Test
    void cuentaProductosConStockBajo() {
        List<Producto> lista = List.of(producto(2, 5), producto(5, 5), producto(20, 5));
        assertEquals(2, servicio.contarStockBajo(lista));
        assertTrue(ProductoService.tieneStockBajo(producto(5, 5)));
        assertFalse(ProductoService.tieneStockBajo(producto(6, 5)));
    }
}
