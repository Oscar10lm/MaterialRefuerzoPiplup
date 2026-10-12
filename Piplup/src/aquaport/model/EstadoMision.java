package aquaport.model;

/**
 * Representa el ciclo de vida o estado actual de una misión de transporte en AquaPort.
 */
public enum EstadoMision {
    PENDIENTE("Misión programada en espera de inicio de navegación"),
    EN_TRANSITO("Drone en trayecto hacia el punto de entrega"),
    ENTREGADA("Carga entregada exitosamente en destino"),
    FALLIDA("Misión interrumpida o fallida por batería/anomalía");

    private final String descripcion;

    EstadoMision(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
