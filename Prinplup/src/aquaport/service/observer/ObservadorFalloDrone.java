package aquaport.service.observer;

import aquaport.model.DroneAcuatico;

/**
 * Contrato del patrón Observer para suscriptores de eventos de fallo y anomalías operativas.
 */
public interface ObservadorFalloDrone {

    /**
     * Notificación recibida cuando un drone entra en estado de FALLO o anomalía crítica.
     *
     * @param drone  Drone que experimentó el fallo
     * @param motivo Descripción de la avería o causa de la contingencia
     */
    void notificarFallo(DroneAcuatico drone, String motivo);
}
