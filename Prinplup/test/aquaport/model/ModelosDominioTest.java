package aquaport.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias de cobertura exhaustiva para las clases de dominio de la v2 (Reto 13).
 * Garantiza >= 80% (meta: 100%) en todo el paquete aquaport.model.
 */
class ModelosDominioTest {

    // -------------------------------------------------------------------------
    // 1. ZonasCampus
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("ZonasCampus: Contiene las 7 zonas oficiales y constructor privado previene instanciación")
    void zonasCampus_constantesYConstructor() throws Exception {
        assertEquals(7, ZonasCampus.TODAS_LAS_ZONAS.size());
        assertTrue(ZonasCampus.TODAS_LAS_ZONAS.contains(ZonasCampus.EMBALSE_NORTE));
        assertTrue(ZonasCampus.TODAS_LAS_ZONAS.contains(ZonasCampus.CANAL_CENTRAL));
        assertTrue(ZonasCampus.TODAS_LAS_ZONAS.contains(ZonasCampus.LAGUNA_SUR));
        assertTrue(ZonasCampus.TODAS_LAS_ZONAS.contains(ZonasCampus.PUNTO_RIBERENO_ESTE));
        assertTrue(ZonasCampus.TODAS_LAS_ZONAS.contains(ZonasCampus.LABORATORIO_HIDRICO));
        assertTrue(ZonasCampus.TODAS_LAS_ZONAS.contains(ZonasCampus.DARSENA_OESTE));
        assertTrue(ZonasCampus.TODAS_LAS_ZONAS.contains(ZonasCampus.BAHIA_CIENCIA));

        Constructor<ZonasCampus> constructor = ZonasCampus.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        InvocationTargetException ex = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertInstanceOf(UnsupportedOperationException.class, ex.getCause());
    }

    // -------------------------------------------------------------------------
    // 2. Mision
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Mision: Getters, validaciones de nulidad, valores por defecto e isCritica")
    void mision_gettersYValidaciones() {
        DroneAcuatico drone = new DroneSuperficial("DS-01", "Model S", 80, EstadoDrone.DISPONIBLE, ZonasCampus.EMBALSE_NORTE);

        // Misión estándar con valores por defecto (nulls opcionales)
        Mision mNormal = new Mision("M-01", drone, ZonasCampus.EMBALSE_NORTE, ZonasCampus.LAGUNA_SUR, null, null, null);
        assertEquals("M-01", mNormal.getId());
        assertEquals(drone, mNormal.getDrone());
        assertEquals(ZonasCampus.EMBALSE_NORTE, mNormal.getPuntoPartida());
        assertEquals(ZonasCampus.LAGUNA_SUR, mNormal.getPuntoLlegada());
        assertEquals(Prioridad.NORMAL, mNormal.getPrioridad());
        assertEquals(TipoCarga.MUESTRA_AGUA, mNormal.getTipoCarga());
        assertEquals(EstadoMision.PENDIENTE, mNormal.getEstado());
        assertFalse(mNormal.isCritica());
        assertTrue(mNormal.toString().contains("M-01"));

        // Misión crítica explícita
        Mision mCritica = new Mision("M-02", drone, ZonasCampus.CANAL_CENTRAL, ZonasCampus.BAHIA_CIENCIA,
                Prioridad.CRITICA, TipoCarga.CARGA_PESADA, EstadoMision.EN_TRANSITO);
        assertTrue(mCritica.isCritica());
        assertEquals(Prioridad.CRITICA, mCritica.getPrioridad());
        assertEquals(TipoCarga.CARGA_PESADA, mCritica.getTipoCarga());
        assertEquals(EstadoMision.EN_TRANSITO, mCritica.getEstado());

        // Validaciones de nulidad obligatoria
        assertThrows(NullPointerException.class, () -> new Mision(null, drone, "A", "B", null, null, null));
        assertThrows(NullPointerException.class, () -> new Mision("ID", null, "A", "B", null, null, null));
        assertThrows(NullPointerException.class, () -> new Mision("ID", drone, null, "B", null, null, null));
        assertThrows(NullPointerException.class, () -> new Mision("ID", drone, "A", null, null, null, null));
    }

    // -------------------------------------------------------------------------
    // 3. Subclases de Drone y DroneAcuatico
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Drones: Constructores sobrecargados con capacidad límite y tipos especializados")
    void drones_constructoresSobreCargadosYTipos() {
        // DroneSuperficial
        DroneSuperficial dsDefecto = new DroneSuperficial("DS-1", "Skimmer", 80, EstadoDrone.DISPONIBLE, ZonasCampus.EMBALSE_NORTE);
        assertEquals(0.500, dsDefecto.getCapacidadCargaKg());
        assertEquals(TipoDrone.SUPERFICIAL, dsDefecto.getTipo());

        DroneSuperficial dsLimite = new DroneSuperficial("DS-2", "Skimmer", 80, EstadoDrone.DISPONIBLE, ZonasCampus.EMBALSE_NORTE, 1.000);
        assertEquals(0.500, dsLimite.getCapacidadCargaKg()); // Clamped to 0.500

        // DroneSemisumergido
        DroneSemisumergido dmDefecto = new DroneSemisumergido("DM-1", "Ranger", 75, EstadoDrone.DISPONIBLE, ZonasCampus.CANAL_CENTRAL);
        assertEquals(1.500, dmDefecto.getCapacidadCargaKg());
        assertEquals(TipoDrone.SEMISUMERGIDO, dmDefecto.getTipo());

        DroneSemisumergido dmLimite = new DroneSemisumergido("DM-2", "Ranger", 75, EstadoDrone.DISPONIBLE, ZonasCampus.CANAL_CENTRAL, 2.000);
        assertEquals(1.500, dmLimite.getCapacidadCargaKg()); // Clamped to 1.500

        // DroneBuceador
        DroneBuceador dbDefecto = new DroneBuceador("DB-1", "Probe", 90, EstadoDrone.DISPONIBLE, ZonasCampus.BAHIA_CIENCIA);
        assertEquals(0.300, dbDefecto.getCapacidadCargaKg());
        assertEquals(TipoDrone.BUCEADOR, dbDefecto.getTipo());

        DroneBuceador dbLimite = new DroneBuceador("DB-2", "Probe", 90, EstadoDrone.DISPONIBLE, ZonasCampus.BAHIA_CIENCIA, 0.900);
        assertEquals(0.300, dbLimite.getCapacidadCargaKg()); // Clamped to 0.300
    }

    @Test
    @DisplayName("DroneAcuatico: Batería delimitada (clamp), equals, hashCode, toString y validaciones de nulidad")
    void droneAcuatico_metodosBase() {
        DroneAcuatico d1 = new DroneSuperficial("D-TEST", "Modelo 1", 120, EstadoDrone.DISPONIBLE, ZonasCampus.EMBALSE_NORTE);
        assertEquals(100, d1.getBateria()); // Math.clamp superior

        DroneAcuatico d2 = new DroneSuperficial("D-TEST", "Modelo 1", -20, EstadoDrone.DISPONIBLE, ZonasCampus.EMBALSE_NORTE);
        assertEquals(0, d2.getBateria()); // Math.clamp inferior

        DroneAcuatico d3 = new DroneSuperficial("D-OTRO", "Modelo 1", 50, EstadoDrone.FALLO, ZonasCampus.EMBALSE_NORTE);

        // Equals & HashCode
        assertEquals(d1, d1);
        assertEquals(d1, d2); // Mismo ID
        assertNotEquals(d1, d3); // Diferente ID
        assertNotEquals(d1, null);
        assertNotEquals(d1, "NoEsDrone");
        assertEquals(d1.hashCode(), d2.hashCode());

        // Getters y métodos booleanos
        assertEquals("D-TEST", d1.getId());
        assertEquals("Modelo 1", d1.getModelo());
        assertEquals(ZonasCampus.EMBALSE_NORTE, d1.getZona());
        assertTrue(d1.isDisponible());
        assertTrue(d1.isActivo());
        assertFalse(d3.isDisponible());
        assertFalse(d3.isActivo()); // FALLO no es activo

        // toString
        assertNotNull(d1.toString());

        // Nulidad de argumentos obligatorios
        assertThrows(NullPointerException.class, () -> new DroneSuperficial(null, "M", 50, EstadoDrone.DISPONIBLE, "Z"));
        assertThrows(NullPointerException.class, () -> new DroneSuperficial("ID", null, 50, EstadoDrone.DISPONIBLE, "Z"));
        assertThrows(NullPointerException.class, () -> new DroneSuperficial("ID", "M", 50, null, "Z"));
        assertThrows(NullPointerException.class, () -> new DroneSuperficial("ID", "M", 50, EstadoDrone.DISPONIBLE, null));
    }

    // -------------------------------------------------------------------------
    // 4. Enums de Dominio (EstadoDrone, EstadoMision, Prioridad, TipoDrone, TipoCarga, CondicionAgua, AlertaFlota)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("EstadoDrone: Verificación de estados activos, disponibles y en fallo")
    void estadoDrone_comportamiento() {
        assertTrue(EstadoDrone.DISPONIBLE.isDisponible());
        assertTrue(EstadoDrone.DISPONIBLE.isActivo());
        assertFalse(EstadoDrone.DISPONIBLE.isFallo());

        assertTrue(EstadoDrone.EN_MISION.isActivo());
        assertFalse(EstadoDrone.EN_MISION.isDisponible());

        assertTrue(EstadoDrone.RECARGANDO.isActivo());
        assertTrue(EstadoDrone.SUMERGIDO.isActivo());

        assertFalse(EstadoDrone.MANTENIMIENTO.isActivo());
        assertFalse(EstadoDrone.FALLO.isActivo());
        assertTrue(EstadoDrone.FALLO.isFallo());

        for (EstadoDrone estado : EstadoDrone.values()) {
            assertNotNull(estado.getDescripcion());
        }
    }

    @Test
    @DisplayName("EstadoMision: Descripciones de todos los estados")
    void estadoMision_descripciones() {
        for (EstadoMision estado : EstadoMision.values()) {
            assertNotNull(estado.getDescripcion());
        }
    }

    @Test
    @DisplayName("Prioridad: Niveles, descripciones e isCritica")
    void prioridad_nivelesYDescripciones() {
        assertTrue(Prioridad.CRITICA.isCritica());
        assertFalse(Prioridad.ALTA.isCritica());
        assertFalse(Prioridad.NORMAL.isCritica());
        assertFalse(Prioridad.BAJA.isCritica());

        assertEquals(1, Prioridad.CRITICA.getNivel());
        assertEquals(2, Prioridad.ALTA.getNivel());
        assertEquals(3, Prioridad.NORMAL.getNivel());
        assertEquals(4, Prioridad.BAJA.getNivel());

        for (Prioridad p : Prioridad.values()) {
            assertNotNull(p.getDescripcion());
        }
    }

    @Test
    @DisplayName("TipoDrone: Nombres, entornos operativos y capacidades máximas")
    void tipoDrone_propiedades() {
        assertEquals("Superficial", TipoDrone.SUPERFICIAL.getNombre());
        assertEquals("Semisumergido", TipoDrone.SEMISUMERGIDO.getNombre());
        assertEquals("Buceador", TipoDrone.BUCEADOR.getNombre());

        for (TipoDrone tipo : TipoDrone.values()) {
            assertNotNull(tipo.getEntornoOperativo());
            assertTrue(tipo.getCapacidadMaximaKg() > 0);
        }
    }

    @Test
    @DisplayName("TipoCarga: Descripciones y pesos promedio en kilogramos")
    void tipoCarga_propiedades() {
        for (TipoCarga tipo : TipoCarga.values()) {
            assertNotNull(tipo.getDescripcion());
            assertTrue(tipo.getPesoPromedioKg() > 0);
        }
    }

    @Test
    @DisplayName("CondicionAgua: Compatibilidad estricta con tipos de drones")
    void condicionAgua_compatibilidad() {
        assertTrue(CondicionAgua.TRANQUILA.esCompatibleCon(TipoDrone.SUPERFICIAL));
        assertTrue(CondicionAgua.TRANQUILA.esCompatibleCon(TipoDrone.SEMISUMERGIDO));
        assertFalse(CondicionAgua.TRANQUILA.esCompatibleCon(TipoDrone.BUCEADOR));

        assertFalse(CondicionAgua.AGITADA.esCompatibleCon(TipoDrone.SUPERFICIAL));
        assertTrue(CondicionAgua.AGITADA.esCompatibleCon(TipoDrone.SEMISUMERGIDO));
        assertFalse(CondicionAgua.AGITADA.esCompatibleCon(TipoDrone.BUCEADOR));

        assertFalse(CondicionAgua.PROFUNDA.esCompatibleCon(TipoDrone.SUPERFICIAL));
        assertFalse(CondicionAgua.PROFUNDA.esCompatibleCon(TipoDrone.SEMISUMERGIDO));
        assertTrue(CondicionAgua.PROFUNDA.esCompatibleCon(TipoDrone.BUCEADOR));

        for (CondicionAgua condicion : CondicionAgua.values()) {
            assertNotNull(condicion.getDescripcion());
            assertNotNull(condicion.getTiposCompatibles());
        }
    }

    @Test
    @DisplayName("AlertaFlota: Record, accessor methods y verificación de criticidad")
    void alertaFlota_recordMethods() {
        LocalDateTime ahora = LocalDateTime.now();
        AlertaFlota alertaCritica = new AlertaFlota("ALT-1", "DM-01", AlertaFlota.NivelAlerta.CRITICA, ZonasCampus.CANAL_CENTRAL, "Derrame detectado", ahora);
        assertEquals("ALT-1", alertaCritica.id());
        assertEquals("DM-01", alertaCritica.droneId());
        assertEquals(AlertaFlota.NivelAlerta.CRITICA, alertaCritica.nivel());
        assertEquals(ZonasCampus.CANAL_CENTRAL, alertaCritica.zona());
        assertEquals("Derrame detectado", alertaCritica.mensaje());
        assertEquals(ahora, alertaCritica.timestamp());
        assertTrue(alertaCritica.isCritica());

        AlertaFlota alertaInfo = new AlertaFlota("ALT-2", "DS-01", AlertaFlota.NivelAlerta.INFO, ZonasCampus.EMBALSE_NORTE, "Operación normal", ahora);
        assertFalse(alertaInfo.isCritica());

        AlertaFlota alertaAdv = new AlertaFlota("ALT-3", "DS-02", AlertaFlota.NivelAlerta.ADVERTENCIA, ZonasCampus.LAGUNA_SUR, "Viento", ahora);
        assertFalse(alertaAdv.isCritica());
    }

    private static void assertInstanceOf(Class<?> expected, Throwable actual) {
        assertTrue(expected.isInstance(actual), "Se esperaba instancia de " + expected.getName() + " pero fue " + actual.getClass().getName());
    }
}
