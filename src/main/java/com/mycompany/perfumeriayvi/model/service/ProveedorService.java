package com.mycompany.perfumeriayvi.model.service;

import com.mycompany.perfumeriayvi.model.entity.Proveedor;
import com.mycompany.perfumeriayvi.model.repository.ProveedorRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.mycompany.perfumeriayvi.model.service.Validaciones.*;

/**
 * Reglas de negocio de proveedores.
 *
 * Requisitos: RF05 (CU05, HU04) registrar, consultar, actualizar y eliminar proveedores.
 * RNF-F2: validaciones. RNF-F3: no se elimina un proveedor con productos asociados.
 */
@Service
public class ProveedorService {

    public static final List<String> TIPOS_PROVEEDOR = List.of(
            "Perfumes", "Perfumería", "Cosméticos", "Cuidado personal",
            "Accesorios", "Natural", "Distribuidor", "Mixto");

    private final ProveedorRepository repositorio;

    public ProveedorService(ProveedorRepository repositorio) {
        this.repositorio = repositorio;
    }

    /** RF05: listado de proveedores (también alimenta el selector de RF06). */
    public List<Proveedor> listar() {
        return repositorio.listar();
    }

    /** RF05: consulta de un proveedor. */
    public Proveedor buscarPorId(int id) {
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new ReglaNegocioException("El proveedor solicitado no existe."));
    }

    /** RF05: registrar proveedor. */
    public void registrar(Proveedor p, String usuario) {
        validar(p);
        repositorio.registrar(p, usuario);
    }

    /** RF05: actualizar proveedor. */
    public void actualizar(Proveedor p, String usuario) {
        exigir(p.getIdProveedor() > 0, "No se indicó el proveedor a actualizar.");
        validar(p);
        repositorio.actualizar(p, usuario);
    }

    /** RF05 + RNF-F3: eliminar proveedor. */
    public void eliminar(int id, String usuario) {
        try {
            repositorio.eliminar(id, usuario);
        } catch (DataIntegrityViolationException e) {
            throw new ReglaNegocioException(
                    "No se puede eliminar el proveedor porque tiene productos asociados.");
        }
    }

    /** RF05 / RNF-F2: reglas de validación del proveedor. */
    void validar(Proveedor p) {
        p.setNombre(limpiar(p.getNombre()));
        p.setNit(limpiar(p.getNit()));
        p.setTelefono(limpiar(p.getTelefono()));
        p.setEmail(limpiar(p.getEmail()));
        p.setCiudad(limpiar(p.getCiudad()));
        p.setBarrio(limpiar(p.getBarrio()));
        p.setNumeroVia(limpiar(p.getNumeroVia()));
        p.setComplemento(limpiar(p.getComplemento()));

        exigir(p.getNombre().length() >= 3, "El nombre debe tener mínimo 3 caracteres.");
        exigir(p.getNit().length() >= 5 && p.getNit().length() <= 20,
                "El NIT debe tener entre 5 y 20 caracteres.");
        exigir(TELEFONO.matcher(p.getTelefono()).matches(), "El teléfono debe contener entre 7 y 15 dígitos.");
        exigir(EMAIL.matcher(p.getEmail()).matches(), "El correo electrónico no es válido.");
        exigir(!p.getCiudad().isEmpty(), "La ciudad es obligatoria.");
        exigir(TIPOS_PROVEEDOR.contains(p.getTipoProveedor()), "Debe seleccionar un tipo de proveedor.");
        exigir(ClienteService.TIPOS_VIA.contains(p.getTipoVia()), "Debe seleccionar un tipo de vía.");
        exigir(!p.getNumeroVia().isEmpty(), "El número de vía es obligatorio.");
        exigir(!repositorio.existeNit(p.getNit(), p.getIdProveedor()),
                "Ya existe un proveedor con el NIT " + p.getNit() + ".");
    }
}
