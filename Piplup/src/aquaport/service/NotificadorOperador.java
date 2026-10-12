package aquaport.service;

import aquaport.model.Mision;

/**
 * Servicio con responsabilidad única: Notificar y comunicar eventos al operador (SRP).
 * 
 * Desacoplado de la persistencia y de la validación.
 */
public class NotificadorOperador {

    public void notificarMisionRegistrada(Mision mision) {
        System.out.printf("  📢 [NOTIFICACIÓN AL OPERADOR] Misión [%s] registrada exitosamente.%n", mision.getId());
        System.out.printf("     Drone: %s | Ruta: %s -> %s | Carga: %s%n",
                mision.getDrone().id(),
                mision.getPuntoPartida(),
                mision.getPuntoLlegada(),
                mision.getTipoCarga());
    }

    public void notificarAdvertencia(String mensaje) {
        System.out.printf("  ⚠️ [ALERTA AL OPERADOR] %s%n", mensaje);
    }

    public void notificarFallo(String mensaje) {
        System.out.printf("  ❌ [ERROR OPERATIVO] %s%n", mensaje);
    }
}
