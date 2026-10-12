package aquaport.service;

import aquaport.model.Mision;
import aquaport.repository.RepositorioMisiones;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Servicio con responsabilidad única: Coordinar el registro y consulta de misiones (SRP).
 * 
 * Aplica Dependency Inversion Principle (DIP):
 * Depende de la abstracción {@link RepositorioMisiones} inyectada mediante constructor,
 * nunca de implementaciones concretas.
 */
public class RegistradorMisiones {

    private final RepositorioMisiones repositorio;
    private final ValidadorMision validador;
    private final NotificadorOperador notificador;

    /**
     * Inyección por constructor respetando DIP y desacoplamiento.
     *
     * @param repositorio Abstracción de persistencia (DIP)
     * @param validador   Servicio especializado en reglas de negocio (SRP)
     * @param notificador Servicio especializado en comunicación al operador (SRP)
     */
    public RegistradorMisiones(RepositorioMisiones repositorio,
                               ValidadorMision validador,
                               NotificadorOperador notificador) {
        this.repositorio = Objects.requireNonNull(repositorio, "El repositorio de misiones no puede ser nulo.");
        this.validador = Objects.requireNonNull(validador, "El validador de misiones no puede ser nulo.");
        this.notificador = Objects.requireNonNull(notificador, "El notificador de operador no puede ser nulo.");
    }

    /**
     * Registra una misión tras validarla con el validador y persistirla en el repositorio.
     *
     * @param mision Misión a registrar
     */
    public void registrarMision(Mision mision) {
        if (mision == null) {
            throw new IllegalArgumentException("La misión a registrar no puede ser nula.");
        }

        if (repositorio.existePorId(mision.getId())) {
            String error = String.format("Ya existe una misión registrada con el ID '%s'.", mision.getId());
            notificador.notificarFallo(error);
            throw new IllegalStateException(error);
        }

        // Delegar la validación al componente con responsabilidad única (SRP)
        validador.validar(mision);

        // Delegar la persistencia al repositorio abstracto (DIP)
        repositorio.guardar(mision);

        // Delegar la notificación al operador (SRP)
        notificador.notificarMisionRegistrada(mision);
    }

    /**
     * Consulta una misión por su identificador.
     *
     * @param id Identificador de la misión
     * @return Optional con la misión encontrada
     */
    public Optional<Mision> consultarMisionPorId(String id) {
        return repositorio.buscarPorId(id);
    }

    /**
     * Consulta el listado completo de misiones registradas.
     *
     * @return Lista de misiones
     */
    public List<Mision> consultarTodasLasMisiones() {
        return repositorio.obtenerTodas();
    }

    /**
     * HU-03: Cancela una misión registrada siempre que su estado sea PENDIENTE.
     *
     * @param id Identificador de la misión a cancelar
     */
    public void cancelarMisionPendiente(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El ID de la misión a cancelar no puede ser nulo ni vacío.");
        }

        Mision mision = repositorio.buscarPorId(id)
                .orElseThrow(() -> new IllegalStateException(
                    String.format("No se encontró ninguna misión registrada con el ID '%s'.", id)
                ));

        if (mision.getEstado() != aquaport.model.EstadoMision.PENDIENTE) {
            String error = String.format(
                "No se puede cancelar la misión '%s' porque su estado actual es '%s' (solo PENDIENTE es cancelable).",
                id, mision.getEstado()
            );
            notificador.notificarAdvertencia(error);
            throw new IllegalStateException(error);
        }

        repositorio.eliminarPorId(id);
        notificador.notificarAdvertencia(String.format("Misión [%s] cancelada por el operador. Drone liberado.", id));
    }
}
