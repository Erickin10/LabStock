package com.integrador.labstock.controller;

import com.integrador.labstock.entity.Item;
import com.integrador.labstock.enums.Category;
import com.integrador.labstock.repository.ItemRepository;
import com.integrador.labstock.repository.LendingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Teste de integracao COM MOCK (estilo da aula do professor):
// o fluxo passa por Controller -> Service e termina nos repositories, que sao MOCKADOS.
// Nenhum SQL e executado: os dados vem das listas fixas criadas no setup().
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ItemControllerMockTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemRepository itemRepository;

    @MockitoBean
    private LendingRepository lendingRepository;

    // Configura os mocks uma unica vez, antes de cada teste (igual ao @BeforeEach da aula)
    @BeforeEach
    void setup() {
        Item resistor = criarItem(1L, "Resistor 10K", Category.ELETRONICA, 10);
        Item motor = criarItem(2L, "Motor DC", Category.ELETROMECANICA, 8);
        Item chassi = criarItem(3L, "Chassi 2WD", Category.ESTRUTURAL, 3);

        when(itemRepository.findByDeletedAtIsNull()).thenReturn(List.of(resistor, motor, chassi));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(resistor));
        when(lendingRepository.sumLentQuantityByItemId(any())).thenReturn(4);
    }

    private Item criarItem(Long id, String nome, Category categoria, int quantidade) {
        Item item = new Item();
        item.setId(id);
        item.setName(nome);
        item.setCategory(categoria);
        item.setQuantity(quantidade);
        item.setMinQuantity(1);
        item.setIsInactive(false);
        return item;
    }

    @Test
    void cenario1_listAllRetornaAListaFixaDoMock() throws Exception {
        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].name").value("Resistor 10K"));

        verify(itemRepository).findByDeletedAtIsNull();
    }

    @Test
    void cenario2_findByIdCalculaDisponivelComOValorMockado() throws Exception {
        // 10 no estoque - 4 emprestados (valor do mock) = 6 disponiveis
        mockMvc.perform(get("/items/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quantity").value(10))
                .andExpect(jsonPath("$.data.availableQuantity").value(6));
    }

    @Test
    void cenario3_findByIdQueOMockNaoConheceRetorna404() throws Exception {
        // O mock devolve Optional vazio para o id 99, entao o service lanca "nao encontrado"
        mockMvc.perform(get("/items/{id}", 99))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Item não encontrado"));
    }

    @Test
    void cenario4_createChamaOSaveDoRepositoryMockado() throws Exception {
        when(itemRepository.save(any(Item.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Jumper","category":"ELETRONICA","quantity":30,"minQuantity":10}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Jumper"));

        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void cenario5_createInvalidoNemChegaNoRepository() throws Exception {
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","category":"ELETRONICA","quantity":30,"minQuantity":10}
                                """))
                .andExpect(status().isBadRequest());

        verify(itemRepository, never()).save(any());
    }
}
