package com.mycompany.perfumeriayvi.model.repository;

import com.mycompany.perfumeriayvi.model.entity.Usuario;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Persistencia de usuarios (tabla users).
 * Requisitos: RF01 (CU01, HU01) autenticación; RF09 (CU09, HU08) gestionar vendedores;
 * RF10 (CU10, HU02) asignar permisos.
 */
public interface UsuarioRepository {

    Optional<Usuario> buscarPorUsername(String username);

    /** RF03: se consulta en cada petición para aplicar bloqueos y permisos al instante. */
    Optional<Usuario> buscarPorId(int id);

    /** RNF-S2: suma un intento fallido y, si se indica, bloquea la cuenta hasta la fecha dada. */
    void registrarIntentoFallido(int id, String username, int intentos, LocalDateTime bloquearHasta);

    /** RF01: reinicia los intentos y guarda la fecha del último acceso (trigger: operación LOGIN). */
    void registrarAccesoExitoso(int id, String username, LocalDateTime fecha);

    List<Usuario> listarVendedores();

    boolean existeUsername(String username);

    void registrar(Usuario usuario, String usuarioSesion);

    void cambiarEstado(int id, boolean activo, String usuarioSesion);

    void actualizarPermisos(int id, boolean clientes, boolean productos,
                            boolean proveedores, boolean ventas, String usuarioSesion);
}
