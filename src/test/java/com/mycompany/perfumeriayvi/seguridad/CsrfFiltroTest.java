package com.mycompany.perfumeriayvi.seguridad;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/** Pruebas de RNF-S4: protección CSRF. */
class CsrfFiltroTest {

    private CsrfFiltro filtro;
    private MockHttpSession sesion;

    @BeforeEach
    void preparar() {
        filtro = new CsrfFiltro(mock(EventosSeguridad.class));
        sesion = new MockHttpSession();
        sesion.setAttribute(CsrfToken.ATRIBUTO_SESION, "token-correcto");
    }

    private MockHttpServletRequest post(String token) {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/clientes/eliminar");
        req.setSession(sesion);
        if (token != null) {
            req.addParameter("_csrf", token);
        }
        return req;
    }

    @Test
    void postConTokenValidoContinua() throws Exception {
        MockFilterChain cadena = new MockFilterChain();
        filtro.doFilter(post("token-correcto"), new MockHttpServletResponse(), cadena);
        assertNotNull(cadena.getRequest());
    }

    @Test
    void postSinTokenSeRechaza() throws Exception {
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain cadena = new MockFilterChain();
        filtro.doFilter(post(null), res, cadena);
        assertEquals(403, res.getStatus());
        assertNull(cadena.getRequest());
    }

    @Test
    void postConTokenFalsoSeRechaza() throws Exception {
        MockHttpServletResponse res = new MockHttpServletResponse();
        filtro.doFilter(post("token-falso"), res, new MockFilterChain());
        assertEquals(403, res.getStatus());
    }

    @Test
    void postConSesionVencidaVuelveAlLogin() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/ventas/registrar");
        MockHttpServletResponse res = new MockHttpServletResponse();
        filtro.doFilter(req, res, new MockFilterChain());
        assertEquals("/login?expirada", res.getRedirectedUrl());
    }

    @Test
    void getNoNecesitaTokenYPublicaElTokenParaLaVista() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/clientes");
        req.setSession(sesion);
        MockFilterChain cadena = new MockFilterChain();
        filtro.doFilter(req, new MockHttpServletResponse(), cadena);
        assertNotNull(cadena.getRequest());
        CsrfToken token = (CsrfToken) req.getAttribute("_csrf");
        assertEquals("token-correcto", token.getToken());
    }
}
