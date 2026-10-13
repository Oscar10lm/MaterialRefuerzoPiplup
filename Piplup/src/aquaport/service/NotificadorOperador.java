package aquaport.service;

import aquaport.model.Mision;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servicio con responsabilidad única: Notificar y comunicar eventos al operador (SRP).
 * 
 * Desacoplado de la persistencia y de la validación.
 * Emplea un Logger estructurado en lugar de salidas estándar directas (Sonar java:S106).
 */
public class NotificadorOperador {

    private static final Logger LOGGER = Logger.getLogger(NotificadorOperador.class.getName());

    public void notificarMisionRegistrada(Mision mision) {
        if (mision == null) {
            return;
        }
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.log(Level.INFO, "[NOTIFICACIÓN AL OPERADOR] Misión [{0}] registrada exitosamente. Drone: {1} | Ruta: {2} -> {3} | Carga: {4}",
                    new Object[]{
                        mision.getId(),
                        mision.getDrone().id(),
                        mision.getPuntoPartida(),
                        mision.getPuntoLlegada(),
                        mision.getTipoCarga()
                    });
        }
    }

    public void notificarAdvertencia(String mensaje) {
        LOGGER.log(Level.WARNING, "[ALERTA AL OPERADOR] {0}", mensaje);
    }

    public void notificarFallo(String mensaje) {
        LOGGER.log(Level.SEVERE, "[ERROR OPERATIVO] {0}", mensaje);
    }
}
