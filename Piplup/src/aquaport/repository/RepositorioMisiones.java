package aquaport.repository;

import aquaport.model.Mision;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de abstracción para la persistencia y recuperación de misiones (DIP).
 * 
 * Permite desacoplar los servicios de la capa de almacenamiento concreta
 * (memoria, base de datos relacional, NoSQL o archivos).
 */
public interface RepositorioMisiones {

    /**
     * Almacena o actualiza una misión en el repositorio.
     *
     * @param mision Misión a persistir
     */
    void guardar(Mision mision);

    /**
     * Busca una misión por su identificador único.
     *
     * @param id Identificador de la misión
     * @return Optional con la misión encontrada o vacío si no existe
     */
    Optional<Mision> buscarPorId(String id);

    /**
     * Retorna todas las misiones registradas.
     *
     * @return Lista inmutable o copia defensiva de las misiones
     */
    List<Mision> obtenerTodas();

    /**
     * Verifica si ya existe una misión registrada con el ID especificado.
     *
     * @param id Identificador a comprobar
     * @return true si ya existe, false en caso contrario
     */
    boolean existePorId(String id);
}
