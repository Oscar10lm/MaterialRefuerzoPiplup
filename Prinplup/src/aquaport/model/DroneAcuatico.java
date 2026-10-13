package aquaport.model;

import java.util.Objects;

/**
 * Clase base abstracta para la jerarquía polimórfica de drones acuáticos en AquaPort v2 (Prinplup).
 */
public abstract class DroneAcuatico {

    private final String id;
    private final String modelo;
    private final int bateria;
    private final EstadoDrone estado;
    private final String zona;
    private final double capacidadCargaKg;

    protected DroneAcuatico(String id, String modelo, int bateria, EstadoDrone estado, String zona, double capacidadCargaKg) {
        this.id = Objects.requireNonNull(id, "El ID del drone no puede ser nulo.");
        this.modelo = Objects.requireNonNull(modelo, "El modelo no puede ser nulo.");
        this.bateria = Math.clamp(bateria, 0, 100);
        this.estado = Objects.requireNonNull(estado, "El estado no puede ser nulo.");
        this.zona = Objects.requireNonNull(zona, "La zona no puede ser nula.");
        this.capacidadCargaKg = capacidadCargaKg;
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

    public boolean isActivo() {
        return estado.isActivo();
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
        return String.format("[%s | %-13s | %-12s] Bat: %3d%% | Est: %-13s | Zona: %-19s | Max: %.2fkg",
                id, modelo, getTipo().getNombre(), bateria, estado.name(), zona, capacidadCargaKg);
    }
}
