package aquaport.service;

import aquaport.MainPrinplup;
import aquaport.model.DroneAcuatico;
import aquaport.model.TipoDrone;
import aquaport.model.ZonasCampus;
import aquaport.service.observer.CentroControlObserver;
import aquaport.service.observer.TecnicoMantenimientoObserver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias complementarias para observadores, servicios auxiliares y punto de entrada (Reto 13).
 */
class PrinplupServiciosYObservadoresTest {

    // -------------------------------------------------------------------------
    // 1. Observadores: Ramas defensivas con drone nulo
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Observadores: notificarFallo con drone nulo registra identificadores DESCONOCIDO y DESCONOCIDA")
    void observadores_droneNulo_manejaTernariosDefensivos() {
        CentroControlObserver centro = new CentroControlObserver();
        centro.notificarFallo(null, "Error anónimo de red");
        assertEquals(1, centro.getRegistroAlertas().size());
        assertTrue(centro.getRegistroAlertas().get(0).contains("DESCONOCIDO"));
        assertTrue(centro.getRegistroAlertas().get(0).contains("DESCONOCIDA"));

        TecnicoMantenimientoObserver tecnico = new TecnicoMantenimientoObserver();
        tecnico.notificarFallo(null, "Avería de baliza externa");
        assertEquals(1, tecnico.getOrdenesTrabajo().size());
        assertTrue(tecnico.getOrdenesTrabajo().get(0).contains("DESCONOCIDO"));
        assertTrue(tecnico.getOrdenesTrabajo().get(0).contains("DESCONOCIDA"));
    }

    // -------------------------------------------------------------------------
    // 2. ConsultorFlotaAvanzado: Validaciones defensivas de listas o argumentos nulos
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("ConsultorFlotaAvanzado: Métodos tolerantes a argumentos nulos retornan estructuras vacías")
    void consultorFlotaAvanzado_argumentosNulos() {
        ConsultorFlotaAvanzado consultor = new ConsultorFlotaAvanzado();
        List<DroneAcuatico> flota = MainPrinplup.crearFlota15Drones();

        // obtenerDroneOptimoParaZona 3 parámetros
        assertTrue(consultor.obtenerDroneOptimoParaZona(null, ZonasCampus.EMBALSE_NORTE, TipoDrone.SUPERFICIAL).isEmpty());
        assertTrue(consultor.obtenerDroneOptimoParaZona(flota, null, TipoDrone.SUPERFICIAL).isEmpty());
        assertTrue(consultor.obtenerDroneOptimoParaZona(flota, ZonasCampus.EMBALSE_NORTE, null).isEmpty());

        // obtenerDroneOptimoParaZona 2 parámetros
        assertTrue(consultor.obtenerDroneOptimoParaZona(null, ZonasCampus.EMBALSE_NORTE).isEmpty());
        assertTrue(consultor.obtenerDroneOptimoParaZona(flota, null).isEmpty());

        // calcularPromedioBateriaPorTipo
        assertTrue(consultor.calcularPromedioBateriaPorTipo(null).isEmpty());

        // separarMisionesCriticas
        Map<Boolean, List<aquaport.model.Mision>> particionadas = consultor.separarMisionesCriticas(null);
        assertNotNull(particionadas);
        assertTrue(particionadas.get(true).isEmpty());
        assertTrue(particionadas.get(false).isEmpty());

        // listarZonasUnicasFlotaActiva
        assertTrue(consultor.listarZonasUnicasFlotaActiva(null).isEmpty());

        // ordenarMisionesPorPrioridadYBateria
        assertTrue(consultor.ordenarMisionesPorPrioridadYBateria(null).isEmpty());
    }

    // -------------------------------------------------------------------------
    // 3. MainPrinplup y constructor utilitario
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("MainPrinplup: Ejecución completa de demostración y constructor privado")
    void mainPrinplup_ejecucionDemoYConstructor() throws Exception {
        // Demostración completa de consola sin excepciones
        assertDoesNotThrow(() -> MainPrinplup.main(new String[0]));

        // Constructor privado Sonar S1118
        Constructor<MainPrinplup> constructor = MainPrinplup.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        MainPrinplup instancia = constructor.newInstance();
        assertNotNull(instancia);
    }
}
