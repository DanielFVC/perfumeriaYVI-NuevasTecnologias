package com.mycompany.perfumeriayvi.model.service;

import com.mycompany.perfumeriayvi.model.entity.Cliente;
import com.mycompany.perfumeriayvi.model.repository.ClienteRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static com.mycompany.perfumeriayvi.model.service.Validaciones.*;

/**
 * Reglas de negocio de clientes.
 *
 * Requisitos: RF04 (CU04, HU03) registrar, consultar, actualizar y eliminar clientes.
 * RNF-F2: validaciones en el servidor. RNF-F3: no se elimina un cliente con ventas.
 */
@Service
public class ClienteService {

    public static final List<String> TIPOS_DOCUMENTO = List.of("CC", "TI", "CE", "PAS");
    public static final List<String> TIPOS_VIA = List.of("Calle", "Carrera", "Avenida", "Diagonal", "Transversal");

    private final ClienteRepository repositorio;

    public ClienteService(ClienteRepository repositorio) {
        this.repositorio = repositorio;
    }

    /** RF04: listado de clientes. */
    public List<Cliente> listar() {
        return repositorio.listar();
    }

    /** RF04: consulta de un cliente para editarlo. */
    public Cliente buscarPorId(int id) {
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new ReglaNegocioException("El cliente solicitado no existe."));
    }

    /** RF04: registrar cliente. */
    public void registrar(Cliente cliente, String usuario) {
        validar(cliente);
        repositorio.registrar(cliente, usuario);
    }

    /** RF04: actualizar cliente (aplica las mismas validaciones que el registro). */
    public void actualizar(Cliente cliente, String usuario) {
        exigir(cliente.getIdCliente() > 0, "No se indicó el cliente a actualizar.");
        validar(cliente);
        repositorio.actualizar(cliente, usuario);
    }

    /** RF04 + RNF-F3: eliminar cliente sin romper la integridad de las ventas. */
    public void eliminar(int id, String usuario) {
        try {
            repositorio.eliminar(id, usuario);
        } catch (DataIntegrityViolationException e) {
            throw new ReglaNegocioException(
                    "No se puede eliminar el cliente porque tiene ventas registradas.");
        }
    }

    /** RF04 / RNF-F2: reglas de validación del cliente. */
    void validar(Cliente c) {
        c.setNombre(limpiar(c.getNombre()));
        c.setNumeroDocumento(limpiar(c.getNumeroDocumento()));
        c.setTelefono(limpiar(c.getTelefono()));
        c.setEmail(limpiar(c.getEmail()));
        c.setCiudad(limpiar(c.getCiudad()));
        c.setBarrio(limpiar(c.getBarrio()));
        c.setNumeroVia(limpiar(c.getNumeroVia()));
        c.setComplemento(limpiar(c.getComplemento()));

        exigir(c.getNombre().length() >= 3, "El nombre debe tener al menos 3 caracteres.");
        exigir(TIPOS_DOCUMENTO.contains(c.getTipoDocumento()), "Debe seleccionar un tipo de documento válido.");
        exigir(!c.getNumeroDocumento().isEmpty(), "El número de documento es obligatorio.");
        exigir(SOLO_DIGITOS.matcher(c.getNumeroDocumento()).matches(),
                "El número de documento solo debe contener números.");
        exigir(c.getNumeroDocumento().length() <= 10,
                "El número de documento no puede tener más de 10 dígitos.");
        exigir(TELEFONO.matcher(c.getTelefono()).matches(),
                "El teléfono debe contener entre 7 y 15 dígitos numéricos.");
        exigir(c.getEmail().isEmpty() || EMAIL.matcher(c.getEmail()).matches(), "El email no es válido.");
        exigir(!c.getCiudad().isEmpty(), "La ciudad es obligatoria.");
        exigir(c.getTipoVia() == null || c.getTipoVia().isEmpty() || Set.copyOf(TIPOS_VIA).contains(c.getTipoVia()),
                "El tipo de vía no es válido.");
        exigir(!repositorio.existeDocumento(c.getNumeroDocumento(), c.getIdCliente()),
                "Ya existe un cliente con el documento " + c.getNumeroDocumento() + ".");
    }
}
