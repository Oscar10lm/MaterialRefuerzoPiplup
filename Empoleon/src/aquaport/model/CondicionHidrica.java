package aquaport.model;

import java.util.Objects;

/**
 * Entidad de dominio que representa las condiciones ambientales de un cuerpo de agua.
 */
public class CondicionHidrica {

    private final NivelAgua nivelAgua;
    private final NivelTurbidez turbidez;
    private final double temperaturaCelsius;
    private final double profundidadMetros;

    public CondicionHidrica(NivelAgua nivelAgua, NivelTurbidez turbidez,
                            double temperaturaCelsius, double profundidadMetros) {
        this.nivelAgua = Objects.requireNonNullElse(nivelAgua, NivelAgua.TRANQUILO);
        this.turbidez = Objects.requireNonNullElse(turbidez, NivelTurbidez.CLARA);
        this.temperaturaCelsius = temperaturaCelsius;
        this.profundidadMetros = Math.max(0.0, profundidadMetros);
    }

    public NivelAgua getNivelAgua() {
        return nivelAgua;
    }

    public NivelTurbidez getTurbidez() {
        return turbidez;
    }

    public double getTemperaturaCelsius() {
        return temperaturaCelsius;
    }

    public double getProfundidadMetros() {
        return profundidadMetros;
    }

    public boolean esSeguraPara(TipoDrone tipo) {
        if (tipo == null || !nivelAgua.esNavegable()) return false;
        if (tipo == TipoDrone.SUPERFICIAL) return nivelAgua == NivelAgua.TRANQUILO;
        if (tipo == TipoDrone.SEMISUMERGIDO) return nivelAgua.getEscala() <= 3;
        return nivelAgua.getEscala() <= 3 && turbidez != NivelTurbidez.CRITICA;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CondicionHidrica that)) return false;
        return Double.compare(that.temperaturaCelsius, temperaturaCelsius) == 0 &&
                Double.compare(that.profundidadMetros, profundidadMetros) == 0 &&
                nivelAgua == that.nivelAgua &&
                turbidez == that.turbidez;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nivelAgua, turbidez, temperaturaCelsius, profundidadMetros);
    }

    @Override
    public String toString() {
        return String.format("Condicion[%s | Turbidez: %s | Temp: %.1fC | Prof: %.1fm]",
                nivelAgua, turbidez, temperaturaCelsius, profundidadMetros);
    }
}
