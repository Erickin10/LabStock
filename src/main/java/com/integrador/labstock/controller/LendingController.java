package com.integrador.labstock.controller;

import com.integrador.labstock.dto.request.LendingRequest;
import com.integrador.labstock.dto.response.ApiResponse;
import com.integrador.labstock.dto.response.LendingResponse;
import com.integrador.labstock.service.LendingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/lendings")
public class LendingController {

    @Autowired
    private LendingService lendingService;

    @PostMapping
    public ResponseEntity<ApiResponse<LendingResponse>> create (@Valid @RequestBody LendingRequest request) {
        LendingResponse response = lendingService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Empréstimo solicitado com sucesso", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LendingResponse>>> listAll (@RequestParam(required = false) String status) {
        List<LendingResponse> responses;

        if (status != null && !status.isEmpty()) {
            responses = lendingService.listByStatus(status);
        } else {
            responses = lendingService.listAll();
        }

        return ResponseEntity.ok(ApiResponse.success("Empréstimos listados com sucesso", responses));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<LendingResponse>>> listByStudent (@RequestParam Long studentId) {
        List<LendingResponse> responses = lendingService.listByStudent(studentId);
        return ResponseEntity.ok(ApiResponse.success("Empréstimos listados com sucesso", responses));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<LendingResponse>> approve (@PathVariable Long id) {
        LendingResponse response = lendingService.approve(id);
        return ResponseEntity.ok(ApiResponse.success("Empréstimo aprovado com sucesso", response));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<LendingResponse>> reject (@PathVariable Long id) {
        LendingResponse response = lendingService.reject(id);
        return ResponseEntity.ok(ApiResponse.success("Empréstimo rejeitado", response));
    }

    @PutMapping("/{id}/return")
    public ResponseEntity<ApiResponse<LendingResponse>> returnItem (@PathVariable Long id) {
        LendingResponse response = lendingService.returnItem(id);
        return ResponseEntity.ok(ApiResponse.success("Item devolvido com sucesso", response));
    }
}
