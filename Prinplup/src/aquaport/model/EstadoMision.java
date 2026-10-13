package aquaport.model;

/**
 * Estados del ciclo de vida de una misión en AquaPort v2.
 */
public enum EstadoMision {
    PENDIENTE("Misión registrada en espera de despacho"),
    EN_TRANSITO("Drone en navegación hacia destino"),
    ENTREGADA("Misión completada satisfactoriamente"),
    FALLIDA("Misión interrumpida por anomalía o batería crítica");

    private final String descripcion;

    EstadoMision(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
