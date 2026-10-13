package aquaport.service.strategy;

import aquaport.model.CondicionAgua;
import aquaport.model.DroneAcuatico;
import aquaport.model.Prioridad;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Estrategia de selección que prioriza la proximidad física (misma zona exacta)
 * para minimizar tiempos de traslado en vacío, y en empate selecciona mayor batería.
 */
public class ZonaCercanaStrategy implements EstrategiaSeleccionDrone {

    @Override
    public Optional<DroneAcuatico> seleccionar(List<DroneAcuatico> candidatos,
                                                String zona,
                                                CondicionAgua condicion,
                                                double pesoCargaKg,
                                                Prioridad prioridad) {
        if (candidatos == null) {
            return Optional.empty();
        }

        return candidatos.stream()
                .filter(DroneAcuatico::isDisponible)
                .filter(d -> condicion == null || condicion.esCompatibleCon(d.getTipo()))
                .filter(d -> d.getCapacidadCargaKg() >= pesoCargaKg)
                .sorted(Comparator.comparing((DroneAcuatico d) -> zona != null && zona.equalsIgnoreCase(d.getZona()))
                        .reversed()
                        .thenComparing(Comparator.comparingInt(DroneAcuatico::getBateria).reversed()))
                .findFirst();
    }

    @Override
    public String getNombre() {
        return "ZonaCercanaStrategy";
    }
}
