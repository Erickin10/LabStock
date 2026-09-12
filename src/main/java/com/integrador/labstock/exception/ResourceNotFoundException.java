package com.integrador.labstock.exception;

// Excecao lancada quando um recurso nao e encontrado no banco de dados.
// Exemplo de uso: throw new ResourceNotFoundException("Item com id 5 nao encontrado");
// O GlobalExceptionHandler captura isso e retorna status 404 automaticamente.
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        // super(message) passa a mensagem para a classe pai (RuntimeException)
        // assim conseguimos acessar a mensagem depois com getMessage()
        super(message);
    }
}
