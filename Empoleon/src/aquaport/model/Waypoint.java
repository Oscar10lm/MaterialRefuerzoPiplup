package aquaport.model;

import java.util.Objects;

/**
 * Representa un punto de control intermedio (boya o esclusa) en una ruta multi-etapa.
 */
public class Waypoint {

    private final String id;
    private final String nombre;
    private final String zona;
    private final double latitud;
    private final double longitud;
    private final int orden;

    public Waypoint(String id, String nombre, String zona, double latitud, double longitud, int orden) {
        this.id = Objects.requireNonNull(id, "ID del waypoint no puede ser nulo.");
        this.nombre = Objects.requireNonNull(nombre, "Nombre no puede ser nulo.");
        this.zona = Objects.requireNonNull(zona, "Zona no puede ser nula.");
        this.latitud = latitud;
        this.longitud = longitud;
        this.orden = orden;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getZona() {
        return zona;
    }

    public double getLatitud() {
        return latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public int getOrden() {
        return orden;
    }

    public double distanciaA(Waypoint otro) {
        if (otro == null) return Double.MAX_VALUE;
        double dLat = Math.toRadians(otro.latitud - this.latitud);
        double dLon = Math.toRadians(otro.longitud - this.longitud);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(this.latitud)) * Math.cos(Math.toRadians(otro.latitud)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 6371.0 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Waypoint that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("WP[%s | %s | %s (#%d)]", id, nombre, zona, orden);
    }
}
