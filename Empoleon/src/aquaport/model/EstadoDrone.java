package aquaport.model;

/**
 * Estados operacionales de un drone acuático en la red multi-embalse.
 */
public enum EstadoDrone {
    DISPONIBLE,
    EN_MISION,
    RECARGANDO,
    MANTENIMIENTO,
    FALLO,
    SUMERGIDO;

    public boolean isDisponible() {
        return this == DISPONIBLE;
    }

    public boolean isOperativo() {
        return this == DISPONIBLE || this == EN_MISION || this == SUMERGIDO;
    }
}
