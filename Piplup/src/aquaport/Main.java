package aquaport;

import aquaport.model.DroneAcuatico;
import aquaport.service.ConsultorFlota;

import java.util.List;
import java.util.Optional;

/**
 * Punto de entrada para el Reto 01 — AquaPort (Nivel Piplup).
 * 
 * Ejecuta y presenta visualmente los resultados de las 5 consultas sobre la flota de prueba.
 */
public class Main {

    public static void main(String[] args) {
        imprimirEncabezado();

        // 1. Definición de la flota de prueba según enunciado oficial
        List<DroneAcuatico> flota = List.of(
            new DroneAcuatico("AR-01", "Aqua-Ranger 100", 92, true,  "Embalse Norte"),
            new DroneAcuatico("AR-02", "Aqua-Ranger 100", 45, true,  "Canal Central"),
            new DroneAcuatico("AR-03", "Aqua-Ranger 100", 18, false, "Laguna Sur"),
            new DroneAcuatico("AR-04", "Aqua-Ranger 100", 73, true,  "Punto Ribereño Este")
        );

        imprimirFlotaInicial(flota);

        ConsultorFlota consultor = new ConsultorFlota();

        // =====================================================================
        // CONSULTA 1: Drones disponibles con batería >= 35% ordenados desc.
        // =====================================================================
        imprimirSeparador("CONSULTA 1: Drones disponibles (batería >= 35%) ordenados de mayor a menor batería");
        System.out.println("  Lógica: stream() -> filter(disponible) -> filter(bateria >= 35) -> sorted(desc) -> toList()");
        List<DroneAcuatico> disponiblesAptos = consultor.obtenerDronesDisponiblesConBateriaSuficiente(flota);
        disponiblesAptos.forEach(d -> System.out.println("   [✓] " + d));

        // =====================================================================
        // CONSULTA 2: Solo IDs de drones disponibles
        // =====================================================================
        imprimirSeparador("CONSULTA 2: Solo IDs de drones disponibles (List<String>)");
        System.out.println("  Lógica: stream() -> filter(disponible) -> map(DroneAcuatico::id) -> toList()");
        List<String> idsDisponibles = consultor.obtenerIdsDronesDisponibles(flota);
        System.out.println("   Resultado: " + idsDisponibles);

        // =====================================================================
        // CONSULTA 3: ¿Existe algún drone disponible con batería >= 35%?
        // =====================================================================
        imprimirSeparador("CONSULTA 3: ¿Existe algún drone disponible con batería >= 35%?");
        System.out.println("  Lógica: stream() -> anyMatch(disponible && bateria >= 35)");
        boolean existeApto = consultor.existeDroneDisponibleConBateriaSuficiente(flota);
        System.out.println("   Resultado: " + (existeApto ? "SÍ (true) — Hay al menos un drone listo para operar" : "NO (false)"));

        // =====================================================================
        // CONSULTA 4: Contar cuántos drones están disponibles
        // =====================================================================
        imprimirSeparador("CONSULTA 4: Total de drones actualmente disponibles");
        System.out.println("  Lógica: stream() -> filter(disponible) -> count()");
        long totalDisponibles = consultor.contarDronesDisponibles(flota);
        System.out.println("   Resultado: " + totalDisponibles + " drones disponibles");

        // =====================================================================
        // CONSULTA 5: Drone con mayor batería de toda la flota
        // =====================================================================
        imprimirSeparador("CONSULTA 5: Drone con mayor batería de toda la flota (Optional<DroneAcuatico>)");
        System.out.println("  Lógica: stream() -> max(Comparator.comparingInt(bateria))");
        Optional<DroneAcuatico> mayorBateria = consultor.obtenerDroneConMayorBateria(flota);
        mayorBateria.ifPresentOrElse(
            d -> System.out.println("   Resultado: " + d),
            () -> System.out.println("   Resultado: Flota vacía")
        );

        imprimirPie();
    }

    private static void imprimirEncabezado() {
        System.out.println("================================================================================");
        System.out.println("  🌊 AQUAPORT — SISTEMA DE GESTIÓN DE FLOTA DE DRONES ACUÁTICOS (ECI)");
        System.out.println("  💧 Nivel Piplup · Reto 01: Streams & Lambdas");
        System.out.println("================================================================================");
    }

    private static void imprimirFlotaInicial(List<DroneAcuatico> flota) {
        System.out.println("\n--- FLOTA BASE REGISTRADA (" + flota.size() + " DRONES) ---");
        flota.forEach(d -> System.out.println("   " + d));
    }

    private static void imprimirSeparador(String titulo) {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println(" ▸ " + titulo);
        System.out.println("--------------------------------------------------------------------------------");
    }

    private static void imprimirPie() {
        System.out.println("\n================================================================================");
        System.out.println("  ✔ Todas las consultas ejecutadas con éxito usando Streams puros.");
        System.out.println("================================================================================\n");
    }
}
