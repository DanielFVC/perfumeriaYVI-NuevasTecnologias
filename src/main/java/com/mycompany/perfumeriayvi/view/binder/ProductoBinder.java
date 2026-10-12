package com.mycompany.perfumeriayvi.view.binder;

import com.mycompany.perfumeriayvi.model.entity.Producto;
import com.mycompany.perfumeriayvi.seguridad.Modulo;
import com.mycompany.perfumeriayvi.seguridad.RequiereAcceso;
import com.mycompany.perfumeriayvi.seguridad.UsuarioSesion;
import com.mycompany.perfumeriayvi.viewmodel.ProductoViewModel;

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
 * Binder (capa View) del módulo de productos. Vistas: inventario_productos.jsp, registrar_productos.jsp, actualizar_productos.jsp.
 *
 * Solo conecta la petición HTTP con los comandos de {@link ProductoViewModel}; no tiene lógica de negocio.
 * Requisitos: RF06 (CU06, HU05); RF03 (CU03) permiso del módulo PRODUCTOS; RNF-S4 cambios solo por POST con CSRF.
 */
@Controller
@RequestMapping("/productos")
@RequiereAcceso(Modulo.PRODUCTOS)
public class ProductoBinder {

    private static final String MENSAJE = "mensaje";
    private static final String ERROR = "error";

    private final ObjectProvider<ProductoViewModel> viewModels;

    public ProductoBinder(ObjectProvider<ProductoViewModel> viewModels) {
        this.viewModels = viewModels;
    }

    /** RF06 (CU06, HU05): consultar inventario con alerta de stock bajo. */
    @GetMapping
    public String listar(Model model) {
        ProductoViewModel vm = viewModels.getObject();
        vm.cargarInventario();
        vm.aplicarMensajes(model.getAttribute(MENSAJE), model.getAttribute(ERROR));
        model.addAttribute("vm", vm);
        return "inventario_productos";
    }

    /** RF06 (CU06, HU05): formulario de registro. */
    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        ProductoViewModel vm = viewModels.getObject();
        vm.prepararNuevo();
        model.addAttribute("vm", vm);
        return "registrar_productos";
    }

    /** RF06 (CU06, HU05): registrar. */
    @PostMapping("/registrar")
    public String registrar(@ModelAttribute("datos") Producto datos, BindingResult formato,
                            @SessionAttribute(UsuarioSesion.ATRIBUTO) UsuarioSesion usuario,
                            Model model, RedirectAttributes redireccion) {
        ProductoViewModel vm = viewModels.getObject();
        if (formato.hasErrors()) {
            vm.prepararNuevo();
            vm.rechazarFormulario("Revise los campos numéricos del formulario.");
        } else if (vm.registrar(datos, usuario.getUsername())) {
            redireccion.addFlashAttribute(MENSAJE, vm.getMensaje());
            return "redirect:/productos";
        }
        model.addAttribute("vm", vm);
        return "registrar_productos";
    }

    /** RF06 (CU06, HU05): formulario de edición. */
    @GetMapping("/editar")
    public String editar(@RequestParam("id") int id, Model model, RedirectAttributes redireccion) {
        ProductoViewModel vm = viewModels.getObject();
        if (!vm.cargarParaEditar(id)) {
            redireccion.addFlashAttribute(ERROR, vm.getError());
            return "redirect:/productos";
        }
        model.addAttribute("vm", vm);
        return "actualizar_productos";
    }

    /** RF06 (CU06, HU05): actualizar. */
    @PostMapping("/actualizar")
    public String actualizar(@ModelAttribute("datos") Producto datos, BindingResult formato,
                             @SessionAttribute(UsuarioSesion.ATRIBUTO) UsuarioSesion usuario,
                             Model model, RedirectAttributes redireccion) {
        ProductoViewModel vm = viewModels.getObject();
        if (formato.hasErrors()) {
            redireccion.addFlashAttribute(ERROR, "Revise los campos numéricos del formulario.");
            return "redirect:/productos";
        }
        if (vm.actualizar(datos, usuario.getUsername())) {
            redireccion.addFlashAttribute(MENSAJE, vm.getMensaje());
            return "redirect:/productos";
        }
        model.addAttribute("vm", vm);
        return "actualizar_productos";
    }

    /** RF06 (CU06, HU05): eliminar (POST + CSRF, RNF-S4). */
    @PostMapping("/eliminar")
    public String eliminar(@RequestParam("id") int id,
                           @SessionAttribute(UsuarioSesion.ATRIBUTO) UsuarioSesion usuario,
                           RedirectAttributes redireccion) {
        ProductoViewModel vm = viewModels.getObject();
        if (vm.eliminar(id, usuario.getUsername())) {
            redireccion.addFlashAttribute(MENSAJE, vm.getMensaje());
        } else {
            redireccion.addFlashAttribute(ERROR, vm.getError());
        }
        return "redirect:/productos";
    }
}
