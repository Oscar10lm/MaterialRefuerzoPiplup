package aquaport.model;

/**
 * Escala de agitación del agua en el dominio de AquaPort.
 */
public enum NivelAgua {
    TRANQUILO(1),
    MODERADO(2),
    AGITADO(3),
    TORMENTOSO(4);

    private final int escala;

    NivelAgua(int escala) {
        this.escala = escala;
    }

    public int getEscala() {
        return escala;
    }

    public boolean esNavegable() {
        return this != TORMENTOSO;
    }
}
