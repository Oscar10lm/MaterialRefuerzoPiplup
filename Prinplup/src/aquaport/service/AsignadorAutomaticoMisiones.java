package aquaport.service;

import aquaport.model.CondicionAgua;
import aquaport.model.DroneAcuatico;
import aquaport.model.EstadoMision;
import aquaport.model.Mision;
import aquaport.model.Prioridad;
import aquaport.model.TipoCarga;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Servicio autónomo de selección y asignación inteligente de drones (Prinplup v2).
 * 
 * Evalúa las condiciones del agua, capacidad de carga requerida y nivel de batería
 * para asignar de forma autónoma el mejor drone sin requerir intervención del operador.
 */
public class AsignadorAutomaticoMisiones {

    public static final int BATERIA_MINIMA_OPERATIVA = 35;

    /**
     * Selecciona el drone idóneo para una misión evaluando compatibilidad y batería.
     *
     * @param flota          Flota disponible para evaluación
     * @param zona           Zona hídrica de origen
     * @param condicion      Condición ambiental del cuerpo de agua
     * @param pesoCargaKg    Peso exacto de la carga a transportar (en kg)
     * @return Optional con el drone seleccionado con mayor batería o vacío si no hay candidato apto
     */
    public Optional<DroneAcuatico> seleccionarDroneOptimo(List<DroneAcuatico> flota,
                                                          String zona,
                                                          CondicionAgua condicion,
                                                          double pesoCargaKg) {
        if (flota == null || zona == null || condicion == null) {
            return Optional.empty();
        }

        return flota.stream()
                .filter(DroneAcuatico::isDisponible)
                .filter(drone -> zona.equalsIgnoreCase(drone.getZona()))
                .filter(drone -> condicion.esCompatibleCon(drone.getTipo()))
                .filter(drone -> drone.getCapacidadCargaKg() >= pesoCargaKg)
                .filter(drone -> drone.getBateria() >= BATERIA_MINIMA_OPERATIVA)
                .max(Comparator.comparingInt(DroneAcuatico::getBateria));
    }

    /**
     * Construye y asigna automáticamente una misión asignando el drone óptimo.
     *
     * @param idMision       Identificador único de la misión
     * @param flota          Flota disponible
     * @param puntoPartida   Zona de partida
     * @param puntoLlegada   Zona de destino
     * @param condicion      Condición del agua en la ruta
     * @param tipoCarga      Tipo de carga transportada
     * @param prioridad      Nivel de prioridad de la misión
     * @return Mision asignada con el drone óptimo
     * @throws IllegalStateException Si no existe ningún drone apto en la zona
     */
    public Mision asignarMisionAutomaticamente(String idMision,
                                              List<DroneAcuatico> flota,
                                              String puntoPartida,
                                              String puntoLlegada,
                                              CondicionAgua condicion,
                                              TipoCarga tipoCarga,
                                              Prioridad prioridad) {
        Objects.requireNonNull(idMision, "El ID de misión no puede ser nulo.");
        Objects.requireNonNull(puntoPartida, "El punto de partida no puede ser nulo.");
        Objects.requireNonNull(puntoLlegada, "El punto de llegada no puede ser nulo.");
        Objects.requireNonNull(tipoCarga, "El tipo de carga no puede ser nulo.");

        double pesoRequerido = tipoCarga.getPesoPromedioKg();
        DroneAcuatico droneAsignado = seleccionarDroneOptimo(flota, puntoPartida, condicion, pesoRequerido)
                .orElseThrow(() -> new IllegalStateException(String.format(
                        "Asignación automática fallida: No hay drone disponible apto en '%s' para condición %s y carga de %.2fkg.",
                        puntoPartida, condicion.name(), pesoRequerido
                )));

        return new Mision(idMision, droneAsignado, puntoPartida, puntoLlegada, prioridad, tipoCarga, EstadoMision.PENDIENTE);
    }
}
