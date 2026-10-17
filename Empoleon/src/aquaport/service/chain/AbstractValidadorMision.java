package aquaport.service.chain;

import aquaport.model.ContextoValidacion;
import aquaport.model.ResultadoValidacion;

/**
 * Clase base abstracta que gestiona la propagación secuencial en la cadena de validación.
 */
public abstract class AbstractValidadorMision implements ValidadorMisionHandler {

    private ValidadorMisionHandler siguiente;

    @Override
    public ValidadorMisionHandler enlazarCon(ValidadorMisionHandler siguiente) {
        this.siguiente = siguiente;
        return siguiente;
    }

    @Override
    public ResultadoValidacion validar(ContextoValidacion contexto) {
        ResultadoValidacion resultado = validarInterno(contexto);
        if (!resultado.isExitoso()) {
            return resultado;
        }
        return siguiente != null ? siguiente.validar(contexto) : ResultadoValidacion.ok();
    }

    protected abstract ResultadoValidacion validarInterno(ContextoValidacion contexto);
}
