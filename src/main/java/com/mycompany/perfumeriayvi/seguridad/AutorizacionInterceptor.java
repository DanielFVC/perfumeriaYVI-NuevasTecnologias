package com.mycompany.perfumeriayvi.seguridad;

import com.mycompany.perfumeriayvi.model.entity.Usuario;
import com.mycompany.perfumeriayvi.model.repository.UsuarioRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Control de acceso de cada petición.
 *
 * Requisitos:
 * - RF01 (CU01): sin sesión iniciada solo se puede ver el login.
 * - RF03 (CU03, HU02): antes de ejecutar una acción se verifica el rol y el permiso del módulo
 *   ({@link RequiereAcceso}). Si no tiene permiso responde 403 y lo registra (RNF-S7).
 * - RF09/RF10: los datos del usuario se recargan en cada petición, así un bloqueo o un cambio
 *   de permisos hecho por el administrador se aplica de inmediato, sin esperar a que cierre sesión.
 */
@Component
public class AutorizacionInterceptor implements HandlerInterceptor {

    static final String URL_SOLICITADA = "urlSolicitada";

    private final UsuarioRepository usuarios;
    private final EventosSeguridad eventos;
    private final Clock reloj;

    public AutorizacionInterceptor(UsuarioRepository usuarios, EventosSeguridad eventos, Clock reloj) {
        this.usuarios = usuarios;
        this.eventos = eventos;
        this.reloj = reloj;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        if (!(handler instanceof HandlerMethod metodo)) {
            return true; // recursos estáticos
        }
        if (esPublico(metodo)) {
            return true;
        }

        HttpSession sesion = request.getSession(false);
        UsuarioSesion actual = sesion == null ? null : (UsuarioSesion) sesion.getAttribute(UsuarioSesion.ATRIBUTO);

        // RF01: no hay sesión iniciada
        if (actual == null) {
            if ("GET".equals(request.getMethod())) {
                String destino = request.getRequestURI().substring(request.getContextPath().length());
                if (request.getQueryString() != null) {
                    destino += "?" + request.getQueryString();
                }
                request.getSession(true).setAttribute(URL_SOLICITADA, destino);
            }
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        // RF03 + RF09: se recargan rol, permisos y estado desde la base de datos
        Optional<Usuario> enBd = usuarios.buscarPorId(actual.getId());
        if (enBd.isEmpty() || !enBd.get().isActive() || enBd.get().estaBloqueado(LocalDateTime.now(reloj))) {
            sesion.invalidate();
            response.sendRedirect(request.getContextPath() + "/login?inactivo");
            return false;
        }
        actual = UsuarioSesion.de(enBd.get());
        sesion.setAttribute(UsuarioSesion.ATRIBUTO, actual);

        // RF03: permiso del módulo
        RequiereAcceso requerido = metodo.getMethodAnnotation(RequiereAcceso.class);
        if (requerido == null) {
            requerido = metodo.getBeanType().getAnnotation(RequiereAcceso.class);
        }
        if (requerido != null && !actual.puedeAcceder(requerido.value())) {
            eventos.accesoDenegado(actual.getUsername(), request.getRequestURI());
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "No tiene permiso para acceder al módulo " + requerido.value().name().toLowerCase() + ".");
            return false;
        }
        return true;
    }

    private boolean esPublico(HandlerMethod metodo) {
        return metodo.hasMethodAnnotation(AccesoPublico.class)
                || metodo.getBeanType().isAnnotationPresent(AccesoPublico.class);
    }
}
