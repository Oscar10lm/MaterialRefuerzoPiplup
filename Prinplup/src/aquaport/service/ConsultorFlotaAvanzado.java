package aquaport.service;

import aquaport.model.DroneAcuatico;
import aquaport.model.Mision;
import aquaport.model.Prioridad;
import aquaport.model.TipoDrone;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio de análisis y consulta operativa avanzada para la flota de 15 drones (Prinplup).
 * 
 * Resuelve las operaciones complejas mediante Streams & Lambdas puros (sin bucles mutables).
 */
public class ConsultorFlotaAvanzado {

    /**
     * Operación 1:
     * Agrupa los drones disponibles por tipo de drone.
     * 
     * @param flota Lista completa de drones
     * @return Mapa agrupando cada TipoDrone con su lista de drones disponibles
     */
    public Map<TipoDrone, List<DroneAcuatico>> agruparDisponiblesPorTipo(List<DroneAcuatico> flota) {
        if (flota == null) {
            return Map.of();
        }
        return flota.stream()
                .filter(DroneAcuatico::isDisponible)
                .collect(Collectors.groupingBy(DroneAcuatico::getTipo));
    }

    /**
     * Operación 2:
     * Obtiene el drone óptimo para una zona dada según tipo compatible y mayor nivel de batería.
     * 
     * @param flota          Lista de drones
     * @param zona           Zona hídrica solicitada
     * @param tipoCompatible Tipo especializado requerido
     * @return Optional con el drone óptimo encontrado o vacío si no hay disponibilidad
     */
    public Optional<DroneAcuatico> obtenerDroneOptimoParaZona(List<DroneAcuatico> flota, String zona, TipoDrone tipoCompatible) {
        if (flota == null || zona == null || tipoCompatible == null) {
            return Optional.empty();
        }
        return flota.stream()
                .filter(DroneAcuatico::isDisponible)
                .filter(drone -> zona.equalsIgnoreCase(drone.getZona()))
                .filter(drone -> drone.getTipo() == tipoCompatible)
                .max(Comparator.comparingInt(DroneAcuatico::getBateria));
    }

    /**
     * Sobrecarga de Operación 2:
     * Obtiene el drone óptimo en una zona (mayor batería entre los disponibles en esa zona).
     * 
     * @param flota Lista de drones
     * @param zona  Zona hídrica solicitada
     * @return Optional con el drone óptimo
     */
    public Optional<DroneAcuatico> obtenerDroneOptimoParaZona(List<DroneAcuatico> flota, String zona) {
        if (flota == null || zona == null) {
            return Optional.empty();
        }
        return flota.stream()
                .filter(DroneAcuatico::isDisponible)
                .filter(drone -> zona.equalsIgnoreCase(drone.getZona()))
                .max(Comparator.comparingInt(DroneAcuatico::getBateria));
    }

    /**
     * Operación 3:
     * Calcula el promedio de porcentaje de batería por tipo de drone.
     * 
     * @param flota Lista de drones
     * @return Mapa asociando cada TipoDrone con el promedio de batería (Double)
     */
    public Map<TipoDrone, Double> calcularPromedioBateriaPorTipo(List<DroneAcuatico> flota) {
        if (flota == null) {
            return Map.of();
        }
        return flota.stream()
                .collect(Collectors.groupingBy(
                        DroneAcuatico::getTipo,
                        Collectors.averagingDouble(DroneAcuatico::getBateria)
                ));
    }

    /**
     * Operación 4:
     * Separa las misiones CRÍTICAS de las demás usando partitioningBy.
     * 
     * @param misiones Lista de misiones
     * @return Mapa particionado (true = Misiones CRÍTICAS, false = Demás prioridades)
     */
    public Map<Boolean, List<Mision>> separarMisionesCriticas(List<Mision> misiones) {
        if (misiones == null) {
            return Map.of(true, List.of(), false, List.of());
        }
        return misiones.stream()
                .collect(Collectors.partitioningBy(
                        mision -> mision.getPrioridad() == Prioridad.CRITICA
                ));
    }

    /**
     * Operación 5:
     * Lista todas las zonas únicas que cubre la flota activa (sin duplicados, ordenada alfabéticamente).
     * 
     * @param flota Lista de drones
     * @return Lista inmutable con las zonas únicas donde operan drones activos
     */
    public List<String> listarZonasUnicasFlotaActiva(List<DroneAcuatico> flota) {
        if (flota == null) {
            return List.of();
        }
        return flota.stream()
                .filter(DroneAcuatico::isActivo)
                .map(DroneAcuatico::getZona)
                .distinct()
                .sorted()
                .toList();
    }

    /**
     * Operación complementaria:
     * Ordena las misiones por urgencia de prioridad (CRITICA primero) y luego por batería restante descendente.
     * 
     * @param misiones Lista de misiones
     * @return Lista de misiones ordenadas
     */
    public List<Mision> ordenarMisionesPorPrioridadYBateria(List<Mision> misiones) {
        if (misiones == null) {
            return List.of();
        }
        return misiones.stream()
                .sorted(Comparator.comparing((Mision m) -> m.getPrioridad().getNivel())
                        .thenComparing(m -> m.getDrone().getBateria(), Comparator.reverseOrder()))
                .toList();
    }
}
