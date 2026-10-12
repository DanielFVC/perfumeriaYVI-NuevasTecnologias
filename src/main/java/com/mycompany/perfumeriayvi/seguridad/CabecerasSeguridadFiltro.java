package com.mycompany.perfumeriayvi.seguridad;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Cabeceras HTTP de seguridad en todas las respuestas.
 *
 * Requisito: RNF-S5.
 * - X-Frame-Options / frame-ancestors: impide que la app se cargue dentro de otra página (clickjacking).
 * - X-Content-Type-Options: el navegador no adivina tipos de archivo.
 * - Content-Security-Policy: solo se cargan scripts y estilos propios o de los CDN usados por las vistas.
 * - Cache-Control: las páginas con datos no quedan guardadas en el navegador al cerrar sesión.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class CabecerasSeguridadFiltro extends OncePerRequestFilter {

    static final String CSP = "default-src 'self'; "
            + "script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; "
            + "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com https://fonts.googleapis.com; "
            + "font-src 'self' data: https://cdnjs.cloudflare.com https://cdn.jsdelivr.net https://fonts.gstatic.com; "
            + "img-src 'self' data:; "
            + "connect-src 'self'; "
            + "frame-ancestors 'none'; "
            + "form-action 'self'; "
            + "base-uri 'self'";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Referrer-Policy", "same-origin");
        response.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
        response.setHeader("Content-Security-Policy", CSP);

        String ruta = request.getRequestURI().substring(request.getContextPath().length());
        if (!ruta.startsWith("/imagenes/")) {
            response.setHeader("Cache-Control", "no-store");
            response.setHeader("Pragma", "no-cache");
        }

        chain.doFilter(request, response);
    }
}
