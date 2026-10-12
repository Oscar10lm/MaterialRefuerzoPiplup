package aquaport.service;

import aquaport.model.DroneAcuatico;
import aquaport.model.Mision;

/**
 * Servicio con responsabilidad única: Validar reglas de negocio para misiones (SRP).
 * 
 * Reglas de negocio del MVP:
 * 1. La misión no debe ser nula.
 * 2. El punto de partida y llegada no pueden ser idénticos.
 * 3. El drone asignado debe tener batería suficiente para operar (umbral mínimo MVP: 20%).
 * 4. El drone asignado debe estar disponible.
 */
public class ValidadorMision {

    private static final int BATERIA_MINIMA_DESPEGUE = 20;

    /**
     * Valida que una misión cumpla con todas las políticas de negocio operativas de AquaPort.
     *
     * @param mision Misión a evaluar
     * @throws IllegalArgumentException si la misión es nula
     * @throws IllegalStateException si alguna regla operativa de negocio es infringida
     */
    public void validar(Mision mision) {
        if (mision == null) {
            throw new IllegalArgumentException("No se puede validar una misión nula.");
        }

        // Regla 1: Trayecto coherente
        if (mision.getPuntoPartida().equalsIgnoreCase(mision.getPuntoLlegada())) {
            throw new IllegalStateException(String.format(
                "Regla de negocio violada: El punto de partida '%s' no puede ser idéntico al de llegada.",
                mision.getPuntoPartida()
            ));
        }

        // Regla 2: Drone y estado
        DroneAcuatico drone = mision.getDrone();
        if (drone == null) {
            throw new IllegalStateException("Regla de negocio violada: La misión no tiene un drone asignado.");
        }

        // Regla 3: Nivel de batería para despegue seguro
        if (drone.bateria() < BATERIA_MINIMA_DESPEGUE) {
            throw new IllegalStateException(String.format(
                "Regla de negocio violada: El drone '%s' tiene %d%% de batería (mínimo requerido: %d%%).",
                drone.id(), drone.bateria(), BATERIA_MINIMA_DESPEGUE
            ));
        }

        // Regla 4: Disponibilidad
        if (!drone.disponible()) {
            throw new IllegalStateException(String.format(
                "Regla de negocio violada: El drone '%s' no se encuentra disponible.",
                drone.id()
            ));
        }
    }
}
