package aquaport.model;

/**
 * Drone acuático de navegación en superficie.
 * Especializado en zonas de agua tranquila con carga de hasta 500g (0.5 kg).
 */
public class DroneSuperficial extends DroneAcuatico {

    public static final double CAPACIDAD_MAXIMA_DEFECTO_KG = 0.500;

    public DroneSuperficial(String id, String modelo, int bateria, EstadoDrone estado, String zona) {
        super(id, modelo, bateria, estado, zona, CAPACIDAD_MAXIMA_DEFECTO_KG);
    }

    public DroneSuperficial(String id, String modelo, int bateria, EstadoDrone estado, String zona, double capacidadCargaKg) {
        super(id, modelo, bateria, estado, zona, Math.min(capacidadCargaKg, CAPACIDAD_MAXIMA_DEFECTO_KG));
    }

    @Override
    public TipoDrone getTipo() {
        return TipoDrone.SUPERFICIAL;
    }
}
