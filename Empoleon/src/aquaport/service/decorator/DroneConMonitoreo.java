package aquaport.service.decorator;

import aquaport.model.DroneAcuatico;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Decorador concreto que agrega registro y auditoría de telemetría a cualquier drone.
 */
public class DroneConMonitoreo extends DroneDecorator {

    private final List<String> bitacoraTelemetria = new ArrayList<>();

    public DroneConMonitoreo(DroneAcuatico droneDecorado) {
        super(droneDecorado);
    }

    @Override
    public String transmitirTelemetria() {
        String base = super.transmitirTelemetria();
        String enriquecida = base + ";MONITOREO=ACTIVO;DIAG=OK";
        bitacoraTelemetria.add(enriquecida);
        return enriquecida;
    }

    public List<String> getBitacoraTelemetria() {
        return Collections.unmodifiableList(bitacoraTelemetria);
    }

    public int getTotalTransmisiones() {
        return bitacoraTelemetria.size();
    }
}
