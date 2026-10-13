package aquaport.service.observer;

import aquaport.model.DroneAcuatico;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Observador concreto que genera órdenes de trabajo inmediatas para el equipo técnico de campo.
 */
public class TecnicoMantenimientoObserver implements ObservadorFalloDrone {

    private static final Logger LOGGER = Logger.getLogger(TecnicoMantenimientoObserver.class.getName());

    private final List<String> ordenesTrabajo = new ArrayList<>();

    @Override
    public void notificarFallo(DroneAcuatico drone, String motivo) {
        String orden = String.format("[TÉCNICO MANTENIMIENTO] Orden de Revisión Urgente: Drone '%s' (Zona: %s). Diagnóstico preliminar: %s",
                drone != null ? drone.getId() : "DESCONOCIDO",
                drone != null ? drone.getZona() : "DESCONOCIDA",
                motivo);

        ordenesTrabajo.add(orden);
        LOGGER.log(Level.WARNING, orden);
    }

    public List<String> getOrdenesTrabajo() {
        return Collections.unmodifiableList(ordenesTrabajo);
    }
}
