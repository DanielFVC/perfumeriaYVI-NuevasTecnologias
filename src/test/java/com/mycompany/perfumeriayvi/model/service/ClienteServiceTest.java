package com.mycompany.perfumeriayvi.model.service;

import com.mycompany.perfumeriayvi.model.entity.Cliente;
import com.mycompany.perfumeriayvi.model.repository.ClienteRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Pruebas de RF04 (CU04): reglas de negocio de clientes, RNF-F2 y RNF-F3. */
class ClienteServiceTest {

    private ClienteRepository repositorio;
    private ClienteService servicio;

    @BeforeEach
    void preparar() {
        repositorio = mock(ClienteRepository.class);
        servicio = new ClienteService(repositorio);
    }

    static Cliente clienteValido() {
        Cliente c = new Cliente();
        c.setNombre("Laura Gómez");
        c.setTipoDocumento("CC");
        c.setNumeroDocumento("1098765432");
        c.setTelefono("3201234567");
        c.setEmail("laura@example.com");
        c.setCiudad("Bucaramanga");
        c.setTipoVia("Calle");
        return c;
    }

    @Test
    void registraUnClienteValido() {
        Cliente c = clienteValido();
        servicio.registrar(c, "admin");
        verify(repositorio).registrar(c, "admin");
    }

    @Test
    void rechazaNombreCorto() {
        Cliente c = clienteValido();
        c.setNombre("Al");
        ReglaNegocioException e = assertThrows(ReglaNegocioException.class, () -> servicio.registrar(c, "admin"));
        assertTrue(e.getMessage().contains("al menos 3"));
        verify(repositorio, never()).registrar(any(), any());
    }

    @Test
    void rechazaDocumentoConLetras() {
        Cliente c = clienteValido();
        c.setNumeroDocumento("12AB");
        assertThrows(ReglaNegocioException.class, () -> servicio.registrar(c, "admin"));
    }

    @Test
    void rechazaDocumentoDuplicado() {
        when(repositorio.existeDocumento("1098765432", 0)).thenReturn(true);
        ReglaNegocioException e = assertThrows(ReglaNegocioException.class,
                () -> servicio.registrar(clienteValido(), "admin"));
        assertTrue(e.getMessage().contains("Ya existe"));
    }

    @Test
    void actualizarTambienValida() {
        Cliente c = clienteValido();
        c.setIdCliente(5);
        c.setTelefono("12");
        assertThrows(ReglaNegocioException.class, () -> servicio.actualizar(c, "admin"));
        verify(repositorio, never()).actualizar(any(), any());
    }

    @Test
    void noEliminaClienteConVentas() {
        doThrow(new DataIntegrityViolationException("fk")).when(repositorio).eliminar(3, "admin");
        ReglaNegocioException e = assertThrows(ReglaNegocioException.class, () -> servicio.eliminar(3, "admin"));
        assertTrue(e.getMessage().contains("tiene ventas"));
    }
}
