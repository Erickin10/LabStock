package com.integrador.labstock.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemResponse {

    private Long id;
    private String name;
    private String category;
    private Integer quantity;
    private Integer availableQuantity;
    private Integer minQuantity;
    private Boolean isInactive;
    private LocalDateTime createdAt;
}
