package aquaport.service;

import aquaport.model.DroneAcuatico;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Collector personalizado que agrupa drones por zona y calcula la batería promedio
 * en una sola pasada (single-pass accumulation) optimizando la complejidad a O(N).
 */
public class BateriaPromedioCollector implements Collector<DroneAcuatico, Map<String, BateriaPromedioCollector.AcumuladorBateria>, Map<String, Double>> {

    public static class AcumuladorBateria {
        private long suma = 0;
        private int cuenta = 0;

        public void aceptar(int bateria) {
            this.suma += bateria;
            this.cuenta++;
        }

        public AcumuladorBateria combinar(AcumuladorBateria otro) {
            this.suma += otro.suma;
            this.cuenta += otro.cuenta;
            return this;
        }

        public double calcularPromedio() {
            return cuenta == 0 ? 0.0 : (double) suma / cuenta;
        }
    }

    public static BateriaPromedioCollector enUnaSolaPasada() {
        return new BateriaPromedioCollector();
    }

    @Override
    public Supplier<Map<String, AcumuladorBateria>> supplier() {
        return HashMap::new;
    }

    @Override
    public BiConsumer<Map<String, AcumuladorBateria>, DroneAcuatico> accumulator() {
        return (mapa, drone) -> {
            if (drone != null) {
                mapa.computeIfAbsent(drone.getZona(), k -> new AcumuladorBateria())
                    .aceptar(drone.getBateria());
            }
        };
    }

    @Override
    public BinaryOperator<Map<String, AcumuladorBateria>> combiner() {
        return (m1, m2) -> {
            m2.forEach((zona, acum) -> m1.merge(zona, acum, AcumuladorBateria::combinar));
            return m1;
        };
    }

    @Override
    public Function<Map<String, AcumuladorBateria>, Map<String, Double>> finisher() {
        return mapa -> {
            Map<String, Double> resultado = new HashMap<>();
            mapa.forEach((zona, acum) -> resultado.put(zona, acum.calcularPromedio()));
            return Collections.unmodifiableMap(resultado);
        };
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of(Characteristics.UNORDERED);
    }
}
