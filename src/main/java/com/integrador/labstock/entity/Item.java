package com.integrador.labstock.entity;

import com.integrador.labstock.enums.Category;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Item extends BaseEntity {

    @Column(nullable = false)
    private String name;

    // Salva como texto no banco: "ELETRONICA", "MECANICA", etc.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer min_quantity;

    @Column(name = "is_inactive", nullable = false)
    private Boolean isInactive = false;
}
