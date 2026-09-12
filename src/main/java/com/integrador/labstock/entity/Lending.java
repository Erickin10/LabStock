package com.integrador.labstock.entity;

import com.integrador.labstock.enums.LendingStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// Emprestimo de um item para um aluno, com aprovacao de um professor
@Entity
@Table(name = "lendings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Lending extends BaseEntity {

    // Item emprestado
    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    // Aluno que solicitou o emprestimo
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    // Professor responsavel
    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @Column(nullable = false)
    private Integer quantity;

    // Status do emprestimo: PENDING → APPROVED ou REJECTED
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LendingStatus status = LendingStatus.PENDING;

    @Column(nullable = false)
    private Boolean returned = false;

    @Column(name = "lending_date")
    private LocalDateTime lendingDate;

    @Column(name = "return_date")
    private LocalDateTime returnDate;
}
