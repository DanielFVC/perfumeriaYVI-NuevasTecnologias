package com.mycompany.perfumeriayvi.view.binder;

import com.mycompany.perfumeriayvi.seguridad.Modulo;
import com.mycompany.perfumeriayvi.seguridad.RequiereAcceso;
import com.mycompany.perfumeriayvi.seguridad.UsuarioSesion;
import com.mycompany.perfumeriayvi.viewmodel.UsuarioViewModel;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Binder (capa View) de gestión de vendedores. Vista: usuarios.jsp.
 *
 * Requisitos: RF09 (CU09, HU08), RF10 (CU10, HU02); RF03: solo ADMIN (módulo USUARIOS);
 * RNF-S4: bloquear/activar ahora es POST con CSRF (antes era un enlace GET).
 */
@Controller
@RequestMapping("/usuarios")
@RequiereAcceso(Modulo.USUARIOS)
public class UsuarioBinder {

    private final ObjectProvider<UsuarioViewModel> viewModels;

    public UsuarioBinder(ObjectProvider<UsuarioViewModel> viewModels) {
        this.viewModels = viewModels;
    }

    /** RF09: listado de vendedores. */
    @GetMapping
    public String listar(Model model) {
        UsuarioViewModel vm = viewModels.getObject();
        vm.cargarVendedores();
        vm.aplicarMensajes(model.getAttribute("mensaje"), model.getAttribute("error"));
        model.addAttribute("vm", vm);
        return "usuarios";
    }

    /** RF09: crear vendedor. */
    @PostMapping("/crear")
    public String crear(@RequestParam("full_name") String nombre,
                        @RequestParam("username") String username,
                        @RequestParam("password") String password,
                        @SessionAttribute(UsuarioSesion.ATRIBUTO) UsuarioSesion actor,
                        Model model, RedirectAttributes redireccion) {
        UsuarioViewModel vm = viewModels.getObject();
        if (vm.crear(nombre, username, password, actor.getUsername())) {
            redireccion.addFlashAttribute("mensaje", vm.getMensaje());
            return "redirect:/usuarios";
        }
        vm.cargarVendedores();
        model.addAttribute("vm", vm);
        return "usuarios";
    }

    /** RF09: bloquear o activar vendedor. */
    @PostMapping("/estado")
    public String cambiarEstado(@RequestParam("id") int id,
                                @RequestParam("activo") boolean activo,
                                @SessionAttribute(UsuarioSesion.ATRIBUTO) UsuarioSesion actor,
                                RedirectAttributes redireccion) {
        UsuarioViewModel vm = viewModels.getObject();
        vm.cambiarEstado(id, activo, actor.getUsername());
        redireccion.addFlashAttribute(vm.getError() == null ? "mensaje" : "error",
                vm.getError() == null ? vm.getMensaje() : vm.getError());
        return "redirect:/usuarios";
    }

    /** RF10: asignar permisos por módulo. */
    @PostMapping("/permisos")
    public String permisos(@RequestParam("id") int id,
                           @RequestParam(name = "acceso_clientes", defaultValue = "false") boolean clientes,
                           @RequestParam(name = "acceso_productos", defaultValue = "false") boolean productos,
                           @RequestParam(name = "acceso_proveedores", defaultValue = "false") boolean proveedores,
                           @RequestParam(name = "acceso_ventas", defaultValue = "false") boolean ventas,
                           @SessionAttribute(UsuarioSesion.ATRIBUTO) UsuarioSesion actor,
                           RedirectAttributes redireccion) {
        UsuarioViewModel vm = viewModels.getObject();
        vm.actualizarPermisos(id, clientes, productos, proveedores, ventas, actor.getUsername());
        redireccion.addFlashAttribute(vm.getError() == null ? "mensaje" : "error",
                vm.getError() == null ? vm.getMensaje() : vm.getError());
        return "redirect:/usuarios";
    }
}
