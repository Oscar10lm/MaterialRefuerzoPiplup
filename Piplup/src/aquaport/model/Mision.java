package aquaport.model;

/**
 * Representa una misión de transporte asignada a un drone acuático en AquaPort.
 * 
 * Implementa el patrón creacional Builder estático para garantizar la construcción
 * de objetos consistentes, inmutables y completamente validados.
 */
public class Mision {

    private final String id;
    private final DroneAcuatico drone;
    private final String puntoPartida;
    private final String puntoLlegada;
    private final TipoCarga tipoCarga;
    private final EstadoMision estado;

    /**
     * Constructor privado accesible únicamente por el Builder.
     */
    private Mision(Builder builder) {
        this.id = builder.id;
        this.drone = builder.drone;
        this.puntoPartida = builder.puntoPartida;
        this.puntoLlegada = builder.puntoLlegada;
        this.tipoCarga = builder.tipoCarga;
        this.estado = builder.estado;
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

    public TipoCarga getTipoCarga() {
        return tipoCarga;
    }

    public EstadoMision getEstado() {
        return estado;
    }

    @Override
    public String toString() {
        return String.format("""
            ╔══════════════════════════════════════════════════════════════════════════
            ║ MISIÓN [%s]
            ╠══════════════════════════════════════════════════════════════════════════
            ║  Drone Asignado : %s (%s, Bat: %d%%)
            ║  Ruta           : %s ──> %s
            ║  Tipo de Carga  : %s
            ║  Estado         : %s
            ╚══════════════════════════════════════════════════════════════════════════""",
            id,
            drone.id(),
            drone.modelo(),
            drone.bateria(),
            puntoPartida,
            puntoLlegada,
            tipoCarga,
            estado
        );
    }

    /**
     * Builder para construir instancias válidas de Mision con validaciones exhaustivas.
     */
    public static class Builder {
        private String id;
        private DroneAcuatico drone;
        private String puntoPartida;
        private String puntoLlegada;
        private TipoCarga tipoCarga;
        private EstadoMision estado = EstadoMision.PENDIENTE; // Valor por defecto sensato en MVP

        public Builder() {
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder drone(DroneAcuatico drone) {
            this.drone = drone;
            return this;
        }

        public Builder puntoPartida(String puntoPartida) {
            this.puntoPartida = puntoPartida;
            return this;
        }

        public Builder puntoLlegada(String puntoLlegada) {
            this.puntoLlegada = puntoLlegada;
            return this;
        }

        public Builder tipoCarga(TipoCarga tipoCarga) {
            this.tipoCarga = tipoCarga;
            return this;
        }

        public Builder estado(EstadoMision estado) {
            this.estado = estado;
            return this;
        }

        /**
         * Construye la misión previa validación de integridad.
         * 
         * Reglas de validación:
         * 1. id no debe ser nulo ni estar en blanco.
         * 2. puntoPartida no debe ser nulo ni estar en blanco.
         * 3. puntoLlegada no debe ser nulo ni estar en blanco.
         * 4. drone no debe ser nulo.
         * 5. el drone asignado debe estar disponible al momento de la construcción.
         * 6. tipoCarga no debe ser nulo.
         * 7. estado no debe ser nulo.
         *
         * @return Instancia inmutable y válida de Mision
         * @throws IllegalStateException Si alguna regla de validación falla
         */
        public Mision build() {
            // Validación 1: ID
            if (id == null || id.isBlank()) {
                throw new IllegalStateException("Error de validación: El ID de la misión no puede ser nulo ni estar vacío.");
            }

            // Validación 2: Punto de Partida
            if (puntoPartida == null || puntoPartida.isBlank()) {
                throw new IllegalStateException("Error de validación: El punto de partida no puede ser nulo ni estar vacío.");
            }

            // Validación 3: Punto de Llegada
            if (puntoLlegada == null || puntoLlegada.isBlank()) {
                throw new IllegalStateException("Error de validación: El punto de llegada no puede ser nulo ni estar vacío.");
            }

            // Validación 4: Drone nulo
            if (drone == null) {
                throw new IllegalStateException("Error de validación: Debe asignarse un drone acuático a la misión.");
            }

            // Validación 5: Drone disponible
            if (!drone.disponible()) {
                throw new IllegalStateException(String.format(
                    "Error de validación: El drone '%s' no está disponible para operar (disponible = false).",
                    drone.id()
                ));
            }

            // Validación 6: Tipo de Carga
            if (tipoCarga == null) {
                throw new IllegalStateException("Error de validación: Debe especificar un tipo de carga válido.");
            }

            // Validación 7: Estado de la misión
            if (estado == null) {
                throw new IllegalStateException("Error de validación: El estado de la misión no puede ser nulo.");
            }

            return new Mision(this);
        }
    }
}
