package aquaport.model;

import java.util.Objects;

/**
 * Representa un segmento individual de una ruta multi-etapa asignado a un drone.
 */
public class TramoRuta {

    private final String idTramo;
    private final Waypoint origen;
    private final Waypoint destino;
    private final DroneAcuatico droneAsignado;
    private final double distanciaKm;
    private boolean completado;

    public TramoRuta(String idTramo, Waypoint origen, Waypoint destino, DroneAcuatico droneAsignado) {
        this.idTramo = Objects.requireNonNull(idTramo, "ID del tramo no puede ser nulo.");
        this.origen = Objects.requireNonNull(origen, "Origen no puede ser nulo.");
        this.destino = Objects.requireNonNull(destino, "Destino no puede ser nulo.");
        this.droneAsignado = Objects.requireNonNull(droneAsignado, "Drone no puede ser nulo.");
        this.distanciaKm = origen.distanciaA(destino);
        this.completado = false;
    }

    public String getIdTramo() {
        return idTramo;
    }

    public Waypoint getOrigen() {
        return origen;
    }

    public Waypoint getDestino() {
        return destino;
    }

    public DroneAcuatico getDroneAsignado() {
        return droneAsignado;
    }

    public double getDistanciaKm() {
        return distanciaKm;
    }

    public boolean isCompletado() {
        return completado;
    }

    public void marcarCompletado() {
        this.completado = true;
    }

    @Override
    public String toString() {
        return String.format("Tramo[%s: %s -> %s | Drone: %s (%.2f km)]",
                idTramo, origen.getNombre(), destino.getNombre(), droneAsignado.getId(), distanciaKm);
    }
}
