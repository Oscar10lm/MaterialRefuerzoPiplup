package aquaport.service;

import aquaport.model.CondicionAgua;
import aquaport.model.DroneAcuatico;
import aquaport.model.DroneSuperficial;
import aquaport.model.EstadoDrone;
import aquaport.model.EstadoMision;
import aquaport.model.Mision;
import aquaport.model.Prioridad;
import aquaport.model.TipoCarga;
import aquaport.model.ZonasCampus;
import aquaport.service.observer.ObservadorFalloDrone;
import aquaport.service.strategy.EstrategiaSeleccionDrone;
import aquaport.service.strategy.PrioridadCriticaStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Reto 12 · TDD con Mockito para AsignadorAutomaticoMisiones.
 *
 * Cubre exhaustivamente las 5 pruebas obligatorias:
 * 1. Asignación exitosa (Happy path).
 * 2. Misión CRÍTICA sin drones disponibles (notifica a observadores y lanza excepción).
 * 3. Drone en FALLO durante el proceso de asignación (notifica y selecciona un sustituto alternativo).
 * 4. Lista de drones vacía (lanza excepción por falta de flota apta).
 * 5. Batería exactamente en el umbral mínimo (caso edge de frontera: 35% y 50%).
 * Adicionalmente valida la notificación simultánea a múltiples observadores (Reto 03).
 */
@ExtendWith(MockitoExtension.class)
class AsignadorAutomaticoTDDTest {

    @Mock
    private EstrategiaSeleccionDrone estrategiaMock;

    @Mock
    private ObservadorFalloDrone observadorMock;

    @Mock
    private ObservadorFalloDrone observadorSecundarioMock;

    @InjectMocks
    private AsignadorAutomaticoMisiones asignador;

    private Mision crearMisionSolicitud(String id, String partida, String llegada, Prioridad prioridad, TipoCarga tipoCarga) {
        DroneAcuatico placeholder = new DroneSuperficial("D-TEMP", "Placeholder", 50, EstadoDrone.DISPONIBLE, partida);
        return new Mision(id, placeholder, partida, llegada, prioridad, tipoCarga, EstadoMision.PENDIENTE);
    }

    @Test
    @DisplayName("1. Happy path: Asignación exitosa retorna misión en estado PENDIENTE con drone asignado")
    void asignacionExitosa_happyPath_retornaMisionPendiente() {
        // ARRANGE
        DroneAcuatico droneOptimo = new DroneSuperficial("DS-01", "SurfaceMaster", 90, EstadoDrone.DISPONIBLE, ZonasCampus.EMBALSE_NORTE);
        List<DroneAcuatico> flota = List.of(droneOptimo);
        Mision mision = crearMisionSolicitud("M-TDD-01", ZonasCampus.EMBALSE_NORTE, ZonasCampus.LAGUNA_SUR, Prioridad.NORMAL, TipoCarga.MUESTRA_AGUA);

        when(estrategiaMock.seleccionar(any(), eq(ZonasCampus.EMBALSE_NORTE), eq(CondicionAgua.TRANQUILA), anyDouble(), eq(Prioridad.NORMAL)))
                .thenReturn(Optional.of(droneOptimo));

        asignador.registrarObservador(observadorMock);

        // ACT
        Mision resultado = asignador.asignar(mision, flota, CondicionAgua.TRANQUILA);

        // ASSERT
        assertNotNull(resultado);
        assertEquals("M-TDD-01", resultado.getId());
        assertEquals(droneOptimo, resultado.getDrone());
        assertEquals(EstadoMision.PENDIENTE, resultado.getEstado());
        verify(observadorMock, never()).notificarFallo(any(), anyString());
    }

    @Test
    @DisplayName("2. Misión CRÍTICA sin drones disponibles notifica alerta a todos los observadores y lanza excepción")
    void misionCritica_sinDronesDisponibles_notificaObservadoresYLanzaExcepcion() {
        // ARRANGE
        Mision misionCritica = crearMisionSolicitud("M-CRIT-99", ZonasCampus.EMBALSE_NORTE, ZonasCampus.LAGUNA_SUR, Prioridad.CRITICA, TipoCarga.EQUIPO_MEDICION);
        List<DroneAcuatico> flota = List.of();

        when(estrategiaMock.seleccionar(any(), anyString(), any(), anyDouble(), any()))
                .thenReturn(Optional.empty());
        when(estrategiaMock.getNombre()).thenReturn("EstrategiaMock");

        asignador.registrarObservador(observadorMock);
        asignador.registrarObservador(observadorSecundarioMock);

        // ACT & ASSERT
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                asignador.asignar(misionCritica, flota, CondicionAgua.AGITADA)
        );

        assertTrue(ex.getMessage().contains("Asignación automática fallida"));
        // Ambos observadores deben ser notificados exactamente una vez con la alerta de emergencia
        verify(observadorMock, times(1)).notificarFallo(isNull(), argThat(m -> m.contains("ALERTA CRÍTICA")));
        verify(observadorSecundarioMock, times(1)).notificarFallo(isNull(), argThat(m -> m.contains("ALERTA CRÍTICA")));
    }

    @Test
    @DisplayName("3. Drone en estado FALLO durante asignación es reportado, descartado y se asigna un alternativo")
    void asignar_droneEnFalloDuranteAsignacion_notificaYAsignaAlternativo() {
        // ARRANGE
        DroneAcuatico droneEnFallo = new DroneSuperficial("DS-FALLO", "SurfaceMaster", 80, EstadoDrone.FALLO, ZonasCampus.EMBALSE_NORTE);
        DroneAcuatico droneAlternativo = new DroneSuperficial("DS-ALT", "SurfaceMaster", 75, EstadoDrone.DISPONIBLE, ZonasCampus.EMBALSE_NORTE);

        List<DroneAcuatico> flota = new ArrayList<>(List.of(droneEnFallo, droneAlternativo));
        Mision mision = crearMisionSolicitud("M-FALLO-REC", ZonasCampus.EMBALSE_NORTE, ZonasCampus.LAGUNA_SUR, Prioridad.ALTA, TipoCarga.SENSOR);

        // Primera llamada retorna el drone en fallo; segunda llamada retorna el alternativo
        when(estrategiaMock.seleccionar(any(), anyString(), any(), anyDouble(), any()))
                .thenReturn(Optional.of(droneEnFallo))
                .thenReturn(Optional.of(droneAlternativo));

        asignador.registrarObservador(observadorMock);

        // ACT
        Mision resultado = asignador.asignar(mision, flota);

        // ASSERT
        assertNotNull(resultado);
        assertEquals("DS-ALT", resultado.getDrone().getId());
        assertEquals(EstadoMision.PENDIENTE, resultado.getEstado());

        // Verificamos que el observador fue notificado del descarte del drone averiado
        verify(observadorMock, times(1)).notificarFallo(eq(droneEnFallo), argThat(m -> m.contains("FALLO descartado")));
    }

    @Test
    @DisplayName("4. Lista de drones vacía lanza IllegalStateException")
    void asignar_flotaVacia_lanzaExcepcion() {
        // ARRANGE
        Mision mision = crearMisionSolicitud("M-VACIA", ZonasCampus.EMBALSE_NORTE, ZonasCampus.LAGUNA_SUR, Prioridad.NORMAL, TipoCarga.MUESTRA_AGUA);
        when(estrategiaMock.seleccionar(any(), anyString(), any(), anyDouble(), any()))
                .thenReturn(Optional.empty());
        when(estrategiaMock.getNombre()).thenReturn("EstrategiaMock");

        asignador.registrarObservador(observadorMock);

        // ACT & ASSERT
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                asignador.asignar(mision, List.of())
        );

        assertTrue(ex.getMessage().contains("No hay drone disponible apto"));
        verify(observadorMock, never()).notificarFallo(any(), anyString()); // Normal priority no notifica alerta de emergencia
    }

    @Test
    @DisplayName("5. Batería en el umbral exacto: 35% operativo es aceptado (edge case), 34% rechazado")
    void umbralBateriaMinima_casoEdgeOperativo() {
        // ARRANGE - Estrategia real para evaluar el filtrado real del Asignador
        AsignadorAutomaticoMisiones asignadorReal = new AsignadorAutomaticoMisiones();

        DroneAcuatico droneExacto35 = new DroneSuperficial("DS-35", "Model35", 35, EstadoDrone.DISPONIBLE, ZonasCampus.EMBALSE_NORTE);
        DroneAcuatico droneBajo34 = new DroneSuperficial("DS-34", "Model34", 34, EstadoDrone.DISPONIBLE, ZonasCampus.EMBALSE_NORTE);

        // ACT
        Optional<DroneAcuatico> opt35 = asignadorReal.seleccionarDroneOptimo(List.of(droneExacto35), ZonasCampus.EMBALSE_NORTE, CondicionAgua.TRANQUILA, 0.200);
        Optional<DroneAcuatico> opt34 = asignadorReal.seleccionarDroneOptimo(List.of(droneBajo34), ZonasCampus.EMBALSE_NORTE, CondicionAgua.TRANQUILA, 0.200);

        // ASSERT
        assertTrue(opt35.isPresent(), "Drone con batería exactamente al 35% debe ser aceptado.");
        assertEquals("DS-35", opt35.get().getId());
        assertTrue(opt34.isEmpty(), "Drone con batería al 34% debe ser descartado.");
    }

    @Test
    @DisplayName("5b. Batería en el umbral exacto para misiones CRÍTICAS: 50% aceptado, 49% descartado")
    void umbralBateriaMinimaCritica_casoEdgeCritico() {
        AsignadorAutomaticoMisiones asignadorCritico = new AsignadorAutomaticoMisiones(new PrioridadCriticaStrategy());

        DroneAcuatico droneExacto50 = new DroneSuperficial("DS-50", "Model50", 50, EstadoDrone.DISPONIBLE, ZonasCampus.EMBALSE_NORTE);
        DroneAcuatico droneBajo49 = new DroneSuperficial("DS-49", "Model49", 49, EstadoDrone.DISPONIBLE, ZonasCampus.EMBALSE_NORTE);

        Optional<DroneAcuatico> opt50 = asignadorCritico.seleccionarDroneOptimo(
                List.of(droneExacto50), ZonasCampus.EMBALSE_NORTE, CondicionAgua.TRANQUILA, 0.200, Prioridad.CRITICA
        );
        Optional<DroneAcuatico> opt49 = asignadorCritico.seleccionarDroneOptimo(
                List.of(droneBajo49), ZonasCampus.EMBALSE_NORTE, CondicionAgua.TRANQUILA, 0.200, Prioridad.CRITICA
        );

        assertTrue(opt50.isPresent(), "Drone con 50% debe ser aceptado para misión CRÍTICA.");
        assertEquals("DS-50", opt50.get().getId());
        assertTrue(opt49.isEmpty(), "Drone con 49% debe ser rechazado para misión CRÍTICA.");
    }

    @Test
    @DisplayName("Observer: registrarMision con drone en FALLO notifica a todos los observadores exactamente una vez")
    void registrarMision_droneEnFallo_notificaATodosLosObservadores() {
        DroneAcuatico droneEnFallo = new DroneSuperficial("DS-FAIL", "ModelF", 70, EstadoDrone.FALLO, ZonasCampus.EMBALSE_NORTE);
        Mision misionConFallo = new Mision("M-FAIL", droneEnFallo, ZonasCampus.EMBALSE_NORTE, ZonasCampus.LAGUNA_SUR, Prioridad.NORMAL, TipoCarga.MUESTRA_AGUA, EstadoMision.PENDIENTE);

        asignador.registrarObservador(observadorMock);
        asignador.registrarObservador(observadorSecundarioMock);

        assertThrows(IllegalStateException.class, () -> asignador.registrarMision(misionConFallo));

        verify(observadorMock, times(1)).notificarFallo(eq(droneEnFallo), argThat(m -> m.contains("FALLO")));
        verify(observadorSecundarioMock, times(1)).notificarFallo(eq(droneEnFallo), argThat(m -> m.contains("FALLO")));
    }
}
