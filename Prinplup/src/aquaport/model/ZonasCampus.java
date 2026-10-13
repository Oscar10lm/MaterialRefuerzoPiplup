package aquaport.model;

import java.util.Set;

/**
 * Constantes oficiales de las zonas hídricas del campus universitario ECI.
 */
public final class ZonasCampus {

    public static final String EMBALSE_NORTE = "Embalse Norte";
    public static final String CANAL_CENTRAL = "Canal Central";
    public static final String LAGUNA_SUR = "Laguna Sur";
    public static final String PUNTO_RIBERENO_ESTE = "Punto Ribereño Este";
    public static final String LABORATORIO_HIDRICO = "Laboratorio Hídrico";
    public static final String DARSENA_OESTE = "Dársena Oeste";
    public static final String BAHIA_CIENCIA = "Bahía Ciencia";

    public static final Set<String> TODAS_LAS_ZONAS = Set.of(
        EMBALSE_NORTE,
        CANAL_CENTRAL,
        LAGUNA_SUR,
        PUNTO_RIBERENO_ESTE,
        LABORATORIO_HIDRICO,
        DARSENA_OESTE,
        BAHIA_CIENCIA
    );

    private ZonasCampus() {
        throw new UnsupportedOperationException("Clase utilitaria de constantes no instanciable.");
    }
}
