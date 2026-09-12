package com.integrador.labstock.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LendingRequest {

    @NotNull(message = "ID do item é obrigatório")
    private Long itemId;

    @NotNull(message = "ID do professor é obrigatório")
    private Long teacherId;

    @NotNull(message = "ID do aluno é obrigatório")
    private Long studentId;

    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade deve ser pelo menos 1")
    private Integer quantity;
}
