package com.mycompany.perfumeriayvi.model.service;

import com.mycompany.perfumeriayvi.model.entity.Usuario;
import com.mycompany.perfumeriayvi.model.repository.UsuarioRepository;
import com.mycompany.perfumeriayvi.seguridad.CifradoContrasena;
import com.mycompany.perfumeriayvi.seguridad.EventosSeguridad;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Autenticación de usuarios.
 *
 * Requisitos:
 * - RF01 (CU01, HU01): validar usuario y contraseña; solo entran cuentas activas.
 * - RNF-S1: la contraseña se compara contra el hash BCrypt.
 * - RNF-S2: tras 5 intentos fallidos la cuenta se bloquea 15 minutos.
 * - RNF-S7: cada intento queda en el log de seguridad y, por trigger, en la auditoría.
 */
@Service
public class AutenticacionService {

    public static final int MAX_INTENTOS = 5;
    public static final Duration DURACION_BLOQUEO = Duration.ofMinutes(15);

    /** Mensaje genérico: no revela si el usuario existe o si falló la contraseña. */
    static final String CREDENCIALES_INVALIDAS = "Usuario o contraseña incorrectos.";

    private final UsuarioRepository usuarios;
    private final CifradoContrasena cifrado;
    private final EventosSeguridad eventos;
    private final Clock reloj;

    public AutenticacionService(UsuarioRepository usuarios, CifradoContrasena cifrado,
                                EventosSeguridad eventos, Clock reloj) {
        this.usuarios = usuarios;
        this.cifrado = cifrado;
        this.eventos = eventos;
        this.reloj = reloj;
    }

    /** RF01: intenta autenticar y devuelve el resultado con un mensaje para la vista. */
    public ResultadoAutenticacion autenticar(String username, String contrasena, String ip) {
        String nombre = username == null ? "" : username.trim();
        if (nombre.isEmpty() || contrasena == null || contrasena.isEmpty()) {
            return ResultadoAutenticacion.fallo("Ingrese su usuario y su contraseña.");
        }

        Optional<Usuario> encontrado = usuarios.buscarPorUsername(nombre);
        if (encontrado.isEmpty()) {
            cifrado.simularVerificacion(contrasena);
            eventos.loginFallido(nombre, ip, "usuario inexistente");
            return ResultadoAutenticacion.fallo(CREDENCIALES_INVALIDAS);
        }

        Usuario usuario = encontrado.get();
        LocalDateTime ahora = LocalDateTime.now(reloj);

        // RNF-S2: cuenta bloqueada temporalmente
        if (usuario.estaBloqueado(ahora)) {
            long minutos = Math.max(1, Duration.between(ahora, usuario.getBloqueadoHasta()).toMinutes() + 1);
            eventos.loginFallido(nombre, ip, "cuenta bloqueada");
            return ResultadoAutenticacion.fallo("Cuenta bloqueada temporalmente por intentos fallidos. "
                    + "Intente de nuevo en " + minutos + " minuto(s).");
        }

        // RNF-S1: verificación contra el hash BCrypt
        if (!cifrado.coincide(contrasena, usuario.getPasswordHash())) {
            return registrarFallo(usuario, ip, ahora);
        }

        // RF01: solo las cuentas activas pueden entrar
        if (!usuario.isActive()) {
            eventos.loginFallido(nombre, ip, "cuenta inactiva");
            return ResultadoAutenticacion.fallo("Su usuario está inactivo. Contacte al administrador.");
        }

        usuarios.registrarAccesoExitoso(usuario.getId(), usuario.getUsername(), ahora);
        eventos.loginExitoso(usuario.getUsername(), ip);
        return ResultadoAutenticacion.exito(usuario);
    }

    /** RNF-S2: cuenta el intento fallido y bloquea al llegar al máximo. */
    private ResultadoAutenticacion registrarFallo(Usuario usuario, String ip, LocalDateTime ahora) {
        int intentos = usuario.getIntentosFallidos() + 1;

        if (intentos >= MAX_INTENTOS) {
            usuarios.registrarIntentoFallido(usuario.getId(), usuario.getUsername(), 0,
                    ahora.plus(DURACION_BLOQUEO));
            eventos.cuentaBloqueada(usuario.getUsername(), ip);
            return ResultadoAutenticacion.fallo("Cuenta bloqueada por " + DURACION_BLOQUEO.toMinutes()
                    + " minutos por superar " + MAX_INTENTOS + " intentos fallidos.");
        }

        usuarios.registrarIntentoFallido(usuario.getId(), usuario.getUsername(), intentos, null);
        eventos.loginFallido(usuario.getUsername(), ip, "contraseña incorrecta");
        int restantes = MAX_INTENTOS - intentos;
        return ResultadoAutenticacion.fallo(CREDENCIALES_INVALIDAS + " Le quedan " + restantes
                + " intento(s) antes del bloqueo temporal.");
    }

    /** Resultado del intento de inicio de sesión. */
    public record ResultadoAutenticacion(boolean exitoso, Usuario usuario, String mensaje) {

        static ResultadoAutenticacion exito(Usuario usuario) {
            return new ResultadoAutenticacion(true, usuario, null);
        }

        static ResultadoAutenticacion fallo(String mensaje) {
            return new ResultadoAutenticacion(false, null, mensaje);
        }
    }
}
