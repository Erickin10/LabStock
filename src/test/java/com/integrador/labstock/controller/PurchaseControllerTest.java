package com.integrador.labstock.controller;

import com.integrador.labstock.entity.Item;
import com.integrador.labstock.entity.Purchase;
import com.integrador.labstock.enums.Category;
import com.integrador.labstock.repository.PurchaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Integracao do PurchaseController com banco H2.
// As sugestoes de compra nascem do cron (ainda nao implementado), entao aqui elas sao salvas direto no banco.
class PurchaseControllerTest extends IntegrationTestBase {

    @Autowired
    private PurchaseRepository purchaseRepository;

    private Purchase pendente;
    private Purchase comprada;

    // Antes de cada teste: 1 sugestao pendente (ESP32) e 1 ja comprada (Servomotor)
    @BeforeEach
    void setup() {
        Item esp32 = salvarItem("ESP32", Category.ELETRONICA, 0, 1);
        Item servo = salvarItem("Servomotor", Category.ELETROMECANICA, 0, 1);
        pendente = salvarCompra(esp32, 5, false);
        comprada = salvarCompra(servo, 3, true);
    }

    private Purchase salvarCompra(Item item, int sugestao, boolean comprado) {
        Purchase purchase = new Purchase();
        purchase.setItem(item);
        purchase.setSuggestion(sugestao);
        purchase.setBought(comprado);
        return purchaseRepository.save(purchase);
    }

    @Test
    void cenario1_listPendingRetornaSoAsNaoCompradas() throws Exception {
        mockMvc.perform(get("/purchases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].itemName").value("ESP32"))
                .andExpect(jsonPath("$.data[0].category").value("ELETRONICA"))
                .andExpect(jsonPath("$.data[0].suggestion").value(5))
                .andExpect(jsonPath("$.data[0].bought").value(false));
    }

    @Test
    void cenario2_listBoughtRetornaSoAsCompradas() throws Exception {
        mockMvc.perform(get("/purchases/bought"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].itemName").value("Servomotor"))
                .andExpect(jsonPath("$.data[0].bought").value(true));
    }

    @Test
    void cenario3_markAsBoughtMoveASugestaoDeUmaListaParaOutra() throws Exception {
        mockMvc.perform(put("/purchases/{id}/bought", pendente.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bought").value(true));

        mockMvc.perform(get("/purchases"))
                .andExpect(jsonPath("$.data.length()").value(0));

        mockMvc.perform(get("/purchases/bought"))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void cenario4_markAsBoughtJaComprada() throws Exception {
        mockMvc.perform(put("/purchases/{id}/bought", comprada.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Sugestão já foi marcada como comprada"));
    }

    @Test
    void cenario5_markAsBoughtInexistente() throws Exception {
        mockMvc.perform(put("/purchases/{id}/bought", 99999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Sugestão de compra não encontrada"));
    }
}
