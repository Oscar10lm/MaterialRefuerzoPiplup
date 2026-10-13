package aquaport.model;

/**
 * Nivel de urgencia de las misiones en AquaPort v2.
 * Las misiones CRÍTICAS tienen precedencia absoluta.
 */
public enum Prioridad {
    CRITICA(1, "Precedencia absoluta por derrame o anomalía severa"),
    ALTA(2, "Misión prioritaria de recolección urgente"),
    NORMAL(3, "Misión de transporte o monitoreo regular"),
    BAJA(4, "Operación preventiva diferible");

    private final int nivel;
    private final String descripcion;

    Prioridad(int nivel, String descripcion) {
        this.nivel = nivel;
        this.descripcion = descripcion;
    }

    public int getNivel() {
        return nivel;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isCritica() {
        return this == CRITICA;
    }
}
