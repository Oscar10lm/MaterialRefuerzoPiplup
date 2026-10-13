package aquaport.model;

/**
 * Drone acuático buceador (sumergible).
 * Especializado en toma de muestras a profundidad con capacidad de carga de hasta 300g (0.3 kg).
 */
public class DroneBuceador extends DroneAcuatico {

    public static final double CAPACIDAD_MAXIMA_DEFECTO_KG = 0.300;

    public DroneBuceador(String id, String modelo, int bateria, EstadoDrone estado, String zona) {
        super(id, modelo, bateria, estado, zona, CAPACIDAD_MAXIMA_DEFECTO_KG);
    }

    public DroneBuceador(String id, String modelo, int bateria, EstadoDrone estado, String zona, double capacidadCargaKg) {
        super(id, modelo, bateria, estado, zona, Math.min(capacidadCargaKg, CAPACIDAD_MAXIMA_DEFECTO_KG));
    }

    @Override
    public TipoDrone getTipo() {
        return TipoDrone.BUCEADOR;
    }
}
