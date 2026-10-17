package aquaport.service;

import aquaport.model.AlertaFlota;
import aquaport.model.DroneAcuatico;
import aquaport.model.DroneSuperficial;
import aquaport.model.EstadoDrone;
import aquaport.model.ZonasCampus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CentroControlAlertasTest {

    private CentroControlAlertas centroControl;

    @BeforeEach
    void setUp() {
        centroControl = new CentroControlAlertas();
    }

    @Test
    @DisplayName("Genera alerta CRÍTICA cuando un drone tiene batería < 20%")
    void evaluarEstadoDrone_bateriaMenorA20_generaAlertaCritica() {
        DroneAcuatico droneBateriaCritica = new DroneSuperficial(
                "DS-04", "Aqua-Skimmer S2", 15, EstadoDrone.RECARGANDO, ZonasCampus.EMBALSE_NORTE
        );

        Optional<AlertaFlota> alerta = centroControl.evaluarEstadoDrone(droneBateriaCritica);

        assertTrue(alerta.isPresent());
        assertTrue(alerta.get().isCritica());
        assertEquals("DS-04", alerta.get().droneId());
        assertEquals(ZonasCampus.EMBALSE_NORTE, alerta.get().zona());
    }

    @Test
    @DisplayName("Genera alerta de ADVERTENCIA cuando un drone entra en MANTENIMIENTO")
    void evaluarEstadoDrone_mantenimiento_generaAlertaAdvertencia() {
        DroneAcuatico droneMantenimiento = new DroneSuperficial(
                "DS-05", "Aqua-Skimmer S1", 50, EstadoDrone.MANTENIMIENTO, ZonasCampus.LABORATORIO_HIDRICO
        );

        Optional<AlertaFlota> alerta = centroControl.evaluarEstadoDrone(droneMantenimiento);

        assertTrue(alerta.isPresent());
        assertEquals(AlertaFlota.NivelAlerta.ADVERTENCIA, alerta.get().nivel());
    }

    @Test
    @DisplayName("No genera alerta si el drone opera en estado normal con buena batería")
    void evaluarEstadoDrone_operacionNormal_noGeneraAlerta() {
        DroneAcuatico droneNormal = new DroneSuperficial(
                "DS-01", "Aqua-Skimmer S1", 95, EstadoDrone.DISPONIBLE, ZonasCampus.EMBALSE_NORTE
        );

        Optional<AlertaFlota> alerta = centroControl.evaluarEstadoDrone(droneNormal);

        assertFalse(alerta.isPresent());
    }

    @Test
    @DisplayName("Permite consultar el listado exclusivo de alertas críticas registradas")
    void obtenerAlertasCriticas_filtraSoloCriticas() {
        centroControl.emitirAlerta("D-01", AlertaFlota.NivelAlerta.INFO, ZonasCampus.CANAL_CENTRAL, "Operación iniciada");
        centroControl.emitirAlerta("D-02", AlertaFlota.NivelAlerta.CRITICA, ZonasCampus.LAGUNA_SUR, "Pérdida de señal");
        centroControl.emitirAlerta("D-03", AlertaFlota.NivelAlerta.ADVERTENCIA, ZonasCampus.EMBALSE_NORTE, "Viento moderado");

        List<AlertaFlota> criticas = centroControl.obtenerAlertasCriticas();

        assertEquals(1, criticas.size());
        assertEquals("D-02", criticas.get(0).droneId());
    }

    @Test
    @DisplayName("Genera alerta CRÍTICA cuando un drone entra en FALLO")
    void evaluarEstadoDrone_fallo_generaAlertaCritica() {
        DroneAcuatico droneFallo = new DroneSuperficial(
                "DS-09", "Aqua-Skimmer S1", 80, EstadoDrone.FALLO, ZonasCampus.CANAL_CENTRAL
        );

        Optional<AlertaFlota> alerta = centroControl.evaluarEstadoDrone(droneFallo);

        assertTrue(alerta.isPresent());
        assertTrue(alerta.get().isCritica());
        assertEquals(AlertaFlota.NivelAlerta.CRITICA, alerta.get().nivel());
        assertTrue(alerta.get().mensaje().contains("FALLO crítico"));
    }

    @Test
    @DisplayName("Drone nulo retorna Optional.empty sin fallar")
    void evaluarEstadoDrone_droneNulo_retornaVacio() {
        assertTrue(centroControl.evaluarEstadoDrone(null).isEmpty());
    }
}
