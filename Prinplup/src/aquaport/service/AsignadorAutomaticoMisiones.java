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

    public void registrarObservador(ObservadorFalloDrone observador) {
        agregarObservador(observador);
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
     * Si un drone seleccionado se detecta en FALLO, notifica a los observadores y busca un alternativo.
     * Si no hay drones para una misión CRÍTICA, notifica inmediatamente a todos los observadores.
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
        List<DroneAcuatico> candidatosDisponibles = new ArrayList<>(flota != null ? flota : List.of());
        DroneAcuatico droneAsignado = null;

        while (true) {
            Optional<DroneAcuatico> optDrone = seleccionarDroneOptimo(candidatosDisponibles, puntoPartida, condicion, pesoRequerido, prioridad);
            if (optDrone.isEmpty()) {
                String motivo = String.format(
                        "Asignación automática fallida: No hay drone disponible apto en '%s' para condición %s y carga de %.2fkg usando %s.",
                        puntoPartida, condicion != null ? condicion.name() : "N/A", pesoRequerido, estrategia.getNombre()
                );
                if (prioridad == Prioridad.CRITICA) {
                    notificarObservadoresFallo(null, "ALERTA CRÍTICA: Misión '" + idMision + "' rechazada por falta de drones aptos en " + puntoPartida + ".");
                }
                throw new IllegalStateException(motivo);
            }

            DroneAcuatico candidato = optDrone.get();
            if (candidato.getEstado() == EstadoDrone.FALLO) {
                notificarObservadoresFallo(candidato, String.format("Drone '%s' en estado FALLO descartado durante asignación. Buscando alternativo.", candidato.getId()));
                candidatosDisponibles.remove(candidato);
            } else {
                droneAsignado = candidato;
                break;
            }
        }

        return new Mision(idMision, droneAsignado, puntoPartida, puntoLlegada, prioridad, tipoCarga, EstadoMision.PENDIENTE);
    }

    /**
     * Asigna automáticamente un drone a una misión existente (Reto 12 TDD).
     */
    public Mision asignar(Mision mision, List<DroneAcuatico> flota, CondicionAgua condicion) {
        Objects.requireNonNull(mision, "La misión no puede ser nula.");
        return asignarMisionAutomaticamente(
                mision.getId(),
                flota,
                mision.getPuntoPartida(),
                mision.getPuntoLlegada(),
                condicion != null ? condicion : CondicionAgua.TRANQUILA,
                mision.getTipoCarga(),
                mision.getPrioridad()
        );
    }

    public Mision asignar(Mision mision, List<DroneAcuatico> flota) {
        return asignar(mision, flota, CondicionAgua.TRANQUILA);
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
