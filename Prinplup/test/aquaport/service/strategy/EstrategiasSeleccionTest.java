package aquaport.service.strategy;

import aquaport.MainPrinplup;
import aquaport.model.CondicionAgua;
import aquaport.model.DroneAcuatico;
import aquaport.model.DroneBuceador;
import aquaport.model.DroneSemisumergido;
import aquaport.model.DroneSuperficial;
import aquaport.model.EstadoDrone;
import aquaport.model.Prioridad;
import aquaport.model.TipoDrone;
import aquaport.model.ZonasCampus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias de cobertura exhaustiva para las tres estrategias de selección (Reto 13).
 * Elimina todas las ramas amarillas y líneas no cubiertas en aquaport.service.strategy.
 */
class EstrategiasSeleccionTest {

    private List<DroneAcuatico> flota;

    @BeforeEach
    void setUp() {
        flota = MainPrinplup.crearFlota15Drones();
    }

    // -------------------------------------------------------------------------
    // 1. MayorBateriaStrategy
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("MayorBateriaStrategy: Manejo de nulls (candidatos, zona, condición) y filtrado por capacidad")
    void mayorBateriaStrategy_coberturaRamas() {
        MayorBateriaStrategy strategy = new MayorBateriaStrategy();
        assertEquals("MayorBateriaStrategy", strategy.getNombre());

        // Rama: candidatos == null
        Optional<DroneAcuatico> resNull = strategy.seleccionar(null, ZonasCampus.EMBALSE_NORTE, CondicionAgua.TRANQUILA, 0.200, Prioridad.NORMAL);
        assertTrue(resNull.isEmpty());

        // Rama: zona == null (acepta de cualquier zona)
        Optional<DroneAcuatico> resSinZona = strategy.seleccionar(flota, null, CondicionAgua.TRANQUILA, 0.200, Prioridad.NORMAL);
        assertTrue(resSinZona.isPresent());
        assertEquals(95, resSinZona.get().getBateria()); // DS-01 tiene 95%

        // Rama: condicion == null (acepta cualquier tipo)
        Optional<DroneAcuatico> resSinCondicion = strategy.seleccionar(flota, ZonasCampus.EMBALSE_NORTE, null, 0.200, Prioridad.NORMAL);
        assertTrue(resSinCondicion.isPresent());

        // Rama: capacidadCargaKg insuficiente (rechaza drone)
        // DS-01 soporta 0.5kg y DM-03 soporta 1.5kg en Embalse Norte
        Optional<DroneAcuatico> resCargaPesada = strategy.seleccionar(flota, ZonasCampus.EMBALSE_NORTE, CondicionAgua.TRANQUILA, 1.200, Prioridad.NORMAL);
        assertTrue(resCargaPesada.isPresent());
        assertEquals("DM-03", resCargaPesada.get().getId()); // Solo el semisumergido soporta 1.2kg

        // Carga imposible (>1.5kg)
        Optional<DroneAcuatico> resCargaImposible = strategy.seleccionar(flota, ZonasCampus.EMBALSE_NORTE, CondicionAgua.TRANQUILA, 5.000, Prioridad.NORMAL);
        assertTrue(resCargaImposible.isEmpty());
    }

    // -------------------------------------------------------------------------
    // 2. PrioridadCriticaStrategy
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("PrioridadCriticaStrategy: Umbral de batería >= 50%, ordenamiento Semisumergido y ramas null")
    void prioridadCriticaStrategy_coberturaRamas() {
        PrioridadCriticaStrategy strategy = new PrioridadCriticaStrategy();
        assertEquals("PrioridadCriticaStrategy", strategy.getNombre());

        // Rama: candidatos == null
        Optional<DroneAcuatico> resNull = strategy.seleccionar(null, ZonasCampus.EMBALSE_NORTE, CondicionAgua.TRANQUILA, 0.200, Prioridad.CRITICA);
        assertTrue(resNull.isEmpty());

        // Rama: zona == null y condicion == null
        Optional<DroneAcuatico> resSinZonaNiCondicion = strategy.seleccionar(flota, null, null, 0.200, Prioridad.CRITICA);
        assertTrue(resSinZonaNiCondicion.isPresent());

        // Rama: Batería inferior a 50% rechazada
        DroneAcuatico drone40 = new DroneSemisumergido("DM-40", "Ranger", 40, EstadoDrone.DISPONIBLE, ZonasCampus.DARSENA_OESTE);
        Optional<DroneAcuatico> resBajaBat = strategy.seleccionar(List.of(drone40), ZonasCampus.DARSENA_OESTE, CondicionAgua.AGITADA, 0.500, Prioridad.CRITICA);
        assertTrue(resBajaBat.isEmpty()); // 40% < 50% mínimo

        // Rama: Condición incompatible
        DroneAcuatico supIncomp = new DroneSuperficial("DS-INCOMP-CRIT", "Skimmer", 99, EstadoDrone.DISPONIBLE, ZonasCampus.EMBALSE_NORTE);
        Optional<DroneAcuatico> resIncomp = strategy.seleccionar(List.of(supIncomp), ZonasCampus.EMBALSE_NORTE, CondicionAgua.AGITADA, 0.200, Prioridad.CRITICA);
        assertTrue(resIncomp.isEmpty());

        // Rama: Capacidad de carga insuficiente
        DroneAcuatico buceadorLigero = new DroneBuceador("DB-80", "Probe", 80, EstadoDrone.DISPONIBLE, ZonasCampus.BAHIA_CIENCIA);
        Optional<DroneAcuatico> resSobrecarga = strategy.seleccionar(List.of(buceadorLigero), ZonasCampus.BAHIA_CIENCIA, CondicionAgua.PROFUNDA, 0.800, Prioridad.CRITICA);
        assertTrue(resSobrecarga.isEmpty()); // 0.800kg > 0.300kg capacidad

        // Comparador: Semisumergido vs Buceador/Superficial en empate o menor batería
        DroneAcuatico sup99 = new DroneSuperficial("DS-99", "Skimmer", 99, EstadoDrone.DISPONIBLE, ZonasCampus.EMBALSE_NORTE);
        DroneAcuatico semi60 = new DroneSemisumergido("DM-60", "Ranger", 60, EstadoDrone.DISPONIBLE, ZonasCampus.EMBALSE_NORTE);
        Optional<DroneAcuatico> optSemi = strategy.seleccionar(List.of(sup99, semi60), ZonasCampus.EMBALSE_NORTE, CondicionAgua.TRANQUILA, 0.300, Prioridad.CRITICA);
        assertTrue(optSemi.isPresent());
        assertEquals("DM-60", optSemi.get().getId()); // Prioriza Semisumergido a pesar de menor batería que el superficial
    }

    // -------------------------------------------------------------------------
    // 3. ZonaCercanaStrategy
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("ZonaCercanaStrategy: Prioriza proximidad exacta, desempata por batería y maneja ramas null")
    void zonaCercanaStrategy_coberturaRamas() {
        ZonaCercanaStrategy strategy = new ZonaCercanaStrategy();
        assertEquals("ZonaCercanaStrategy", strategy.getNombre());

        // Rama: candidatos == null
        Optional<DroneAcuatico> resNull = strategy.seleccionar(null, ZonasCampus.EMBALSE_NORTE, CondicionAgua.TRANQUILA, 0.200, Prioridad.NORMAL);
        assertTrue(resNull.isEmpty());

        // Rama: Priorizar drone en la misma zona exacta sobre otro más cargado en zona lejana
        DroneAcuatico droneEnZona = new DroneSemisumergido("DM-LOCAL", "Ranger", 60, EstadoDrone.DISPONIBLE, ZonasCampus.EMBALSE_NORTE);
        DroneAcuatico droneLejano = new DroneSemisumergido("DM-LEJOS", "Ranger", 95, EstadoDrone.DISPONIBLE, ZonasCampus.LAGUNA_SUR);
        Optional<DroneAcuatico> resProximidad = strategy.seleccionar(List.of(droneLejano, droneEnZona), ZonasCampus.EMBALSE_NORTE, CondicionAgua.AGITADA, 0.500, Prioridad.NORMAL);
        assertTrue(resProximidad.isPresent());
        assertEquals("DM-LOCAL", resProximidad.get().getId());

        // Rama: Dos drones en la misma zona desempata por mayor batería
        DroneAcuatico droneEnZonaMenor = new DroneSemisumergido("DM-ZONA-1", "Ranger", 70, EstadoDrone.DISPONIBLE, ZonasCampus.EMBALSE_NORTE);
        DroneAcuatico droneEnZonaMayor = new DroneSemisumergido("DM-ZONA-2", "Ranger", 90, EstadoDrone.DISPONIBLE, ZonasCampus.EMBALSE_NORTE);
        Optional<DroneAcuatico> resMayorBatLocal = strategy.seleccionar(List.of(droneEnZonaMenor, droneEnZonaMayor), ZonasCampus.EMBALSE_NORTE, CondicionAgua.AGITADA, 0.500, Prioridad.NORMAL);
        assertTrue(resMayorBatLocal.isPresent());
        assertEquals("DM-ZONA-2", resMayorBatLocal.get().getId());

        // Rama: zona == null (todos se tratan igual y desempata batería)
        Optional<DroneAcuatico> resZonaNull = strategy.seleccionar(List.of(droneEnZonaMenor, droneEnZonaMayor), null, CondicionAgua.AGITADA, 0.500, Prioridad.NORMAL);
        assertTrue(resZonaNull.isPresent());
        assertEquals("DM-ZONA-2", resZonaNull.get().getId());

        // Rama: condicion == null (ignora compatibilidad)
        Optional<DroneAcuatico> resCondicionNull = strategy.seleccionar(flota, ZonasCampus.EMBALSE_NORTE, null, 0.200, Prioridad.NORMAL);
        assertTrue(resCondicionNull.isPresent());

        // Rama: condicion incompatible filtra drones no aptos
        DroneAcuatico droneSuperficialIncompatible = new DroneSuperficial("DS-INCOMP", "Skimmer", 99, EstadoDrone.DISPONIBLE, ZonasCampus.EMBALSE_NORTE);
        Optional<DroneAcuatico> resCondIncomp = strategy.seleccionar(List.of(droneSuperficialIncompatible), ZonasCampus.EMBALSE_NORTE, CondicionAgua.AGITADA, 0.200, Prioridad.NORMAL);
        assertTrue(resCondIncomp.isEmpty());

        // Rama: Capacidad de carga insuficiente
        Optional<DroneAcuatico> resCargaExcedida = strategy.seleccionar(List.of(droneEnZona), ZonasCampus.EMBALSE_NORTE, CondicionAgua.AGITADA, 5.000, Prioridad.NORMAL);
        assertTrue(resCargaExcedida.isEmpty());
    }
}
