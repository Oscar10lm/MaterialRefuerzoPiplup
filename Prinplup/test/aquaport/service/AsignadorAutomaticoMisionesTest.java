package aquaport.service;

import aquaport.MainPrinplup;
import aquaport.model.CondicionAgua;
import aquaport.model.DroneAcuatico;
import aquaport.model.Mision;
import aquaport.model.Prioridad;
import aquaport.model.TipoCarga;
import aquaport.model.ZonasCampus;
import aquaport.model.EstadoDrone;
import aquaport.model.TipoDrone;
import aquaport.service.strategy.PrioridadCriticaStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
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

    @Test
    @DisplayName("RNF-01: El algoritmo de asignación automática selecciona el drone óptimo en menos de 400ms para 30 drones")
    void rnf01_rendimientoAsignacionAutomatica_menosDe400msPara30Drones() {
        List<DroneAcuatico> flota30 = new ArrayList<>(flota);
        // Generar una flota ampliada de 30 drones a partir de la fábrica
        FabricaDrones fabrica = new FabricaDrones();
        for (int i = 16; i <= 30; i++) {
            TipoDrone tipo = (i % 3 == 0) ? TipoDrone.BUCEADOR : (i % 2 == 0) ? TipoDrone.SEMISUMERGIDO : TipoDrone.SUPERFICIAL;
            flota30.add(fabrica.crearDrone(tipo, "D-EXP-" + i, "Exp-Model", 80, EstadoDrone.DISPONIBLE, ZonasCampus.EMBALSE_NORTE));
        }
        assertEquals(30, flota30.size());

        assertTimeout(Duration.ofMillis(400), () -> {
            Optional<DroneAcuatico> resultado = asignador.seleccionarDroneOptimo(
                    flota30, ZonasCampus.EMBALSE_NORTE, CondicionAgua.TRANQUILA, 0.250, Prioridad.NORMAL
            );
            assertTrue(resultado.isPresent());
        });
    }

    @Test
    @DisplayName("Resolución Tensión AP-07 vs AP-08: Misión CRÍTICA prioriza aptitud técnica y robustez sobre mayor batería bruta")
    void resolucionTension_ap07_vs_ap08_misionCriticaPriorizaAptitudTecnica() {
        // En Embalse Norte conviven:
        // DS-01 (Superficial, 95% batería) -> Sería elegido ciegamente por AP-07 (Mayor batería)
        // DM-03 (Semisumergido, 90% batería, alta resistencia estructural)
        AsignadorAutomaticoMisiones asignadorCritico = new AsignadorAutomaticoMisiones(new PrioridadCriticaStrategy());

        Optional<DroneAcuatico> seleccionadoCritico = asignadorCritico.seleccionarDroneOptimo(
                flota, ZonasCampus.EMBALSE_NORTE, CondicionAgua.TRANQUILA, 0.400, Prioridad.CRITICA
        );

        assertTrue(seleccionadoCritico.isPresent());
        // Se resuelve la tensión: Ante prioridad CRÍTICA, se favorece el Semisumergido por robustez (AP-08)
        // con reserva energética segura >= 50%, evitando elegir el dron superficial más frágil.
        assertEquals("DM-03", seleccionadoCritico.get().getId());
        assertEquals(TipoDrone.SEMISUMERGIDO, seleccionadoCritico.get().getTipo());
        assertTrue(seleccionadoCritico.get().getBateria() >= PrioridadCriticaStrategy.BATERIA_MINIMA_CRITICA);
    }
}
