package com.integrador.labstock.service;

import com.integrador.labstock.dto.request.ItemRequest;
import com.integrador.labstock.dto.response.ItemResponse;
import com.integrador.labstock.entity.Item;
import com.integrador.labstock.enums.Category;
import com.integrador.labstock.exception.BusinessException;
import com.integrador.labstock.exception.ResourceNotFoundException;
import com.integrador.labstock.repository.ItemRepository;
import com.integrador.labstock.repository.LendingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Testes unitarios do ItemService (repositories mockados, sem banco)
@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private LendingRepository lendingRepository;

    @InjectMocks
    private ItemService itemService;

    private Item item;

    @BeforeEach
    void setup() {
        item = new Item();
        item.setId(1L);
        item.setName("Resistor 10K");
        item.setCategory(Category.ELETRONICA);
        item.setQuantity(10);
        item.setMinQuantity(2);
        item.setIsInactive(false);

        lenient().when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
    }

    // ===================== create =====================

    @Test
    void cenario1_createComSucesso() {
        ItemResponse response = itemService.create(new ItemRequest("Jumper", "eletronica", 30, 10));

        ArgumentCaptor<Item> captor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository).save(captor.capture());
        assertEquals("Jumper", captor.getValue().getName());
        assertEquals(Category.ELETRONICA, captor.getValue().getCategory());

        assertEquals("ELETRONICA", response.getCategory());
        assertEquals(30, response.getQuantity());
        assertEquals(10, response.getMinQuantity());
        assertFalse(response.getIsInactive());
    }

    @Test
    void cenario2_createComCategoriaInvalida() {
        BusinessException erro = assertThrows(BusinessException.class,
                () -> itemService.create(new ItemRequest("Jumper", "INEXISTENTE", 30, 10)));

        assertEquals("Categoria inválida", erro.getMessage());
        verify(itemRepository, never()).save(any());
    }

    @Test
    void cenario3_createCalculaEstoqueDisponivel() {
        // 30 no estoque, 4 emprestados -> 26 disponiveis
        when(lendingRepository.sumLentQuantityByItemId(any())).thenReturn(4);

        ItemResponse response = itemService.create(new ItemRequest("Jumper", "ELETRONICA", 30, 10));

        assertEquals(26, response.getAvailableQuantity());
    }

    // ===================== listAll =====================

    @Test
    void cenario4_listAllSemFiltrosUsaListagemPadrao() {
        when(itemRepository.findByDeletedAtIsNull()).thenReturn(List.of(item));

        List<ItemResponse> responses = itemService.listAll(null, null);

        assertEquals(1, responses.size());
        verify(itemRepository).findByDeletedAtIsNull();
    }

    @Test
    void cenario5_listAllComFiltrosVaziosUsaListagemPadrao() {
        when(itemRepository.findByDeletedAtIsNull()).thenReturn(List.of());

        List<ItemResponse> responses = itemService.listAll("", "");

        assertTrue(responses.isEmpty());
        verify(itemRepository).findByDeletedAtIsNull();
    }

    @Test
    void cenario6_listAllComNomeUsaFindBySearch() {
        when(itemRepository.findBySearch("resis")).thenReturn(List.of(item));

        List<ItemResponse> responses = itemService.listAll("resis", null);

        assertEquals("Resistor 10K", responses.get(0).getName());
        verify(itemRepository).findBySearch("resis");
    }

    @Test
    void cenario7_listAllComCategoriaUsaFindByCategory() {
        when(itemRepository.findByCategory("ELETRONICA")).thenReturn(List.of(item));

        List<ItemResponse> responses = itemService.listAll(null, "ELETRONICA");

        assertEquals(1, responses.size());
        verify(itemRepository).findByCategory("ELETRONICA");
    }

    @Test
    void cenario8_listAllComNomeECategoriaUsaFindBySearchAndCategory() {
        when(itemRepository.findBySearchAndCategory("resis", "ELETRONICA")).thenReturn(List.of(item));

        List<ItemResponse> responses = itemService.listAll("resis", "ELETRONICA");

        assertEquals(1, responses.size());
        verify(itemRepository).findBySearchAndCategory("resis", "ELETRONICA");
        verify(itemRepository, never()).findBySearch(any());
        verify(itemRepository, never()).findByCategory(any());
    }

    // ===================== findById =====================

    @Test
    void cenario9_findByIdCalculaEstoqueDisponivel() {
        // 10 no estoque, 3 emprestados -> 7 disponiveis
        when(lendingRepository.sumLentQuantityByItemId(1L)).thenReturn(3);

        ItemResponse response = itemService.findById(1L);

        assertEquals(1L, response.getId());
        assertEquals(10, response.getQuantity());
        assertEquals(7, response.getAvailableQuantity());
    }

    @Test
    void cenario10_findByIdInexistente() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException erro = assertThrows(ResourceNotFoundException.class,
                () -> itemService.findById(1L));

        assertEquals("Item não encontrado", erro.getMessage());
    }

    @Test
    void cenario11_findByIdDeletado() {
        item.setDeletedAt(LocalDateTime.now());

        assertThrows(ResourceNotFoundException.class, () -> itemService.findById(1L));
    }

    // ===================== update =====================

    @Test
    void cenario12_updateComSucesso() {
        ItemResponse response = itemService.update(1L, new ItemRequest("Resistor 20K", "MECANICA", 50, 5));

        assertEquals("Resistor 20K", response.getName());
        assertEquals("MECANICA", response.getCategory());
        assertEquals(50, response.getQuantity());
        assertEquals(5, response.getMinQuantity());
        verify(itemRepository).save(item);
    }

    @Test
    void cenario13_updateComCategoriaInvalida() {
        BusinessException erro = assertThrows(BusinessException.class,
                () -> itemService.update(1L, new ItemRequest("Resistor", "INEXISTENTE", 50, 5)));

        assertEquals("Categoria inválida", erro.getMessage());
        assertEquals("Resistor 10K", item.getName());
        verify(itemRepository, never()).save(any());
    }

    @Test
    void cenario14_updateInexistente() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> itemService.update(1L, new ItemRequest("Resistor", "ELETRONICA", 50, 5)));

        verify(itemRepository, never()).save(any());
    }

    @Test
    void cenario15_updateDeletado() {
        item.setDeletedAt(LocalDateTime.now());

        assertThrows(ResourceNotFoundException.class,
                () -> itemService.update(1L, new ItemRequest("Resistor", "ELETRONICA", 50, 5)));

        verify(itemRepository, never()).save(any());
    }

    // ===================== inactivate =====================

    @Test
    void cenario16_inactivateAlternaDeAtivoParaInativo() {
        ItemResponse response = itemService.inactivate(1L);

        assertTrue(response.getIsInactive());
        verify(itemRepository).save(item);
    }

    @Test
    void cenario17_inactivateAlternaDeInativoParaAtivo() {
        item.setIsInactive(true);

        ItemResponse response = itemService.inactivate(1L);

        assertFalse(response.getIsInactive());
        verify(itemRepository).save(item);
    }

    @Test
    void cenario18_inactivateInexistente() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> itemService.inactivate(1L));

        verify(itemRepository, never()).save(any());
    }

    @Test
    void cenario19_inactivateDeletado() {
        item.setDeletedAt(LocalDateTime.now());

        assertThrows(ResourceNotFoundException.class, () -> itemService.inactivate(1L));

        verify(itemRepository, never()).save(any());
    }

    // ===================== delete =====================

    @Test
    void cenario20_deleteFazSoftDelete() {
        itemService.delete(1L);

        assertNotNull(item.getDeletedAt());
        verify(itemRepository).save(item);
    }

    @Test
    void cenario21_deleteInexistente() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> itemService.delete(1L));

        verify(itemRepository, never()).save(any());
    }

    @Test
    void cenario22_deleteJaDeletado() {
        item.setDeletedAt(LocalDateTime.now());

        assertThrows(ResourceNotFoundException.class, () -> itemService.delete(1L));

        verify(itemRepository, never()).save(any());
    }
}
