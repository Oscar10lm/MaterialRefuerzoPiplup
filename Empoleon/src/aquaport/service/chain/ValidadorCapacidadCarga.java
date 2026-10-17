package aquaport.service.chain;

import aquaport.model.ContextoValidacion;
import aquaport.model.DroneAcuatico;
import aquaport.model.MisionEnterprise;
import aquaport.model.ResultadoValidacion;

/**
 * Eslabón 2: Valida que la carga útil requerida no exceda la capacidad máxima del drone.
 */
public class ValidadorCapacidadCarga extends AbstractValidadorMision {

    @Override
    protected ResultadoValidacion validarInterno(ContextoValidacion contexto) {
        DroneAcuatico drone = contexto.getDrone();
        MisionEnterprise mision = contexto.getMision();
        double pesoRequerido = mision.getTipoCarga().getPesoPromedioKg();

        if (drone.getCapacidadCargaKg() < pesoRequerido) {
            return ResultadoValidacion.rechazo(String.format(
                    "Capacidad de carga excedida en drone '%s' (cap: %.2fkg < requerido: %.2fkg).",
                    drone.getId(), drone.getCapacidadCargaKg(), pesoRequerido
            ));
        }
        return ResultadoValidacion.ok();
    }
}
