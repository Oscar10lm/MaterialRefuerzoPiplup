package aquaport;

import aquaport.model.DroneAcuatico;
import aquaport.model.EstadoMision;
import aquaport.model.MisionEnterprise;
import aquaport.model.Prioridad;
import aquaport.model.TipoCarga;
import aquaport.model.TramoRuta;
import aquaport.model.Waypoint;
import aquaport.model.ZonasEnterprise;
import aquaport.service.ConsultorFlotaEnterprise;
import aquaport.service.Flota60Factory;

import java.util.List;
import java.util.Map;

/**
 * Punto de entrada y demostración de capacidades Enterprise de AquaPort (Empoleon).
 */
public final class MainEmpoleon {

    private MainEmpoleon() {
        throw new UnsupportedOperationException("Clase utilitaria de inicio.");
    }

    public static void main(String[] args) {
        List<DroneAcuatico> flota = Flota60Factory.crearFlota60();
        ConsultorFlotaEnterprise consultor = new ConsultorFlotaEnterprise();
        List<Waypoint> waypoints = crearRutaDemo();
        List<MisionEnterprise> misiones = crearMisionesDemo(flota, waypoints);

        imprimirEncabezado(flota.size());
        demostrarCargaYEficiencia(consultor, misiones);
        demostrarRutasYPromedios(consultor, flota, waypoints, misiones);
    }

    private static void imprimirEncabezado(int totalDrones) {
        System.out.println("================================================================================");
        System.out.println("  👑 AquaPort Enterprise (Empoleon v3) - Red Multi-Embalse");
        System.out.println("  Flota Activa: " + totalDrones + " drones en 4 zonas hidricas");
        System.out.println("================================================================================");
    }

    private static List<Waypoint> crearRutaDemo() {
        return List.of(
                new Waypoint("WP-01", "Boya Limnologica Norte", ZonasEnterprise.EMBALSE_INVESTIGACION, 4.783, -74.043, 1),
                new Waypoint("WP-02", "Esclusa de Transferencia", ZonasEnterprise.RED_CANALES, 4.781, -74.042, 2),
                new Waypoint("WP-03", "Boya Laguna Sur", ZonasEnterprise.LAGUNA_RESERVA, 4.779, -74.041, 3),
                new Waypoint("WP-04", "Muelle Central", ZonasEnterprise.LABORATORIO_CENTRAL, 4.780, -74.040, 4)
        );
    }

    private static List<MisionEnterprise> crearMisionesDemo(List<DroneAcuatico> flota, List<Waypoint> waypoints) {
        MisionEnterprise m1 = new MisionEnterprise("MIS-301", ZonasEnterprise.EMBALSE_INVESTIGACION, ZonasEnterprise.LABORATORIO_CENTRAL,
                waypoints, flota.get(0), Prioridad.CRITICA, TipoCarga.MUESTRA_AGUA, EstadoMision.EN_TRANSITO);
        MisionEnterprise m2 = new MisionEnterprise("MIS-302", ZonasEnterprise.RED_CANALES, ZonasEnterprise.LABORATORIO_CENTRAL,
                waypoints.subList(1, 4), flota.get(20), Prioridad.ALTA, TipoCarga.SENSOR, EstadoMision.COMPLETADA);
        MisionEnterprise m3 = new MisionEnterprise("MIS-303", ZonasEnterprise.LAGUNA_RESERVA, ZonasEnterprise.LABORATORIO_CENTRAL,
                waypoints.subList(2, 4), flota.get(40), Prioridad.NORMAL, TipoCarga.EQUIPO_MEDICION, EstadoMision.COMPLETADA);
        return List.of(m1, m2, m3);
    }

    private static void demostrarCargaYEficiencia(ConsultorFlotaEnterprise consultor, List<MisionEnterprise> misiones) {
        Map<String, Long> carga = consultor.calcularCargaTrabajoPorZona(misiones);
        Map<String, Double> eficiencia = consultor.calcularEficienciaPorZona(misiones);
        System.out.println("\n📊 1. Carga de Trabajo Activa por Zona (EN_TRANSITO): " + carga);
        System.out.println("📈 2. Eficiencia de Misiones por Zona: " + eficiencia);
    }

    private static void demostrarRutasYPromedios(ConsultorFlotaEnterprise consultor, List<DroneAcuatico> flota,
                                                 List<Waypoint> waypoints, List<MisionEnterprise> misiones) {
        List<TramoRuta> tramos = consultor.planificarRutaOptimaMultiEtapa(waypoints, flota);
        System.out.println("\n🗺️ 3. Planificacion Multi-Etapa con Traspasos (Tramos: " + tramos.size() + "):");
        tramos.forEach(t -> System.out.println("   -> " + t));
        consultor.encontrarDroneMejorHistorial(misiones)
                .ifPresent(d -> System.out.println("\n🏆 4. Drone con Mejor Historial: " + d.getId() + " (" + d.getModelo() + ")"));
        Map<String, Double> promedios = consultor.calcularBateriaPromedioPorZona(flota);
        System.out.println("\n⚡ 5. Bateria Promedio en Sola Pasada (Custom Collector): " + promedios);
    }
}
