package aquaport.service;

import aquaport.model.DroneAcuatico;
import aquaport.model.DroneBuceador;
import aquaport.model.DroneSemisumergido;
import aquaport.model.DroneSuperficial;
import aquaport.model.EstadoDrone;
import aquaport.model.ZonasEnterprise;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fábrica enterprise de la flota de 60 drones distribuidos en las 4 zonas hídricas ECI.
 */
public final class Flota60Factory {

    private Flota60Factory() {
        throw new UnsupportedOperationException("Clase utilitaria de generacion de flota.");
    }

    public static List<DroneAcuatico> crearFlota60() {
        List<DroneAcuatico> flota = new ArrayList<>();
        poblarEmbalseInvestigacion(flota);
        poblarRedCanales(flota);
        poblarLagunaReserva(flota);
        poblarLaboratorioCentral(flota);
        return Collections.unmodifiableList(flota);
    }

    private static void poblarEmbalseInvestigacion(List<DroneAcuatico> flota) {
        for (int i = 1; i <= 20; i++) {
            int bat = 40 + (i * 3) % 60;
            EstadoDrone est = (i == 5) ? EstadoDrone.MANTENIMIENTO : (i == 12) ? EstadoDrone.SUMERGIDO : EstadoDrone.DISPONIBLE;
            flota.add(new DroneBuceador(String.format("DB-%02d", i), "DeepProbe-E3", bat, est, ZonasEnterprise.EMBALSE_INVESTIGACION));
        }
    }

    private static void poblarRedCanales(List<DroneAcuatico> flota) {
        for (int i = 1; i <= 20; i++) {
            int bat = 45 + (i * 2) % 55;
            EstadoDrone est = (i == 8) ? EstadoDrone.RECARGANDO : (i == 19) ? EstadoDrone.FALLO : EstadoDrone.DISPONIBLE;
            flota.add(new DroneSuperficial(String.format("DS-%02d", i), "AquaSkimmer-E3", bat, est, ZonasEnterprise.RED_CANALES));
        }
    }

    private static void poblarLagunaReserva(List<DroneAcuatico> flota) {
        for (int i = 1; i <= 15; i++) {
            int bat = 50 + (i * 3) % 50;
            EstadoDrone est = (i == 3) ? EstadoDrone.EN_MISION : (i == 14) ? EstadoDrone.RECARGANDO : EstadoDrone.DISPONIBLE;
            flota.add(new DroneSemisumergido(String.format("DM-%02d", i), "WaveRanger-E3", bat, est, ZonasEnterprise.LAGUNA_RESERVA));
        }
    }

    private static void poblarLaboratorioCentral(List<DroneAcuatico> flota) {
        flota.add(new DroneSemisumergido("DM-16", "WaveRanger-Hub", 95, EstadoDrone.DISPONIBLE, ZonasEnterprise.LABORATORIO_CENTRAL));
        flota.add(new DroneSemisumergido("DM-17", "WaveRanger-Hub", 88, EstadoDrone.DISPONIBLE, ZonasEnterprise.LABORATORIO_CENTRAL));
        flota.add(new DroneSuperficial("DS-21", "AquaSkimmer-Hub", 92, EstadoDrone.DISPONIBLE, ZonasEnterprise.LABORATORIO_CENTRAL));
        flota.add(new DroneSuperficial("DS-22", "AquaSkimmer-Hub", 85, EstadoDrone.DISPONIBLE, ZonasEnterprise.LABORATORIO_CENTRAL));
        flota.add(new DroneBuceador("DB-21", "DeepProbe-Hub", 90, EstadoDrone.DISPONIBLE, ZonasEnterprise.LABORATORIO_CENTRAL));
    }
}
