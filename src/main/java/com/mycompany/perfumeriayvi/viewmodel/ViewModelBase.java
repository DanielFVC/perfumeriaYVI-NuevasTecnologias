package com.mycompany.perfumeriayvi.viewmodel;

import com.mycompany.perfumeriayvi.model.service.ReglaNegocioException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

/**
 * Base de todos los ViewModel (capa ViewModel del patrón MVVM, RNF-M1).
 *
 * Un ViewModel guarda el estado que la vista muestra (listas, formulario, mensajes)
 * y expone comandos (registrar, actualizar, eliminar...). No conoce HTTP ni JSP:
 * la vista JSP se enlaza a sus propiedades con ${vm.propiedad}.
 *
 * Requisitos: RNF-F2 (los errores se convierten en mensajes claros) y RNF-U2 (mensajes de éxito).
 */
public abstract class ViewModelBase {

    private static final Logger LOG = LoggerFactory.getLogger(ViewModelBase.class);

    static final String ERROR_TECNICO =
            "No fue posible completar la operación por un problema con la base de datos. Intente de nuevo.";

    private String mensaje;
    private String error;

    /**
     * Ejecuta un comando y traduce las excepciones a mensajes para la vista.
     * @return true si el comando terminó bien.
     */
    protected boolean ejecutar(Runnable comando, String mensajeExito) {
        try {
            comando.run();
            if (mensajeExito != null) {
                this.mensaje = mensajeExito;
                this.error = null;
            }
            return true;
        } catch (ReglaNegocioException e) {
            this.error = e.getMessage();
        } catch (DataAccessException e) {
            LOG.error("Error de base de datos en {}", getClass().getSimpleName(), e);
            this.error = ERROR_TECNICO;
        }
        this.mensaje = null;
        return false;
    }

    /** Recibe los mensajes que llegan después de una redirección (patrón POST-Redirect-GET). */
    public void aplicarMensajes(Object mensajeFlash, Object errorFlash) {
        if (mensajeFlash instanceof String m && !m.isBlank()) {
            this.mensaje = m;
        }
        if (errorFlash instanceof String e && !e.isBlank()) {
            this.error = e;
        }
    }

    /** RNF-F2: datos del formulario con formato incorrecto (por ejemplo, letras en un precio). */
    public void rechazarFormulario(String motivo) {
        this.mensaje = null;
        this.error = motivo;
    }

    public String getMensaje() { return mensaje; }

    public String getError() { return error; }

    protected void setError(String error) { this.error = error; }
}
