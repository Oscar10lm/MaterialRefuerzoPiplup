package aquaport.model;

/**
 * Drone acuático semisumergido.
 * Especializado en operar en aguas agitadas o corrientes con carga de hasta 1.5kg.
 */
public class DroneSemisumergido extends DroneAcuatico {

    public static final double CAPACIDAD_MAXIMA_DEFECTO_KG = 1.500;

    public DroneSemisumergido(String id, String modelo, int bateria, EstadoDrone estado, String zona) {
        super(id, modelo, bateria, estado, zona, CAPACIDAD_MAXIMA_DEFECTO_KG);
    }

    public DroneSemisumergido(String id, String modelo, int bateria, EstadoDrone estado, String zona, double capacidadCargaKg) {
        super(id, modelo, bateria, estado, zona, Math.min(capacidadCargaKg, CAPACIDAD_MAXIMA_DEFECTO_KG));
    }

    @Override
    public TipoDrone getTipo() {
        return TipoDrone.SEMISUMERGIDO;
    }
}
