package com.mycompany.perfumeriayvi.view.binder;

import com.mycompany.perfumeriayvi.model.entity.Cliente;
import com.mycompany.perfumeriayvi.seguridad.Modulo;
import com.mycompany.perfumeriayvi.seguridad.RequiereAcceso;
import com.mycompany.perfumeriayvi.seguridad.UsuarioSesion;
import com.mycompany.perfumeriayvi.viewmodel.ClienteViewModel;

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
 * Binder (capa View) del módulo de clientes. Vistas: ver_clientes.jsp, registrar_cliente.jsp, actualizar_cliente.jsp.
 *
 * Solo conecta la petición HTTP con los comandos de {@link ClienteViewModel}; no tiene lógica de negocio.
 * Requisitos: RF04 (CU04, HU03); RF03 (CU03) permiso del módulo CLIENTES; RNF-S4 cambios solo por POST con CSRF.
 */
@Controller
@RequestMapping("/clientes")
@RequiereAcceso(Modulo.CLIENTES)
public class ClienteBinder {

    private static final String MENSAJE = "mensaje";
    private static final String ERROR = "error";

    private final ObjectProvider<ClienteViewModel> viewModels;

    public ClienteBinder(ObjectProvider<ClienteViewModel> viewModels) {
        this.viewModels = viewModels;
    }

    /** RF04 (CU04, HU03): consultar. */
    @GetMapping
    public String listar(Model model) {
        ClienteViewModel vm = viewModels.getObject();
        vm.cargarLista();
        vm.aplicarMensajes(model.getAttribute(MENSAJE), model.getAttribute(ERROR));
        model.addAttribute("vm", vm);
        return "ver_clientes";
    }

    /** RF04 (CU04, HU03): formulario de registro. */
    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        ClienteViewModel vm = viewModels.getObject();
        vm.prepararNuevo();
        model.addAttribute("vm", vm);
        return "registrar_cliente";
    }

    /** RF04 (CU04, HU03): registrar. */
    @PostMapping("/registrar")
    public String registrar(@ModelAttribute("datos") Cliente datos, BindingResult formato,
                            @SessionAttribute(UsuarioSesion.ATRIBUTO) UsuarioSesion usuario,
                            Model model, RedirectAttributes redireccion) {
        ClienteViewModel vm = viewModels.getObject();
        if (formato.hasErrors()) {
            vm.prepararNuevo();
            vm.rechazarFormulario("Revise los campos numéricos del formulario.");
        } else if (vm.registrar(datos, usuario.getUsername())) {
            redireccion.addFlashAttribute(MENSAJE, vm.getMensaje());
            return "redirect:/clientes";
        }
        model.addAttribute("vm", vm);
        return "registrar_cliente";
    }

    /** RF04 (CU04, HU03): formulario de edición. */
    @GetMapping("/editar")
    public String editar(@RequestParam("id") int id, Model model, RedirectAttributes redireccion) {
        ClienteViewModel vm = viewModels.getObject();
        if (!vm.cargarParaEditar(id)) {
            redireccion.addFlashAttribute(ERROR, vm.getError());
            return "redirect:/clientes";
        }
        model.addAttribute("vm", vm);
        return "actualizar_cliente";
    }

    /** RF04 (CU04, HU03): actualizar. */
    @PostMapping("/actualizar")
    public String actualizar(@ModelAttribute("datos") Cliente datos, BindingResult formato,
                             @SessionAttribute(UsuarioSesion.ATRIBUTO) UsuarioSesion usuario,
                             Model model, RedirectAttributes redireccion) {
        ClienteViewModel vm = viewModels.getObject();
        if (formato.hasErrors()) {
            redireccion.addFlashAttribute(ERROR, "Revise los campos numéricos del formulario.");
            return "redirect:/clientes";
        }
        if (vm.actualizar(datos, usuario.getUsername())) {
            redireccion.addFlashAttribute(MENSAJE, vm.getMensaje());
            return "redirect:/clientes";
        }
        model.addAttribute("vm", vm);
        return "actualizar_cliente";
    }

    /** RF04 (CU04, HU03): eliminar (POST + CSRF, RNF-S4). */
    @PostMapping("/eliminar")
    public String eliminar(@RequestParam("id") int id,
                           @SessionAttribute(UsuarioSesion.ATRIBUTO) UsuarioSesion usuario,
                           RedirectAttributes redireccion) {
        ClienteViewModel vm = viewModels.getObject();
        if (vm.eliminar(id, usuario.getUsername())) {
            redireccion.addFlashAttribute(MENSAJE, vm.getMensaje());
        } else {
            redireccion.addFlashAttribute(ERROR, vm.getError());
        }
        return "redirect:/clientes";
    }
}
