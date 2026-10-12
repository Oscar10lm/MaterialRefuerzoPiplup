package aquaport.repository;

import aquaport.model.Mision;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación en memoria del repositorio de misiones para el MVP de AquaPort.
 */
public class RepositorioMisionesEnMemoria implements RepositorioMisiones {

    private final Map<String, Mision> almacenamiento = new LinkedHashMap<>();

    @Override
    public void guardar(Mision mision) {
        if (mision == null) {
            throw new IllegalArgumentException("No se puede guardar una misión nula.");
        }
        almacenamiento.put(mision.getId(), mision);
    }

    @Override
    public Optional<Mision> buscarPorId(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(almacenamiento.get(id));
    }

    @Override
    public List<Mision> obtenerTodas() {
        return Collections.unmodifiableList(new ArrayList<>(almacenamiento.values()));
    }

    @Override
    public boolean existePorId(String id) {
        if (id == null) {
            return false;
        }
        return almacenamiento.containsKey(id);
    }
}
