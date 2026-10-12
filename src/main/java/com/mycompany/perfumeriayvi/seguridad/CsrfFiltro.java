package com.mycompany.perfumeriayvi.seguridad;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Set;

/**
 * Protección contra falsificación de peticiones entre sitios (CSRF).
 *
 * Requisito: RNF-S4. Toda petición que modifica datos (POST, PUT, PATCH, DELETE)
 * debe traer el token de la sesión; si no coincide se rechaza con 403 y se registra (RNF-S7).
 * Por eso eliminar, bloquear y activar ahora se hacen con formularios POST y no con enlaces GET.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class CsrfFiltro extends OncePerRequestFilter {

    private static final Set<String> METODOS_SEGUROS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");

    private final EventosSeguridad eventos;

    public CsrfFiltro(EventosSeguridad eventos) {
        this.eventos = eventos;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        request.setAttribute(CsrfToken.ATRIBUTO_REQUEST, new CsrfToken(request));

        if (METODOS_SEGUROS.contains(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession sesion = request.getSession(false);
        String esperado = sesion == null ? null : (String) sesion.getAttribute(CsrfToken.ATRIBUTO_SESION);
        String recibido = request.getHeader(CsrfToken.CABECERA);
        if (recibido == null) {
            recibido = request.getParameter(CsrfToken.PARAMETRO);
        }

        if (esperado == null) {
            // RNF-S3: la sesión expiró (por ejemplo, el formulario quedó abierto más de 30 min)
            eventos.csrfInvalido(request.getRequestURI(), request.getRemoteAddr());
            response.sendRedirect(request.getContextPath() + "/login?expirada");
            return;
        }
        if (recibido == null || !iguales(esperado, recibido)) {
            eventos.csrfInvalido(request.getRequestURI(), request.getRemoteAddr());
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "La solicitud no es válida o la sesión expiró. Recargue la página e intente de nuevo.");
            return;
        }

        chain.doFilter(request, response);
    }

    /** Comparación en tiempo constante para no filtrar información por el tiempo de respuesta. */
    static boolean iguales(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }
}
