package aquaport.model;

/**
 * Estados operativos posibles para un drone acuático en AquaPort v2 (Prinplup).
 */
public enum EstadoDrone {
    DISPONIBLE("Listo para recibir y ejecutar una nueva misión"),
    EN_MISION("Navegando o ejecutando transporte activo"),
    RECARGANDO("En estación de recarga de batería"),
    MANTENIMIENTO("En revisión técnica o fuera de servicio"),
    SUMERGIDO("Operando en inmersión activa bajo el agua");

    private final String descripcion;

    EstadoDrone(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Determina si el drone se considera parte de la flota activa (no en mantenimiento/baja).
     */
    public boolean isActivo() {
        return this != MANTENIMIENTO;
    }

    /**
     * Determina si el drone está libre para asignación inmediata.
     */
    public boolean isDisponible() {
        return this == DISPONIBLE;
    }
}
