package aquaport.model;

/**
 * Drone superficial diseñado para canales tranquilos y tránsito veloz.
 */
public class DroneSuperficial extends DroneAcuatico {

    public static final double CAPACIDAD_MAX_KG = 0.50;

    public DroneSuperficial(String id, String modelo, int bateria,
                            EstadoDrone estado, String zona) {
        super(id, modelo, bateria, estado, zona, CAPACIDAD_MAX_KG);
    }

    @Override
    public TipoDrone getTipo() {
        return TipoDrone.SUPERFICIAL;
    }
}
