package com.integrador.labstock.exception;

import org.springframework.dao.DataIntegrityViolationException;
import com.integrador.labstock.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// @RestControllerAdvice faz essa classe "escutar" todos os controllers.
// Quando qualquer controller lanca uma excecao, o Spring vem aqui primeiro
// antes de retornar a resposta, permitindo padronizar o formato de erro.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Captura ResourceNotFoundException e retorna 404 (Not Found)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    // Captura BusinessException e retorna 400 (Bad Request)
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    // Captura erros de validacao do @Valid (campo em branco, email invalido, etc.)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
    }

    // Captura qualquer outra excecao nao tratada (erro inesperado)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Erro interno do servidor"));
    }

    //Captura qualquer excecao que nao tenha sido pega por um handler mais especifico antes dele,
    // sendo um tipo de rede de seguranca, caso aconteca algo que nao esteja previsto,
    //resumindo para o usario nao receber um erro feio, masi conhecido como "catch all"
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("Operação viola uma regra de integridade dos dados"));
    }
}
