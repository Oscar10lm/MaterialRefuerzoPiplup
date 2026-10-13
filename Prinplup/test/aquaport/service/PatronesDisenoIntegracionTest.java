package aquaport.service;

import aquaport.model.CondicionAgua;
import aquaport.model.DroneAcuatico;
import aquaport.model.DroneSuperficial;
import aquaport.model.EstadoDrone;
import aquaport.model.EstadoMision;
import aquaport.model.Mision;
import aquaport.model.Prioridad;
import aquaport.model.TipoCarga;
import aquaport.model.TipoDrone;
import aquaport.model.ZonasCampus;
import aquaport.service.observer.CentroControlObserver;
import aquaport.service.observer.ObservadorFalloDrone;
import aquaport.service.observer.TecnicoMantenimientoObserver;
import aquaport.service.strategy.MayorBateriaStrategy;
import aquaport.service.strategy.PrioridadCriticaStrategy;
import aquaport.service.strategy.ZonaCercanaStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Pruebas de integración de los 3 Patrones de Diseño (Factory Method, Strategy, Observer)
 * con verificación estricta mediante Mockito (Reto 03 Prinplup).
 */
@ExtendWith(MockitoExtension.class)
class PatronesDisenoIntegracionTest {

    private FabricaDrones fabrica;
    private AsignadorAutomaticoMisiones asignador;

    @Mock
    private ObservadorFalloDrone mockCentroControl;

    @Mock
    private ObservadorFalloDrone mockTecnicoMantenimiento;

    @BeforeEach
    void setUp() {
        fabrica = new FabricaDrones();
        asignador = new AsignadorAutomaticoMisiones();
    }

    // -------------------------------------------------------------------------
    // 1. Patrón Factory Method
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Factory Method: FabricaDrones instancia correctamente los 3 tipos especializados")
    void fabricaDrones_creaLos3TiposEspecializados() {
        DroneAcuatico superficial = fabrica.crearDrone(TipoDrone.SUPERFICIAL, "DS-10", "Skimmer", 90, EstadoDrone.DISPONIBLE, ZonasCampus.EMBALSE_NORTE);
        DroneAcuatico semisumergido = fabrica.crearDrone(TipoDrone.SEMISUMERGIDO, "DM-10", "Ranger", 85, EstadoDrone.DISPONIBLE, ZonasCampus.CANAL_CENTRAL);
        DroneAcuatico buceador = fabrica.crearDrone(TipoDrone.BUCEADOR, "DB-10", "Probe", 80, EstadoDrone.DISPONIBLE, ZonasCampus.BAHIA_CIENCIA);

        assertEquals(TipoDrone.SUPERFICIAL, superficial.getTipo());
        assertEquals(0.500, superficial.getCapacidadCargaKg());

        assertEquals(TipoDrone.SEMISUMERGIDO, semisumergido.getTipo());
        assertEquals(1.500, semisumergido.getCapacidadCargaKg());

        assertEquals(TipoDrone.BUCEADOR, buceador.getTipo());
        assertEquals(0.300, buceador.getCapacidadCargaKg());
    }

    // -------------------------------------------------------------------------
    // 2. Patrón Strategy
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Strategy: Asignador cambia dinámicamente de estrategia sin usar instanceof")
    void asignador_cambiaEstrategiaDinamica_sinInstanceOf() {
        DroneAcuatico d1 = fabrica.crearSuperficial("DS-01", "Skimmer S1", 95, ZonasCampus.EMBALSE_NORTE);
        DroneAcuatico d2 = fabrica.crearSemisumergido("DM-01", "Ranger M1", 80, ZonasCampus.EMBALSE_NORTE);
        List<DroneAcuatico> flota = List.of(d1, d2);

        // Estrategia 1: MayorBateriaStrategy -> Elige DS-01 (95%)
        asignador.setEstrategia(new MayorBateriaStrategy());
        Optional<DroneAcuatico> opt1 = asignador.seleccionarDroneOptimo(flota, ZonasCampus.EMBALSE_NORTE, CondicionAgua.TRANQUILA, 0.300);
        assertTrue(opt1.isPresent());
        assertEquals("DS-01", opt1.get().getId());

        // Estrategia 2: PrioridadCriticaStrategy -> Prioriza Semisumergido DM-01 por robustez
        asignador.setEstrategia(new PrioridadCriticaStrategy());
        Optional<DroneAcuatico> opt2 = asignador.seleccionarDroneOptimo(flota, ZonasCampus.EMBALSE_NORTE, CondicionAgua.TRANQUILA, 0.300, Prioridad.CRITICA);
        assertTrue(opt2.isPresent());
        assertEquals("DM-01", opt2.get().getId());

        // Estrategia 3: ZonaCercanaStrategy
        asignador.setEstrategia(new ZonaCercanaStrategy());
        assertNotNull(asignador.getEstrategia());
        assertEquals("ZonaCercanaStrategy", asignador.getEstrategia().getNombre());
    }

    // -------------------------------------------------------------------------
    // 3. Patrón Observer con Mockito
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Observer: Al registrar misión con drone en FALLO, todos los observadores son notificados exactamente una vez")
    void registrarMisionConDroneEnFallo_todosLosObservadoresSonNotificadosExactamenteUnaVez() {
        // ARRANGE: Registrar observadores mockeados en el asignador
        asignador.agregarObservador(mockCentroControl);
        asignador.agregarObservador(mockTecnicoMantenimiento);

        // Crear drone en estado FALLO usando la fábrica
        DroneAcuatico droneEnFallo = fabrica.crearDrone(
                TipoDrone.SUPERFICIAL, "DS-ERROR", "Aqua-Skimmer Defectuoso", 0, EstadoDrone.FALLO, ZonasCampus.EMBALSE_NORTE
        );

        Mision misionInvalida = new Mision(
                "MIS-FALLO-99", droneEnFallo, ZonasCampus.EMBALSE_NORTE, ZonasCampus.LABORATORIO_HIDRICO,
                Prioridad.NORMAL, TipoCarga.MUESTRA_AGUA, EstadoMision.PENDIENTE
        );

        // ACT: Registrar misión con drone averiado debe fallar
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> asignador.registrarMision(misionInvalida)
        );
        assertTrue(exception.getMessage().contains("FALLO"));

        // ASSERT: Verificar con Mockito que cada observador fue notificado exactamente 1 vez
        verify(mockCentroControl, times(1)).notificarFallo(eq(droneEnFallo), anyString());
        verify(mockTecnicoMantenimiento, times(1)).notificarFallo(eq(droneEnFallo), anyString());

        // Garantizar que no hubo llamadas adicionales imprevistas
        verifyNoMoreInteractions(mockCentroControl);
        verifyNoMoreInteractions(mockTecnicoMantenimiento);
    }

    @Test
    @DisplayName("Observer: Los observadores pueden desuscribirse dinámicamente y dejan de recibir alertas")
    void desuscribirObservador_noRecibeNotificacion() {
        asignador.agregarObservador(mockCentroControl);
        asignador.agregarObservador(mockTecnicoMantenimiento);
        asignador.removerObservador(mockTecnicoMantenimiento);

        DroneAcuatico droneEnFallo = fabrica.crearDrone(
                TipoDrone.BUCEADOR, "DB-FALLO", "Deep Probe Roto", 10, EstadoDrone.FALLO, ZonasCampus.BAHIA_CIENCIA
        );
        Mision mision = new Mision("MIS-99", droneEnFallo, ZonasCampus.BAHIA_CIENCIA, ZonasCampus.LAGUNA_SUR,
                Prioridad.ALTA, TipoCarga.MUESTRA_AGUA, EstadoMision.PENDIENTE);

        assertThrows(IllegalStateException.class, () -> asignador.registrarMision(mision));

        verify(mockCentroControl, times(1)).notificarFallo(eq(droneEnFallo), anyString());
        // mockTecnicoMantenimiento fue removido, no debe haber recibido ninguna notificación
        verifyNoMoreInteractions(mockTecnicoMantenimiento);
    }

    @Test
    @DisplayName("Observer Concreto: CentroControlObserver y TecnicoMantenimientoObserver registran el evento en sus listas internas")
    void observadoresConcretos_registranEventoInternamente() {
        CentroControlObserver centroReal = new CentroControlObserver();
        TecnicoMantenimientoObserver tecnicoReal = new TecnicoMantenimientoObserver();

        asignador.agregarObservador(centroReal);
        asignador.agregarObservador(tecnicoReal);

        DroneAcuatico droneFallo = fabrica.crearDrone(
                TipoDrone.SEMISUMERGIDO, "DM-F1", "Ranger Falla", 5, EstadoDrone.FALLO, ZonasCampus.CANAL_CENTRAL
        );
        Mision mision = new Mision("MIS-REAL", droneFallo, ZonasCampus.CANAL_CENTRAL, ZonasCampus.PUNTO_RIBERENO_ESTE,
                Prioridad.NORMAL, TipoCarga.SENSOR, EstadoMision.PENDIENTE);

        assertThrows(IllegalStateException.class, () -> asignador.registrarMision(mision));

        assertEquals(1, centroReal.getRegistroAlertas().size());
        assertTrue(centroReal.getRegistroAlertas().get(0).contains("DM-F1"));

        assertEquals(1, tecnicoReal.getOrdenesTrabajo().size());
        assertTrue(tecnicoReal.getOrdenesTrabajo().get(0).contains("DM-F1"));
    }
}
