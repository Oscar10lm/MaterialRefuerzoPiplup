package aquaport.service;

import aquaport.model.DroneAcuatico;
import aquaport.model.TipoDrone;
import aquaport.model.ZonasEnterprise;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Flota60FactoryTest {

    @Test
    @DisplayName("Flota60Factory genera exactamente 60 drones en la distribucion correcta")
    void crearFlota60_generaDistribucionEnterprise() {
        List<DroneAcuatico> flota = Flota60Factory.crearFlota60();
        assertEquals(60, flota.size());

        long enEmbalse = flota.stream().filter(d -> d.getZona().equals(ZonasEnterprise.EMBALSE_INVESTIGACION)).count();
        long enCanales = flota.stream().filter(d -> d.getZona().equals(ZonasEnterprise.RED_CANALES)).count();
        long enLaguna = flota.stream().filter(d -> d.getZona().equals(ZonasEnterprise.LAGUNA_RESERVA)).count();
        long enHub = flota.stream().filter(d -> d.getZona().equals(ZonasEnterprise.LABORATORIO_CENTRAL)).count();

        assertEquals(20, enEmbalse);
        assertEquals(20, enCanales);
        assertEquals(15, enLaguna);
        assertEquals(5, enHub);

        long buceadores = flota.stream().filter(d -> d.getTipo() == TipoDrone.BUCEADOR).count();
        long superficiales = flota.stream().filter(d -> d.getTipo() == TipoDrone.SUPERFICIAL).count();
        long semisumergidos = flota.stream().filter(d -> d.getTipo() == TipoDrone.SEMISUMERGIDO).count();

        assertEquals(21, buceadores);
        assertEquals(22, superficiales);
        assertEquals(17, semisumergidos);
    }

    @Test
    @DisplayName("Constructor privado de Flota60Factory lanza excepcion por reflexion")
    void constructorPrivado_lanzaExcepcion() throws Exception {
        Constructor<Flota60Factory> c = Flota60Factory.class.getDeclaredConstructor();
        c.setAccessible(true);
        assertThrows(InvocationTargetException.class, c::newInstance);
    }
}
