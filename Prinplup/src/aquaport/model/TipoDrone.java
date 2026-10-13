package aquaport.model;

/**
 * Tipos especializados de drones acuáticos en la flota ampliada v2 de AquaPort.
 */
public enum TipoDrone {
    SUPERFICIAL("Superficial", "Zonas tranquilas de embalse o laguna", 0.500),
    SEMISUMERGIDO("Semisumergido", "Agua agitada, canales y corrientes medias", 1.500),
    BUCEADOR("Buceador", "Muestras a profundidad e inmersión subacuática", 0.300);

    private final String nombre;
    private final String entornoOperativo;
    private final double capacidadMaximaKg;

    TipoDrone(String nombre, String entornoOperativo, double capacidadMaximaKg) {
        this.nombre = nombre;
        this.entornoOperativo = entornoOperativo;
        this.capacidadMaximaKg = capacidadMaximaKg;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEntornoOperativo() {
        return entornoOperativo;
    }

    public double getCapacidadMaximaKg() {
        return capacidadMaximaKg;
    }
}
