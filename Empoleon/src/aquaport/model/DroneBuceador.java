package aquaport.model;

/**
 * Drone buceador para inmersión subacuática profunda y muestreo limnológico.
 */
public class DroneBuceador extends DroneAcuatico {

    public static final double CAPACIDAD_MAX_KG = 0.30;

    public DroneBuceador(String id, String modelo, int bateria,
                         EstadoDrone estado, String zona) {
        super(id, modelo, bateria, estado, zona, CAPACIDAD_MAX_KG);
    }

    @Override
    public TipoDrone getTipo() {
        return TipoDrone.BUCEADOR;
    }
}
