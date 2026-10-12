package com.mycompany.perfumeriayvi.seguridad;

import com.mycompany.perfumeriayvi.model.entity.Usuario;
import com.mycompany.perfumeriayvi.model.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.method.HandlerMethod;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Pruebas de RF01 y RF03 (CU03): control de acceso de cada petición. */
class AutorizacionInterceptorTest {

    @RequiereAcceso(Modulo.CLIENTES)
    static class BinderDeClientes {
        public String listar() { return "ok"; }
    }

    static class BinderPublico {
        @AccesoPublico
        public String login() { return "ok"; }
    }

    private UsuarioRepository usuarios;
    private EventosSeguridad eventos;
    private AutorizacionInterceptor interceptor;
    private HandlerMethod accionClientes;

    @BeforeEach
    void preparar() throws Exception {
        usuarios = mock(UsuarioRepository.class);
        eventos = mock(EventosSeguridad.class);
        interceptor = new AutorizacionInterceptor(usuarios, eventos, Clock.systemDefaultZone());
        accionClientes = new HandlerMethod(new BinderDeClientes(), BinderDeClientes.class.getMethod("listar"));
    }

    private MockHttpServletRequest peticionCon(Usuario u) {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/clientes");
        MockHttpSession sesion = new MockHttpSession();
        sesion.setAttribute(UsuarioSesion.ATRIBUTO, UsuarioSesion.de(u));
        req.setSession(sesion);
        when(usuarios.buscarPorId(u.getId())).thenReturn(Optional.of(u));
        return req;
    }

    @Test
    void sinSesionRedirigeAlLoginYGuardaLaRuta() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/clientes");
        MockHttpServletResponse res = new MockHttpServletResponse();
        assertFalse(interceptor.preHandle(req, res, accionClientes));
        assertEquals("/login", res.getRedirectedUrl());
        assertEquals("/clientes", req.getSession().getAttribute("urlSolicitada"));
    }

    @Test
    void elLoginEsPublico() throws Exception {
        HandlerMethod login = new HandlerMethod(new BinderPublico(), BinderPublico.class.getMethod("login"));
        assertTrue(interceptor.preHandle(new MockHttpServletRequest("GET", "/login"), new MockHttpServletResponse(), login));
    }

    @Test
    void vendedorSinPermisoRecibe403() throws Exception {
        Usuario v = UsuarioSesionTest.usuario(Usuario.ROL_VENDEDOR, false, true, true, true);
        MockHttpServletResponse res = new MockHttpServletResponse();
        assertFalse(interceptor.preHandle(peticionCon(v), res, accionClientes));
        assertEquals(403, res.getStatus());
        verify(eventos).accesoDenegado("u", "/clientes");
    }

    @Test
    void vendedorConPermisoPasa() throws Exception {
        Usuario v = UsuarioSesionTest.usuario(Usuario.ROL_VENDEDOR, true, false, false, false);
        assertTrue(interceptor.preHandle(peticionCon(v), new MockHttpServletResponse(), accionClientes));
    }

    @Test
    void permisoRetiradoSeAplicaDeInmediato() throws Exception {
        Usuario enSesion = UsuarioSesionTest.usuario(Usuario.ROL_VENDEDOR, true, false, false, false);
        MockHttpServletRequest req = peticionCon(enSesion);
        // el administrador le quitó el permiso mientras estaba conectado
        when(usuarios.buscarPorId(1)).thenReturn(Optional.of(
                UsuarioSesionTest.usuario(Usuario.ROL_VENDEDOR, false, false, false, false)));
        MockHttpServletResponse res = new MockHttpServletResponse();
        assertFalse(interceptor.preHandle(req, res, accionClientes));
        assertEquals(403, res.getStatus());
    }

    @Test
    void usuarioBloqueadoPierdeLaSesion() throws Exception {
        Usuario v = UsuarioSesionTest.usuario(Usuario.ROL_VENDEDOR, true, true, true, true);
        MockHttpServletRequest req = peticionCon(v);
        v.setActive(false);
        MockHttpServletResponse res = new MockHttpServletResponse();
        assertFalse(interceptor.preHandle(req, res, accionClientes));
        assertEquals("/login?inactivo", res.getRedirectedUrl());
    }

    @Test
    void usuarioConBloqueoTemporalPierdeLaSesion() throws Exception {
        Usuario v = UsuarioSesionTest.usuario(Usuario.ROL_VENDEDOR, true, true, true, true);
        MockHttpServletRequest req = peticionCon(v);
        v.setBloqueadoHasta(LocalDateTime.now().plusMinutes(5));
        MockHttpServletResponse res = new MockHttpServletResponse();
        assertFalse(interceptor.preHandle(req, res, accionClientes));
    }
}
