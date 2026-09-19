package com.integrador.labstock.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
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
@Transactional(readOnly = true)
public class PurchaseService {

    @Autowired
    private PurchaseRepository purchaseRepository;

    private static final Logger logger = LoggerFactory.getLogger(PurchaseService.class);

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

    @Transactional
    public PurchaseResponse markAsBought (Long id) {

        logger.info("Marcando sugestao de compra id={} como comprada", id);

        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sugestão de compra não encontrada"));

        if (purchase.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Sugestão de compra não encontrada");
        }

        if (purchase.getBought()) {
            logger.warn("Operacao recusada: sugestao id={} ja estava marcada como comprada", id);
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
