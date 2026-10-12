package com.mycompany.perfumeriayvi.viewmodel;

import com.mycompany.perfumeriayvi.model.entity.Cliente;
import com.mycompany.perfumeriayvi.model.repository.AuditoriaRepository.FiltroAuditoria;
import com.mycompany.perfumeriayvi.model.service.AuditoriaService;
import com.mycompany.perfumeriayvi.model.service.AuditoriaService.PaginaAuditoria;
import com.mycompany.perfumeriayvi.model.service.ClienteService;
import com.mycompany.perfumeriayvi.model.service.ExportadorAuditoria;
import com.mycompany.perfumeriayvi.model.service.ProductoService;
import com.mycompany.perfumeriayvi.model.service.ReglaNegocioException;
import com.mycompany.perfumeriayvi.model.service.VentaService;
import com.mycompany.perfumeriayvi.seguridad.UsuarioSesion;
import com.mycompany.perfumeriayvi.seguridad.UsuarioSesionTest;
import com.mycompany.perfumeriayvi.model.entity.Usuario;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Pruebas de la capa ViewModel (RNF-M1): estado y comandos sin servidor web ni JSP. */
class ViewModelsTest {

    @Test
    void clienteViewModelMuestraElErrorYConservaElFormulario() {
        ClienteService servicio = mock(ClienteService.class);
        doThrow(new ReglaNegocioException("El email no es válido.")).when(servicio).registrar(any(), any());
        ClienteViewModel vm = new ClienteViewModel(servicio);

        Cliente datos = new Cliente();
        datos.setNombre("Laura");
        assertFalse(vm.registrar(datos, "admin"));
        assertEquals("El email no es válido.", vm.getError());
        assertSame(datos, vm.getCliente()); // RNF-U3
    }

    @Test
    void clienteViewModelConvierteErroresDeBaseDeDatosEnMensajeClaro() {
        ClienteService servicio = mock(ClienteService.class);
        when(servicio.listar()).thenThrow(new DataAccessResourceFailureException("sin conexión"));
        ClienteViewModel vm = new ClienteViewModel(servicio);
        vm.cargarLista();
        assertEquals(ViewModelBase.ERROR_TECNICO, vm.getError()); // RNF-F2
        assertTrue(vm.getClientes().isEmpty());
    }

    @Test
    void panelSoloMuestraLosModulosPermitidos() {
        PanelViewModel vm = new PanelViewModel();
        vm.cargar(UsuarioSesion.de(UsuarioSesionTest.usuario(Usuario.ROL_VENDEDOR, true, false, false, true)));
        assertTrue(vm.isPuedeVerClientes());
        assertTrue(vm.isPuedeVerVentas());
        assertFalse(vm.isPuedeVerProductos());
        assertFalse(vm.isAdmin());
        assertFalse(vm.isSinModulos());
    }

    @Test
    void ventaViewModelConservaLasLineasCuandoFalla() {
        VentaService ventas = mock(VentaService.class);
        when(ventas.registrar(any(), anyInt(), any(), any(), anyInt(), any()))
                .thenThrow(new ReglaNegocioException("Stock insuficiente"));
        VentaViewModel vm = new VentaViewModel(ventas, mock(ClienteService.class), mock(ProductoService.class));

        assertFalse(vm.registrar("2026-10-07", 4, List.of(1, 2), List.of(3, 1), 7, "v"));
        assertEquals(2, vm.getVenta().getDetalles().size());
        assertEquals(3, vm.getVenta().getDetalles().get(0).getCantidad());
        assertEquals(4, vm.getVenta().getIdCliente());
    }

    @Test
    void auditoriaViewModelSoloAceptaFiltrosConocidosYEscapaElJson() {
        AuditoriaService servicio = mock(AuditoriaService.class);
        Map<String, Integer> conteo = new LinkedHashMap<>();
        conteo.put("INSERT", 3);
        conteo.put("x\"</script>", 1);
        when(servicio.consultar(any(), anyInt(), anyInt()))
                .thenReturn(new PaginaAuditoria(List.of(), 1, 1, 4, conteo));
        AuditoriaViewModel vm = new AuditoriaViewModel(servicio, mock(ExportadorAuditoria.class));

        vm.consultar("users; DROP TABLE", " admin ", "INSERT", 1);

        assertEquals(new FiltroAuditoria(null, "admin", "INSERT"), vm.getFiltro());
        assertEquals("&usuario=admin&operacion=INSERT", vm.getConsultaFiltros());
        assertFalse(vm.getOperacionesJSON().contains("</script>"));
        assertTrue(vm.getOperacionesJSON().startsWith("{\"INSERT\":3,"));
    }
}
