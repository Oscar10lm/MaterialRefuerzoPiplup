package aquaport.service;

import aquaport.MainPrinplup;
import aquaport.model.DroneAcuatico;
import aquaport.model.DroneBuceador;
import aquaport.model.DroneSemisumergido;
import aquaport.model.DroneSuperficial;
import aquaport.model.EstadoDrone;
import aquaport.model.EstadoMision;
import aquaport.model.Mision;
import aquaport.model.Prioridad;
import aquaport.model.TipoCarga;
import aquaport.model.TipoDrone;
import aquaport.model.ZonasCampus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias para {@link ConsultorFlotaAvanzado} (Reto 01 Prinplup).
 * Sigue el patrón AAA y las mejores prácticas de JUnit 5.
 */
class ConsultorFlotaAvanzadoTest {

    private ConsultorFlotaAvanzado consultor;
    private List<DroneAcuatico> flota15;

    @BeforeEach
    void setUp() {
        consultor = new ConsultorFlotaAvanzado();
        flota15 = MainPrinplup.crearFlota15Drones();
    }

    // -------------------------------------------------------------------------
    // 1. Agrupar disponibles por tipo
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Agrupa correctamente los drones disponibles por cada uno de los 3 tipos")
    void agruparDisponiblesPorTipo_agrupaCorrectamente() {
        Map<TipoDrone, List<DroneAcuatico>> resultado = consultor.agruparDisponiblesPorTipo(flota15);

        // En la flota base:
        // Superficial disponibles: DS-01 (95%), DS-03 (40%) = 2
        // Semisumergido disponibles: DM-01 (88%), DM-03 (90%), DM-04 (55%) = 3
        // Buceador disponibles: DB-01 (92%), DB-03 (65%), DB-04 (30%) = 3
        assertEquals(2, resultado.get(TipoDrone.SUPERFICIAL).size());
        assertEquals(3, resultado.get(TipoDrone.SEMISUMERGIDO).size());
        assertEquals(3, resultado.get(TipoDrone.BUCEADOR).size());

        // Asegurar que ninguno de los agrupados esté en estado no disponible
        resultado.values().forEach(lista ->
                lista.forEach(d -> assertTrue(d.isDisponible()))
        );
    }

    @Test
    @DisplayName("Agrupar disponibles con lista nula o vacía retorna mapa vacío")
    void agruparDisponiblesPorTipo_listaNulaOVacia_retornaVacio() {
        assertTrue(consultor.agruparDisponiblesPorTipo(null).isEmpty());
        assertTrue(consultor.agruparDisponiblesPorTipo(List.of()).isEmpty());
    }

    // -------------------------------------------------------------------------
    // 2. Drone óptimo para una zona dada
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Obtiene el drone óptimo con mayor batería para Embalse Norte y tipo Semisumergido")
    void obtenerDroneOptimoParaZona_encuentraDroneConMayorBateria() {
        Optional<DroneAcuatico> optimo = consultor.obtenerDroneOptimoParaZona(
                flota15, ZonasCampus.EMBALSE_NORTE, TipoDrone.SEMISUMERGIDO
        );

        assertTrue(optimo.isPresent());
        assertEquals("DM-03", optimo.get().getId());
        assertEquals(90, optimo.get().getBateria());
    }

    @Test
    @DisplayName("Retorna Optional.empty si no hay drones disponibles del tipo en la zona")
    void obtenerDroneOptimoParaZona_sinDronesDisponibles_retornaVacio() {
        // En Dársena Oeste solo hay DM-04 (Semisumergido), no hay Buceadores
        Optional<DroneAcuatico> optimo = consultor.obtenerDroneOptimoParaZona(
                flota15, ZonasCampus.DARSENA_OESTE, TipoDrone.BUCEADOR
        );

        assertFalse(optimo.isPresent());
    }

    @Test
    @DisplayName("Sobrecarga de zona general obtiene el drone con mayor batería disponible")
    void obtenerDroneOptimoParaZona_sobrecargaGeneral_obtieneMayorBateria() {
        // En Embalse Norte disponibles: DS-01 (95%, Superficial), DM-03 (90%, Semisumergido)
        Optional<DroneAcuatico> optimo = consultor.obtenerDroneOptimoParaZona(flota15, ZonasCampus.EMBALSE_NORTE);

        assertTrue(optimo.isPresent());
        assertEquals("DS-01", optimo.get().getId());
        assertEquals(95, optimo.get().getBateria());
    }

    @Test
    @DisplayName("Parámetros nulos al buscar drone óptimo retornan Optional.empty")
    void obtenerDroneOptimoParaZona_parametrosNulos_retornaVacio() {
        assertFalse(consultor.obtenerDroneOptimoParaZona(null, ZonasCampus.EMBALSE_NORTE, TipoDrone.SUPERFICIAL).isPresent());
        assertFalse(consultor.obtenerDroneOptimoParaZona(flota15, null, TipoDrone.SUPERFICIAL).isPresent());
        assertFalse(consultor.obtenerDroneOptimoParaZona(flota15, ZonasCampus.EMBALSE_NORTE, null).isPresent());
    }

    // -------------------------------------------------------------------------
    // 3. Promedio de batería por tipo de drone
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Calcula el promedio de batería por tipo de drone correctamente")
    void calcularPromedioBateriaPorTipo_calculaPromediosCorrectos() {
        Map<TipoDrone, Double> promedios = consultor.calcularPromedioBateriaPorTipo(flota15);

        assertEquals(3, promedios.size());

        // Superficial: (95 + 82 + 40 + 15 + 0) / 5 = 232 / 5 = 46.4
        assertEquals(46.4, promedios.get(TipoDrone.SUPERFICIAL), 0.001);

        // Semisumergido: (88 + 75 + 90 + 55 + 20) / 5 = 328 / 5 = 65.6
        assertEquals(65.6, promedios.get(TipoDrone.SEMISUMERGIDO), 0.001);

        // Buceador: (92 + 78 + 65 + 30 + 12) / 5 = 277 / 5 = 55.4
        assertEquals(55.4, promedios.get(TipoDrone.BUCEADOR), 0.001);
    }

    @Test
    @DisplayName("Promedio de batería con flota vacía o nula retorna mapa vacío")
    void calcularPromedioBateriaPorTipo_flotaNula_retornaVacio() {
        assertTrue(consultor.calcularPromedioBateriaPorTipo(null).isEmpty());
        assertTrue(consultor.calcularPromedioBateriaPorTipo(List.of()).isEmpty());
    }

    // -------------------------------------------------------------------------
    // 4. Separar misiones CRÍTICAS usando partitioningBy
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Separa correctamente las misiones CRÍTICAS de las no críticas")
    void separarMisionesCriticas_particionaAdecuadamente() {
        List<Mision> misiones = MainPrinplup.crearMisionesDemostracion(flota15);
        Map<Boolean, List<Mision>> resultado = consultor.separarMisionesCriticas(misiones);

        // 2 críticas (MIS-202 y MIS-204)
        List<Mision> criticas = resultado.get(true);
        assertEquals(2, criticas.size());
        assertTrue(criticas.stream().allMatch(m -> m.getPrioridad() == Prioridad.CRITICA));

        // 3 no críticas (MIS-201 NORMAL, MIS-203 ALTA, MIS-205 BAJA)
        List<Mision> noCriticas = resultado.get(false);
        assertEquals(3, noCriticas.size());
        assertTrue(noCriticas.stream().allMatch(m -> m.getPrioridad() != Prioridad.CRITICA));
    }

    @Test
    @DisplayName("Separar misiones con lista nula o vacía no lanza excepción")
    void separarMisionesCriticas_listaVacia_noFalla() {
        Map<Boolean, List<Mision>> vacio = consultor.separarMisionesCriticas(List.of());
        assertTrue(vacio.get(true).isEmpty());
        assertTrue(vacio.get(false).isEmpty());
    }

    // -------------------------------------------------------------------------
    // 5. Zonas únicas que cubre la flota activa
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Lista todas las zonas únicas cubiertas por la flota activa sin duplicados ni zonas en mantenimiento")
    void listarZonasUnicasFlotaActiva_listaSinDuplicados() {
        List<String> zonas = consultor.listarZonasUnicasFlotaActiva(flota15);

        // Zonas en flota activa: Embalse Norte, Laguna Sur, Canal Central, Punto Ribereño Este, Dársena Oeste, Bahía Ciencia, Laboratorio Hídrico
        assertFalse(zonas.isEmpty());
        // No debe haber duplicados
        assertEquals(zonas.size(), zonas.stream().distinct().count());
        // Debe estar ordenada alfabéticamente
        assertTrue(zonas.contains(ZonasCampus.EMBALSE_NORTE));
        assertTrue(zonas.contains(ZonasCampus.BAHIA_CIENCIA));
        assertTrue(zonas.contains(ZonasCampus.DARSENA_OESTE));
    }

    // -------------------------------------------------------------------------
    // Operación complementaria: Ordenamiento por prioridad y batería
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Ordena misiones por prioridad de mayor a menor urgencia y luego por batería descendente")
    void ordenarMisionesPorPrioridadYBateria_ordenaCorrectamente() {
        List<Mision> misiones = MainPrinplup.crearMisionesDemostracion(flota15);
        List<Mision> ordenadas = consultor.ordenarMisionesPorPrioridadYBateria(misiones);

        // Las dos primeras deben ser CRÍTICAS
        assertEquals(Prioridad.CRITICA, ordenadas.get(0).getPrioridad());
        assertEquals(Prioridad.CRITICA, ordenadas.get(1).getPrioridad());

        // Entre las dos críticas, DB-01 (Bat: 92%) debe ir antes que DM-03 (Bat: 90%)
        assertEquals("DB-01", ordenadas.get(0).getDrone().getId());
        assertEquals("DM-03", ordenadas.get(1).getDrone().getId());

        // La tercera debe ser ALTA (MIS-203)
        assertEquals(Prioridad.ALTA, ordenadas.get(2).getPrioridad());

        // La última debe ser BAJA (MIS-205)
        assertEquals(Prioridad.BAJA, ordenadas.get(4).getPrioridad());
    }
}
