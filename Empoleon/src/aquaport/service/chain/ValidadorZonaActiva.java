package aquaport.service.chain;

import aquaport.model.ContextoValidacion;
import aquaport.model.DroneAcuatico;
import aquaport.model.MisionEnterprise;
import aquaport.model.ResultadoValidacion;
import aquaport.model.ZonasEnterprise;

/**
 * Eslabón 3: Valida que la zona solicitada sea válida y que el drone esté presente en ella.
 */
public class ValidadorZonaActiva extends AbstractValidadorMision {

    @Override
    protected ResultadoValidacion validarInterno(ContextoValidacion contexto) {
        MisionEnterprise mision = contexto.getMision();
        DroneAcuatico drone = contexto.getDrone();

        if (!ZonasEnterprise.esValida(mision.getZonaOrigen())) {
            return ResultadoValidacion.rechazo("Zona de origen no catalogada: " + mision.getZonaOrigen());
        }
        if (!drone.getZona().equalsIgnoreCase(mision.getZonaOrigen())) {
            return ResultadoValidacion.rechazo(String.format(
                    "Drone '%s' ubicado en '%s', no disponible en zona de partida '%s'.",
                    drone.getId(), drone.getZona(), mision.getZonaOrigen()
            ));
        }
        return ResultadoValidacion.ok();
    }
}
