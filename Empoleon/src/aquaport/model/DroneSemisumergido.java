package aquaport.model;

/**
 * Drone semisumergido con casco resiliente para aguas con oleaje y corrientes.
 */
public class DroneSemisumergido extends DroneAcuatico {

    public static final double CAPACIDAD_MAX_KG = 1.50;

    public DroneSemisumergido(String id, String modelo, int bateria,
                              EstadoDrone estado, String zona) {
        super(id, modelo, bateria, estado, zona, CAPACIDAD_MAX_KG);
    }

    @Override
    public TipoDrone getTipo() {
        return TipoDrone.SEMISUMERGIDO;
    }
}
