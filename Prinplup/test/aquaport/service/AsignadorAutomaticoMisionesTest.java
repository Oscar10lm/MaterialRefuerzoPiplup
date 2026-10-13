package aquaport.service;

import aquaport.MainPrinplup;
import aquaport.model.CondicionAgua;
import aquaport.model.DroneAcuatico;
import aquaport.model.Mision;
import aquaport.model.Prioridad;
import aquaport.model.TipoCarga;
import aquaport.model.ZonasCampus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AsignadorAutomaticoMisionesTest {

    private AsignadorAutomaticoMisiones asignador;
    private List<DroneAcuatico> flota;

    @BeforeEach
    void setUp() {
        asignador = new AsignadorAutomaticoMisiones();
        flota = MainPrinplup.crearFlota15Drones();
    }

    @Test
    @DisplayName("Asigna de forma autónoma el drone con mayor batería apto para aguas tranquilas")
    void seleccionarDroneOptimo_aguasTranquilas_seleccionaMayorBateria() {
        // En Embalse Norte disponibles: DS-01 (95%, Superficial), DM-03 (90%, Semisumergido)
        Optional<DroneAcuatico> seleccionado = asignador.seleccionarDroneOptimo(
                flota, ZonasCampus.EMBALSE_NORTE, CondicionAgua.TRANQUILA, 0.300
        );

        assertTrue(seleccionado.isPresent());
        assertEquals("DS-01", seleccionado.get().getId());
        assertEquals(95, seleccionado.get().getBateria());
    }

    @Test
    @DisplayName("Filtra y asigna drone semisumergido para condición de agua agitada")
    void seleccionarDroneOptimo_aguasAgitadas_seleccionaSemisumergido() {
        // En Canal Central disponible con agua agitada: DM-01 (Semisumergido, 88%)
        Optional<DroneAcuatico> seleccionado = asignador.seleccionarDroneOptimo(
                flota, ZonasCampus.CANAL_CENTRAL, CondicionAgua.AGITADA, 1.000
        );

        assertTrue(seleccionado.isPresent());
        assertEquals("DM-01", seleccionado.get().getId());
    }

    @Test
    @DisplayName("Crea la misión automáticamente con el drone óptimo asignado")
    void asignarMisionAutomaticamente_creaMisionConDroneOptimo() {
        Mision mision = asignador.asignarMisionAutomaticamente(
                "M-AUTO-01", flota, ZonasCampus.BAHIA_CIENCIA, ZonasCampus.LAGUNA_SUR,
                CondicionAgua.PROFUNDA, TipoCarga.MUESTRA_AGUA, Prioridad.CRITICA
        );

        assertNotNull(mision);
        assertEquals("DB-01", mision.getDrone().getId());
        assertEquals(Prioridad.CRITICA, mision.getPrioridad());
    }

    @Test
    @DisplayName("Lanza IllegalStateException si no hay ningún drone disponible que cumpla los requisitos")
    void asignarMisionAutomaticamente_sinDronesAptos_lanzaExcepcion() {
        // En Bahía Ciencia solo hay Buceadores, por lo que condición AGITADA no tiene drones aptos
        assertThrows(IllegalStateException.class, () ->
                asignador.asignarMisionAutomaticamente(
                        "M-AUTO-02", flota, ZonasCampus.BAHIA_CIENCIA, ZonasCampus.LAGUNA_SUR,
                        CondicionAgua.AGITADA, TipoCarga.MUESTRA_AGUA, Prioridad.NORMAL
                ));
    }
}
