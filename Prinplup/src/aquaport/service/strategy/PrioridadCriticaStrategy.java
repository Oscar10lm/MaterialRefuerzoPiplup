package aquaport.service.strategy;

import aquaport.model.CondicionAgua;
import aquaport.model.DroneAcuatico;
import aquaport.model.Prioridad;
import aquaport.model.TipoDrone;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Estrategia de selección para contingencias críticas:
 * Exige umbral de seguridad reforzado (batería >= 50%) y prioriza drones
 * de alta resistencia (Semisumergido o Buceador) con mayor batería para garantizar cero fallos.
 */
public class PrioridadCriticaStrategy implements EstrategiaSeleccionDrone {

    public static final int BATERIA_MINIMA_CRITICA = 50;

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
                .filter(d -> zona == null || zona.equalsIgnoreCase(d.getZona()))
                .filter(d -> condicion == null || condicion.esCompatibleCon(d.getTipo()))
                .filter(d -> d.getCapacidadCargaKg() >= pesoCargaKg)
                .filter(d -> d.getBateria() >= BATERIA_MINIMA_CRITICA)
                // Prioriza drones Semisumergidos por robustez estructural ante contingencias
                .sorted(Comparator.comparing((DroneAcuatico d) -> d.getTipo() == TipoDrone.SEMISUMERGIDO)
                        .reversed()
                        .thenComparing(Comparator.comparingInt(DroneAcuatico::getBateria).reversed()))
                .findFirst();
    }

    @Override
    public String getNombre() {
        return "PrioridadCriticaStrategy";
    }
}
