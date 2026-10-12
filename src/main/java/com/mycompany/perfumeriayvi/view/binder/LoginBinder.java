package com.mycompany.perfumeriayvi.view.binder;

import com.mycompany.perfumeriayvi.seguridad.AccesoPublico;
import com.mycompany.perfumeriayvi.seguridad.CsrfToken;
import com.mycompany.perfumeriayvi.seguridad.EventosSeguridad;
import com.mycompany.perfumeriayvi.seguridad.UsuarioSesion;
import com.mycompany.perfumeriayvi.viewmodel.LoginViewModel;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Binder (capa View) de inicio y cierre de sesión. Vista: login.jsp.
 *
 * Requisitos:
 * - RF01 (CU01, HU01) iniciar sesión.
 * - RF02 (CU02, HU01) cerrar sesión (POST + CSRF).
 * - RNF-S3: al iniciar sesión se cambia el identificador de la sesión (evita la fijación de sesión).
 */
@Controller
public class LoginBinder {

    private final ObjectProvider<LoginViewModel> viewModels;
    private final EventosSeguridad eventos;

    public LoginBinder(ObjectProvider<LoginViewModel> viewModels, EventosSeguridad eventos) {
        this.viewModels = viewModels;
        this.eventos = eventos;
    }

    @AccesoPublico
    @GetMapping("/")
    public String raiz() {
        return "redirect:/index";
    }

    /** RF01: mostrar el formulario de inicio de sesión. */
    @AccesoPublico
    @GetMapping("/login")
    public String mostrar(@RequestParam(required = false) String logout,
                          @RequestParam(required = false) String expirada,
                          @RequestParam(required = false) String inactivo,
                          HttpSession sesion, Model model) {
        if (sesion.getAttribute(UsuarioSesion.ATRIBUTO) != null) {
            return "redirect:/index";
        }
        LoginViewModel vm = viewModels.getObject();
        vm.mostrar(logout != null, expirada != null, inactivo != null);
        model.addAttribute("vm", vm);
        return "login";
    }

    /** RF01: comando iniciar sesión. */
    @AccesoPublico
    @PostMapping("/login")
    public String iniciarSesion(@RequestParam(defaultValue = "") String username,
                                @RequestParam(defaultValue = "") String password,
                                HttpServletRequest request, Model model) {
        LoginViewModel vm = viewModels.getObject();
        if (!vm.iniciarSesion(username, password, request.getRemoteAddr())) {
            model.addAttribute("vm", vm);
            return "login";
        }

        // RNF-S3: nuevo ID de sesión y nuevo token CSRF después de autenticarse
        HttpSession sesion = request.getSession(true);
        request.changeSessionId();
        CsrfToken.renovar(sesion);
        sesion.setAttribute(UsuarioSesion.ATRIBUTO, UsuarioSesion.de(vm.getUsuarioAutenticado()));

        Object solicitada = sesion.getAttribute("urlSolicitada");
        sesion.removeAttribute("urlSolicitada");
        if (solicitada instanceof String destino && destino.startsWith("/") && !destino.startsWith("//")
                && !destino.startsWith("/login")) {
            return "redirect:" + destino;
        }
        return "redirect:/index";
    }

    /** RF02: comando cerrar sesión. */
    @PostMapping("/logout")
    public String cerrarSesion(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion != null) {
            UsuarioSesion usuario = (UsuarioSesion) sesion.getAttribute(UsuarioSesion.ATRIBUTO);
            eventos.logout(usuario == null ? null : usuario.getUsername());
            sesion.invalidate();
        }
        return "redirect:/login?logout";
    }
}
