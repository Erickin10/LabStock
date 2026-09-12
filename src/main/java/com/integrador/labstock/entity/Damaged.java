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

// Registro de dano a um item causado por um aluno
@Entity
@Table(name = "damaged")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Damaged extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    // Usuario LAB que registrou o dano
    @ManyToOne
    @JoinColumn(name = "lab_id", nullable = false)
    private User lab;

    // Motivo do dano
    @Column(nullable = false)
    private String reason;

    // Quantidade danificada
    @Column(nullable = false)
    private Integer quantity;
}
