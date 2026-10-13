package aquaport;

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
import aquaport.service.ConsultorFlotaAvanzado;

import java.util.List;
import java.util.Map;

/**
 * Punto de entrada y demostración operativa para AquaPort v2 (Prinplup).
 * 
 * Flota autónoma de 15 drones con alertas y asignación inteligente.
 */
public class MainPrinplup {

    private MainPrinplup() {
        // Constructor privado para clase utilitaria con método estático main (Sonar java:S1118)
    }

    public static void main(String[] args) {
        imprimirEncabezado();

        List<DroneAcuatico> flota15 = crearFlota15Drones();
        ConsultorFlotaAvanzado consultor = new ConsultorFlotaAvanzado();

        imprimirFlota(flota15);

        // 1. Agrupar disponibles por tipo
        imprimirSeccion("1. Agrupar drones disponibles por tipo de drone");
        Map<TipoDrone, List<DroneAcuatico>> porTipo = consultor.agruparDisponiblesPorTipo(flota15);
        porTipo.forEach((tipo, drones) -> {
            System.out.printf("  🏷️  Tipo [%s] (%d disponibles):%n", tipo.getNombre(), drones.size());
            drones.forEach(d -> System.out.println("      • " + d));
        });

        // 2. Drone óptimo para zona dada
        imprimirSeccion("2. Drone óptimo para 'Embalse Norte' (mayor batería, tipo SEMISUMERGIDO)");
        consultor.obtenerDroneOptimoParaZona(flota15, ZonasCampus.EMBALSE_NORTE, TipoDrone.SEMISUMERGIDO)
                .ifPresentOrElse(
                        d -> System.out.println("  🎯 Drone seleccionado: " + d),
                        () -> System.out.println("  ⚠️ No se encontró drone compatible disponible.")
                );

        // 3. Promedio de batería por tipo
        imprimirSeccion("3. Promedio de batería restante por tipo de drone");
        Map<TipoDrone, Double> promedios = consultor.calcularPromedioBateriaPorTipo(flota15);
        promedios.forEach((tipo, avg) ->
                System.out.printf("  🔋 Tipo [%-13s] Promedio Batería: %5.1f%%%n", tipo.getNombre(), avg));

        // 4. Separar misiones CRÍTICAS de las demás
        imprimirSeccion("4. Separar misiones CRÍTICAS vs no críticas (partitioningBy)");
        List<Mision> misiones = crearMisionesDemostracion(flota15);
        Map<Boolean, List<Mision>> particionadas = consultor.separarMisionesCriticas(misiones);

        System.out.printf("  🚨 Misiones CRÍTICAS (Precedencia Absoluta) [%d]:%n", particionadas.get(true).size());
        particionadas.get(true).forEach(m -> System.out.println("      [CRÍTICA] " + m));

        System.out.printf("%n  📋 Misiones Regulares / Otras Prioridades [%d]:%n", particionadas.get(false).size());
        particionadas.get(false).forEach(m -> System.out.println("      [REGULAR] " + m));

        // 5. Zonas únicas que cubre la flota activa
        imprimirSeccion("5. Zonas únicas cubiertas por la flota activa");
        List<String> zonas = consultor.listarZonasUnicasFlotaActiva(flota15);
        System.out.printf("  🗺️ Total zonas cubiertas: %d%n", zonas.size());
        zonas.forEach(z -> System.out.println("      📍 " + z));

        // Complementario: Ordenamiento por prioridad y batería
        imprimirSeccion("Extra: Cola de misiones ordenada por Prioridad y Batería restante");
        List<Mision> ordenadas = consultor.ordenarMisionesPorPrioridadYBateria(misiones);
        ordenadas.forEach(m -> System.out.println("      " + m));

        imprimirPie();
    }

    public static List<DroneAcuatico> crearFlota15Drones() {
        return List.of(
            // 5 Drones Superficiales (Zonas tranquilas, carga hasta 500g)
            new DroneSuperficial("DS-01", "Aqua-Skimmer S1", 95, EstadoDrone.DISPONIBLE, ZonasCampus.EMBALSE_NORTE),
            new DroneSuperficial("DS-02", "Aqua-Skimmer S1", 82, EstadoDrone.EN_MISION,  ZonasCampus.LAGUNA_SUR),
            new DroneSuperficial("DS-03", "Aqua-Skimmer S2", 40, EstadoDrone.DISPONIBLE, ZonasCampus.CANAL_CENTRAL),
            new DroneSuperficial("DS-04", "Aqua-Skimmer S2", 15, EstadoDrone.RECARGANDO, ZonasCampus.EMBALSE_NORTE),
            new DroneSuperficial("DS-05", "Aqua-Skimmer S1",  0, EstadoDrone.MANTENIMIENTO, ZonasCampus.LABORATORIO_HIDRICO),

            // 5 Drones Semisumergidos (Agua agitada, carga hasta 1.5kg)
            new DroneSemisumergido("DM-01", "Wave-Ranger M1", 88, EstadoDrone.DISPONIBLE, ZonasCampus.CANAL_CENTRAL),
            new DroneSemisumergido("DM-02", "Wave-Ranger M1", 75, EstadoDrone.EN_MISION,  ZonasCampus.PUNTO_RIBERENO_ESTE),
            new DroneSemisumergido("DM-03", "Wave-Ranger M2", 90, EstadoDrone.DISPONIBLE, ZonasCampus.EMBALSE_NORTE),
            new DroneSemisumergido("DM-04", "Wave-Ranger M2", 55, EstadoDrone.DISPONIBLE, ZonasCampus.DARSENA_OESTE),
            new DroneSemisumergido("DM-05", "Wave-Ranger M1", 20, EstadoDrone.RECARGANDO, ZonasCampus.LABORATORIO_HIDRICO),

            // 5 Drones Buceadores (Muestras profundidad, carga hasta 300g)
            new DroneBuceador("DB-01", "Deep-Probe B1", 92, EstadoDrone.DISPONIBLE, ZonasCampus.BAHIA_CIENCIA),
            new DroneBuceador("DB-02", "Deep-Probe B1", 78, EstadoDrone.SUMERGIDO,  ZonasCampus.EMBALSE_NORTE),
            new DroneBuceador("DB-03", "Deep-Probe B2", 65, EstadoDrone.DISPONIBLE, ZonasCampus.LAGUNA_SUR),
            new DroneBuceador("DB-04", "Deep-Probe B2", 30, EstadoDrone.DISPONIBLE, ZonasCampus.LABORATORIO_HIDRICO),
            new DroneBuceador("DB-05", "Deep-Probe B1", 12, EstadoDrone.MANTENIMIENTO, ZonasCampus.BAHIA_CIENCIA)
        );
    }

    public static List<Mision> crearMisionesDemostracion(List<DroneAcuatico> flota) {
        return List.of(
            new Mision("MIS-201", flota.get(0), ZonasCampus.EMBALSE_NORTE, ZonasCampus.LABORATORIO_HIDRICO,
                    Prioridad.NORMAL, TipoCarga.MUESTRA_AGUA, EstadoMision.PENDIENTE),
            new Mision("MIS-202", flota.get(7), ZonasCampus.EMBALSE_NORTE, ZonasCampus.BAHIA_CIENCIA,
                    Prioridad.CRITICA, TipoCarga.CARGA_PESADA, EstadoMision.EN_TRANSITO),
            new Mision("MIS-203", flota.get(5), ZonasCampus.CANAL_CENTRAL, ZonasCampus.PUNTO_RIBERENO_ESTE,
                    Prioridad.ALTA, TipoCarga.SENSOR, EstadoMision.PENDIENTE),
            new Mision("MIS-204", flota.get(10), ZonasCampus.BAHIA_CIENCIA, ZonasCampus.LAGUNA_SUR,
                    Prioridad.CRITICA, TipoCarga.MUESTRA_AGUA, EstadoMision.PENDIENTE),
            new Mision("MIS-205", flota.get(2), ZonasCampus.CANAL_CENTRAL, ZonasCampus.LABORATORIO_HIDRICO,
                    Prioridad.BAJA, TipoCarga.PAQUETE_LIGERO, EstadoMision.PENDIENTE)
        );
    }

    private static void imprimirEncabezado() {
        System.out.println("================================================================================");
        System.out.println("  🐧 AQUAPORT v2 (PRINPLUP) — GESTIÓN AUTÓNOMA DE FLOTA DE 15 DRONES");
        System.out.println("  🌊 Reto 01: Streams & Lambdas Avanzados en Flota Multimodal");
        System.out.println("================================================================================");
    }

    private static void imprimirFlota(List<DroneAcuatico> flota) {
        System.out.printf("%n📋 FLOTA TOTAL REGISTRADA (%d DRONES):%n", flota.size());
        flota.forEach(d -> System.out.println("  " + d));
    }

    private static void imprimirSeccion(String titulo) {
        System.out.println("\n────────────────────────────────────────────────────────────────────────────────");
        System.out.println(" ▸ " + titulo);
        System.out.println("────────────────────────────────────────────────────────────────────────────────");
    }

    private static void imprimirPie() {
        System.out.println("\n================================================================================");
        System.out.println("  ✔ Demostración Reto 01 Prinplup ejecutada exitosamente.");
        System.out.println("================================================================================\n");
    }
}
