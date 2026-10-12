package com.mycompany.perfumeriayvi.seguridad;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Token anti-CSRF por sesión. Las vistas lo usan así:
 * {@code <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">}
 *
 * Requisito: RNF-S4.
 */
public class CsrfToken {

    public static final String ATRIBUTO_REQUEST = "_csrf";
    public static final String PARAMETRO = "_csrf";
    public static final String CABECERA = "X-CSRF-TOKEN";
    static final String ATRIBUTO_SESION = "CSRF_TOKEN";

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final HttpServletRequest request;

    CsrfToken(HttpServletRequest request) {
        this.request = request;
    }

    public String getParameterName() {
        return PARAMETRO;
    }

    /** Devuelve el token de la sesión; lo crea la primera vez. */
    public String getToken() {
        HttpSession sesion = request.getSession(true);
        String token = (String) sesion.getAttribute(ATRIBUTO_SESION);
        if (token == null) {
            token = nuevoToken();
            sesion.setAttribute(ATRIBUTO_SESION, token);
        }
        return token;
    }

    /** RNF-S3/RNF-S4: al iniciar sesión se genera un token nuevo. */
    public static void renovar(HttpSession sesion) {
        sesion.setAttribute(ATRIBUTO_SESION, nuevoToken());
    }

    static String nuevoToken() {
        byte[] bytes = new byte[32];
        ALEATORIO.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
