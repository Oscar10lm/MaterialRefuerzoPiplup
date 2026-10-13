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
     * Indica si el drone tiene la batería mínima (&gt;= 35%) para ser asignado.
     *
     * @param drone drone a evaluar
     * @return true si su batería es mayor o igual al umbral operativo
     * @throws IllegalArgumentException si el drone es nulo
     */
    public boolean tieneBateriaSuficiente(DroneAcuatico drone) {
        exigirDrone(drone);
        return drone.bateria() >= BATERIA_MINIMA_DESPEGUE;
    }

    /**
     * Indica si el drone está libre para recibir una misión.
     *
     * @param drone drone a evaluar
     * @return true si disponible = true
     * @throws IllegalArgumentException si el drone es nulo
     */
    public boolean estaDisponible(DroneAcuatico drone) {
        exigirDrone(drone);
        return drone.disponible();
    }

    /**
     * Valida que el punto de llegada no sea nulo/vacío y pertenezca a una zona habilitada.
     *
     * @param puntoLlegada zona de destino
     * @throws IllegalArgumentException si es nulo, vacío o no es una zona habilitada
     */
    public void validarPuntoLlegada(String puntoLlegada) {
        if (puntoLlegada == null || puntoLlegada.isBlank()) {
            throw new IllegalArgumentException("El punto de llegada no puede ser nulo ni vacío.");
        }
        if (!esZonaHabilitada(puntoLlegada)) {
            throw new IllegalArgumentException(
                "La zona '" + puntoLlegada + "' no es una zona habilitada del campus.");
        }
    }

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
        if (!esZonaHabilitada(mision.getPuntoPartida()) || !esZonaHabilitada(mision.getPuntoLlegada())) {
            throw new IllegalStateException(String.format(
                "Regla de negocio violada (RN-02): Ruta con zonas no habilitadas en el campus (Origen: '%s', Destino: '%s').",
                mision.getPuntoPartida(), mision.getPuntoLlegada()
            ));
        }

        // Regla 2: Drone asignado
        DroneAcuatico drone = mision.getDrone();
        if (drone == null) {
            throw new IllegalStateException("Regla de negocio violada: La misión no tiene un drone asignado.");
        }

        // Regla 3: Nivel de batería para despegue seguro (RN-01: >= 35%)
        if (!tieneBateriaSuficiente(drone)) {
            throw new IllegalStateException(String.format(
                "Regla de negocio violada (RN-01): El drone '%s' tiene %d%% de batería (mínimo requerido: %d%%).",
                drone.id(), drone.bateria(), BATERIA_MINIMA_DESPEGUE
            ));
        }

        // Regla 4: Disponibilidad (RN-01)
        if (!estaDisponible(drone)) {
            throw new IllegalStateException(String.format(
                "Regla de negocio violada (RN-01): El drone '%s' no se encuentra disponible.",
                drone.id()
            ));
        }
    }

    private boolean esZonaHabilitada(String zona) {
        return ZONAS_HABILITADAS.stream().anyMatch(z -> z.equalsIgnoreCase(zona));
    }

    private void exigirDrone(DroneAcuatico drone) {
        if (drone == null) {
            throw new IllegalArgumentException("El drone no puede ser nulo.");
        }
    }

}
