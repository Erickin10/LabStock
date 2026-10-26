package com.integrador.labstock.service;

import com.integrador.labstock.dto.response.PurchaseResponse;
import com.integrador.labstock.entity.Item;
import com.integrador.labstock.entity.Purchase;
import com.integrador.labstock.enums.Category;
import com.integrador.labstock.exception.BusinessException;
import com.integrador.labstock.exception.ResourceNotFoundException;
import com.integrador.labstock.repository.PurchaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Testes unitarios do PurchaseService (repository mockado, sem banco)
@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {

    @Mock
    private PurchaseRepository purchaseRepository;

    @InjectMocks
    private PurchaseService purchaseService;

    private Item item;
    private Purchase purchase;

    @BeforeEach
    void setup() {
        item = new Item();
        item.setId(1L);
        item.setName("ESP32");
        item.setCategory(Category.ELETRONICA);
        item.setQuantity(0);
        item.setMinQuantity(1);
        item.setIsInactive(false);

        purchase = new Purchase();
        purchase.setId(30L);
        purchase.setItem(item);
        purchase.setSuggestion(5);
        purchase.setBought(false);

        lenient().when(purchaseRepository.findById(30L)).thenReturn(Optional.of(purchase));
    }

    // ===================== listPending =====================

    @Test
    void cenario1_listPendingRetornaSugestoesNaoCompradas() {
        when(purchaseRepository.findByBoughtFalseAndDeletedAtIsNull()).thenReturn(List.of(purchase));

        List<PurchaseResponse> responses = purchaseService.listPending();

        assertEquals(1, responses.size());
        assertEquals("ESP32", responses.get(0).getItemName());
        assertEquals("ELETRONICA", responses.get(0).getCategory());
        assertEquals(5, responses.get(0).getSuggestion());
        assertFalse(responses.get(0).getBought());
        verify(purchaseRepository).findByBoughtFalseAndDeletedAtIsNull();
        verify(purchaseRepository, never()).findByBoughtTrueAndDeletedAtIsNull();
    }

    @Test
    void cenario2_listPendingSemSugestoesRetornaListaVazia() {
        when(purchaseRepository.findByBoughtFalseAndDeletedAtIsNull()).thenReturn(List.of());

        assertTrue(purchaseService.listPending().isEmpty());
    }

    // ===================== listBought =====================

    @Test
    void cenario3_listBoughtRetornaSugestoesCompradas() {
        purchase.setBought(true);
        when(purchaseRepository.findByBoughtTrueAndDeletedAtIsNull()).thenReturn(List.of(purchase));

        List<PurchaseResponse> responses = purchaseService.listBought();

        assertEquals(1, responses.size());
        assertTrue(responses.get(0).getBought());
        verify(purchaseRepository).findByBoughtTrueAndDeletedAtIsNull();
        verify(purchaseRepository, never()).findByBoughtFalseAndDeletedAtIsNull();
    }

    @Test
    void cenario4_listBoughtSemComprasRetornaListaVazia() {
        when(purchaseRepository.findByBoughtTrueAndDeletedAtIsNull()).thenReturn(List.of());

        assertTrue(purchaseService.listBought().isEmpty());
    }

    // ===================== markAsBought =====================

    @Test
    void cenario5_markAsBoughtComSucesso() {
        PurchaseResponse response = purchaseService.markAsBought(30L);

        assertTrue(response.getBought());
        assertTrue(purchase.getBought());
        verify(purchaseRepository).save(purchase);
    }

    @Test
    void cenario6_markAsBoughtInexistente() {
        when(purchaseRepository.findById(30L)).thenReturn(Optional.empty());

        ResourceNotFoundException erro = assertThrows(ResourceNotFoundException.class,
                () -> purchaseService.markAsBought(30L));

        assertEquals("Sugestão de compra não encontrada", erro.getMessage());
        verify(purchaseRepository, never()).save(any());
    }

    @Test
    void cenario7_markAsBoughtDeletada() {
        purchase.setDeletedAt(LocalDateTime.now());

        assertThrows(ResourceNotFoundException.class, () -> purchaseService.markAsBought(30L));

        verify(purchaseRepository, never()).save(any());
    }

    @Test
    void cenario8_markAsBoughtJaMarcadaComoComprada() {
        purchase.setBought(true);

        BusinessException erro = assertThrows(BusinessException.class,
                () -> purchaseService.markAsBought(30L));

        assertEquals("Sugestão já foi marcada como comprada", erro.getMessage());
        verify(purchaseRepository, never()).save(any());
    }
}
