package aquaport.model;

/**
 * Familias de drones especializados en la flota Enterprise.
 */
public enum TipoDrone {
    SUPERFICIAL("Superficial", 0.50),
    SEMISUMERGIDO("Semisumergido", 1.50),
    BUCEADOR("Buceador", 0.30);

    private final String etiqueta;
    private final double capacidadMaximaKg;

    TipoDrone(String etiqueta, double capacidadMaximaKg) {
        this.etiqueta = etiqueta;
        this.capacidadMaximaKg = capacidadMaximaKg;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public double getCapacidadMaximaKg() {
        return capacidadMaximaKg;
    }
}
