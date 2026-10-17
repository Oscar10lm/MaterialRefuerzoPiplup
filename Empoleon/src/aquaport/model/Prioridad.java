package aquaport.model;

/**
 * Niveles de prioridad operacional en AquaPort Enterprise.
 */
public enum Prioridad {
    CRITICA(1),
    ALTA(2),
    NORMAL(3),
    BAJA(4);

    private final int nivel;

    Prioridad(int nivel) {
        this.nivel = nivel;
    }

    public int getNivel() {
        return nivel;
    }

    public boolean isCritica() {
        return this == CRITICA;
    }
}
