package aquaport.service.chain;

import aquaport.model.CondicionHidrica;
import aquaport.model.ContextoValidacion;
import aquaport.model.DroneAcuatico;
import aquaport.model.ResultadoValidacion;

/**
 * Eslabón 4: Valida que las condiciones hidrológicas sean seguras para el casco del drone.
 */
public class ValidadorCondicionesHidricas extends AbstractValidadorMision {

    @Override
    protected ResultadoValidacion validarInterno(ContextoValidacion contexto) {
        DroneAcuatico drone = contexto.getDrone();
        CondicionHidrica condicion = contexto.getCondicion();

        if (!condicion.esSeguraPara(drone.getTipo())) {
            return ResultadoValidacion.rechazo(String.format(
                    "Condicion hidrica (%s, turbidez: %s) peligrosa para el tipo %s.",
                    condicion.getNivelAgua(), condicion.getTurbidez(), drone.getTipo()
            ));
        }
        return ResultadoValidacion.ok();
    }
}
