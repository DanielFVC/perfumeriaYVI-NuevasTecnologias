package com.mycompany.perfumeriayvi.viewmodel;

import com.mycompany.perfumeriayvi.seguridad.Modulo;
import com.mycompany.perfumeriayvi.seguridad.UsuarioSesion;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * ViewModel del panel principal (index.jsp).
 *
 * Requisito: RF03 (CU03, HU02). La vista solo muestra las tarjetas de los módulos a los que
 * el usuario tiene acceso; la regla vive aquí y no en la JSP.
 */
@Component
@Scope("prototype")
public class PanelViewModel extends ViewModelBase {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-CO"));

    private UsuarioSesion usuario;

    public void cargar(UsuarioSesion usuario) {
        this.usuario = usuario;
    }

    public String getNombre() { return usuario.getNombre(); }
    public String getUsername() { return usuario.getUsername(); }
    public String getRol() { return usuario.getRol(); }
    public boolean isAdmin() { return usuario.isAdmin(); }

    /** Fecha de hoy en español para el encabezado del panel (por ejemplo "lunes 12 de octubre de 2026"). */
    public String getFecha() {
        return LocalDate.now(ZoneId.of("America/Bogota")).format(FORMATO_FECHA);
    }

    public boolean isPuedeVerClientes() { return usuario.puedeAcceder(Modulo.CLIENTES); }
    public boolean isPuedeVerProveedores() { return usuario.puedeAcceder(Modulo.PROVEEDORES); }
    public boolean isPuedeVerProductos() { return usuario.puedeAcceder(Modulo.PRODUCTOS); }
    public boolean isPuedeVerVentas() { return usuario.puedeAcceder(Modulo.VENTAS); }

    /** True si un vendedor no tiene ningún módulo habilitado. */
    public boolean isSinModulos() {
        return !isPuedeVerClientes() && !isPuedeVerProveedores() && !isPuedeVerProductos() && !isPuedeVerVentas();
    }
}
