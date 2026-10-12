package com.mycompany.perfumeriayvi.model.service;

import com.mycompany.perfumeriayvi.model.entity.Proveedor;
import com.mycompany.perfumeriayvi.model.repository.ProveedorRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Pruebas de RF05 (CU05): validaciones de proveedores. */
class ProveedorServiceTest {

    private ProveedorRepository repositorio;
    private ProveedorService servicio;

    @BeforeEach
    void preparar() {
        repositorio = mock(ProveedorRepository.class);
        servicio = new ProveedorService(repositorio);
    }

    static Proveedor proveedor() {
        Proveedor p = new Proveedor();
        p.setNombre("Fragancias del Oriente S.A.S.");
        p.setNit("900123456-7");
        p.setTelefono("6076543210");
        p.setEmail("ventas@fraganciasoriente.example");
        p.setCiudad("Bucaramanga");
        p.setTipoVia("Carrera");
        p.setNumeroVia("27");
        p.setTipoProveedor("Perfumes");
        return p;
    }

    @Test
    void registraProveedorValido() {
        Proveedor p = proveedor();
        servicio.registrar(p, "admin");
        verify(repositorio).registrar(p, "admin");
    }

    @Test
    void rechazaCorreoInvalido() {
        Proveedor p = proveedor();
        p.setEmail("sin-arroba");
        assertThrows(ReglaNegocioException.class, () -> servicio.registrar(p, "admin"));
    }

    @Test
    void rechazaNitDuplicado() {
        when(repositorio.existeNit("900123456-7", 0)).thenReturn(true);
        assertThrows(ReglaNegocioException.class, () -> servicio.registrar(proveedor(), "admin"));
    }

    @Test
    void rechazaTipoDeProveedorDesconocido() {
        Proveedor p = proveedor();
        p.setTipoProveedor("Otro");
        assertThrows(ReglaNegocioException.class, () -> servicio.registrar(p, "admin"));
    }
}
