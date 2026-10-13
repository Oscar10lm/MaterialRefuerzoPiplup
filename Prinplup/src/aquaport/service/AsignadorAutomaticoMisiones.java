package aquaport.service;

import aquaport.model.CondicionAgua;
import aquaport.model.DroneAcuatico;
import aquaport.model.EstadoDrone;
import aquaport.model.EstadoMision;
import aquaport.model.Mision;
import aquaport.model.Prioridad;
import aquaport.model.TipoCarga;
import aquaport.service.observer.ObservadorFalloDrone;
import aquaport.service.strategy.EstrategiaSeleccionDrone;
import aquaport.service.strategy.MayorBateriaStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Servicio autónomo de selección y asignación inteligente de drones (Prinplup v2).
 * 
 * Integra los patrones de diseño arquitecturales:
 * 1. Strategy: Delega la heurística de selección a la estrategia activa sin verificaciones con instanceof.
 * 2. Observer: Administra y notifica a todos los observadores ante fallas o anomalías críticas.
 * 3. Factory Method: Compatible con las instancias polimórficas creadas por FabricaDrones.
 */
public class AsignadorAutomaticoMisiones {

    public static final int BATERIA_MINIMA_OPERATIVA = 35;

    private EstrategiaSeleccionDrone estrategia;
    private final List<ObservadorFalloDrone> observadores = new ArrayList<>();

    public AsignadorAutomaticoMisiones() {
        this(new MayorBateriaStrategy());
    }

    public AsignadorAutomaticoMisiones(EstrategiaSeleccionDrone estrategia) {
        this.estrategia = Objects.requireNonNullElseGet(estrategia, MayorBateriaStrategy::new);
    }

    // -------------------------------------------------------------------------
    // Gestión del Patrón Strategy (sin instanceof)
    // -------------------------------------------------------------------------

    public void setEstrategia(EstrategiaSeleccionDrone estrategia) {
        this.estrategia = Objects.requireNonNull(estrategia, "La estrategia no puede ser nula.");
    }

    public EstrategiaSeleccionDrone getEstrategia() {
        return estrategia;
    }

    // -------------------------------------------------------------------------
    // Gestión del Patrón Observer
    // -------------------------------------------------------------------------

    public void agregarObservador(ObservadorFalloDrone observador) {
        if (observador != null && !observadores.contains(observador)) {
            observadores.add(observador);
        }
    }

    public void removerObservador(ObservadorFalloDrone observador) {
        observadores.remove(observador);
    }

    public List<ObservadorFalloDrone> getObservadores() {
        return Collections.unmodifiableList(observadores);
    }

    public void notificarObservadoresFallo(DroneAcuatico drone, String motivo) {
        for (ObservadorFalloDrone observador : observadores) {
            observador.notificarFallo(drone, motivo);
        }
    }

    // -------------------------------------------------------------------------
    // Selección y Asignación con Strategy y Observer integrados
    // -------------------------------------------------------------------------

    /**
     * Delega la selección del mejor drone a la estrategia configurada (Strategy Pattern).
     */
    public Optional<DroneAcuatico> seleccionarDroneOptimo(List<DroneAcuatico> flota,
                                                          String zona,
                                                          CondicionAgua condicion,
                                                          double pesoCargaKg) {
        return seleccionarDroneOptimo(flota, zona, condicion, pesoCargaKg, Prioridad.NORMAL);
    }

    public Optional<DroneAcuatico> seleccionarDroneOptimo(List<DroneAcuatico> flota,
                                                          String zona,
                                                          CondicionAgua condicion,
                                                          double pesoCargaKg,
                                                          Prioridad prioridad) {
        if (flota == null || zona == null || condicion == null) {
            return Optional.empty();
        }

        List<DroneAcuatico> candidatos = flota.stream()
                .filter(DroneAcuatico::isDisponible)
                .filter(d -> d.getBateria() >= BATERIA_MINIMA_OPERATIVA)
                .toList();

        // Delegación polimórfica estricta al Strategy (sin instanceof)
        return estrategia.seleccionar(candidatos, zona, condicion, pesoCargaKg, prioridad);
    }

    /**
     * Construye y asigna automáticamente una misión empleando la estrategia activa.
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
        DroneAcuatico droneAsignado = seleccionarDroneOptimo(flota, puntoPartida, condicion, pesoRequerido, prioridad)
                .orElseThrow(() -> new IllegalStateException(String.format(
                        "Asignación automática fallida: No hay drone disponible apto en '%s' para condición %s y carga de %.2fkg usando %s.",
                        puntoPartida, condicion.name(), pesoRequerido, estrategia.getNombre()
                )));

        return new Mision(idMision, droneAsignado, puntoPartida, puntoLlegada, prioridad, tipoCarga, EstadoMision.PENDIENTE);
    }

    /**
     * Registra formalmente una misión. Si el drone asignado se encuentra en FALLO,
     * notifica inmediatamente a todos los observadores registrados (Observer Pattern).
     *
     * @param mision Misión a registrar
     * @return Misión registrada
     * @throws IllegalStateException Si el drone asignado está en estado de FALLO
     */
    public Mision registrarMision(Mision mision) {
        Objects.requireNonNull(mision, "La misión a registrar no puede ser nula.");
        DroneAcuatico drone = mision.getDrone();

        if (drone != null && drone.getEstado() == EstadoDrone.FALLO) {
            String motivo = String.format("Misión rechazada: El drone '%s' asignado está en estado FALLO.", drone.getId());
            notificarObservadoresFallo(drone, motivo);
            throw new IllegalStateException(motivo);
        }

        return mision;
    }
}
