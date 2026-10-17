package aquaport.infrastructure.adapter;

import aquaport.infrastructure.external.ExternalHydricApiResponse;
import aquaport.infrastructure.external.ServicioAPIHidricaExterna;
import aquaport.model.CondicionHidrica;
import aquaport.model.NivelAgua;
import aquaport.model.NivelTurbidez;
import aquaport.service.ProveedorCondicionesHidricas;

import java.util.Objects;

/**
 * Adaptador de infraestructura que traduce las respuestas en inglés de la API externa
 * a las entidades canónicas del modelo de dominio de AquaPort.
 */
public class AdaptadorAPIHidrica implements ProveedorCondicionesHidricas {

    private final ServicioAPIHidricaExterna servicioExterno;

    public AdaptadorAPIHidrica(ServicioAPIHidricaExterna servicioExterno) {
        this.servicioExterno = Objects.requireNonNull(servicioExterno, "Servicio externo no puede ser nulo.");
    }

    @Override
    public CondicionHidrica consultarCondiciones(String zona) {
        ExternalHydricApiResponse resp = servicioExterno.fetchHydricConditions(zona);
        if (resp == null) {
            return new CondicionHidrica(NivelAgua.TRANQUILO, NivelTurbidez.CLARA, 20.0, 0.0);
        }
        NivelAgua nivel = mapearNivelAgua(resp.getWaterLevel());
        NivelTurbidez turbidez = mapearTurbidez(resp.getTurbidity());
        return new CondicionHidrica(nivel, turbidez, resp.getTemperature(), resp.getDepth());
    }

    private NivelAgua mapearNivelAgua(double waterLevel) {
        if (waterLevel <= 2.0) return NivelAgua.TRANQUILO;
        if (waterLevel <= 4.0) return NivelAgua.MODERADO;
        if (waterLevel <= 6.0) return NivelAgua.AGITADO;
        return NivelAgua.TORMENTOSO;
    }

    private NivelTurbidez mapearTurbidez(double turbidity) {
        if (turbidity <= 10.0) return NivelTurbidez.CLARA;
        if (turbidity <= 50.0) return NivelTurbidez.MEDIA;
        if (turbidity <= 100.0) return NivelTurbidez.ALTA;
        return NivelTurbidez.CRITICA;
    }
}
