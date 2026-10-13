package aquaport.service;

import aquaport.model.DroneAcuatico;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas TDD (patrón AAA) para {@link ValidadorMision}.
 * Cada prueba verifica UN solo comportamiento.
 */
class ValidadorMisionTest {

    private ValidadorMision v;

    @BeforeEach
    void setUp() {
        v = new ValidadorMision();
    }

    // ---------------------------------------------------------------
    // Batería
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Drone con batería ≥ 35% puede ser asignado")
    void droneBateriaSuficiente_puedeAsignarse() {
        // ARRANGE
        DroneAcuatico d = new DroneAcuatico("AR-01", "Aqua-Ranger 100", 85, true, "Embalse Norte");
        // ACT
        boolean resultado = v.tieneBateriaSuficiente(d);
        // ASSERT
        assertTrue(resultado);
    }

    @Test
    @DisplayName("Drone con batería < 35% NO puede ser asignado")
    void droneBateriaCritica_noAsignable() {
        DroneAcuatico d = new DroneAcuatico("AR-03", "Aqua-Ranger 100", 18, false, "Laguna Sur");

        assertFalse(v.tieneBateriaSuficiente(d));
    }

    @Test
    @DisplayName("Borde: batería exactamente 35% SÍ es suficiente")
    void bateriaExactamenteEnUmbral_esSuficiente() {
        DroneAcuatico d = new DroneAcuatico("AR-02", "Aqua-Ranger 100", 35, true, "Canal Central");

        assertTrue(v.tieneBateriaSuficiente(d));
    }

    @Test
    @DisplayName("Borde: batería 34% NO es suficiente")
    void bateriaUnoPorDebajoDelUmbral_noEsSuficiente() {
        DroneAcuatico d = new DroneAcuatico("AR-02", "Aqua-Ranger 100", 34, true, "Canal Central");

        assertFalse(v.tieneBateriaSuficiente(d));
    }

    @Test
    @DisplayName("Drone nulo al evaluar batería lanza IllegalArgumentException")
    void droneNuloAlEvaluarBateria_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> v.tieneBateriaSuficiente(null));
    }

    // ---------------------------------------------------------------
    // Disponibilidad
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Drone con disponible = true está disponible")
    void droneDisponible_estaDisponible() {
        DroneAcuatico d = new DroneAcuatico("AR-04", "Aqua-Ranger 100", 73, true, "Punto Ribereño Este");

        assertTrue(v.estaDisponible(d));
    }

    @Test
    @DisplayName("Drone no disponible NO puede ser asignado aunque tenga batería alta")
    void droneNoDisponible_noEstaDisponible() {
        DroneAcuatico d = new DroneAcuatico("AR-05", "Aqua-Ranger 100", 95, false, "Embalse Norte");

        assertFalse(v.estaDisponible(d));
    }

    // ---------------------------------------------------------------
    // Punto de llegada
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Punto de llegada nulo lanza IllegalArgumentException")
    void puntoLlegadaNulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> v.validarPuntoLlegada(null));
    }

    @Test
    @DisplayName("Punto de llegada vacío lanza IllegalArgumentException")
    void puntoLlegadaVacio_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> v.validarPuntoLlegada(""));
    }

    @Test
    @DisplayName("Punto de llegada solo con espacios lanza IllegalArgumentException")
    void puntoLlegadaEnBlanco_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> v.validarPuntoLlegada("   "));
    }

    @Test
    @DisplayName("Zona de llegada válida no lanza excepción")
    void puntoLlegadaValido_noLanzaExcepcion() {
        assertDoesNotThrow(() -> v.validarPuntoLlegada("Laboratorio Hídrico"));
    }

    @Test
    @DisplayName("Edge: zona inexistente en el campus lanza IllegalArgumentException")
    void zonaInvalida_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> v.validarPuntoLlegada("Océano Pacífico"));
    }

    @Test
    @DisplayName("Edge: la validación de zona no distingue mayúsculas/minúsculas")
    void zonaValidaEnMayusculas_noLanzaExcepcion() {
        assertDoesNotThrow(() -> v.validarPuntoLlegada("EMBALSE NORTE"));
    }

    @Test
    @DisplayName("Drone nulo al evaluar disponibilidad lanza IllegalArgumentException")
    void droneNuloAlEvaluarDisponibilidad_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> v.estaDisponible(null));
    }

    // ---------------------------------------------------------------
    // Validación completa de Mision (RN-01 y RN-02)
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Validar misión nula lanza IllegalArgumentException")
    void misionNula_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> v.validar(null));
    }

    @Test
    @DisplayName("Misión válida cumple con todas las reglas y no lanza excepción")
    void misionValida_noLanzaExcepcion() {
        DroneAcuatico drone = new DroneAcuatico("AR-01", "Aqua-Ranger 100", 80, true, "Embalse Norte");
        aquaport.model.Mision mision = new aquaport.model.Mision.Builder()
                .id("M-01")
                .drone(drone)
                .puntoPartida("Embalse Norte")
                .puntoLlegada("Laboratorio Hídrico")
                .tipoCarga(aquaport.model.TipoCarga.MUESTRA_AGUA)
                .build();

        assertDoesNotThrow(() -> v.validar(mision));
    }

    @Test
    @DisplayName("Misión con origen idéntico al destino lanza IllegalStateException (RN-02)")
    void misionOrigenIgualDestino_lanzaIllegalStateException() {
        DroneAcuatico drone = new DroneAcuatico("AR-01", "Aqua-Ranger 100", 80, true, "Embalse Norte");
        aquaport.model.Mision mision = new aquaport.model.Mision.Builder()
                .id("M-02")
                .drone(drone)
                .puntoPartida("Embalse Norte")
                .puntoLlegada("Embalse Norte")
                .tipoCarga(aquaport.model.TipoCarga.MUESTRA_AGUA)
                .build();

        assertThrows(IllegalStateException.class, () -> v.validar(mision));
    }

    @Test
    @DisplayName("Misión con zona de partida no habilitada lanza IllegalStateException (RN-02)")
    void misionZonaPartidaInvalida_lanzaIllegalStateException() {
        DroneAcuatico drone = new DroneAcuatico("AR-01", "Aqua-Ranger 100", 80, true, "Embalse Norte");
        aquaport.model.Mision mision = new aquaport.model.Mision.Builder()
                .id("M-03")
                .drone(drone)
                .puntoPartida("Mar Caribe")
                .puntoLlegada("Laboratorio Hídrico")
                .tipoCarga(aquaport.model.TipoCarga.MUESTRA_AGUA)
                .build();

        assertThrows(IllegalStateException.class, () -> v.validar(mision));
    }

    @Test
    @DisplayName("Misión con zona de llegada no habilitada lanza IllegalStateException (RN-02)")
    void misionZonaLlegadaInvalida_lanzaIllegalStateException() {
        DroneAcuatico drone = new DroneAcuatico("AR-01", "Aqua-Ranger 100", 80, true, "Embalse Norte");
        aquaport.model.Mision mision = new aquaport.model.Mision.Builder()
                .id("M-04")
                .drone(drone)
                .puntoPartida("Embalse Norte")
                .puntoLlegada("Río Magdalena")
                .tipoCarga(aquaport.model.TipoCarga.MUESTRA_AGUA)
                .build();

        assertThrows(IllegalStateException.class, () -> v.validar(mision));
    }

    @Test
    @DisplayName("Misión con drone con batería < 35% lanza IllegalStateException (RN-01)")
    void misionDroneBateriaInsuficiente_lanzaIllegalStateException() {
        // Drone disponible pero con 25% de batería
        DroneAcuatico drone = new DroneAcuatico("AR-02", "Aqua-Ranger 100", 25, true, "Embalse Norte");
        aquaport.model.Mision mision = new aquaport.model.Mision.Builder()
                .id("M-05")
                .drone(drone)
                .puntoPartida("Embalse Norte")
                .puntoLlegada("Laboratorio Hídrico")
                .tipoCarga(aquaport.model.TipoCarga.MUESTRA_AGUA)
                .build();

        assertThrows(IllegalStateException.class, () -> v.validar(mision));
    }
}
