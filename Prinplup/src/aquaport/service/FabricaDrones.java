package aquaport.service;

import aquaport.model.DroneAcuatico;
import aquaport.model.DroneBuceador;
import aquaport.model.DroneSemisumergido;
import aquaport.model.DroneSuperficial;
import aquaport.model.EstadoDrone;
import aquaport.model.TipoDrone;

import java.util.Objects;

/**
 * Fábrica especializada de drones acuáticos (patrón Factory Method).
 * 
 * Centraliza la creación y parametrización de las instancias polimórficas de la flota v2.
 */
public class FabricaDrones {

    /**
     * Factory Method principal que instancia la subclase especializada según el TipoDrone solicitado.
     *
     * @param tipo    Tipo especializado (SUPERFICIAL, SEMISUMERGIDO, BUCEADOR)
     * @param id      Identificador único del drone
     * @param modelo  Nombre de modelo
     * @param bateria Nivel de carga de batería (0 - 100%)
     * @param estado  Estado inicial del drone
     * @param zona    Zona hídrica donde se ubica
     * @return Instancia concreta de DroneAcuatico
     */
    public DroneAcuatico crearDrone(TipoDrone tipo, String id, String modelo, int bateria, EstadoDrone estado, String zona) {
        Objects.requireNonNull(tipo, "El tipo de drone no puede ser nulo.");
        return switch (tipo) {
            case SUPERFICIAL -> new DroneSuperficial(id, modelo, bateria, estado, zona);
            case SEMISUMERGIDO -> new DroneSemisumergido(id, modelo, bateria, estado, zona);
            case BUCEADOR -> new DroneBuceador(id, modelo, bateria, estado, zona);
        };
    }

    public DroneSuperficial crearSuperficial(String id, String modelo, int bateria, String zona) {
        return new DroneSuperficial(id, modelo, bateria, EstadoDrone.DISPONIBLE, zona);
    }

    public DroneSemisumergido crearSemisumergido(String id, String modelo, int bateria, String zona) {
        return new DroneSemisumergido(id, modelo, bateria, EstadoDrone.DISPONIBLE, zona);
    }

    public DroneBuceador crearBuceador(String id, String modelo, int bateria, String zona) {
        return new DroneBuceador(id, modelo, bateria, EstadoDrone.DISPONIBLE, zona);
    }
}
