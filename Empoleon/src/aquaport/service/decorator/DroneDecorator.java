package aquaport.service.decorator;

import aquaport.model.DroneAcuatico;
import aquaport.model.TipoDrone;

import java.util.Objects;

/**
 * Decorador base abstracto que envuelve un DroneAcuatico respetando la interfaz del componente.
 */
public abstract class DroneDecorator extends DroneAcuatico {

    protected final DroneAcuatico droneDecorado;

    protected DroneDecorator(DroneAcuatico droneDecorado) {
        super(
                Objects.requireNonNull(droneDecorado, "Drone no puede ser nulo.").getId(),
                droneDecorado.getModelo(),
                droneDecorado.getBateria(),
                droneDecorado.getEstado(),
                droneDecorado.getZona(),
                droneDecorado.getCapacidadCargaKg()
        );
        this.droneDecorado = droneDecorado;
    }

    @Override
    public TipoDrone getTipo() {
        return droneDecorado.getTipo();
    }

    @Override
    public String transmitirTelemetria() {
        return droneDecorado.transmitirTelemetria();
    }

    public DroneAcuatico getDroneDecorado() {
        return droneDecorado;
    }
}
