package aquaport.infrastructure.external;

/**
 * Contrato del cliente de infraestructura para interactuar con la API externa en inglés.
 */
public interface ServicioAPIHidricaExterna {

    ExternalHydricApiResponse fetchHydricConditions(String zoneIdentifier);
}
