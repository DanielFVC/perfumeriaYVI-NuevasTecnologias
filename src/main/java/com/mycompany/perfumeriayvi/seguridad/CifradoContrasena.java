package com.mycompany.perfumeriayvi.seguridad;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;

/**
 * Cifrado de contraseñas con BCrypt.
 *
 * Requisito: RNF-S1. Las contraseñas nunca se guardan ni se comparan en texto plano;
 * se almacena un hash BCrypt con costo 10 y sal aleatoria.
 */
@Component
public class CifradoContrasena {

    public static final int COSTO = 10;

    /**
     * Hash de referencia para comparar cuando el usuario no existe y así tardar
     * lo mismo que con un usuario real (evita adivinar usuarios por el tiempo de respuesta).
     */
    private static final String HASH_FICTICIO = BCrypt.hashpw("contrasena-ficticia", BCrypt.gensalt(COSTO));

    public String cifrar(String contrasenaPlana) {
        return BCrypt.hashpw(contrasenaPlana, BCrypt.gensalt(COSTO));
    }

    public boolean coincide(String contrasenaPlana, String hash) {
        if (contrasenaPlana == null || hash == null || !hash.startsWith("$2")) {
            return false;
        }
        try {
            return BCrypt.checkpw(contrasenaPlana, hash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /** Consume el mismo tiempo que una verificación real. */
    public void simularVerificacion(String contrasenaPlana) {
        coincide(contrasenaPlana == null ? "" : contrasenaPlana, HASH_FICTICIO);
    }
}
