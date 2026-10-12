package com.mycompany.perfumeriayvi.viewmodel;

import com.mycompany.perfumeriayvi.model.entity.Usuario;
import com.mycompany.perfumeriayvi.model.service.AutenticacionService;
import com.mycompany.perfumeriayvi.model.service.AutenticacionService.ResultadoAutenticacion;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * ViewModel de la pantalla de inicio de sesión (login.jsp).
 *
 * Requisitos: RF01 (CU01, HU01) iniciar sesión; RF02 (CU02) mensaje de sesión cerrada;
 * RNF-S2 (mensajes de bloqueo); RNF-S3 (mensaje de sesión expirada).
 */
@Component
@Scope("prototype")
public class LoginViewModel extends ViewModelBase {

    private final AutenticacionService autenticacion;

    private String username;
    private Usuario usuarioAutenticado;

    public LoginViewModel(AutenticacionService autenticacion) {
        this.autenticacion = autenticacion;
    }

    /** Mensajes según el motivo por el que se llegó al login. */
    public void mostrar(boolean cerroSesion, boolean expirada, boolean inactivo) {
        if (cerroSesion) {
            aplicarMensajes("Cerró sesión correctamente.", null);
        } else if (expirada) {
            aplicarMensajes(null, "Su sesión expiró. Inicie sesión de nuevo.");
        } else if (inactivo) {
            aplicarMensajes(null, "Su cuenta fue bloqueada o desactivada. Contacte al administrador.");
        }
    }

    /** RF01: comando de inicio de sesión. */
    public boolean iniciarSesion(String username, String contrasena, String ip) {
        this.username = username;
        ResultadoAutenticacion resultado = autenticacion.autenticar(username, contrasena, ip);
        if (resultado.exitoso()) {
            this.usuarioAutenticado = resultado.usuario();
            return true;
        }
        setError(resultado.mensaje());
        return false;
    }

    public String getUsername() { return username; }

    public Usuario getUsuarioAutenticado() { return usuarioAutenticado; }
}
