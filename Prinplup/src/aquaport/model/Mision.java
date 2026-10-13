package aquaport.model;

import java.util.Objects;

/**
 * Representa una misión de transporte o monitoreo en AquaPort v2 (Prinplup).
 */
public class Mision {

    private final String id;
    private final DroneAcuatico drone;
    private final String puntoPartida;
    private final String puntoLlegada;
    private final Prioridad prioridad;
    private final TipoCarga tipoCarga;
    private final EstadoMision estado;

    public Mision(String id, DroneAcuatico drone, String puntoPartida, String puntoLlegada,
                  Prioridad prioridad, TipoCarga tipoCarga, EstadoMision estado) {
        this.id = Objects.requireNonNull(id, "El ID de la misión no puede ser nulo.");
        this.drone = Objects.requireNonNull(drone, "El drone asignado no puede ser nulo.");
        this.puntoPartida = Objects.requireNonNull(puntoPartida, "El punto de partida no puede ser nulo.");
        this.puntoLlegada = Objects.requireNonNull(puntoLlegada, "El punto de llegada no puede ser nulo.");
        this.prioridad = Objects.requireNonNullElse(prioridad, Prioridad.NORMAL);
        this.tipoCarga = Objects.requireNonNullElse(tipoCarga, TipoCarga.MUESTRA_AGUA);
        this.estado = Objects.requireNonNullElse(estado, EstadoMision.PENDIENTE);
    }

    public String getId() {
        return id;
    }

    public DroneAcuatico getDrone() {
        return drone;
    }

    public String getPuntoPartida() {
        return puntoPartida;
    }

    public String getPuntoLlegada() {
        return puntoLlegada;
    }

    public Prioridad getPrioridad() {
        return prioridad;
    }

    public TipoCarga getTipoCarga() {
        return tipoCarga;
    }

    public EstadoMision getEstado() {
        return estado;
    }

    public boolean isCritica() {
        return prioridad == Prioridad.CRITICA;
    }

    @Override
    public String toString() {
        return String.format("[%s | %-7s] %s -> %s | Drone: %s (Bat: %d%%) | Carga: %s | Est: %s",
                id, prioridad.name(), puntoPartida, puntoLlegada, drone.getId(), drone.getBateria(), tipoCarga.name(), estado.name());
    }
}
