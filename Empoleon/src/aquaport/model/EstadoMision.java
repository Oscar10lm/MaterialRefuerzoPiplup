package aquaport.model;

/**
 * Estados del ciclo de vida de una misión multi-etapa Enterprise.
 */
public enum EstadoMision {
    PENDIENTE,
    EN_TRANSITO,
    COMPLETADA,
    FALLIDA,
    CANCELADA;

    public boolean isTerminada() {
        return this == COMPLETADA || this == FALLIDA || this == CANCELADA;
    }

    public boolean isExitosa() {
        return this == COMPLETADA;
    }
}
