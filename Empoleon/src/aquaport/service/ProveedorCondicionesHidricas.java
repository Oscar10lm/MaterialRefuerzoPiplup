package aquaport.service;

import aquaport.model.CondicionHidrica;

/**
 * Puerto de dominio (DIP) para consulta agnóstica de condiciones ambientales hídricas.
 */
public interface ProveedorCondicionesHidricas {

    CondicionHidrica consultarCondiciones(String zona);
}
