package com.integrador.labstock.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

// Envelope padrao de resposta da API.
// Todas as respostas (sucesso e erro) seguem esse formato,
// assim o Angular sempre sabe o que esperar.
//
// Sucesso: { "success": true,  "message": "Item criado",          "data": { ... } }
// Erro:    { "success": false, "message": "Item nao encontrado",  "data": null    }
@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    // true = deu certo, false = deu erro
    private boolean success;

    // Mensagem descritiva (ex: "Item criado com sucesso" ou "Estoque insuficiente")
    private String message;

    // O objeto de retorno (DTO). No caso de erro, vem null.
    // O <T> é um "generico" — significa que data pode ser qualquer tipo
    // (UserResponse, ItemResponse, uma lista, etc.)
    private T data;

    // Metodo utilitario para criar resposta de sucesso
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    // Metodo utilitario para criar resposta de erro
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
