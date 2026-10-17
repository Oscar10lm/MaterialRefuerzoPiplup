package aquaport.service.chain;

import aquaport.model.ContextoValidacion;
import aquaport.model.ResultadoValidacion;

/**
 * Contrato del patrón Chain of Responsibility para validación de misiones Enterprise.
 */
public interface ValidadorMisionHandler {

    ValidadorMisionHandler enlazarCon(ValidadorMisionHandler siguiente);

    ResultadoValidacion validar(ContextoValidacion contexto);
}
