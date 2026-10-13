package aquaport.model;

/**
 * Representa los tipos de carga transportables en AquaPort v2.
 */
public enum TipoCarga {
    MUESTRA_AGUA("Muestra de agua para análisis ambiental", 0.250),
    SENSOR("Sensor hidrológico o boya de telemetría", 0.450),
    PAQUETE_LIGERO("Paquete de reactivos o insumos ligeros", 0.300),
    CARGA_PESADA("Kit de muestreo pesado o batería de relevo", 1.200);

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
