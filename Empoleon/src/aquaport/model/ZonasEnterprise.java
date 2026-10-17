package aquaport.model;

import java.util.List;

/**
 * Zonas hídricas de la topología campus ECI en AquaPort Enterprise.
 */
public final class ZonasEnterprise {

    public static final String EMBALSE_INVESTIGACION = "Embalse de Investigacion";
    public static final String RED_CANALES = "Red de Canales";
    public static final String LAGUNA_RESERVA = "Laguna de Reserva";
    public static final String LABORATORIO_CENTRAL = "Laboratorio Hidrico Central";

    private static final List<String> TODAS = List.of(
            EMBALSE_INVESTIGACION,
            RED_CANALES,
            LAGUNA_RESERVA,
            LABORATORIO_CENTRAL
    );

    private ZonasEnterprise() {
        throw new UnsupportedOperationException("Clase utilitaria de constantes.");
    }

    public static List<String> obtenerTodas() {
        return TODAS;
    }

    public static boolean esValida(String zona) {
        return zona != null && TODAS.contains(zona);
    }
}
