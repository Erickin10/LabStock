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
public class LendingResponse {

    private Long id;
    private Long itemId;
    private String itemName;
    private Long studentId;
    private String studentName;
    private Long teacherId;
    private String teacherName;
    private Integer quantity;
    private String status;
    private Boolean returned;
    private LocalDateTime lendingDate;
    private LocalDateTime returnDate;
    private LocalDateTime createdAt;
}
