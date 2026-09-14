package com.integrador.labstock.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// Purchase so tem Response — o registro é criado automaticamente pelo cron
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseResponse {

    private Long id;
    private Long itemId;
    private String itemName;
    private String category;
    private Integer suggestion;
    private Boolean bought;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
