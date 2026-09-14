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

// Tabela intermediaria N:N entre Project e Item
// Aqui criamos como entidade propria porque tem um campo extra (quantityNeeded)
@Entity
@Table(name = "project_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectItem extends BaseEntity {

    // muitos ProjectItems pertencem a um Project
    // @JoinColumn define qual coluna no banco guarda a FK
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // muitos ProjectItems podem apontar para o mesmo Item
    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    // Quantidade necessaria desse item no projeto
    @Column(name = "quantity_needed", nullable = false)
    private Integer quantityNeeded;
}
