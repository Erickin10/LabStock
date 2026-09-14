package com.integrador.labstock.controller;

import com.integrador.labstock.dto.request.DamagedRequest;
import com.integrador.labstock.dto.response.ApiResponse;
import com.integrador.labstock.dto.response.DamagedResponse;
import com.integrador.labstock.service.DamagedService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/damaged")
public class DamagedController {

    @Autowired
    private DamagedService damagedService;

    @PostMapping
    public ResponseEntity<ApiResponse<DamagedResponse>> create(@Valid @RequestBody DamagedRequest request) {
        DamagedResponse response = damagedService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Dano registrado com sucesso", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DamagedResponse>>> listAll() {
        List<DamagedResponse> responses = damagedService.listAll();
        return ResponseEntity.ok(ApiResponse.success("Registros de dano listados com sucesso", responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DamagedResponse>> findById(@PathVariable Long id) {
        DamagedResponse response = damagedService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("Registro de dano encontrado", response));
    }
}
