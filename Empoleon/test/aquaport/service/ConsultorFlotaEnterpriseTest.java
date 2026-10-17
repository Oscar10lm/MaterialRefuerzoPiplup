package aquaport.service;

import aquaport.model.DroneAcuatico;
import aquaport.model.DroneSuperficial;
import aquaport.model.EstadoDrone;
import aquaport.model.EstadoMision;
import aquaport.model.MisionEnterprise;
import aquaport.model.Prioridad;
import aquaport.model.TipoCarga;
import aquaport.model.TramoRuta;
import aquaport.model.Waypoint;
import aquaport.model.ZonasEnterprise;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsultorFlotaEnterpriseTest {

    private ConsultorFlotaEnterprise consultor;
    private List<DroneAcuatico> flota;
    private List<Waypoint> waypoints;

    @BeforeEach
    void setUp() {
        consultor = new ConsultorFlotaEnterprise();
        flota = Flota60Factory.crearFlota60();
        waypoints = List.of(
                new Waypoint("W1", "Norte", ZonasEnterprise.EMBALSE_INVESTIGACION, 4.78, -74.04, 1),
                new Waypoint("W2", "Canal", ZonasEnterprise.RED_CANALES, 4.77, -74.03, 2),
                new Waypoint("W3", "Hub", ZonasEnterprise.LABORATORIO_CENTRAL, 4.76, -74.02, 3)
        );
    }

    @Test
    @DisplayName("Carga de trabajo por zona filtra correctamente solo misiones en transito")
    void calcularCargaTrabajoPorZona_misionesEnTransito() {
        MisionEnterprise m1 = new MisionEnterprise("M1", ZonasEnterprise.EMBALSE_INVESTIGACION, ZonasEnterprise.LABORATORIO_CENTRAL,
                waypoints, flota.get(0), Prioridad.CRITICA, TipoCarga.MUESTRA_AGUA, EstadoMision.EN_TRANSITO);
        MisionEnterprise m2 = new MisionEnterprise("M2", ZonasEnterprise.RED_CANALES, ZonasEnterprise.LABORATORIO_CENTRAL,
                waypoints.subList(1, 3), flota.get(20), Prioridad.NORMAL, TipoCarga.SENSOR, EstadoMision.COMPLETADA);

        Map<String, Long> carga = consultor.calcularCargaTrabajoPorZona(List.of(m1, m2));
        assertEquals(1L, carga.get(ZonasEnterprise.EMBALSE_INVESTIGACION));
        assertEquals(1L, carga.get(ZonasEnterprise.RED_CANALES));
        assertEquals(1L, carga.get(ZonasEnterprise.LABORATORIO_CENTRAL));
        assertTrue(consultor.calcularCargaTrabajoPorZona(null).isEmpty());
    }

    @Test
    @DisplayName("Eficiencia por zona calcula el ratio de misiones completadas")
    void calcularEficienciaPorZona_calculaRatio() {
        MisionEnterprise m1 = new MisionEnterprise("M1", ZonasEnterprise.RED_CANALES, ZonasEnterprise.LABORATORIO_CENTRAL,
                List.of(), flota.get(20), Prioridad.NORMAL, TipoCarga.SENSOR, EstadoMision.COMPLETADA);
        MisionEnterprise m2 = new MisionEnterprise("M2", ZonasEnterprise.RED_CANALES, ZonasEnterprise.LABORATORIO_CENTRAL,
                List.of(), flota.get(21), Prioridad.NORMAL, TipoCarga.SENSOR, EstadoMision.FALLIDA);

        Map<String, Double> ef = consultor.calcularEficienciaPorZona(List.of(m1, m2));
        assertEquals(0.50, ef.get(ZonasEnterprise.RED_CANALES), 0.001);
        assertTrue(consultor.calcularEficienciaPorZona(null).isEmpty());
        assertTrue(consultor.calcularEficienciaPorZona(List.of()).isEmpty());
    }

    @Test
    @DisplayName("Drone con mejor historial selecciona la unidad con mayor numero de entregas")
    void encontrarDroneMejorHistorial_encuentraGanador() {
        DroneAcuatico droneTop = flota.get(20);
        MisionEnterprise m1 = new MisionEnterprise("M1", ZonasEnterprise.RED_CANALES, ZonasEnterprise.LABORATORIO_CENTRAL,
                List.of(), droneTop, Prioridad.NORMAL, TipoCarga.SENSOR, EstadoMision.COMPLETADA);
        MisionEnterprise m2 = new MisionEnterprise("M2", ZonasEnterprise.RED_CANALES, ZonasEnterprise.LABORATORIO_CENTRAL,
                List.of(), droneTop, Prioridad.NORMAL, TipoCarga.SENSOR, EstadoMision.COMPLETADA);

        Optional<DroneAcuatico> ganador = consultor.encontrarDroneMejorHistorial(List.of(m1, m2));
        assertTrue(ganador.isPresent());
        assertEquals(droneTop.getId(), ganador.get().getId());
        assertTrue(consultor.encontrarDroneMejorHistorial(null).isEmpty());
    }

    @Test
    @DisplayName("Planificacion multi-etapa asigna drone mas cercano por cada tramo")
    void planificarRutaOptimaMultiEtapa_asignaDrones() {
        List<TramoRuta> ruta = consultor.planificarRutaOptimaMultiEtapa(waypoints, flota);
        assertEquals(2, ruta.size());
        assertEquals("TR-1", ruta.get(0).getIdTramo());
        assertEquals(ZonasEnterprise.EMBALSE_INVESTIGACION, ruta.get(0).getDroneAsignado().getZona());
        assertEquals(ZonasEnterprise.RED_CANALES, ruta.get(1).getDroneAsignado().getZona());
        assertTrue(consultor.planificarRutaOptimaMultiEtapa(null, flota).isEmpty());
        assertTrue(consultor.planificarRutaOptimaMultiEtapa(List.of(waypoints.get(0)), flota).isEmpty());
    }

    @Test
    @DisplayName("Planificacion multi-etapa lanza excepcion si no hay drone disponible en la zona")
    void planificarRutaOptimaMultiEtapa_sinDrones_lanzaExcepcion() {
        List<DroneAcuatico> flotaSinEmbalse = List.of(
                new DroneSuperficial("DS-99", "M99", 90, EstadoDrone.DISPONIBLE, ZonasEnterprise.RED_CANALES)
        );
        assertThrows(IllegalStateException.class, () ->
                consultor.planificarRutaOptimaMultiEtapa(waypoints, flotaSinEmbalse)
        );
    }

    @Test
    @DisplayName("Bateria promedio calcula medias por zona con el colector en una sola pasada")
    void calcularBateriaPromedioPorZona_calculaCorrectamente() {
        Map<String, Double> promedios = consultor.calcularBateriaPromedioPorZona(flota);
        assertNotNull(promedios);
        assertEquals(4, promedios.size());
        assertTrue(promedios.get(ZonasEnterprise.LABORATORIO_CENTRAL) >= 80.0);
        assertTrue(consultor.calcularBateriaPromedioPorZona(null).isEmpty());
    }
}
