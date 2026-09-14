package com.integrador.labstock.controller;

import com.integrador.labstock.dto.response.ApiResponse;
import com.integrador.labstock.dto.response.PurchaseResponse;
import com.integrador.labstock.service.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/purchases")
public class PurchaseController {

    @Autowired
    private PurchaseService purchaseService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PurchaseResponse>>> listPending() {
        List<PurchaseResponse> responses = purchaseService.listPending();
        return ResponseEntity.ok(ApiResponse.success("Compras pendentes listadas com sucesso", responses));
    }

    @GetMapping("/bought")
    public ResponseEntity<ApiResponse<List<PurchaseResponse>>> listBought() {
        List<PurchaseResponse> responses = purchaseService.listBought();
        return ResponseEntity.ok(ApiResponse.success("Compras realizadas listadas com sucesso", responses));
    }

    @PutMapping("/{id}/bought")
    public ResponseEntity<ApiResponse<PurchaseResponse>> markAsBought(@PathVariable Long id) {
        PurchaseResponse response = purchaseService.markAsBought(id);
        return ResponseEntity.ok(ApiResponse.success("Compra marcada como realizada", response));
    }
}
