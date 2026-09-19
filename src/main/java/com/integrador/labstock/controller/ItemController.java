package com.integrador.labstock.controller;

import com.integrador.labstock.dto.request.ItemRequest;
import com.integrador.labstock.dto.response.ApiResponse;
import com.integrador.labstock.dto.response.ItemResponse;
import com.integrador.labstock.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @PostMapping
    public ResponseEntity<ApiResponse<ItemResponse>> create (@Valid @RequestBody ItemRequest request) {
        ItemResponse response = itemService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Item criado com sucesso", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ItemResponse>>> listAll (
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category) {

        List<ItemResponse> responses = itemService.listAll(name, category);

        return ResponseEntity.ok(ApiResponse.success("Itens listados com sucesso", responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemResponse>> findById (@PathVariable Long id) {
        ItemResponse response = itemService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("Item encontrado", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemResponse>> update (@PathVariable Long id, @Valid @RequestBody ItemRequest request) {
        ItemResponse response = itemService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Item atualizado com sucesso", response));
    }

    @PutMapping("/{id}/inactivate")
    public ResponseEntity<ApiResponse<ItemResponse>> inactivate (@PathVariable Long id) {
        ItemResponse response = itemService.inactivate(id);
        return ResponseEntity.ok(ApiResponse.success("Status do item alterado", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete (@PathVariable Long id) {
        itemService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Item removido com sucesso", null));
    }
}
