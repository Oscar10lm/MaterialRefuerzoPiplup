package aquaport.model;

import java.time.LocalDateTime;

/**
 * Notificación formal emitida por el Centro de Control ante anomalías en la flota (Prinplup v2).
 */
public record AlertaFlota(
    String id,
    String droneId,
    NivelAlerta nivel,
    String zona,
    String mensaje,
    LocalDateTime timestamp
) {
    public enum NivelAlerta {
        INFO,
        ADVERTENCIA,
        CRITICA
    }

    public boolean isCritica() {
        return nivel == NivelAlerta.CRITICA;
    }
}
