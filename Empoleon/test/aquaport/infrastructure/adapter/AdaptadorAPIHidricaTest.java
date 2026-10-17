package aquaport.infrastructure.adapter;

import aquaport.infrastructure.external.ExternalHydricApiResponse;
import aquaport.infrastructure.external.ServicioAPIHidricaExterna;
import aquaport.model.CondicionHidrica;
import aquaport.model.NivelAgua;
import aquaport.model.NivelTurbidez;
import aquaport.model.ZonasEnterprise;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdaptadorAPIHidricaTest {

    @Mock
    private ServicioAPIHidricaExterna servicioMock;

    private AdaptadorAPIHidrica adaptador;

    @BeforeEach
    void setUp() {
        adaptador = new AdaptadorAPIHidrica(servicioMock);
    }

    @Test
    @DisplayName("Convierte valores limite de waterLevel a NivelAgua correspondiente")
    void consultarCondiciones_limitesWaterLevel() {
        probarLimiteNivelAgua(0.0, NivelAgua.TRANQUILO);
        probarLimiteNivelAgua(2.0, NivelAgua.TRANQUILO);
        probarLimiteNivelAgua(2.1, NivelAgua.MODERADO);
        probarLimiteNivelAgua(4.0, NivelAgua.MODERADO);
        probarLimiteNivelAgua(4.1, NivelAgua.AGITADO);
        probarLimiteNivelAgua(6.0, NivelAgua.AGITADO);
        probarLimiteNivelAgua(6.1, NivelAgua.TORMENTOSO);
        probarLimiteNivelAgua(15.0, NivelAgua.TORMENTOSO);
    }

    private void probarLimiteNivelAgua(double nivel, NivelAgua esperado) {
        when(servicioMock.fetchHydricConditions(anyString()))
                .thenReturn(new ExternalHydricApiResponse(nivel, 5.0, 18.0, 2.0));
        CondicionHidrica cond = adaptador.consultarCondiciones(ZonasEnterprise.RED_CANALES);
        assertEquals(esperado, cond.getNivelAgua(), "Fallo en limite waterLevel: " + nivel);
    }

    @Test
    @DisplayName("Convierte valores limite de turbidity a NivelTurbidez correspondiente")
    void consultarCondiciones_limitesTurbidity() {
        probarLimiteTurbidez(10.0, NivelTurbidez.CLARA);
        probarLimiteTurbidez(10.1, NivelTurbidez.MEDIA);
        probarLimiteTurbidez(50.0, NivelTurbidez.MEDIA);
        probarLimiteTurbidez(50.1, NivelTurbidez.ALTA);
        probarLimiteTurbidez(100.0, NivelTurbidez.ALTA);
        probarLimiteTurbidez(100.1, NivelTurbidez.CRITICA);
    }

    private void probarLimiteTurbidez(double ntu, NivelTurbidez esperado) {
        when(servicioMock.fetchHydricConditions(anyString()))
                .thenReturn(new ExternalHydricApiResponse(1.0, ntu, 18.0, 2.0));
        CondicionHidrica cond = adaptador.consultarCondiciones(ZonasEnterprise.EMBALSE_INVESTIGACION);
        assertEquals(esperado, cond.getTurbidez(), "Fallo en limite turbidity: " + ntu);
    }

    @Test
    @DisplayName("Maneja respuesta externa nula con valores seguros por defecto")
    void consultarCondiciones_respuestaNula_retornaDefaultsSeguros() {
        when(servicioMock.fetchHydricConditions(anyString())).thenReturn(null);
        CondicionHidrica cond = adaptador.consultarCondiciones(ZonasEnterprise.RED_CANALES);

        assertNotNull(cond);
        assertEquals(NivelAgua.TRANQUILO, cond.getNivelAgua());
        assertEquals(NivelTurbidez.CLARA, cond.getTurbidez());
    }
}
