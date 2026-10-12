package com.mycompany.perfumeriayvi.viewmodel;

import com.mycompany.perfumeriayvi.model.entity.Cliente;
import com.mycompany.perfumeriayvi.model.service.ClienteService;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ViewModel del módulo de clientes (ver_clientes.jsp, registrar_cliente.jsp, actualizar_cliente.jsp).
 *
 * Requisitos: RF04 (CU04, HU03). RNF-U3: si hay un error, el formulario conserva lo que se escribió.
 */
@Component
@Scope("prototype")
public class ClienteViewModel extends ViewModelBase {

    private final ClienteService servicio;

    private List<Cliente> clientes = List.of();
    private Cliente cliente = new Cliente();

    public ClienteViewModel(ClienteService servicio) {
        this.servicio = servicio;
    }

    /** RF04: consultar clientes. */
    public void cargarLista() {
        ejecutar(() -> clientes = servicio.listar(), null);
    }

    /** RF04: formulario vacío de registro. */
    public void prepararNuevo() {
        cliente = new Cliente();
    }

    /** RF04: cargar el cliente que se va a editar. */
    public boolean cargarParaEditar(int id) {
        return ejecutar(() -> cliente = servicio.buscarPorId(id), null);
    }

    /** RF04: comando registrar. */
    public boolean registrar(Cliente datos, String usuario) {
        cliente = datos;
        boolean ok = ejecutar(() -> servicio.registrar(datos, usuario), "Cliente registrado correctamente.");
        if (ok) {
            cliente = new Cliente();
        }
        return ok;
    }

    /** RF04: comando actualizar. */
    public boolean actualizar(Cliente datos, String usuario) {
        cliente = datos;
        return ejecutar(() -> servicio.actualizar(datos, usuario), "Cliente actualizado correctamente.");
    }

    /** RF04 + RNF-F3: comando eliminar. */
    public boolean eliminar(int id, String usuario) {
        return ejecutar(() -> servicio.eliminar(id, usuario), "Cliente eliminado correctamente.");
    }

    public List<Cliente> getClientes() { return clientes; }
    public Cliente getCliente() { return cliente; }
    public List<String> getTiposDocumento() { return ClienteService.TIPOS_DOCUMENTO; }
    public List<String> getTiposVia() { return ClienteService.TIPOS_VIA; }
}
