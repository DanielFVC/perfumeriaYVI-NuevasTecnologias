package com.mycompany.perfumeriayvi.view.binder;

import com.mycompany.perfumeriayvi.seguridad.UsuarioSesion;
import com.mycompany.perfumeriayvi.viewmodel.PanelViewModel;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

/**
 * Binder del panel principal. Vista: index.jsp.
 * Requisito: RF03 (CU03) mostrar solo los módulos autorizados.
 */
@Controller
public class PanelBinder {

    private final ObjectProvider<PanelViewModel> viewModels;

    public PanelBinder(ObjectProvider<PanelViewModel> viewModels) {
        this.viewModels = viewModels;
    }

    @GetMapping("/index")
    public String panel(@SessionAttribute(UsuarioSesion.ATRIBUTO) UsuarioSesion usuario, Model model) {
        PanelViewModel vm = viewModels.getObject();
        vm.cargar(usuario);
        model.addAttribute("vm", vm);
        return "index";
    }

    /** Ruta anterior del inventario: se conserva por compatibilidad. */
    @GetMapping("/inventario")
    public String inventarioAnterior() {
        return "redirect:/productos";
    }
}
