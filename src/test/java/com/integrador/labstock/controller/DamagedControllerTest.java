package com.integrador.labstock.controller;

import com.integrador.labstock.entity.Item;
import com.integrador.labstock.entity.User;
import com.integrador.labstock.enums.Category;
import com.integrador.labstock.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Integracao do DamagedController com banco H2
class DamagedControllerTest extends IntegrationTestBase {

    private Item item;
    private User aluno;
    private User lab;

    // Antes de cada teste: 1 item com 10 unidades, 1 aluno e 1 laboratorista
    @BeforeEach
    void setup() {
        item = salvarItem("Motor DC", Category.ELETROMECANICA, 10, 2);
        aluno = salvarUsuario("Joao Aluno", "joao@email.com", Role.STUDENT);
        lab = salvarUsuario("Admin Lab", "lab@email.com", Role.LAB);
    }

    // Corpo JSON de um registro de dano
    private String dano(Long itemId, String motivo, int quantidade) {
        return """
                {"itemId":%d,"studentId":%d,"labId":%d,"reason":"%s","quantity":%d}
                """.formatted(itemId, aluno.getId(), lab.getId(), motivo, quantidade);
    }

    @Test
    void cenario1_createComSucessoDescontaDoEstoque() throws Exception {
        mockMvc.perform(post("/damaged")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dano(item.getId(), "Queimou durante experimento", 3)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.quantity").value(3))
                .andExpect(jsonPath("$.data.itemName").value("Motor DC"))
                .andExpect(jsonPath("$.data.studentName").value("Joao Aluno"))
                .andExpect(jsonPath("$.data.labName").value("Admin Lab"));

        // O estoque total do item caiu de 10 para 7
        mockMvc.perform(get("/items/{id}", item.getId()))
                .andExpect(jsonPath("$.data.quantity").value(7))
                .andExpect(jsonPath("$.data.availableQuantity").value(7));
    }

    @Test
    void cenario2_createComQuantidadeMaiorQueOEstoque() throws Exception {
        mockMvc.perform(post("/damaged")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dano(item.getId(), "Quebrou", 11)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Quantidade danificada maior que o estoque total"));

        // O estoque nao pode ter mudado
        mockMvc.perform(get("/items/{id}", item.getId()))
                .andExpect(jsonPath("$.data.quantity").value(10));
    }

    @Test
    void cenario3_createComMotivoEmBranco() throws Exception {
        mockMvc.perform(post("/damaged")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dano(item.getId(), "", 1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Motivo é obrigatório"));
    }

    @Test
    void cenario4_createComItemInexistente() throws Exception {
        mockMvc.perform(post("/damaged")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dano(99999L, "Quebrou", 1)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Item não encontrado"));
    }

    @Test
    void cenario5_listAllRetornaOsRegistros() throws Exception {
        mockMvc.perform(post("/damaged")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dano(item.getId(), "Quebrou", 1)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/damaged"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].reason").value("Quebrou"));
    }

    @Test
    void cenario6_findByIdComSucesso() throws Exception {
        Long id = extrairId(mockMvc.perform(post("/damaged")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dano(item.getId(), "Quebrou", 2)))
                .andExpect(status().isCreated())
                .andReturn());

        mockMvc.perform(get("/damaged/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quantity").value(2))
                .andExpect(jsonPath("$.data.reason").value("Quebrou"));
    }

    @Test
    void cenario7_findByIdInexistente() throws Exception {
        mockMvc.perform(get("/damaged/{id}", 99999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Registro de dano não encontrado"));
    }
}
