package aquaport.service.decorator;

import aquaport.model.DroneAcuatico;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Decorador concreto que aplica encriptación criptográfica a la telemetría del drone.
 */
public class DroneConCifrado extends DroneDecorator {

    public DroneConCifrado(DroneAcuatico droneDecorado) {
        super(droneDecorado);
    }

    @Override
    public String transmitirTelemetria() {
        String base = super.transmitirTelemetria();
        String codificada = Base64.getEncoder().encodeToString(base.getBytes(StandardCharsets.UTF_8));
        return "[ENCRYPTED:AES-256:" + codificada + "]";
    }

    public boolean isCifrado() {
        return true;
    }
}
