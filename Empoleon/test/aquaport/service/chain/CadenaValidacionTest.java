package aquaport.service.chain;

import aquaport.model.CondicionHidrica;
import aquaport.model.ContextoValidacion;
import aquaport.model.DroneAcuatico;
import aquaport.model.DroneBuceador;
import aquaport.model.DroneSuperficial;
import aquaport.model.EstadoDrone;
import aquaport.model.EstadoMision;
import aquaport.model.MisionEnterprise;
import aquaport.model.NivelAgua;
import aquaport.model.NivelTurbidez;
import aquaport.model.Prioridad;
import aquaport.model.ResultadoValidacion;
import aquaport.model.TipoCarga;
import aquaport.model.ZonasEnterprise;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CadenaValidacionTest {

    private ValidadorBateria valBateria;
    private ValidadorCapacidadCarga valCarga;
    private ValidadorZonaActiva valZona;
    private ValidadorCondicionesHidricas valCondicion;

    @BeforeEach
    void setUp() {
        valBateria = spy(new ValidadorBateria());
        valCarga = spy(new ValidadorCapacidadCarga());
        valZona = spy(new ValidadorZonaActiva());
        valCondicion = spy(new ValidadorCondicionesHidricas());

        valBateria.enlazarCon(valCarga)
                  .enlazarCon(valZona)
                  .enlazarCon(valCondicion);
    }

    @Test
    @DisplayName("Happy Path: Todos los eslabones pasan y la cadena retorna OK")
    void cadena_todosValidos_retornaOk() {
        DroneAcuatico drone = new DroneSuperficial("DS-01", "M1", 80, EstadoDrone.DISPONIBLE, ZonasEnterprise.RED_CANALES);
        MisionEnterprise mision = new MisionEnterprise("M1", ZonasEnterprise.RED_CANALES, ZonasEnterprise.LABORATORIO_CENTRAL,
                List.of(), drone, Prioridad.NORMAL, TipoCarga.MUESTRA_AGUA, EstadoMision.PENDIENTE);
        CondicionHidrica condicion = new CondicionHidrica(NivelAgua.TRANQUILO, NivelTurbidez.CLARA, 20, 1);

        ResultadoValidacion res = valBateria.validar(new ContextoValidacion(mision, drone, condicion));
        assertTrue(res.isExitoso());
        verify(valCondicion).validar(any());
    }

    @Test
    @DisplayName("La cadena se detiene en el primer validador que falla (Bateria)")
    void cadena_fallaBateria_detieneSiguientesEslabones() {
        DroneAcuatico droneDescargado = new DroneSuperficial("DS-01", "M1", 20, EstadoDrone.DISPONIBLE, ZonasEnterprise.RED_CANALES);
        MisionEnterprise mision = new MisionEnterprise("M1", ZonasEnterprise.RED_CANALES, ZonasEnterprise.LABORATORIO_CENTRAL,
                List.of(), droneDescargado, Prioridad.NORMAL, TipoCarga.MUESTRA_AGUA, EstadoMision.PENDIENTE);
        CondicionHidrica condicion = new CondicionHidrica(NivelAgua.TRANQUILO, NivelTurbidez.CLARA, 20, 1);

        ResultadoValidacion res = valBateria.validar(new ContextoValidacion(mision, droneDescargado, condicion));
        assertFalse(res.isExitoso());
        assertTrue(res.getMotivoRechazo().contains("Bateria insuficiente"));

        // Eslabones posteriores NO deben ser invocados
        verify(valCarga, never()).validar(any());
        verify(valZona, never()).validar(any());
        verify(valCondicion, never()).validar(any());
    }

    @Test
    @DisplayName("La cadena se detiene en ValidadorCapacidadCarga si la carga excede capacidad")
    void cadena_fallaCarga_detieneZonaYCondicion() {
        DroneAcuatico droneBuceador = new DroneBuceador("DB-01", "B1", 90, EstadoDrone.DISPONIBLE, ZonasEnterprise.EMBALSE_INVESTIGACION);
        MisionEnterprise misionPesada = new MisionEnterprise("M1", ZonasEnterprise.EMBALSE_INVESTIGACION, ZonasEnterprise.LABORATORIO_CENTRAL,
                List.of(), droneBuceador, Prioridad.NORMAL, TipoCarga.CARGA_PESADA, EstadoMision.PENDIENTE);
        CondicionHidrica condicion = new CondicionHidrica(NivelAgua.TRANQUILO, NivelTurbidez.CLARA, 20, 1);

        ResultadoValidacion res = valBateria.validar(new ContextoValidacion(misionPesada, droneBuceador, condicion));
        assertFalse(res.isExitoso());
        assertTrue(res.getMotivoRechazo().contains("Capacidad de carga excedida"));

        verify(valCarga).validar(any());
        verify(valZona, never()).validar(any());
        verify(valCondicion, never()).validar(any());
    }

    @Test
    @DisplayName("La cadena se detiene en ValidadorZonaActiva si la zona no coincide")
    void cadena_fallaZona_detieneCondicion() {
        DroneAcuatico drone = new DroneSuperficial("DS-01", "M1", 80, EstadoDrone.DISPONIBLE, ZonasEnterprise.RED_CANALES);
        MisionEnterprise mision = new MisionEnterprise("M1", ZonasEnterprise.LAGUNA_RESERVA, ZonasEnterprise.LABORATORIO_CENTRAL,
                List.of(), drone, Prioridad.NORMAL, TipoCarga.MUESTRA_AGUA, EstadoMision.PENDIENTE);
        CondicionHidrica condicion = new CondicionHidrica(NivelAgua.TRANQUILO, NivelTurbidez.CLARA, 20, 1);

        ResultadoValidacion res = valBateria.validar(new ContextoValidacion(mision, drone, condicion));
        assertFalse(res.isExitoso());
        assertTrue(res.getMotivoRechazo().contains("ubicado en"));
        verify(valCondicion, never()).validar(any());
    }

    @Test
    @DisplayName("La cadena rechaza en ValidadorCondicionesHidricas si el agua es peligrosa")
    void cadena_fallaCondicionHidrica_retornaRechazo() {
        DroneAcuatico superficial = new DroneSuperficial("DS-01", "M1", 80, EstadoDrone.DISPONIBLE, ZonasEnterprise.RED_CANALES);
        MisionEnterprise mision = new MisionEnterprise("M1", ZonasEnterprise.RED_CANALES, ZonasEnterprise.LABORATORIO_CENTRAL,
                List.of(), superficial, Prioridad.NORMAL, TipoCarga.MUESTRA_AGUA, EstadoMision.PENDIENTE);
        CondicionHidrica agitada = new CondicionHidrica(NivelAgua.AGITADO, NivelTurbidez.CLARA, 20, 1);

        ResultadoValidacion res = valBateria.validar(new ContextoValidacion(mision, superficial, agitada));
        assertFalse(res.isExitoso());
        assertTrue(res.getMotivoRechazo().contains("peligrosa para el tipo"));
    }

    @Test
    @DisplayName("ValidadorBateria evalua umbral del 50% en mision critica y ValidadorZonaActiva zona no catalogada")
    void validadores_ramasCriticasYZonaInvalida() {
        DroneAcuatico drone45 = new DroneSuperficial("DS-01", "M1", 45, EstadoDrone.DISPONIBLE, ZonasEnterprise.RED_CANALES);
        MisionEnterprise mCritica = new MisionEnterprise("MC", ZonasEnterprise.RED_CANALES, ZonasEnterprise.LABORATORIO_CENTRAL,
                List.of(), drone45, Prioridad.CRITICA, TipoCarga.MUESTRA_AGUA, EstadoMision.PENDIENTE);
        CondicionHidrica cond = new CondicionHidrica(NivelAgua.TRANQUILO, NivelTurbidez.CLARA, 20, 1);

        ResultadoValidacion rCrit = valBateria.validar(new ContextoValidacion(mCritica, drone45, cond));
        assertFalse(rCrit.isExitoso());
        assertTrue(rCrit.getMotivoRechazo().contains("50%"));

        MisionEnterprise mZonaInvalida = new MisionEnterprise("MZ", "Zona Desconocida", ZonasEnterprise.LABORATORIO_CENTRAL,
                List.of(), drone45, Prioridad.NORMAL, TipoCarga.MUESTRA_AGUA, EstadoMision.PENDIENTE);
        ResultadoValidacion rZona = new ValidadorZonaActiva().validar(new ContextoValidacion(mZonaInvalida, drone45, cond));
        assertFalse(rZona.isExitoso());
        assertTrue(rZona.getMotivoRechazo().contains("no catalogada"));
    }
}
