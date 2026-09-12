package com.integrador.labstock.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// DTO para vincular um item a um projeto
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectItemRequest {

    @NotNull(message = "ID do item é obrigatório")
    private Long itemId;

    @NotNull(message = "Quantidade necessária é obrigatória")
    @Min(value = 1, message = "Quantidade necessária deve ser pelo menos 1")
    private Integer quantityNeeded;
}
