package com.integrador.labstock.controller;

import com.integrador.labstock.entity.Item;
import com.integrador.labstock.enums.Category;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Integracao do ItemController com banco H2
class ItemControllerTest extends IntegrationTestBase {

    @Test
    void cenario1_createComSucessoSalvaNoBanco() throws Exception {
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Jumper","category":"ELETRONICA","quantity":30,"minQuantity":10}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Jumper"))
                .andExpect(jsonPath("$.data.category").value("ELETRONICA"))
                .andExpect(jsonPath("$.data.availableQuantity").value(30))
                .andExpect(jsonPath("$.data.isInactive").value(false));

        assertEquals(1, itemRepository.findAll().size());
    }

    @Test
    void cenario2_createComCategoriaInvalida() throws Exception {
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Jumper","category":"INEXISTENTE","quantity":30,"minQuantity":10}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Categoria inválida"));

        assertEquals(0, itemRepository.findAll().size());
    }

    @Test
    void cenario3_createComNomeEmBranco() throws Exception {
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","category":"ELETRONICA","quantity":30,"minQuantity":10}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Nome é obrigatório"));
    }

    @Test
    void cenario4_createComQuantidadeNegativa() throws Exception {
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Jumper","category":"ELETRONICA","quantity":-1,"minQuantity":10}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Quantidade não pode ser negativa"));
    }

    @Test
    void cenario5_listAllRetornaTodosOsItens() throws Exception {
        salvarItem("Resistor 10K", Category.ELETRONICA, 100, 20);
        salvarItem("Motor DC", Category.ELETROMECANICA, 10, 4);

        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void cenario6_listAllFiltraPorNome() throws Exception {
        salvarItem("Resistor 10K", Category.ELETRONICA, 100, 20);
        salvarItem("Motor DC", Category.ELETROMECANICA, 10, 4);

        mockMvc.perform(get("/items").param("name", "resis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Resistor 10K"));
    }

    @Test
    void cenario7_listAllFiltraPorCategoria() throws Exception {
        salvarItem("Resistor 10K", Category.ELETRONICA, 100, 20);
        salvarItem("Motor DC", Category.ELETROMECANICA, 10, 4);

        mockMvc.perform(get("/items").param("category", "ELETROMECANICA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Motor DC"));
    }

    @Test
    void cenario8_listAllFiltraPorNomeECategoria() throws Exception {
        salvarItem("Resistor 10K", Category.ELETRONICA, 100, 20);
        salvarItem("Resistor de potencia", Category.ELETROMECANICA, 10, 4);

        mockMvc.perform(get("/items").param("name", "resistor").param("category", "ELETRONICA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Resistor 10K"));
    }

    @Test
    void cenario9_findByIdComSucesso() throws Exception {
        Item item = salvarItem("Resistor 10K", Category.ELETRONICA, 100, 20);

        mockMvc.perform(get("/items/{id}", item.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Resistor 10K"))
                .andExpect(jsonPath("$.data.quantity").value(100))
                .andExpect(jsonPath("$.data.availableQuantity").value(100));
    }

    @Test
    void cenario10_findByIdInexistente() throws Exception {
        mockMvc.perform(get("/items/{id}", 99999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Item não encontrado"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void cenario11_updateComSucessoPersisteNoBanco() throws Exception {
        Item item = salvarItem("Resistor 10K", Category.ELETRONICA, 100, 20);

        mockMvc.perform(put("/items/{id}", item.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Resistor 20K","category":"MECANICA","quantity":150,"minQuantity":30}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Resistor 20K"))
                .andExpect(jsonPath("$.data.quantity").value(150));

        Item salvo = itemRepository.findById(item.getId()).orElseThrow();
        assertEquals("Resistor 20K", salvo.getName());
        assertEquals(Category.MECANICA, salvo.getCategory());
    }

    @Test
    void cenario12_updateInexistente() throws Exception {
        mockMvc.perform(put("/items/{id}", 99999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Resistor","category":"ELETRONICA","quantity":150,"minQuantity":30}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void cenario13_inactivateAlternaOStatus() throws Exception {
        Item item = salvarItem("Resistor 10K", Category.ELETRONICA, 100, 20);

        mockMvc.perform(put("/items/{id}/inactivate", item.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isInactive").value(true));

        mockMvc.perform(put("/items/{id}/inactivate", item.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isInactive").value(false));
    }

    @Test
    void cenario14_deleteFazSoftDeleteEItemSomeDaListagem() throws Exception {
        Item item = salvarItem("Resistor 10K", Category.ELETRONICA, 100, 20);

        mockMvc.perform(delete("/items/{id}", item.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/items"))
                .andExpect(jsonPath("$.data.length()").value(0));

        mockMvc.perform(get("/items/{id}", item.getId()))
                .andExpect(status().isNotFound());

        // Soft delete: o registro continua no banco, so ganha o deletedAt preenchido
        assertNotNull(itemRepository.findById(item.getId()).orElseThrow().getDeletedAt());
    }
}
