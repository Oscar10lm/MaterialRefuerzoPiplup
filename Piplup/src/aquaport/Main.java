package aquaport;

import aquaport.model.DroneAcuatico;
import aquaport.model.EstadoMision;
import aquaport.model.Mision;
import aquaport.model.TipoCarga;
import aquaport.service.ConsultorFlota;

import java.util.List;
import java.util.Optional;

/**
 * Punto de entrada para el sistema AquaPort (Nivel Piplup).
 * Demuestra:
 *  - Reto 01: ConsultorFlota mediante Streams & Lambdas.
 *  - Reto 03: Construcción validada de misiones con Patrón Builder.
 */
public class Main {

    public static void main(String[] args) {
        imprimirEncabezadoGeneral();

        // ---------------------------------------------------------------------
        // 1. FLOTA BASE OFICIAL (Reto 01)
        // ---------------------------------------------------------------------
        List<DroneAcuatico> flota = List.of(
            new DroneAcuatico("AR-01", "Aqua-Ranger 100", 92, true,  "Embalse Norte"),
            new DroneAcuatico("AR-02", "Aqua-Ranger 100", 45, true,  "Canal Central"),
            new DroneAcuatico("AR-03", "Aqua-Ranger 100", 18, false, "Laguna Sur"),
            new DroneAcuatico("AR-04", "Aqua-Ranger 100", 73, true,  "Punto Ribereño Este")
        );

        ejecutarDemostracionReto01(flota);
        ejecutarDemostracionReto03(flota);

        imprimirPieGeneral();
    }

    private static void ejecutarDemostracionReto01(List<DroneAcuatico> flota) {
        System.out.println("\n╔═══════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    RETO 01: CONSULTOR FLOTA CON STREAMS                       ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════════════════════╝");

        imprimirFlota(flota);

        ConsultorFlota consultor = new ConsultorFlota();

        // Consulta 1
        imprimirSubtitulo("1. Drones disponibles con batería >= 35% (descendente)");
        consultor.obtenerDronesDisponiblesConBateriaSuficiente(flota)
                .forEach(d -> System.out.println("   [✓] " + d));

        // Consulta 2
        imprimirSubtitulo("2. Solo IDs de drones disponibles");
        System.out.println("   Resultado: " + consultor.obtenerIdsDronesDisponibles(flota));

        // Consulta 3
        imprimirSubtitulo("3. ¿Existe algún drone disponible con batería >= 35%?");
        System.out.println("   Resultado: " + (consultor.existeDroneDisponibleConBateriaSuficiente(flota) ? "SÍ (true)" : "NO (false)"));

        // Consulta 4
        imprimirSubtitulo("4. Total de drones disponibles");
        System.out.println("   Resultado: " + consultor.contarDronesDisponibles(flota) + " drones");

        // Consulta 5
        imprimirSubtitulo("5. Drone con mayor batería");
        consultor.obtenerDroneConMayorBateria(flota)
                .ifPresentOrElse(d -> System.out.println("   Resultado: " + d), () -> System.out.println("   Flota vacía"));
    }

    private static void ejecutarDemostracionReto03(List<DroneAcuatico> flota) {
        System.out.println("\n\n╔═══════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║               RETO 03: PATRÓN BUILDER PARA MISIONES VÁLIDAS                  ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════════════════════╝");

        DroneAcuatico droneDisponible = flota.get(0); // AR-01 (disponible = true)
        DroneAcuatico droneOcupado = flota.get(2);    // AR-03 (disponible = false)

        // Caso de éxito: Construcción válida con Builder
        imprimirSubtitulo("CASO 1 [EXITOSO]: Construcción de Misión con datos válidos");
        try {
            Mision misionValida = new Mision.Builder()
                    .id("M-001")
                    .drone(droneDisponible)
                    .puntoPartida("Embalse Norte")
                    .puntoLlegada("Laboratorio Hídrico")
                    .tipoCarga(TipoCarga.MUESTRA_AGUA)
                    .estado(EstadoMision.PENDIENTE)
                    .build();

            System.out.println(" ✔ Misión construida con éxito:");
            System.out.println(misionValida);
        } catch (IllegalStateException e) {
            System.err.println(" [!] Error inesperado: " + e.getMessage());
        }

        // Caso de validación 1: Asignar un drone no disponible
        imprimirSubtitulo("CASO 2 [VALIDACIÓN]: Intento de asignar Drone NO DISPONIBLE (AR-03)");
        try {
            new Mision.Builder()
                    .id("M-002")
                    .drone(droneOcupado)
                    .puntoPartida("Laguna Sur")
                    .puntoLlegada("Canal Central")
                    .tipoCarga(TipoCarga.SENSOR)
                    .build();
            System.err.println(" [!] ERROR: El Builder debió rechazar el drone no disponible.");
        } catch (IllegalStateException e) {
            System.out.println(" [X] Excepción capturada exitosamente:");
            System.out.println("     ➜ " + e.getMessage());
        }

        // Caso de validación 2: Campos obligatorios faltantes / nulos
        imprimirSubtitulo("CASO 3 [VALIDACIÓN]: Intento de construir misión sin punto de llegada");
        try {
            new Mision.Builder()
                    .id("M-003")
                    .drone(droneDisponible)
                    .puntoPartida("Embalse Norte")
                    // puntoLlegada omitido deliberadamente
                    .tipoCarga(TipoCarga.PAQUETE_LIGERO)
                    .build();
            System.err.println(" [!] ERROR: El Builder debió rechazar el puntoLlegada nulo/vacío.");
        } catch (IllegalStateException e) {
            System.out.println(" [X] Excepción capturada exitosamente:");
            System.out.println("     ➜ " + e.getMessage());
        }
    }

    private static void imprimirEncabezadoGeneral() {
        System.out.println("================================================================================");
        System.out.println("  🌊 AQUAPORT — SISTEMA DE GESTIÓN DE FLOTA DE DRONES ACUÁTICOS (ECI)");
        System.out.println("  💧 Nivel Piplup · Módulo Principal de Pruebas y Operaciones");
        System.out.println("================================================================================");
    }

    private static void imprimirFlota(List<DroneAcuatico> flota) {
        System.out.println("\n--- FLOTA BASE REGISTRADA (" + flota.size() + " DRONES) ---");
        flota.forEach(d -> System.out.println("   " + d));
    }

    private static void imprimirSubtitulo(String titulo) {
        System.out.println("\n ▸ " + titulo);
        System.out.println("   ─────────────────────────────────────────────────────────────────────────────");
    }

    private static void imprimirPieGeneral() {
        System.out.println("\n================================================================================");
        System.out.println("  ✔ Todas las validaciones y ejecuciones de AquaPort concluyeron con éxito.");
        System.out.println("================================================================================\n");
    }
}
