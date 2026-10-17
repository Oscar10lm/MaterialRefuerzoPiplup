package aquaport.model;

import java.util.Objects;

/**
 * Resultado inmutable emitido por los eslabones de la cadena de validación.
 */
public final class ResultadoValidacion {

    private final boolean exitoso;
    private final String motivoRechazo;

    private ResultadoValidacion(boolean exitoso, String motivoRechazo) {
        this.exitoso = exitoso;
        this.motivoRechazo = motivoRechazo;
    }

    public static ResultadoValidacion ok() {
        return new ResultadoValidacion(true, null);
    }

    public static ResultadoValidacion rechazo(String motivo) {
        return new ResultadoValidacion(false, Objects.requireNonNullElse(motivo, "Validacion fallida."));
    }

    public boolean isExitoso() {
        return exitoso;
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    @Override
    public String toString() {
        return exitoso ? "OK" : "RECHAZO: " + motivoRechazo;
    }
}
