package com.mycompany.perfumeriayvi.model.service;

import com.mycompany.perfumeriayvi.model.entity.Usuario;
import com.mycompany.perfumeriayvi.model.repository.UsuarioRepository;
import com.mycompany.perfumeriayvi.model.service.AutenticacionService.ResultadoAutenticacion;
import com.mycompany.perfumeriayvi.seguridad.CifradoContrasena;
import com.mycompany.perfumeriayvi.seguridad.EventosSeguridad;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Pruebas de RF01 (CU01), RNF-S1 y RNF-S2: autenticación y bloqueo por intentos. */
class AutenticacionServiceTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-10-07T15:00:00Z"), ZONA);
    private static final LocalDateTime AHORA = LocalDateTime.now(RELOJ);

    private final CifradoContrasena cifrado = new CifradoContrasena();
    private UsuarioRepository usuarios;
    private AutenticacionService servicio;
    private Usuario vendedor;

    @BeforeEach
    void preparar() {
        usuarios = mock(UsuarioRepository.class);
        servicio = new AutenticacionService(usuarios, cifrado, mock(EventosSeguridad.class), RELOJ);
        vendedor = new Usuario();
        vendedor.setId(2);
        vendedor.setUsername("ana.vendedora");
        vendedor.setRole(Usuario.ROL_VENDEDOR);
        vendedor.setActive(true);
        vendedor.setPasswordHash(cifrado.cifrar("Vendedor2026*"));
        when(usuarios.buscarPorUsername("ana.vendedora")).thenReturn(Optional.of(vendedor));
    }

    @Test
    void autenticaConCredencialesCorrectasYReiniciaIntentos() {
        ResultadoAutenticacion r = servicio.autenticar("ana.vendedora", "Vendedor2026*", "127.0.0.1");
        assertTrue(r.exitoso());
        assertSame(vendedor, r.usuario());
        verify(usuarios).registrarAccesoExitoso(2, "ana.vendedora", AHORA);
    }

    @Test
    void usuarioInexistenteRecibeMensajeGenerico() {
        ResultadoAutenticacion r = servicio.autenticar("nadie", "x", "127.0.0.1");
        assertFalse(r.exitoso());
        assertEquals(AutenticacionService.CREDENCIALES_INVALIDAS, r.mensaje());
    }

    @Test
    void contrasenaIncorrectaSumaUnIntento() {
        vendedor.setIntentosFallidos(1);
        ResultadoAutenticacion r = servicio.autenticar("ana.vendedora", "otra", "127.0.0.1");
        assertFalse(r.exitoso());
        assertTrue(r.mensaje().contains("Le quedan 3"));
        verify(usuarios).registrarIntentoFallido(2, "ana.vendedora", 2, null);
    }

    @Test
    void elQuintoIntentoFallidoBloqueaQuinceMinutos() {
        vendedor.setIntentosFallidos(4);
        ResultadoAutenticacion r = servicio.autenticar("ana.vendedora", "otra", "127.0.0.1");
        assertFalse(r.exitoso());
        assertTrue(r.mensaje().contains("bloqueada"));
        verify(usuarios).registrarIntentoFallido(2, "ana.vendedora", 0, AHORA.plusMinutes(15));
    }

    @Test
    void cuentaBloqueadaNoEntraNiConLaContrasenaCorrecta() {
        vendedor.setBloqueadoHasta(AHORA.plusMinutes(10));
        ResultadoAutenticacion r = servicio.autenticar("ana.vendedora", "Vendedor2026*", "127.0.0.1");
        assertFalse(r.exitoso());
        assertTrue(r.mensaje().contains("bloqueada temporalmente"));
        verify(usuarios, never()).registrarAccesoExitoso(anyInt(), any(), any());
    }

    @Test
    void cuentaInactivaNoEntra() {
        vendedor.setActive(false);
        ResultadoAutenticacion r = servicio.autenticar("ana.vendedora", "Vendedor2026*", "127.0.0.1");
        assertFalse(r.exitoso());
        assertTrue(r.mensaje().contains("inactivo"));
    }
}
