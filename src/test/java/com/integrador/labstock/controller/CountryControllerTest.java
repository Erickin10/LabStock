package com.integrador.labstock.controller;

import com.integrador.labstock.client.CountryClient;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Integracao do CountryController.
// O CountryClient (que chama a internet) e MOCKADO com @MockitoBean: o teste nunca sai da maquina.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CountryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // @MockitoBean troca o CountryClient real por um de mentira dentro da aplicacao
    @MockitoBean
    private CountryClient countryClient;

    @Test
    void cenario1_getCountryComSucesso() throws Exception {
        when(countryClient.getCountryByName(anyString(), anyString()))
                .thenReturn(Map.of("name", "Brazil"));

        mockMvc.perform(get("/countries").param("name", "brazil"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("País encontrado"))
                .andExpect(jsonPath("$.data.name").value("Brazil"));
    }

    @Test
    void cenario2_apiExternaForaDoArRetorna502() throws Exception {
        when(countryClient.getCountryByName(anyString(), anyString()))
                .thenThrow(mock(FeignException.class));

        mockMvc.perform(get("/countries").param("name", "brazil"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Erro ao consultar serviço externo"));
    }
}
