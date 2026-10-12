package aquaport.service;

import aquaport.model.DroneAcuatico;
import aquaport.model.Mision;

import java.util.Set;

/**
 * Servicio con responsabilidad única: Validar reglas de negocio para misiones (SRP).
 * 
 * Reglas de negocio del MVP (alineadas con AP-01 en plantilla DOSW):
 * 1. La misión no debe ser nula.
 * 2. Zonas oficiales: puntoPartida y puntoLlegada deben ser zonas válidas de la ECI y diferentes entre sí.
 * 3. RN-01: El drone asignado debe tener batería >= 35% para operar.
 * 4. RN-01: El drone asignado debe estar disponible (disponible = true).
 */
public class ValidadorMision {

    private static final int BATERIA_MINIMA_DESPEGUE = 35;

    private static final Set<String> ZONAS_HABILITADAS = Set.of(
        "Embalse Norte",
        "Canal Central",
        "Laguna Sur",
        "Punto Ribereño Este",
        "Laboratorio Hídrico"
    );

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

        // Regla 1: Trayecto coherente (origen != destino)
        if (mision.getPuntoPartida().equalsIgnoreCase(mision.getPuntoLlegada())) {
            throw new IllegalStateException(String.format(
                "Regla de negocio violada (RN-02): El punto de partida '%s' no puede ser idéntico al de llegada.",
                mision.getPuntoPartida()
            ));
        }

        // Regla 1b: Puntos pertenecientes a zonas oficiales de la ECI
        boolean origenValido = ZONAS_HABILITADAS.stream().anyMatch(z -> z.equalsIgnoreCase(mision.getPuntoPartida()));
        boolean destinoValido = ZONAS_HABILITADAS.stream().anyMatch(z -> z.equalsIgnoreCase(mision.getPuntoLlegada()));
        if (!origenValido || !destinoValido) {
            throw new IllegalStateException(String.format(
                "Regla de negocio violada (RN-02): Ruta con zonas no habilitadas en el campus (Origen: '%s', Destino: '%s').",
                mision.getPuntoPartida(), mision.getPuntoLlegada()
            ));
        }

        // Regla 2: Drone y estado
        DroneAcuatico drone = mision.getDrone();
        if (drone == null) {
            throw new IllegalStateException("Regla de negocio violada: La misión no tiene un drone asignado.");
        }

        // Regla 3: Nivel de batería para despegue seguro (RN-01: >= 35%)
        if (drone.bateria() < BATERIA_MINIMA_DESPEGUE) {
            throw new IllegalStateException(String.format(
                "Regla de negocio violada (RN-01): El drone '%s' tiene %d%% de batería (mínimo requerido: %d%%).",
                drone.id(), drone.bateria(), BATERIA_MINIMA_DESPEGUE
            ));
        }

        // Regla 4: Disponibilidad (RN-01)
        if (!drone.disponible()) {
            throw new IllegalStateException(String.format(
                "Regla de negocio violada (RN-01): El drone '%s' no se encuentra disponible.",
                drone.id()
            ));
        }
    }
}
