package aquaport.service.strategy;

import aquaport.model.CondicionAgua;
import aquaport.model.DroneAcuatico;
import aquaport.model.Prioridad;

import java.util.List;
import java.util.Optional;

/**
 * Contrato del patrón Strategy para la selección autónoma de drones.
 * 
 * Permite alternar la heurística de asignación en tiempo de ejecución
 * sin acoplar ni modificar el código del AsignadorAutomaticoMisiones.
 */
public interface EstrategiaSeleccionDrone {

    /**
     * Selecciona el drone idóneo según la política de la estrategia concreta.
     *
     * @param candidatos  Lista de drones prefiltrados
     * @param zona        Zona de origen solicitada
     * @param condicion   Condición ambiental del cuerpo de agua
     * @param pesoCargaKg Peso de la carga requerida (kg)
     * @param prioridad   Nivel de prioridad de la misión
     * @return Optional con el drone seleccionado o vacío si ninguno cumple los criterios
     */
    Optional<DroneAcuatico> seleccionar(List<DroneAcuatico> candidatos,
                                        String zona,
                                        CondicionAgua condicion,
                                        double pesoCargaKg,
                                        Prioridad prioridad);

    String getNombre();
}
