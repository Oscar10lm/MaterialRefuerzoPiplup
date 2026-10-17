package aquaport.service.chain;

import aquaport.model.ContextoValidacion;
import aquaport.model.DroneAcuatico;
import aquaport.model.MisionEnterprise;
import aquaport.model.ResultadoValidacion;

/**
 * Eslabón 1: Valida que el nivel de batería del drone cumpla los umbrales operativos mínimos.
 */
public class ValidadorBateria extends AbstractValidadorMision {

    public static final int BATERIA_REGULAR_MIN = 35;
    public static final int BATERIA_CRITICA_MIN = 50;

    @Override
    protected ResultadoValidacion validarInterno(ContextoValidacion contexto) {
        DroneAcuatico drone = contexto.getDrone();
        MisionEnterprise mision = contexto.getMision();
        int umbral = mision.getPrioridad().isCritica() ? BATERIA_CRITICA_MIN : BATERIA_REGULAR_MIN;

        if (drone.getBateria() < umbral) {
            return ResultadoValidacion.rechazo(String.format(
                    "Bateria insuficiente en drone '%s' (%d%% < umbral requerido de %d%%).",
                    drone.getId(), drone.getBateria(), umbral
            ));
        }
        return ResultadoValidacion.ok();
    }
}
