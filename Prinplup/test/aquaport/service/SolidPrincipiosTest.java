package aquaport.service;

import aquaport.model.CondicionAgua;
import aquaport.model.DroneAcuatico;
import aquaport.model.DroneBuceador;
import aquaport.model.DroneSemisumergido;
import aquaport.model.DroneSuperficial;
import aquaport.model.EstadoDrone;
import aquaport.model.EstadoMision;
import aquaport.model.Mision;
import aquaport.model.Prioridad;
import aquaport.model.TipoCarga;
import aquaport.model.TipoDrone;
import aquaport.model.ZonasCampus;
import aquaport.service.observer.ObservadorFalloDrone;
import aquaport.service.strategy.EstrategiaSeleccionDrone;
import aquaport.service.strategy.MayorBateriaStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas formales de cumplimiento de los Principios SOLID en AquaPort v2 (Reto 04 Prinplup).
 */
class SolidPrincipiosTest {

    /**
     * Cuarto tipo de drone agregado para demostrar OCP (Open/Closed Principle).
     * No requiere modificar AsignadorAutomaticoMisiones ni el modelo base.
     */
    static class DroneSumergibleProfundo extends DroneAcuatico {
        public static final double CAPACIDAD_CARGA_PROFUNDA_KG = 2.500;

        public DroneSumergibleProfundo(String id, String modelo, int bateria, EstadoDrone estado, String zona) {
            super(id, modelo, bateria, estado, zona, CAPACIDAD_CARGA_PROFUNDA_KG);
        }

        @Override
        public TipoDrone getTipo() {
            return TipoDrone.BUCEADOR; // Especialización subacuática profunda
        }
    }

    // -------------------------------------------------------------------------
    // 1. OCP (Open/Closed Principle)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("OCP: Se agrega el 4to tipo de drone (DroneSumergibleProfundo) sin modificar AsignadorAutomaticoMisiones")
    void ocp_cuartoTipoDeDrone_funcionaSinModificarAsignador() {
        AsignadorAutomaticoMisiones asignador = new AsignadorAutomaticoMisiones(new MayorBateriaStrategy());

        // Flota con el nuevo tipo de drone extendido
        DroneAcuatico nuevoDrone = new DroneSumergibleProfundo(
                "DSP-01", "Abyssal Explorer 3000", 99, EstadoDrone.DISPONIBLE, ZonasCampus.BAHIA_CIENCIA
        );
        List<DroneAcuatico> flotaConNuevoTipo = List.of(nuevoDrone);

        // El asignador procesa el nuevo tipo sin ningún cambio en su código fuente
        Optional<DroneAcuatico> seleccionado = asignador.seleccionarDroneOptimo(
                flotaConNuevoTipo, ZonasCampus.BAHIA_CIENCIA, CondicionAgua.PROFUNDA, 0.250
        );

        assertTrue(seleccionado.isPresent());
        assertEquals("DSP-01", seleccionado.get().getId());
        assertEquals(2.500, seleccionado.get().getCapacidadCargaKg());
    }

    @Test
    @DisplayName("OCP: Se incorpora una nueva estrategia de selección sin modificar AsignadorAutomaticoMisiones")
    void ocp_nuevaEstrategiaDeSeleccion_seIntegraSinTocarAsignador() {
        // Nueva estrategia ad-hoc (por ejemplo: seleccionar el primer drone libre que cumpla)
        EstrategiaSeleccionDrone estrategiaPrimerDisponible = new EstrategiaSeleccionDrone() {
            @Override
            public Optional<DroneAcuatico> seleccionar(List<DroneAcuatico> candidatos, String zona,
                                                       CondicionAgua condicion, double pesoCargaKg, Prioridad prioridad) {
                return candidatos.stream()
                        .filter(d -> d.getCapacidadCargaKg() >= pesoCargaKg)
                        .findFirst();
            }

            @Override
            public String getNombre() {
                return "PrimerDisponibleStrategy";
            }
        };

        AsignadorAutomaticoMisiones asignador = new AsignadorAutomaticoMisiones(estrategiaPrimerDisponible);
        assertEquals("PrimerDisponibleStrategy", asignador.getEstrategia().getNombre());
    }

    // -------------------------------------------------------------------------
    // 2. LSP (Liskov Substitution Principle)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("LSP: Todas las subclases sustituyen a DroneAcuatico respetando contratos e invariantes")
    void lsp_todasLasSubclases_sustituyenADroneAcuaticoSinEfectosSecundarios() {
        List<DroneAcuatico> drones = List.of(
                new DroneSuperficial("DS-01", "Skimmer", 85, EstadoDrone.DISPONIBLE, ZonasCampus.EMBALSE_NORTE),
                new DroneSemisumergido("DM-01", "Ranger", 70, EstadoDrone.DISPONIBLE, ZonasCampus.CANAL_CENTRAL),
                new DroneBuceador("DB-01", "Probe", 90, EstadoDrone.DISPONIBLE, ZonasCampus.BAHIA_CIENCIA),
                new DroneSumergibleProfundo("DSP-01", "Abyssal", 95, EstadoDrone.DISPONIBLE, ZonasCampus.LAGUNA_SUR)
        );

        // Donde el sistema espera DroneAcuatico, cada subclase cumple invariantes idénticos
        for (DroneAcuatico drone : drones) {
            assertNotNull(drone.getId());
            assertNotNull(drone.getModelo());
            assertNotNull(drone.getZona());
            assertNotNull(drone.getTipo());
            assertTrue(drone.getBateria() >= 0 && drone.getBateria() <= 100);
            assertTrue(drone.getCapacidadCargaKg() > 0);
            assertTrue(drone.isDisponible());
            assertTrue(drone.isActivo());
            assertDoesNotThrow(drone::toString);
        }
    }

    // -------------------------------------------------------------------------
    // 3. DIP & ISP (Dependency Inversion & Interface Segregation)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("DIP & ISP: AsignadorAutomaticoMisiones depende exclusivamente de abstracciones segregadas")
    void dip_asignadorDependeDeInterfacesSegregadas() {
        // Observer segregado con interfaz de método único (ISP)
        ObservadorFalloDrone observadorMock = (drone, motivo) -> {
            // Contrato mínimo enfocado exclusivamente en notificar fallas
        };

        AsignadorAutomaticoMisiones asignador = new AsignadorAutomaticoMisiones();
        asignador.agregarObservador(observadorMock);

        assertEquals(1, asignador.getObservadores().size());
    }
}
