package com.integrador.labstock.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Representa um item vinculado a um projeto na resposta
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectItemResponse {

    private Long itemId;
    private String itemName;
    private String category;
    private Integer quantityNeeded;
    private Integer availableQuantity;
}
