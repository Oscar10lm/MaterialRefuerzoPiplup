package aquaport.service;

import aquaport.model.DroneAcuatico;
import aquaport.model.DroneBuceador;
import aquaport.model.DroneSemisumergido;
import aquaport.model.DroneSuperficial;
import aquaport.model.EstadoDrone;
import aquaport.model.TipoDrone;
import aquaport.model.ZonasCampus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricaDronesTest {

    private FabricaDrones fabrica;

    @BeforeEach
    void setUp() {
        fabrica = new FabricaDrones();
    }

    @Test
    @DisplayName("Crea correctamente un DroneSuperficial con sus capacidades específicas")
    void crearSuperficial_retornaInstanciaSuperficial() {
        DroneAcuatico drone = fabrica.crearDrone(TipoDrone.SUPERFICIAL, "DS-99", "Skimmer Test", 85, EstadoDrone.DISPONIBLE, ZonasCampus.EMBALSE_NORTE);

        assertInstanceOf(DroneSuperficial.class, drone);
        assertEquals(TipoDrone.SUPERFICIAL, drone.getTipo());
        assertEquals(0.500, drone.getCapacidadCargaKg());
        assertTrue(drone.isDisponible());
    }

    @Test
    @DisplayName("Crea correctamente un DroneSemisumergido")
    void crearSemisumergido_retornaInstanciaSemisumergido() {
        DroneAcuatico drone = fabrica.crearDrone(TipoDrone.SEMISUMERGIDO, "DM-99", "Ranger Test", 90, EstadoDrone.DISPONIBLE, ZonasCampus.CANAL_CENTRAL);

        assertInstanceOf(DroneSemisumergido.class, drone);
        assertEquals(TipoDrone.SEMISUMERGIDO, drone.getTipo());
        assertEquals(1.500, drone.getCapacidadCargaKg());
    }

    @Test
    @DisplayName("Crea correctamente un DroneBuceador")
    void crearBuceador_retornaInstanciaBuceador() {
        DroneAcuatico drone = fabrica.crearDrone(TipoDrone.BUCEADOR, "DB-99", "Probe Test", 70, EstadoDrone.DISPONIBLE, ZonasCampus.BAHIA_CIENCIA);

        assertInstanceOf(DroneBuceador.class, drone);
        assertEquals(TipoDrone.BUCEADOR, drone.getTipo());
        assertEquals(0.300, drone.getCapacidadCargaKg());
    }

    @Test
    @DisplayName("Tipo nulo arroja NullPointerException")
    void crearDrone_tipoNulo_lanzaExcepcion() {
        assertThrows(NullPointerException.class, () ->
                fabrica.crearDrone(null, "D-01", "Model", 50, EstadoDrone.DISPONIBLE, ZonasCampus.EMBALSE_NORTE));
    }
}
