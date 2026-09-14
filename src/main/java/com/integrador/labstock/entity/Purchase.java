package com.integrador.labstock.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Sugestao de compra gerada automaticamente pelo cron quando estoque disponivel < minimo
@Entity
@Table(name = "purchases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Purchase extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    // Quantidade sugerida para compra (min - estoque disponivel)
    @Column(nullable = false)
    private Integer suggestion;

    // false = pendente, true = ja foi comprado
    @Column(nullable = false)
    private Boolean bought = false;
}
