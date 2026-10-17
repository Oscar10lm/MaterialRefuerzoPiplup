package aquaport.service.strategy;

import aquaport.model.CondicionAgua;
import aquaport.model.DroneAcuatico;
import aquaport.model.Prioridad;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Estrategia de selección basada en maximizar el nivel de batería restante.
 */
public class MayorBateriaStrategy implements EstrategiaSeleccionDrone {

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
                .max(Comparator.comparingInt(DroneAcuatico::getBateria));
    }

    @Override
    public String getNombre() {
        return "MayorBateriaStrategy";
    }
}
