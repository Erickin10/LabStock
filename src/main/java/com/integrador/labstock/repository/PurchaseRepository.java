package com.integrador.labstock.repository;

import com.integrador.labstock.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    // Lista compras pendentes (ainda nao compradas)
    List<Purchase> findByBoughtFalseAndDeletedAtIsNull();

    // Lista compras compradas
    List<Purchase> findByBoughtTrueAndDeletedAtIsNull();

    // Busca purchase pendente de um item (usado pelo cron para atualizar em vez de duplicar)
    Optional<Purchase> findByItemIdAndBoughtFalseAndDeletedAtIsNull(Long itemId);
}
