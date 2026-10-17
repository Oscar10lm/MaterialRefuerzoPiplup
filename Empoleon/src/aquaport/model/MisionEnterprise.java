package aquaport.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Representa una misión hídrica multi-etapa con waypoints y asignación por tramos.
 */
public class MisionEnterprise {

    private final String id;
    private final String zonaOrigen;
    private final String zonaDestino;
    private final List<Waypoint> waypoints;
    private final List<TramoRuta> tramos;
    private final DroneAcuatico drone;
    private final Prioridad prioridad;
    private final TipoCarga tipoCarga;
    private EstadoMision estado;

    public MisionEnterprise(String id, String zonaOrigen, String zonaDestino,
                            List<Waypoint> waypoints, DroneAcuatico drone,
                            Prioridad prioridad, TipoCarga tipoCarga, EstadoMision estado) {
        this.id = Objects.requireNonNull(id, "ID no puede ser nulo.");
        this.zonaOrigen = Objects.requireNonNull(zonaOrigen, "Zona origen no puede ser nula.");
        this.zonaDestino = Objects.requireNonNull(zonaDestino, "Zona destino no puede ser nula.");
        this.waypoints = waypoints != null ? new ArrayList<>(waypoints) : new ArrayList<>();
        this.tramos = new ArrayList<>();
        this.drone = drone;
        this.prioridad = Objects.requireNonNullElse(prioridad, Prioridad.NORMAL);
        this.tipoCarga = Objects.requireNonNullElse(tipoCarga, TipoCarga.MUESTRA_AGUA);
        this.estado = Objects.requireNonNullElse(estado, EstadoMision.PENDIENTE);
    }

    public String getId() {
        return id;
    }

    public String getZonaOrigen() {
        return zonaOrigen;
    }

    public String getZonaDestino() {
        return zonaDestino;
    }

    public List<Waypoint> getWaypoints() {
        return Collections.unmodifiableList(waypoints);
    }

    public List<TramoRuta> getTramos() {
        return Collections.unmodifiableList(tramos);
    }

    public void agregarTramo(TramoRuta tramo) {
        if (tramo != null) {
            this.tramos.add(tramo);
        }
    }

    public DroneAcuatico getDrone() {
        return drone;
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

    public void setEstado(EstadoMision estado) {
        this.estado = Objects.requireNonNull(estado, "Estado no puede ser nulo.");
    }

    public boolean isCompletada() {
        return estado == EstadoMision.COMPLETADA;
    }

    public List<DroneAcuatico> getDronesInvolucrados() {
        List<DroneAcuatico> lista = new ArrayList<>();
        if (drone != null) {
            lista.add(drone);
        }
        for (TramoRuta t : tramos) {
            if (t.getDroneAsignado() != null && !lista.contains(t.getDroneAsignado())) {
                lista.add(t.getDroneAsignado());
            }
        }
        return Collections.unmodifiableList(lista);
    }

    @Override
    public String toString() {
        return String.format("MisionEnt[%s | %s -> %s | Est: %s | WPs: %d]",
                id, zonaOrigen, zonaDestino, estado, waypoints.size());
    }
}
