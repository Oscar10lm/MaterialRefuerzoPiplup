package aquaport.model;

/**
 * Categorías de carga útil transportadas en la red acuática.
 */
public enum TipoCarga {
    MUESTRA_AGUA("Muestra de agua para análisis limnológico", 0.25),
    SENSOR("Sensor hidrológico o boya de telemetría", 0.45),
    PAQUETE_LIGERO("Reactivos químicos de laboratorio", 0.30),
    CARGA_PESADA("Módulo de instrumentación pesada", 1.20),
    EQUIPO_MEDICION("Estación meteorológica subacuática", 1.10);

    private final String descripcion;
    private final double pesoPromedioKg;

    TipoCarga(String descripcion, double pesoPromedioKg) {
        this.descripcion = descripcion;
        this.pesoPromedioKg = pesoPromedioKg;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public double getPesoPromedioKg() {
        return pesoPromedioKg;
    }
}
