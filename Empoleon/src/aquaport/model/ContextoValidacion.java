package aquaport.model;

import java.util.Objects;

/**
 * Parámetros de entrada encapsulados para la evaluación en la cadena de validación.
 */
public class ContextoValidacion {

    private final MisionEnterprise mision;
    private final DroneAcuatico drone;
    private final CondicionHidrica condicion;

    public ContextoValidacion(MisionEnterprise mision, DroneAcuatico drone, CondicionHidrica condicion) {
        this.mision = Objects.requireNonNull(mision, "Mision no puede ser nula.");
        this.drone = Objects.requireNonNull(drone, "Drone no puede ser nulo.");
        this.condicion = Objects.requireNonNull(condicion, "Condicion hidrica no puede ser nula.");
    }

    public MisionEnterprise getMision() {
        return mision;
    }

    public DroneAcuatico getDrone() {
        return drone;
    }

    public CondicionHidrica getCondicion() {
        return condicion;
    }
}
