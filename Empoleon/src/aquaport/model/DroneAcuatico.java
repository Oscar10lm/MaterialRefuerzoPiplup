package aquaport.model;

import java.util.Objects;

/**
 * Entidad base abstracta del dominio para drones acuáticos en red Enterprise.
 */
public abstract class DroneAcuatico {

    private final String id;
    private final String modelo;
    private final int bateria;
    private final EstadoDrone estado;
    private final String zona;
    private final double capacidadCargaKg;

    protected DroneAcuatico(String id, String modelo, int bateria,
                            EstadoDrone estado, String zona, double capacidadCargaKg) {
        this.id = Objects.requireNonNull(id, "ID no puede ser nulo.");
        this.modelo = Objects.requireNonNull(modelo, "Modelo no puede ser nulo.");
        this.zona = Objects.requireNonNull(zona, "Zona no puede ser nula.");
        this.bateria = validarBateria(bateria);
        this.estado = Objects.requireNonNullElse(estado, EstadoDrone.DISPONIBLE);
        this.capacidadCargaKg = validarCapacidad(capacidadCargaKg);
    }

    private static int validarBateria(int bateria) {
        if (bateria < 0 || bateria > 100) {
            throw new IllegalArgumentException("Bateria fuera de rango [0-100]: " + bateria);
        }
        return bateria;
    }

    private static double validarCapacidad(double cap) {
        if (cap <= 0.0) {
            throw new IllegalArgumentException("Capacidad de carga invalida: " + cap);
        }
        return cap;
    }

    public abstract TipoDrone getTipo();

    public String getId() {
        return id;
    }

    public String getModelo() {
        return modelo;
    }

    public int getBateria() {
        return bateria;
    }

    public EstadoDrone getEstado() {
        return estado;
    }

    public String getZona() {
        return zona;
    }

    public double getCapacidadCargaKg() {
        return capacidadCargaKg;
    }

    public boolean isDisponible() {
        return estado.isDisponible();
    }

    public String transmitirTelemetria() {
        return String.format("ID=%s;BAT=%d%%;ZONA=%s", id, bateria, zona);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DroneAcuatico that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("[%s | %-12s] Bat: %3d%% | Est: %-12s | Zona: %s",
                id, getTipo().getEtiqueta(), bateria, estado, zona);
    }
}
