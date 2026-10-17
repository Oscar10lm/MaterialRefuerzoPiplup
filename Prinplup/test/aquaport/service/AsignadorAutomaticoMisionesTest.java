package aquaport.service;

import aquaport.MainPrinplup;
import aquaport.model.CondicionAgua;
import aquaport.model.DroneAcuatico;
import aquaport.model.Mision;
import aquaport.model.Prioridad;
import aquaport.model.TipoCarga;
import aquaport.model.ZonasCampus;
import aquaport.model.EstadoDrone;
import aquaport.model.EstadoMision;
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

    // -------------------------------------------------------------------------
    // Cobertura de Ramas Amarillas (Reto 13 Prinplup)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Rama Amarilla: Parámetros nulos en seleccionarDroneOptimo retornan Optional.empty()")
    void seleccionarDroneOptimo_parametrosNulos_retornaVacio() {
        // flota == null
        assertTrue(asignador.seleccionarDroneOptimo(null, ZonasCampus.EMBALSE_NORTE, CondicionAgua.TRANQUILA, 0.300, Prioridad.NORMAL).isEmpty());
        // zona == null
        assertTrue(asignador.seleccionarDroneOptimo(flota, null, CondicionAgua.TRANQUILA, 0.300, Prioridad.NORMAL).isEmpty());
        // condicion == null
        assertTrue(asignador.seleccionarDroneOptimo(flota, ZonasCampus.EMBALSE_NORTE, null, 0.300, Prioridad.NORMAL).isEmpty());
    }

    @Test
    @DisplayName("Rama Amarilla: Drones con batería inferior al umbral operativo (< 35%) son descartados")
    void seleccionarDroneOptimo_bateriaMenorA35_descartada() {
        // En Laboratorio Hídrico el único disponible es DB-04 (Buceador) con 30% de batería (< 35%)
        Optional<DroneAcuatico> seleccionado = asignador.seleccionarDroneOptimo(
                flota, ZonasCampus.LABORATORIO_HIDRICO, CondicionAgua.PROFUNDA, 0.100, Prioridad.NORMAL
        );
        assertTrue(seleccionado.isEmpty(), "Un drone con 30% de batería debe ser descartado por BATERIA_MINIMA_OPERATIVA (35%)");
    }

    @Test
    @DisplayName("Rama Amarilla: Sobrecarga de 4 parámetros de seleccionarDroneOptimo delega con Prioridad.NORMAL")
    void seleccionarDroneOptimo_sobrecarga4Parametros_asignaNormal() {
        Optional<DroneAcuatico> seleccionado = asignador.seleccionarDroneOptimo(
                flota, ZonasCampus.EMBALSE_NORTE, CondicionAgua.TRANQUILA, 0.300
        );
        assertTrue(seleccionado.isPresent());
        assertEquals("DS-01", seleccionado.get().getId());
    }

    @Test
    @DisplayName("Rama Amarilla: Gestión defensiva de observadores (nulls, duplicados, desuscripción y lista inmutable)")
    void gestionObservadores_ramasDefensivas() {
        List<String> alertas = new ArrayList<>();
        aquaport.service.observer.ObservadorFalloDrone obs = (d, m) -> alertas.add(m);

        // Agregar null no debe fallar ni agregar nada
        asignador.agregarObservador(null);
        assertEquals(0, asignador.getObservadores().size());

        // Agregar observador legítimo
        asignador.agregarObservador(obs);
        assertEquals(1, asignador.getObservadores().size());

        // Agregar observador duplicado no debe duplicar la entrada
        asignador.agregarObservador(obs);
        assertEquals(1, asignador.getObservadores().size());

        // Intentar modificar getObservadores() directamente debe lanzar UnsupportedOperationException
        List<aquaport.service.observer.ObservadorFalloDrone> lista = asignador.getObservadores();
        assertThrows(UnsupportedOperationException.class, () -> lista.add(obs));

        // Remover observador inexistente y existente
        asignador.removerObservador((d, m) -> {});
        assertEquals(1, asignador.getObservadores().size());

        asignador.removerObservador(obs);
        assertEquals(0, asignador.getObservadores().size());
    }

    @Test
    @DisplayName("Rama Amarilla: Constructor con estrategia nula aplica MayorBateriaStrategy por defecto y setEstrategia valida nulidad")
    void constructorYSetEstrategia_validaciones() {
        AsignadorAutomaticoMisiones asignadorPorDefecto = new AsignadorAutomaticoMisiones(null);
        assertNotNull(asignadorPorDefecto.getEstrategia());
        assertEquals("MayorBateriaStrategy", asignadorPorDefecto.getEstrategia().getNombre());

        assertThrows(NullPointerException.class, () -> asignador.setEstrategia(null));
    }

    @Test
    @DisplayName("Rama Amarilla: registrarMision con drone en estado saludable (DISPONIBLE) completa exitosamente")
    void registrarMision_droneSaludable_retornaMisionSinNotificarObservadores() {
        List<String> fallos = new ArrayList<>();
        asignador.agregarObservador((d, m) -> fallos.add(m));

        DroneAcuatico droneOk = flota.get(0); // DS-01, DISPONIBLE
        Mision misionOk = new Mision("MIS-OK", droneOk, ZonasCampus.EMBALSE_NORTE, ZonasCampus.LAGUNA_SUR,
                Prioridad.NORMAL, TipoCarga.MUESTRA_AGUA, EstadoMision.PENDIENTE);

        Mision misionRegistrada = asignador.registrarMision(misionOk);
        assertNotNull(misionRegistrada);
        assertEquals("MIS-OK", misionRegistrada.getId());
        assertTrue(fallos.isEmpty(), "No deben haberse emitido notificaciones de fallo para un drone saludable");

        // Rama: drone == null en mision mockeada completa limpiamente sin lanzar
        Mision mockMisionSinDrone = org.mockito.Mockito.mock(Mision.class);
        org.mockito.Mockito.when(mockMisionSinDrone.getDrone()).thenReturn(null);
        assertEquals(mockMisionSinDrone, asignador.registrarMision(mockMisionSinDrone));

        // Nulidad de mision a registrar
        assertThrows(NullPointerException.class, () -> asignador.registrarMision(null));
    }

    @Test
    @DisplayName("Rama Amarilla: Validaciones de nulidad en asignarMisionAutomaticamente")
    void asignarMisionAutomaticamente_validacionesNulidad() {
        assertThrows(NullPointerException.class, () ->
                asignador.asignarMisionAutomaticamente(null, flota, "A", "B", CondicionAgua.TRANQUILA, TipoCarga.MUESTRA_AGUA, Prioridad.NORMAL));
        assertThrows(NullPointerException.class, () ->
                asignador.asignarMisionAutomaticamente("M-01", flota, null, "B", CondicionAgua.TRANQUILA, TipoCarga.MUESTRA_AGUA, Prioridad.NORMAL));
        assertThrows(NullPointerException.class, () ->
                asignador.asignarMisionAutomaticamente("M-01", flota, "A", null, CondicionAgua.TRANQUILA, TipoCarga.MUESTRA_AGUA, Prioridad.NORMAL));
        assertThrows(NullPointerException.class, () ->
                asignador.asignarMisionAutomaticamente("M-01", flota, "A", "B", CondicionAgua.TRANQUILA, null, Prioridad.NORMAL));
    }

    @Test
    @DisplayName("Rama Amarilla: Asignación con prioridades ALTA y BAJA preservadas en la misión creada")
    void asignarMisionAutomaticamente_prioridadesAltaYBaja() {
        Mision misionAlta = asignador.asignarMisionAutomaticamente(
                "M-ALTA", flota, ZonasCampus.EMBALSE_NORTE, ZonasCampus.CANAL_CENTRAL,
                CondicionAgua.TRANQUILA, TipoCarga.SENSOR, Prioridad.ALTA
        );
        assertEquals(Prioridad.ALTA, misionAlta.getPrioridad());

        Mision misionBaja = asignador.asignarMisionAutomaticamente(
                "M-BAJA", flota, ZonasCampus.EMBALSE_NORTE, ZonasCampus.LAGUNA_SUR,
                CondicionAgua.TRANQUILA, TipoCarga.PAQUETE_LIGERO, Prioridad.BAJA
        );
        assertEquals(Prioridad.BAJA, misionBaja.getPrioridad());
    }
}
