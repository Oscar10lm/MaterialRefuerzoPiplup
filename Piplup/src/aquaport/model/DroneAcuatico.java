package aquaport.model;

/**
 * Representa un drone acuático autónomo dentro de la flota de AquaPort.
 * 
 * Inmutable mediante Java Record: almacena el estado actual del dron en el sistema.
 *
 * @param id          Identificador único del drone (ej. "AR-01")
 * @param modelo      Nombre del modelo (ej. "Aqua-Ranger 100")
 * @param bateria     Porcentaje de carga restante de la batería (0 - 100%)
 * @param disponible  Disponibilidad del dron para recibir una nueva misión (true = libre, false = ocupado/mantenimiento)
 * @param zona        Zona hídrica actual del campus en la que se encuentra posicionado
 */
public record DroneAcuatico(
    String id,
    String modelo,
    int bateria,
    boolean disponible,
    String zona
) {
    @Override
    public String toString() {
        return String.format("[%s | %s] Batería: %3d%% | Disponible: %-5s | Zona: %s",
                id, modelo, bateria, disponible ? "SÍ" : "NO", zona);
    }
}
