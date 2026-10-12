package aquaport.service;

import aquaport.model.DroneAcuatico;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de consulta y análisis operativo de la flota de AquaPort mediante Streams & Lambdas.
 * 
 * Reglas de diseño y restricciones aplicadas:
 * - Sin bucles (for, while, do-while).
 * - Sin variables mutables ni efectos secundarios externos.
 * - Cada consulta se resuelve en una única canalización (pipeline) pura de Java Stream.
 */
public class ConsultorFlota {

    private static final int UMBRAL_BATERIA_MINIMA = 35;

    /**
     * Consulta 1:
     * Obtener la lista de drones disponibles con batería >= 35%, ordenados de mayor a menor batería.
     * 
     * Pipeline visual:
     *   flota (List)
     *     │
     *     ├──> stream()
     *     ├──> filter(disponible == true)             // Descarta drones no disponibles
     *     ├──> filter(bateria >= 35)                  // Descarta drones con carga crítica
     *     ├──> sorted(Comparator.bateria DESC)        // Orden descendente por batería
     *     └──> toList()                               // Colección inmutable resultante
     * 
     * @param flota Lista de drones acuáticos a evaluar
     * @return Lista inmutable de drones aptos ordenados por batería descendente
     */
    public List<DroneAcuatico> obtenerDronesDisponiblesConBateriaSuficiente(List<DroneAcuatico> flota) {
        return flota.stream()
                .filter(DroneAcuatico::disponible)
                .filter(drone -> drone.bateria() >= UMBRAL_BATERIA_MINIMA)
                .sorted(Comparator.comparingInt(DroneAcuatico::bateria).reversed())
                .toList();
    }

    /**
     * Consulta 2:
     * Obtener únicamente los IDs de los drones que se encuentran disponibles.
     * 
     * Pipeline visual:
     *   flota (List<DroneAcuatico>)
     *     │
     *     ├──> stream()
     *     ├──> filter(disponible == true)             // Filtra sólo los drones listos
     *     ├──> map(DroneAcuatico::id)                 // Transforma DroneAcuatico -> String (ID)
     *     └──> toList()                               // Lista de IDs (List<String>)
     * 
     * @param flota Lista de drones acuáticos
     * @return Lista con los identificadores (String) de los drones disponibles
     */
    public List<String> obtenerIdsDronesDisponibles(List<DroneAcuatico> flota) {
        return flota.stream()
                .filter(DroneAcuatico::disponible)
                .map(DroneAcuatico::id)
                .toList();
    }

    /**
     * Consulta 3:
     * Determinar si existe al menos un drone disponible con batería >= 35%.
     * 
     * Pipeline visual (Cortocircuito / Short-circuit):
     *   flota (List)
     *     │
     *     ├──> stream()
     *     └──> anyMatch(disponible && bateria >= 35)  // Detiene la evaluación apenas halla uno
     * 
     * @param flota Lista de drones acuáticos
     * @return true si al menos un drone cumple la condición de disponibilidad y batería; false en caso contrario
     */
    public boolean existeDroneDisponibleConBateriaSuficiente(List<DroneAcuatico> flota) {
        return flota.stream()
                .anyMatch(drone -> drone.disponible() && drone.bateria() >= UMBRAL_BATERIA_MINIMA);
    }

    /**
     * Consulta 4:
     * Contar cuántos drones están disponibles en toda la flota.
     * 
     * Pipeline visual:
     *   flota (List)
     *     │
     *     ├──> stream()
     *     ├──> filter(disponible == true)             // Retiene únicamente drones disponibles
     *     └──> count()                                // Operación terminal de conteo (retorna long)
     * 
     * @param flota Lista de drones acuáticos
     * @return Cantidad total de drones con disponibilidad activa
     */
    public long contarDronesDisponibles(List<DroneAcuatico> flota) {
        return flota.stream()
                .filter(DroneAcuatico::disponible)
                .count();
    }

    /**
     * Consulta 5:
     * Obtener el drone con mayor batería de toda la flota.
     * 
     * Pipeline visual:
     *   flota (List)
     *     │
     *     ├──> stream()
     *     └──> max(Comparator.bateria)                // Reducción al valor máximo (retorna Optional)
     * 
     * @param flota Lista de drones acuáticos
     * @return Optional con el DroneAcuatico de mayor nivel de batería, o Optional.empty() si la flota está vacía
     */
    public Optional<DroneAcuatico> obtenerDroneConMayorBateria(List<DroneAcuatico> flota) {
        return flota.stream()
                .max(Comparator.comparingInt(DroneAcuatico::bateria));
    }
}
