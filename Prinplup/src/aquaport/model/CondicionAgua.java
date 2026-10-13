package aquaport.model;

import java.util.Set;

/**
 * Representa las condiciones dinámicas del cuerpo de agua en la zona de la misión (Prinplup v2).
 */
public enum CondicionAgua {
    TRANQUILA("Aguas calmas sin corriente relevante", Set.of(TipoDrone.SUPERFICIAL, TipoDrone.SEMISUMERGIDO)),
    AGITADA("Aguas con corriente, oleaje moderado o canal estrecho", Set.of(TipoDrone.SEMISUMERGIDO)),
    PROFUNDA("Inmersión requerida para toma de muestras en estrato inferior", Set.of(TipoDrone.BUCEADOR));

    private final String descripcion;
    private final Set<TipoDrone> tiposCompatibles;

    CondicionAgua(String descripcion, Set<TipoDrone> tiposCompatibles) {
        this.descripcion = descripcion;
        this.tiposCompatibles = tiposCompatibles;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Set<TipoDrone> getTiposCompatibles() {
        return tiposCompatibles;
    }

    public boolean esCompatibleCon(TipoDrone tipo) {
        return tiposCompatibles.contains(tipo);
    }
}
