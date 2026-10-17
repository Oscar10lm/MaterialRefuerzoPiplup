package aquaport.infrastructure.external;

/**
 * DTO que simula la respuesta JSON del servicio HTTP externo en inglés.
 */
public class ExternalHydricApiResponse {

    private final double waterLevel;
    private final double turbidity;
    private final double temperature;
    private final double depth;

    public ExternalHydricApiResponse(double waterLevel, double turbidity,
                                     double temperature, double depth) {
        this.waterLevel = waterLevel;
        this.turbidity = turbidity;
        this.temperature = temperature;
        this.depth = depth;
    }

    public double getWaterLevel() {
        return waterLevel;
    }

    public double getTurbidity() {
        return turbidity;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getDepth() {
        return depth;
    }
}
