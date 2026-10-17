package aquaport.service.decorator;

import aquaport.model.DroneAcuatico;
import aquaport.model.DroneBuceador;
import aquaport.model.EstadoDrone;
import aquaport.model.TipoDrone;
import aquaport.model.ZonasEnterprise;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DroneDecoratorTest {

    private DroneAcuatico droneBase;

    @BeforeEach
    void setUp() {
        droneBase = new DroneBuceador("DB-01", "DeepProbe-E3", 85, EstadoDrone.DISPONIBLE, ZonasEnterprise.EMBALSE_INVESTIGACION);
    }

    @Test
    @DisplayName("DroneConMonitoreo enriquece telemetria manteniendo invariantes del drone")
    void droneConMonitoreo_registraTelemetriaSinModificarBase() {
        DroneConMonitoreo monitoreado = new DroneConMonitoreo(droneBase);

        assertEquals("DB-01", monitoreado.getId());
        assertEquals("DeepProbe-E3", monitoreado.getModelo());
        assertEquals(85, monitoreado.getBateria());
        assertEquals(TipoDrone.BUCEADOR, monitoreado.getTipo());
        assertEquals(0, monitoreado.getTotalTransmisiones());

        String telemetria = monitoreado.transmitirTelemetria();
        assertTrue(telemetria.contains("ID=DB-01"));
        assertTrue(telemetria.contains("MONITOREO=ACTIVO"));
        assertEquals(1, monitoreado.getTotalTransmisiones());
        assertEquals(droneBase, monitoreado.getDroneDecorado());
    }

    @Test
    @DisplayName("DroneConCifrado encripta payload en base64 sin modificar propiedades")
    void droneConCifrado_encriptaSinAlterarComportamiento() {
        DroneConCifrado cifrado = new DroneConCifrado(droneBase);

        assertTrue(cifrado.isCifrado());
        assertEquals(TipoDrone.BUCEADOR, cifrado.getTipo());
        String transmision = cifrado.transmitirTelemetria();
        assertTrue(transmision.startsWith("[ENCRYPTED:AES-256:"));
        assertTrue(transmision.endsWith("]"));
    }

    @Test
    @DisplayName("Apilamiento: DroneConCifrado(DroneConMonitoreo(drone)) combina ambas capacidades")
    void apilamientoDecorators_combinaMonitoreoYCifrado() {
        DroneConMonitoreo monitoreado = new DroneConMonitoreo(droneBase);
        DroneConCifrado apilado = new DroneConCifrado(monitoreado);

        assertEquals("DB-01", apilado.getId());
        assertEquals(TipoDrone.BUCEADOR, apilado.getTipo());
        assertTrue(apilado.isCifrado());

        String resultado = apilado.transmitirTelemetria();
        assertTrue(resultado.startsWith("[ENCRYPTED:AES-256:"));
        assertEquals(1, monitoreado.getTotalTransmisiones());
        assertTrue(monitoreado.getBitacoraTelemetria().get(0).contains("MONITOREO=ACTIVO"));
    }
}
