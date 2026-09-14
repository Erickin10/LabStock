package com.integrador.labstock.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

// @MappedSuperclass diz ao JPA: "essa classe nao e uma tabela, mas as classes filhas herdam esses campos"
@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {

    // - marca esse campo como chave primaria (PRIMARY KEY)
    // - auto-increment no banco
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // - nao pode ser nulo no banco
    // - uma vez criado, nao pode ser alterado por UPDATE
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // e executado automaticamente ANTES de salvar um registro novo no banco
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // e executado automaticamente ANTES de atualizar um registro existente
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
