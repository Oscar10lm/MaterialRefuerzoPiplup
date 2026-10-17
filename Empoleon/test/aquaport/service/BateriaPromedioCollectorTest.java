package aquaport.service;

import aquaport.model.DroneAcuatico;
import aquaport.model.DroneSuperficial;
import aquaport.model.EstadoDrone;
import aquaport.model.ZonasEnterprise;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BateriaPromedioCollectorTest {

    @Test
    @DisplayName("Collector personalizado en stream secuencial calcula promedios exactos")
    void collector_streamSecuencial_calculaPromedios() {
        List<DroneAcuatico> drones = List.of(
                new DroneSuperficial("DS-01", "M1", 80, EstadoDrone.DISPONIBLE, ZonasEnterprise.RED_CANALES),
                new DroneSuperficial("DS-02", "M1", 60, EstadoDrone.DISPONIBLE, ZonasEnterprise.RED_CANALES),
                new DroneSuperficial("DS-03", "M1", 90, EstadoDrone.DISPONIBLE, ZonasEnterprise.LABORATORIO_CENTRAL)
        );

        Map<String, Double> resultado = drones.stream().collect(BateriaPromedioCollector.enUnaSolaPasada());
        assertEquals(70.0, resultado.get(ZonasEnterprise.RED_CANALES), 0.001);
        assertEquals(90.0, resultado.get(ZonasEnterprise.LABORATORIO_CENTRAL), 0.001);
    }

    @Test
    @DisplayName("Collector en stream paralelo ejecuta combiner sin distorsionar promedios")
    void collector_streamParalelo_ejecutaCombinerExitosamente() {
        List<DroneAcuatico> flota = Flota60Factory.crearFlota60();
        Map<String, Double> seq = flota.stream().collect(BateriaPromedioCollector.enUnaSolaPasada());
        Map<String, Double> par = flota.parallelStream().collect(BateriaPromedioCollector.enUnaSolaPasada());

        assertEquals(seq.size(), par.size());
        seq.forEach((zona, prom) -> assertEquals(prom, par.get(zona), 0.001));
    }

    @Test
    @DisplayName("Collector con elementos nulos los descarta defensivamente")
    void collector_elementosNulos_losIgnora() {
        List<DroneAcuatico> conNulos = new ArrayList<>();
        conNulos.add(new DroneSuperficial("DS-01", "M1", 80, EstadoDrone.DISPONIBLE, ZonasEnterprise.RED_CANALES));
        conNulos.add(null);

        Map<String, Double> res = conNulos.stream().collect(BateriaPromedioCollector.enUnaSolaPasada());
        assertEquals(80.0, res.get(ZonasEnterprise.RED_CANALES), 0.001);
    }

    @Test
    @DisplayName("Acumulador aislado calcula 0.0 cuando cuenta es cero")
    void acumulador_sinElementos_retornaCero() {
        BateriaPromedioCollector.AcumuladorBateria acum = new BateriaPromedioCollector.AcumuladorBateria();
        assertEquals(0.0, acum.calcularPromedio());
    }

    @Test
    @DisplayName("Characteristics reporta conjunto UNORDERED")
    void characteristics_contieneUnordered() {
        BateriaPromedioCollector collector = new BateriaPromedioCollector();
        assertNotNull(collector.characteristics());
        assertTrue(collector.characteristics().contains(Collector.Characteristics.UNORDERED));
    }
}
