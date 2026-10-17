package aquaport.service;

import aquaport.model.DroneAcuatico;
import aquaport.model.EstadoMision;
import aquaport.model.MisionEnterprise;
import aquaport.model.TramoRuta;
import aquaport.model.Waypoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Motor funcional avanzado para análisis y planificación de la red multi-embalse.
 */
public class ConsultorFlotaEnterprise {

    public Map<String, Long> calcularCargaTrabajoPorZona(List<MisionEnterprise> misiones) {
        if (misiones == null) return Map.of();
        return misiones.stream()
                .filter(m -> m.getEstado() == EstadoMision.EN_TRANSITO)
                .flatMap(m -> m.getWaypoints().stream())
                .collect(Collectors.groupingBy(Waypoint::getZona, Collectors.counting()));
    }

    public Map<String, Double> calcularEficienciaPorZona(List<MisionEnterprise> misiones) {
        if (misiones == null || misiones.isEmpty()) return Map.of();
        Map<String, Long> totales = contarMisiones(misiones, m -> true);
        Map<String, Long> completadas = contarMisiones(misiones, MisionEnterprise::isCompletada);
        Map<String, Double> eficiencia = new HashMap<>();
        totales.forEach((zona, total) -> {
            long ok = completadas.getOrDefault(zona, 0L);
            eficiencia.put(zona, total == 0 ? 0.0 : (double) ok / total);
        });
        return Collections.unmodifiableMap(eficiencia);
    }

    private Map<String, Long> contarMisiones(List<MisionEnterprise> misiones, java.util.function.Predicate<MisionEnterprise> filtro) {
        return misiones.stream()
                .filter(filtro)
                .collect(Collectors.groupingBy(MisionEnterprise::getZonaOrigen, Collectors.counting()));
    }

    public Optional<DroneAcuatico> encontrarDroneMejorHistorial(List<MisionEnterprise> misiones) {
        if (misiones == null) return Optional.empty();
        return misiones.stream()
                .filter(MisionEnterprise::isCompletada)
                .flatMap(m -> m.getDronesInvolucrados().stream())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    public List<TramoRuta> planificarRutaOptimaMultiEtapa(List<Waypoint> waypoints, List<DroneAcuatico> flota) {
        if (waypoints == null || waypoints.size() < 2 || flota == null) return List.of();
        List<TramoRuta> ruta = new ArrayList<>();
        for (int i = 0; i < waypoints.size() - 1; i++) {
            Waypoint origen = waypoints.get(i);
            Waypoint destino = waypoints.get(i + 1);
            DroneAcuatico asignado = seleccionarDroneCercano(flota, origen.getZona())
                    .orElseThrow(() -> new IllegalStateException("Sin drone apto en tramo: " + origen.getZona()));
            ruta.add(new TramoRuta("TR-" + (i + 1), origen, destino, asignado));
        }
        return Collections.unmodifiableList(ruta);
    }

    private Optional<DroneAcuatico> seleccionarDroneCercano(List<DroneAcuatico> flota, String zona) {
        return flota.stream()
                .filter(DroneAcuatico::isDisponible)
                .filter(d -> Objects.equals(d.getZona(), zona))
                .max(Comparator.comparingInt(DroneAcuatico::getBateria));
    }

    public Map<String, Double> calcularBateriaPromedioPorZona(List<DroneAcuatico> flota) {
        if (flota == null) return Map.of();
        return flota.stream().collect(BateriaPromedioCollector.enUnaSolaPasada());
    }
}
