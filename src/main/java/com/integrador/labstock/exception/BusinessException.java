package com.integrador.labstock.exception;

// Excecao lancada quando uma regra de negocio e violada.
// Exemplo de uso: throw new BusinessException("Estoque insuficiente para este emprestimo");
// O GlobalExceptionHandler captura isso e retorna status 400 (Bad Request) automaticamente.
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
