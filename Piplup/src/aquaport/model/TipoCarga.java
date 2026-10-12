package aquaport.model;

/**
 * Representa los tipos de carga permitidos en el sistema de transporte AquaPort MVP.
 */
public enum TipoCarga {
    MUESTRA_AGUA("Muestra de agua para análisis ambiental"),
    SENSOR("Sensor hidrológico ligero"),
    PAQUETE_LIGERO("Paquete de reactivos o insumos ligeros");

    private final String descripcion;

    TipoCarga(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
