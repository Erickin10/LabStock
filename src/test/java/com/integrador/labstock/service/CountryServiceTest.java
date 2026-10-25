package com.integrador.labstock.service;

import com.integrador.labstock.client.CountryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Testes unitarios do CountryService (o client Feign e mockado: nenhuma chamada real a internet)
@ExtendWith(MockitoExtension.class)
class CountryServiceTest {

    @Mock
    private CountryClient countryClient;

    @InjectMocks
    private CountryService countryService;

    @BeforeEach
    void setup() {
        // O token vem do application.properties via @Value, que nao funciona sem o Spring.
        // Aqui colocamos o valor "na mao" dentro do campo apiToken.
        ReflectionTestUtils.setField(countryService, "apiToken", "token-teste");
    }

    @Test
    void cenario1_getCountryByNameEnviaTokenComBearerERetornaORespostaDoClient() {
        Map<String, String> resposta = Map.of("name", "Brazil");
        when(countryClient.getCountryByName("brazil", "Bearer token-teste")).thenReturn(resposta);

        Object resultado = countryService.getCountryByName("brazil");

        assertEquals(resposta, resultado);
        verify(countryClient).getCountryByName("brazil", "Bearer token-teste");
    }

    @Test
    void cenario2_getCountryByNamePropagaErroDoClient() {
        when(countryClient.getCountryByName("xyz", "Bearer token-teste"))
                .thenThrow(new RuntimeException("API externa fora do ar"));

        RuntimeException erro = assertThrows(RuntimeException.class,
                () -> countryService.getCountryByName("xyz"));

        assertEquals("API externa fora do ar", erro.getMessage());
    }
}
