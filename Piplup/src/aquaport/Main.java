package aquaport;

import aquaport.model.DroneAcuatico;
import aquaport.model.EstadoMision;
import aquaport.model.Mision;
import aquaport.model.TipoCarga;
import aquaport.model.ZonasCampus;
import aquaport.repository.RepositorioMisiones;
import aquaport.repository.RepositorioMisionesEnMemoria;
import aquaport.service.ConsultorFlota;
import aquaport.service.NotificadorOperador;
import aquaport.service.RegistradorMisiones;
import aquaport.service.ValidadorMision;

import java.util.List;

/**
 * Punto de entrada para el sistema AquaPort (Nivel Piplup).
 * 
 * Demuestra:
 *  - Reto 01: ConsultorFlota mediante Streams & Lambdas.
 *  - Reto 03: Construcción validada de misiones con Patrón Builder.
 *  - Reto SRP & DIP: Arquitectura desacoplada en el núcleo operativo.
 */
public class Main {

    private Main() {
        // Constructor privado para clase utilitaria con método estático main (Sonar java:S1118)
    }

    public static void main(String[] args) {
        imprimirEncabezadoGeneral();

        // Flota base oficial
        List<DroneAcuatico> flota = List.of(
            new DroneAcuatico("AR-01", "Aqua-Ranger 100", 92, true,  ZonasCampus.EMBALSE_NORTE),
            new DroneAcuatico("AR-02", "Aqua-Ranger 100", 45, true,  ZonasCampus.CANAL_CENTRAL),
            new DroneAcuatico("AR-03", "Aqua-Ranger 100", 18, false, ZonasCampus.LAGUNA_SUR),
            new DroneAcuatico("AR-04", "Aqua-Ranger 100", 73, true,  ZonasCampus.PUNTO_RIBERENO_ESTE)
        );

        ejecutarDemostracionReto01(flota);
        ejecutarDemostracionReto03(flota);
        ejecutarDemostracionRetoSrpDip(flota);

        imprimirPieGeneral();
    }

    private static void ejecutarDemostracionReto01(List<DroneAcuatico> flota) {
        System.out.println("\n╔═══════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    RETO 01: CONSULTOR FLOTA CON STREAMS                       ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════════════════════╝");

        imprimirFlota(flota);
        ConsultorFlota consultor = new ConsultorFlota();

        imprimirSubtitulo("1. Drones disponibles con batería >= 35% (descendente)");
        consultor.obtenerDronesDisponiblesConBateriaSuficiente(flota).forEach(d -> System.out.println("   [✓] " + d));

        imprimirSubtitulo("2. Solo IDs de drones disponibles");
        System.out.println("   Resultado: " + consultor.obtenerIdsDronesDisponibles(flota));

        imprimirSubtitulo("3. ¿Existe algún drone disponible con batería >= 35%?");
        System.out.println("   Resultado: " + (consultor.existeDroneDisponibleConBateriaSuficiente(flota) ? "SÍ (true)" : "NO (false)"));

        imprimirSubtitulo("4. Total de drones disponibles");
        System.out.println("   Resultado: " + consultor.contarDronesDisponibles(flota) + " drones");

        imprimirSubtitulo("5. Drone con mayor batería");
        consultor.obtenerDroneConMayorBateria(flota)
                .ifPresentOrElse(d -> System.out.println("   Resultado: " + d), () -> System.out.println("   Flota vacía"));
    }

    private static void ejecutarDemostracionReto03(List<DroneAcuatico> flota) {
        System.out.println("\n\n╔═══════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║               RETO 03: PATRÓN BUILDER PARA MISIONES VÁLIDAS                  ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════════════════════╝");

        DroneAcuatico droneDisponible = flota.get(0); // AR-01
        DroneAcuatico droneOcupado = flota.get(2);    // AR-03

        imprimirSubtitulo("CASO 1 [EXITOSO]: Construcción de Misión con datos válidos");
        try {
            Mision misionValida = new Mision.Builder()
                    .id("M-001")
                    .drone(droneDisponible)
                    .puntoPartida(ZonasCampus.EMBALSE_NORTE)
                    .puntoLlegada(ZonasCampus.LABORATORIO_HIDRICO)
                    .tipoCarga(TipoCarga.MUESTRA_AGUA)
                    .estado(EstadoMision.PENDIENTE)
                    .build();
            System.out.println(" ✔ Misión construida con éxito:\n" + misionValida);
        } catch (IllegalStateException e) {
            System.err.println(" [!] Error inesperado: " + e.getMessage());
        }

        imprimirSubtitulo("CASO 2 [VALIDACIÓN]: Intento de asignar Drone NO DISPONIBLE (AR-03)");
        try {
            new Mision.Builder()
                    .id("M-002")
                    .drone(droneOcupado)
                    .puntoPartida(ZonasCampus.LAGUNA_SUR)
                    .puntoLlegada(ZonasCampus.CANAL_CENTRAL)
                    .tipoCarga(TipoCarga.SENSOR)
                    .build();
        } catch (IllegalStateException e) {
            System.out.println(" [X] Excepción capturada exitosamente:\n     ➜ " + e.getMessage());
        }
    }

    private static void ejecutarDemostracionRetoSrpDip(List<DroneAcuatico> flota) {
        System.out.println("\n\n╔═══════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                 RETO ARQUITECTURA: PRINCIPIOS SRP Y DIP EN NÚCLEO             ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════════════════════╝");

        // Configuración de dependencias (DIP: Inyección por constructor)
        RepositorioMisiones repositorio = new RepositorioMisionesEnMemoria();
        ValidadorMision validador = new ValidadorMision();
        NotificadorOperador notificador = new NotificadorOperador();

        RegistradorMisiones registrador = new RegistradorMisiones(repositorio, validador, notificador);

        DroneAcuatico drone1 = flota.get(0); // AR-01 (92%, disponible)
        DroneAcuatico drone2 = flota.get(1); // AR-02 (45%, disponible)

        // CASO 1: Registro exitoso
        imprimirSubtitulo("CASO 1 [SRP/DIP]: Registro exitoso de misión mediante servicios desacoplados");
        Mision mision1 = new Mision.Builder()
                .id("MIS-101")
                .drone(drone1)
                .puntoPartida(ZonasCampus.EMBALSE_NORTE)
                .puntoLlegada(ZonasCampus.LABORATORIO_HIDRICO)
                .tipoCarga(TipoCarga.MUESTRA_AGUA)
                .build();
        registrador.registrarMision(mision1);

        Mision mision2 = new Mision.Builder()
                .id("MIS-102")
                .drone(drone2)
                .puntoPartida(ZonasCampus.CANAL_CENTRAL)
                .puntoLlegada(ZonasCampus.PUNTO_RIBERENO_ESTE)
                .tipoCarga(TipoCarga.SENSOR)
                .build();
        registrador.registrarMision(mision2);

        // CASO 2: Validación de regla de negocio violada (partida == llegada)
        imprimirSubtitulo("CASO 2 [SRP]: Rechazo por regla de negocio (origen igual a destino)");
        try {
            Mision misionInvalidaRuta = new Mision.Builder()
                    .id("MIS-103")
                    .drone(drone1)
                    .puntoPartida(ZonasCampus.EMBALSE_NORTE)
                    .puntoLlegada(ZonasCampus.EMBALSE_NORTE) // Mismo punto
                    .tipoCarga(TipoCarga.PAQUETE_LIGERO)
                    .build();
            registrador.registrarMision(misionInvalidaRuta);
        } catch (IllegalStateException e) {
            System.out.println(" [X] Regla capturada por ValidadorMision (SRP):\n     ➜ " + e.getMessage());
        }

        // Consulta de misiones en el repositorio
        imprimirSubtitulo("CONSULTA FINAL: Misiones persistidas en RepositorioMisiones (DIP)");
        List<Mision> todas = registrador.consultarTodasLasMisiones();
        System.out.println(" Total en repositorio: " + todas.size());
        todas.forEach(m -> System.out.println("   • ID: " + m.getId() + " | Drone: " + m.getDrone().id() + " | Ruta: " + m.getPuntoPartida() + " -> " + m.getPuntoLlegada()));
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
