package aquaport.service.observer;

import aquaport.model.DroneAcuatico;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Observador concreto que comunica las fallas de drones al Centro de Control de AquaPort.
 */
public class CentroControlObserver implements ObservadorFalloDrone {

    private static final Logger LOGGER = Logger.getLogger(CentroControlObserver.class.getName());

    private final List<String> registroAlertas = new ArrayList<>();

    @Override
    public void notificarFallo(DroneAcuatico drone, String motivo) {
        String alerta = String.format("[CENTRO DE CONTROL] Alerta de Fallo: Drone '%s' en zona '%s'. Motivo: %s",
                drone != null ? drone.getId() : "DESCONOCIDO",
                drone != null ? drone.getZona() : "DESCONOCIDA",
                motivo);

        registroAlertas.add(alerta);
        LOGGER.log(Level.SEVERE, alerta);
    }

    public List<String> getRegistroAlertas() {
        return Collections.unmodifiableList(registroAlertas);
    }
}
