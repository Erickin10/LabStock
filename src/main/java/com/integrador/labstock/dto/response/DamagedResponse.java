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
public class DamagedResponse {

    private Long id;
    private Long itemId;
    private String itemName;
    private Long studentId;
    private String studentName;
    private Long labId;
    private String labName;
    private String reason;
    private Integer quantity;
    private LocalDateTime createdAt;
}
