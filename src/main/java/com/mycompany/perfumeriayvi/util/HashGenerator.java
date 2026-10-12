package com.mycompany.perfumeriayvi.util;

import com.mycompany.perfumeriayvi.seguridad.CifradoContrasena;

/**
 * Utilidad de consola para generar el hash BCrypt de una contraseña (RNF-S1).
 * Sirve para crear o restablecer manualmente la contraseña de un usuario en la base de datos.
 *
 * Uso: java -cp perfumeriaYVI.war ... HashGenerator "MiClave2026"
 */
public final class HashGenerator {

    private HashGenerator() {
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Uso: HashGenerator <contraseña>");
            return;
        }
        System.out.println(new CifradoContrasena().cifrar(args[0]));
    }
}
