package com.mycompany.perfumeriayvi.model.service;

import com.mycompany.perfumeriayvi.model.entity.Usuario;
import com.mycompany.perfumeriayvi.model.repository.UsuarioRepository;
import com.mycompany.perfumeriayvi.seguridad.CifradoContrasena;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Pruebas de RF09 (CU09), RNF-S1 y RNF-S6: creación de vendedores y política de contraseñas. */
class UsuarioServiceTest {

    private UsuarioRepository repositorio;
    private UsuarioService servicio;
    private final CifradoContrasena cifrado = new CifradoContrasena();

    @BeforeEach
    void preparar() {
        repositorio = mock(UsuarioRepository.class);
        servicio = new UsuarioService(repositorio, cifrado);
    }

    @Test
    void creaVendedorConHashYSinPermisos() {
        servicio.crearVendedor("Ana Ruiz", "ana.ruiz", "Clave2026x", "admin");

        ArgumentCaptor<Usuario> u = ArgumentCaptor.forClass(Usuario.class);
        verify(repositorio).registrar(u.capture(), eq("admin"));
        assertEquals(Usuario.ROL_VENDEDOR, u.getValue().getRole());
        assertNotEquals("Clave2026x", u.getValue().getPasswordHash());
        assertTrue(cifrado.coincide("Clave2026x", u.getValue().getPasswordHash()));
        assertFalse(u.getValue().isAccesoClientes());
    }

    @Test
    void politicaDeContrasenas() {
        assertThrows(ReglaNegocioException.class, () -> servicio.validarContrasena("Corta1"));
        assertThrows(ReglaNegocioException.class, () -> servicio.validarContrasena("sinmayuscula1"));
        assertThrows(ReglaNegocioException.class, () -> servicio.validarContrasena("SINMINUSCULA1"));
        assertThrows(ReglaNegocioException.class, () -> servicio.validarContrasena("SinNumeros"));
        assertDoesNotThrow(() -> servicio.validarContrasena("Valida2026"));
    }

    @Test
    void rechazaUsuarioDuplicadoOInvalido() {
        when(repositorio.existeUsername("ana.ruiz")).thenReturn(true);
        assertThrows(ReglaNegocioException.class,
                () -> servicio.crearVendedor("Ana Ruiz", "ana.ruiz", "Clave2026x", "admin"));
        assertThrows(ReglaNegocioException.class,
                () -> servicio.crearVendedor("Ana Ruiz", "a b", "Clave2026x", "admin"));
    }
}
