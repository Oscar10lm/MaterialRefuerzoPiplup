package aquaport.service;

import aquaport.model.AlertaFlota;
import aquaport.model.AlertaFlota.NivelAlerta;
import aquaport.model.DroneAcuatico;
import aquaport.model.EstadoDrone;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servicio del Centro de Control de AquaPort v2 (Prinplup).
 * 
 * Monitorea el estado de la flota acuática y despacha alertas automáticas en tiempo real
 * cuando un drone entra en fallo, batería crítica o estado de mantenimiento anómalo.
 */
public class CentroControlAlertas {

    private static final Logger LOGGER = Logger.getLogger(CentroControlAlertas.class.getName());
    public static final int UMBRAL_BATERIA_CRITICA = 20;

    private final List<AlertaFlota> historialAlertas = new ArrayList<>();

    /**
     * Evalúa el estado operativo de un drone y genera una alerta automática si detecta anomalías.
     *
     * @param drone Drone a monitorear
     * @return Optional con la alerta generada o vacío si el drone opera normalmente
     */
    public Optional<AlertaFlota> evaluarEstadoDrone(DroneAcuatico drone) {
        if (drone == null) {
            return Optional.empty();
        }

        // Caso 1: Batería crítica (< 20%)
        if (drone.getBateria() < UMBRAL_BATERIA_CRITICA) {
            return Optional.of(emitirAlerta(
                    drone.getId(),
                    NivelAlerta.CRITICA,
                    drone.getZona(),
                    String.format("Batería crítica (%d%%) en drone '%s'. Retorno a base o recarga urgente requerida.",
                            drone.getBateria(), drone.getId())
            ));
        }

        // Caso 2: Drone en mantenimiento/fallo
        if (drone.getEstado() == EstadoDrone.MANTENIMIENTO) {
            return Optional.of(emitirAlerta(
                    drone.getId(),
                    NivelAlerta.ADVERTENCIA,
                    drone.getZona(),
                    String.format("Drone '%s' en estado de MANTENIMIENTO fuera de servicio en zona '%s'.",
                            drone.getId(), drone.getZona())
            ));
        }

        return Optional.empty();
    }

    /**
     * Emite, registra y propaga formalmente una alerta en el centro de control.
     */
    public AlertaFlota emitirAlerta(String droneId, NivelAlerta nivel, String zona, String mensaje) {
        Objects.requireNonNull(droneId, "El ID del drone no puede ser nulo.");
        Objects.requireNonNull(nivel, "El nivel de alerta no puede ser nulo.");

        String idAlerta = "ALT-" + (historialAlertas.size() + 1);
        AlertaFlota alerta = new AlertaFlota(idAlerta, droneId, nivel, zona, mensaje, LocalDateTime.now());
        historialAlertas.add(alerta);

        Level logLevel = switch (nivel) {
            case CRITICA -> Level.SEVERE;
            case ADVERTENCIA -> Level.WARNING;
            case INFO -> Level.INFO;
        };

        LOGGER.log(logLevel, "[CENTRO DE CONTROL] Alerta [{0}] en {1}: {2}",
                new Object[]{idAlerta, zona, mensaje});

        return alerta;
    }

    public List<AlertaFlota> obtenerAlertasCriticas() {
        return historialAlertas.stream()
                .filter(AlertaFlota::isCritica)
                .toList();
    }

    public List<AlertaFlota> obtenerHistorialAlertas() {
        return Collections.unmodifiableList(historialAlertas);
    }
}
