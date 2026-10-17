package aquaport.model;

/**
 * Escala de turbidez y claridad del agua en unidades nefelométricas (NTU).
 */
public enum NivelTurbidez {
    CLARA(10.0),
    MEDIA(50.0),
    ALTA(100.0),
    CRITICA(500.0);

    private final double maxNtu;

    NivelTurbidez(double maxNtu) {
        this.maxNtu = maxNtu;
    }

    public double getMaxNtu() {
        return maxNtu;
    }

    public boolean permiteVisionOptica() {
        return this == CLARA || this == MEDIA;
    }
}
