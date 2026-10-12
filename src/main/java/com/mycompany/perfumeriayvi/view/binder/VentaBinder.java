package com.mycompany.perfumeriayvi.view.binder;

import com.mycompany.perfumeriayvi.seguridad.Modulo;
import com.mycompany.perfumeriayvi.seguridad.RequiereAcceso;
import com.mycompany.perfumeriayvi.seguridad.UsuarioSesion;
import com.mycompany.perfumeriayvi.viewmodel.VentaViewModel;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Binder (capa View) del módulo de ventas. Vistas: nueva_venta.jsp, reportes.jsp, actualizar_reportes.jsp.
 *
 * Requisitos: RF07 (CU07, HU06) registrar venta; RF08 (CU08, HU07) consultar, actualizar y eliminar;
 * RF03 (CU03) permiso del módulo VENTAS; RNF-S4 cambios solo por POST con CSRF.
 */
@Controller
@RequestMapping("/ventas")
@RequiereAcceso(Modulo.VENTAS)
public class VentaBinder {

    private final ObjectProvider<VentaViewModel> viewModels;

    public VentaBinder(ObjectProvider<VentaViewModel> viewModels) {
        this.viewModels = viewModels;
    }

    /** RF08: reporte de ventas. */
    @GetMapping
    public String reporte(Model model) {
        VentaViewModel vm = viewModels.getObject();
        vm.cargarReporte();
        vm.aplicarMensajes(model.getAttribute("mensaje"), model.getAttribute("error"));
        model.addAttribute("vm", vm);
        return "reportes";
    }

    /** RF07: formulario de nueva venta. */
    @GetMapping("/nuevo")
    public String nueva(Model model) {
        VentaViewModel vm = viewModels.getObject();
        vm.prepararNueva();
        model.addAttribute("vm", vm);
        return "nueva_venta";
    }

    /** RF07: registrar venta (descuenta stock). */
    @PostMapping("/registrar")
    public String registrar(@RequestParam(defaultValue = "") String fecha,
                            @RequestParam(defaultValue = "0") int idCliente,
                            @RequestParam(name = "idProducto[]", required = false) List<Integer> productos,
                            @RequestParam(name = "cantidad[]", required = false) List<Integer> cantidades,
                            @SessionAttribute(UsuarioSesion.ATRIBUTO) UsuarioSesion usuario,
                            Model model, RedirectAttributes redireccion) {
        VentaViewModel vm = viewModels.getObject();
        if (vm.registrar(fecha, idCliente, productos, cantidades, usuario.getId(), usuario.getUsername())) {
            redireccion.addFlashAttribute("mensaje", vm.getMensaje());
            return "redirect:/ventas";
        }
        model.addAttribute("vm", vm);
        return "nueva_venta";
    }

    /** RF08: formulario de edición. */
    @GetMapping("/editar")
    public String editar(@RequestParam("id") int id, Model model, RedirectAttributes redireccion) {
        VentaViewModel vm = viewModels.getObject();
        if (!vm.cargarParaEditar(id)) {
            redireccion.addFlashAttribute("error", vm.getError());
            return "redirect:/ventas";
        }
        model.addAttribute("vm", vm);
        return "actualizar_reportes";
    }

    /** RF08: actualizar venta (ajusta el stock). */
    @PostMapping("/actualizar")
    public String actualizar(@RequestParam("id_venta") int idVenta,
                             @RequestParam(defaultValue = "") String fecha,
                             @RequestParam(defaultValue = "0") int idCliente,
                             @RequestParam(name = "idProducto[]", required = false) List<Integer> productos,
                             @RequestParam(name = "cantidad[]", required = false) List<Integer> cantidades,
                             @SessionAttribute(UsuarioSesion.ATRIBUTO) UsuarioSesion usuario,
                             Model model, RedirectAttributes redireccion) {
        VentaViewModel vm = viewModels.getObject();
        if (vm.actualizar(idVenta, fecha, idCliente, productos, cantidades, usuario.getUsername())) {
            redireccion.addFlashAttribute("mensaje", vm.getMensaje());
            return "redirect:/ventas";
        }
        model.addAttribute("vm", vm);
        return "actualizar_reportes";
    }

    /** RF08: eliminar venta (devuelve stock). */
    @PostMapping("/eliminar")
    public String eliminar(@RequestParam("id") int id,
                           @SessionAttribute(UsuarioSesion.ATRIBUTO) UsuarioSesion usuario,
                           RedirectAttributes redireccion) {
        VentaViewModel vm = viewModels.getObject();
        if (vm.eliminar(id, usuario.getUsername())) {
            redireccion.addFlashAttribute("mensaje", vm.getMensaje());
        } else {
            redireccion.addFlashAttribute("error", vm.getError());
        }
        return "redirect:/ventas";
    }
}
