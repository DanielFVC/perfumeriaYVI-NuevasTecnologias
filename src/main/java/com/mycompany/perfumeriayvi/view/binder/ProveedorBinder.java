package com.mycompany.perfumeriayvi.view.binder;

import com.mycompany.perfumeriayvi.model.entity.Proveedor;
import com.mycompany.perfumeriayvi.seguridad.Modulo;
import com.mycompany.perfumeriayvi.seguridad.RequiereAcceso;
import com.mycompany.perfumeriayvi.seguridad.UsuarioSesion;
import com.mycompany.perfumeriayvi.viewmodel.ProveedorViewModel;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Binder (capa View) del módulo de proveedores. Vistas: ver_proveedores.jsp, registrar_proveedores.jsp, actualizar_proveedores.jsp.
 *
 * Solo conecta la petición HTTP con los comandos de {@link ProveedorViewModel}; no tiene lógica de negocio.
 * Requisitos: RF05 (CU05, HU04); RF03 (CU03) permiso del módulo PROVEEDORES; RNF-S4 cambios solo por POST con CSRF.
 */
@Controller
@RequestMapping("/proveedores")
@RequiereAcceso(Modulo.PROVEEDORES)
public class ProveedorBinder {

    private static final String MENSAJE = "mensaje";
    private static final String ERROR = "error";

    private final ObjectProvider<ProveedorViewModel> viewModels;

    public ProveedorBinder(ObjectProvider<ProveedorViewModel> viewModels) {
        this.viewModels = viewModels;
    }

    /** RF05 (CU05, HU04): consultar. */
    @GetMapping
    public String listar(Model model) {
        ProveedorViewModel vm = viewModels.getObject();
        vm.cargarLista();
        vm.aplicarMensajes(model.getAttribute(MENSAJE), model.getAttribute(ERROR));
        model.addAttribute("vm", vm);
        return "ver_proveedores";
    }

    /** RF05 (CU05, HU04): formulario de registro. */
    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        ProveedorViewModel vm = viewModels.getObject();
        vm.prepararNuevo();
        model.addAttribute("vm", vm);
        return "registrar_proveedores";
    }

    /** RF05 (CU05, HU04): registrar. */
    @PostMapping("/registrar")
    public String registrar(@ModelAttribute("datos") Proveedor datos, BindingResult formato,
                            @SessionAttribute(UsuarioSesion.ATRIBUTO) UsuarioSesion usuario,
                            Model model, RedirectAttributes redireccion) {
        ProveedorViewModel vm = viewModels.getObject();
        if (formato.hasErrors()) {
            vm.prepararNuevo();
            vm.rechazarFormulario("Revise los campos numéricos del formulario.");
        } else if (vm.registrar(datos, usuario.getUsername())) {
            redireccion.addFlashAttribute(MENSAJE, vm.getMensaje());
            return "redirect:/proveedores";
        }
        model.addAttribute("vm", vm);
        return "registrar_proveedores";
    }

    /** RF05 (CU05, HU04): formulario de edición. */
    @GetMapping("/editar")
    public String editar(@RequestParam("id") int id, Model model, RedirectAttributes redireccion) {
        ProveedorViewModel vm = viewModels.getObject();
        if (!vm.cargarParaEditar(id)) {
            redireccion.addFlashAttribute(ERROR, vm.getError());
            return "redirect:/proveedores";
        }
        model.addAttribute("vm", vm);
        return "actualizar_proveedores";
    }

    /** RF05 (CU05, HU04): actualizar. */
    @PostMapping("/actualizar")
    public String actualizar(@ModelAttribute("datos") Proveedor datos, BindingResult formato,
                             @SessionAttribute(UsuarioSesion.ATRIBUTO) UsuarioSesion usuario,
                             Model model, RedirectAttributes redireccion) {
        ProveedorViewModel vm = viewModels.getObject();
        if (formato.hasErrors()) {
            redireccion.addFlashAttribute(ERROR, "Revise los campos numéricos del formulario.");
            return "redirect:/proveedores";
        }
        if (vm.actualizar(datos, usuario.getUsername())) {
            redireccion.addFlashAttribute(MENSAJE, vm.getMensaje());
            return "redirect:/proveedores";
        }
        model.addAttribute("vm", vm);
        return "actualizar_proveedores";
    }

    /** RF05 (CU05, HU04): eliminar (POST + CSRF, RNF-S4). */
    @PostMapping("/eliminar")
    public String eliminar(@RequestParam("id") int id,
                           @SessionAttribute(UsuarioSesion.ATRIBUTO) UsuarioSesion usuario,
                           RedirectAttributes redireccion) {
        ProveedorViewModel vm = viewModels.getObject();
        if (vm.eliminar(id, usuario.getUsername())) {
            redireccion.addFlashAttribute(MENSAJE, vm.getMensaje());
        } else {
            redireccion.addFlashAttribute(ERROR, vm.getError());
        }
        return "redirect:/proveedores";
    }
}
