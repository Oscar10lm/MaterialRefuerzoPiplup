package aquaport;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MainEmpoleonTest {

    @Test
    @DisplayName("MainEmpoleon ejecuta demostracion completa sin errores")
    void main_ejecutaDemostracionSinErrores() {
        assertDoesNotThrow(() -> MainEmpoleon.main(new String[0]));
    }

    @Test
    @DisplayName("Constructor privado de MainEmpoleon lanza excepcion por reflexion")
    void constructorPrivado_lanzaExcepcion() throws Exception {
        Constructor<MainEmpoleon> c = MainEmpoleon.class.getDeclaredConstructor();
        c.setAccessible(true);
        assertThrows(InvocationTargetException.class, c::newInstance);
    }
}
