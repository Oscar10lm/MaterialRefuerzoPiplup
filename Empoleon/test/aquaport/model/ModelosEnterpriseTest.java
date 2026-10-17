package aquaport.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelosEnterpriseTest {

    @Test
    @DisplayName("DroneAcuatico y subclases validan parametros y limites fisicos")
    void drones_validanPropiedades() {
        DroneAcuatico ds = new DroneSuperficial("DS-01", "S1", 90, EstadoDrone.DISPONIBLE, ZonasEnterprise.RED_CANALES);
        DroneAcuatico dm = new DroneSemisumergido("DM-01", "M1", 85, EstadoDrone.EN_MISION, ZonasEnterprise.LAGUNA_RESERVA);
        DroneAcuatico db = new DroneBuceador("DB-01", "B1", 80, EstadoDrone.SUMERGIDO, ZonasEnterprise.EMBALSE_INVESTIGACION);

        assertEquals(TipoDrone.SUPERFICIAL, ds.getTipo());
        assertEquals(TipoDrone.SEMISUMERGIDO, dm.getTipo());
        assertEquals(TipoDrone.BUCEADOR, db.getTipo());
        assertTrue(ds.isDisponible());
        assertTrue(dm.getEstado().isOperativo());
        assertEquals(ds, new DroneSuperficial("DS-01", "Otro", 50, EstadoDrone.RECARGANDO, "X"));
        assertNotEquals(ds, dm);
        assertNotEquals(ds, "string");
        assertNotNull(ds.toString());
    }

    @Test
    @DisplayName("DroneAcuatico lanza excepciones ante parametros nulos o rangos invalidos")
    void drones_excepcionesValidacion() {
        assertThrows(NullPointerException.class, () -> new DroneSuperficial(null, "M", 50, EstadoDrone.DISPONIBLE, "Z"));
        assertThrows(NullPointerException.class, () -> new DroneSuperficial("D", null, 50, EstadoDrone.DISPONIBLE, "Z"));
        assertThrows(NullPointerException.class, () -> new DroneSuperficial("D", "M", 50, EstadoDrone.DISPONIBLE, null));
        assertThrows(IllegalArgumentException.class, () -> new DroneSuperficial("D", "M", -1, EstadoDrone.DISPONIBLE, "Z"));
        assertThrows(IllegalArgumentException.class, () -> new DroneSuperficial("D", "M", 101, EstadoDrone.DISPONIBLE, "Z"));
    }

    @Test
    @DisplayName("Waypoint calcula distancias euclidianas y valida identidad")
    void waypoint_propiedadesYDistancia() {
        Waypoint w1 = new Waypoint("W1", "Alfa", ZonasEnterprise.RED_CANALES, 4.78, -74.04, 1);
        Waypoint w2 = new Waypoint("W2", "Beta", ZonasEnterprise.RED_CANALES, 4.79, -74.05, 2);

        assertTrue(w1.distanciaA(w2) > 0.0);
        assertEquals(Double.MAX_VALUE, w1.distanciaA(null));
        assertEquals(w1, new Waypoint("W1", "X", "Y", 0, 0, 0));
        assertNotEquals(w1, w2);
        assertNotEquals(w1, null);
        assertNotNull(w1.toString());
        assertThrows(NullPointerException.class, () -> new Waypoint(null, "B", "C", 0, 0, 0));
    }

    @Test
    @DisplayName("TramoRuta y MisionEnterprise gestionan trazabilidad y estado")
    void tramoYMision_gestionCicloVida() {
        DroneAcuatico drone = new DroneSuperficial("DS-01", "S1", 90, EstadoDrone.DISPONIBLE, ZonasEnterprise.RED_CANALES);
        Waypoint w1 = new Waypoint("W1", "A", ZonasEnterprise.RED_CANALES, 4.78, -74.04, 1);
        Waypoint w2 = new Waypoint("W2", "B", ZonasEnterprise.RED_CANALES, 4.79, -74.04, 2);
        TramoRuta tramo = new TramoRuta("TR-1", w1, w2, drone);

        assertFalse(tramo.isCompletado());
        tramo.marcarCompletado();
        assertTrue(tramo.isCompletado());
        assertNotNull(tramo.toString());

        MisionEnterprise mision = new MisionEnterprise("M-1", ZonasEnterprise.RED_CANALES, ZonasEnterprise.LABORATORIO_CENTRAL,
                List.of(w1, w2), drone, Prioridad.CRITICA, TipoCarga.SENSOR, EstadoMision.PENDIENTE);
        mision.agregarTramo(tramo);
        mision.agregarTramo(null);
        assertEquals(1, mision.getTramos().size());
        assertEquals(1, mision.getDronesInvolucrados().size());
        assertTrue(mision.getPrioridad().isCritica());
        mision.setEstado(EstadoMision.COMPLETADA);
        assertTrue(mision.isCompletada());
        assertTrue(mision.getEstado().isExitosa());
        assertTrue(mision.getEstado().isTerminada());
        assertNotNull(mision.toString());
    }

    @Test
    @DisplayName("ZonasEnterprise valida catalogo y previene instanciacion utilitaria")
    void zonasEnterprise_catalogoYConstructorPrivado() throws Exception {
        assertTrue(ZonasEnterprise.esValida(ZonasEnterprise.EMBALSE_INVESTIGACION));
        assertFalse(ZonasEnterprise.esValida("Zona Invalida"));
        assertFalse(ZonasEnterprise.esValida(null));
        assertEquals(4, ZonasEnterprise.obtenerTodas().size());

        Constructor<ZonasEnterprise> c = ZonasEnterprise.class.getDeclaredConstructor();
        c.setAccessible(true);
        assertThrows(InvocationTargetException.class, c::newInstance);
    }

    @Test
    @DisplayName("CondicionHidrica y enums evaluan todas las ramas logicas de navegabilidad")
    void condicionesHidricas_evaluaTodasLasRamas() {
        CondicionHidrica cTranquila = new CondicionHidrica(NivelAgua.TRANQUILO, NivelTurbidez.CLARA, 20, 5);
        CondicionHidrica cAgitada = new CondicionHidrica(NivelAgua.AGITADO, NivelTurbidez.MEDIA, 18, 12);
        CondicionHidrica cTormenta = new CondicionHidrica(NivelAgua.TORMENTOSO, NivelTurbidez.CRITICA, 10, 20);
        CondicionHidrica cTurbia = new CondicionHidrica(NivelAgua.MODERADO, NivelTurbidez.CRITICA, 15, 8);

        assertTrue(cTranquila.esSeguraPara(TipoDrone.SUPERFICIAL));
        assertFalse(cAgitada.esSeguraPara(TipoDrone.SUPERFICIAL));
        assertTrue(cAgitada.esSeguraPara(TipoDrone.SEMISUMERGIDO));
        assertFalse(cTormenta.esSeguraPara(TipoDrone.SEMISUMERGIDO));
        assertTrue(cAgitada.esSeguraPara(TipoDrone.BUCEADOR));
        assertFalse(cTurbia.esSeguraPara(TipoDrone.BUCEADOR));
        assertFalse(cTranquila.esSeguraPara(null));
        assertFalse(cTormenta.esSeguraPara(TipoDrone.BUCEADOR));

        assertTrue(NivelTurbidez.CLARA.permiteVisionOptica());
        assertTrue(NivelTurbidez.MEDIA.permiteVisionOptica());
        assertFalse(NivelTurbidez.ALTA.permiteVisionOptica());
        assertFalse(NivelTurbidez.CRITICA.permiteVisionOptica());
        assertTrue(NivelAgua.TRANQUILO.esNavegable());
        assertFalse(NivelAgua.TORMENTOSO.esNavegable());

        assertFalse(ResultadoValidacion.rechazo(null).isExitoso());
        assertTrue(ResultadoValidacion.ok().toString().contains("OK"));
        assertTrue(ResultadoValidacion.rechazo("X").toString().contains("RECHAZO"));
        assertTrue(EstadoDrone.EN_MISION.isOperativo());
        assertTrue(EstadoDrone.SUMERGIDO.isOperativo());
        assertFalse(EstadoDrone.RECARGANDO.isOperativo());
        assertFalse(EstadoMision.PENDIENTE.isTerminada());
        assertTrue(EstadoMision.FALLIDA.isTerminada());
        assertTrue(EstadoMision.CANCELADA.isTerminada());
    }
}
