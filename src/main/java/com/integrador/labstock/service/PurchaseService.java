package com.integrador.labstock.service;

import com.integrador.labstock.dto.response.PurchaseResponse;
import com.integrador.labstock.entity.Purchase;
import com.integrador.labstock.exception.BusinessException;
import com.integrador.labstock.exception.ResourceNotFoundException;
import com.integrador.labstock.repository.PurchaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PurchaseService {

    @Autowired
    private PurchaseRepository purchaseRepository;

    public List<PurchaseResponse> listPending () {

        List<Purchase> purchases = purchaseRepository.findByBoughtFalseAndDeletedAtIsNull();

        List<PurchaseResponse> responses = new ArrayList<>();
        for (Purchase purchase : purchases) {
            responses.add(toPurchaseResponse(purchase));
        }
        return responses;
    }

    public List<PurchaseResponse> listBought () {

        List<Purchase> purchases = purchaseRepository.findByBoughtTrueAndDeletedAtIsNull();

        List<PurchaseResponse> responses = new ArrayList<>();
        for (Purchase purchase : purchases) {
            responses.add(toPurchaseResponse(purchase));
        }
        return responses;
    }

    public PurchaseResponse markAsBought (Long id) {

        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sugestão de compra não encontrada"));

        if (purchase.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Sugestão de compra não encontrada");
        }

        if (purchase.getBought()) {
            throw new BusinessException("Sugestão já foi marcada como comprada");
        }

        purchase.setBought(true);

        purchaseRepository.save(purchase);

        return toPurchaseResponse(purchase);
    }

    private PurchaseResponse toPurchaseResponse(Purchase purchase) {
        PurchaseResponse response = new PurchaseResponse();
        response.setId(purchase.getId());
        response.setItemId(purchase.getItem().getId());
        response.setItemName(purchase.getItem().getName());
        response.setCategory(purchase.getItem().getCategory().name());
        response.setSuggestion(purchase.getSuggestion());
        response.setBought(purchase.getBought());
        response.setCreatedAt(purchase.getCreatedAt());
        response.setUpdatedAt(purchase.getUpdatedAt());
        return response;
    }
}
